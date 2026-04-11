package com.doe.manager.recovery;

import com.doe.core.model.Job;
import com.doe.core.model.JobStatus;
import com.doe.core.registry.JobRegistry;
import com.doe.core.registry.WorkerRegistry;
import com.doe.manager.persistence.entity.JobEntity;
import com.doe.manager.persistence.repository.JobRepository;
import com.doe.manager.persistence.repository.WorkerRepository;
import com.doe.manager.scheduler.JobQueue;
import com.doe.manager.scheduler.JobScheduler;
import com.doe.manager.server.ManagerServer;
import com.doe.worker.client.WorkerClient;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end chaos + persistence test.
 * <p>
 * Combines worker crash recovery with PostgreSQL persistence to validate the full
 * lifecycle: submit jobs → let some reach RUNNING → kill Manager abruptly →
 * restart Manager → verify orphaned jobs are recovered from DB and completed by a worker.
 * <p>
 * This bridges the gap between:
 * <ul>
 *   <li>{@link StartupRecoveryIntegrationTest} — tests DB recovery in isolation (no live jobs)</li>
 *   <li>{@link com.doe.manager.server.CrashRecoveryIntegrationTest} — tests worker crash recovery (no DB)</li>
 * </ul>
 * <p>
 * Scenario: Manager process crash while jobs are in-flight → restart → zero jobs lost.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class ManagerCrashRecoveryEndToEndTest {

    private static final String JWT_SECRET = "3c34e62a26514757c2c159851f50a80d46dddc7fa0a06df5c689f928e4e9b94z";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired ManagerServer        server;
    @Autowired JobScheduler         jobScheduler;
    @Autowired JobQueue             jobQueue;
    @Autowired JobRegistry          jobRegistry;
    @Autowired JobRepository        jobRepository;
    @Autowired WorkerRepository     workerRepository;
    @Autowired StartupRecoveryService recoveryService;

    private final List<WorkerClient> workers = new ArrayList<>();
    private final List<Thread> workerThreads = new ArrayList<>();

    // ─── helpers ──────────────────────────────────────────────────────────────

    private String generateToken() {
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private WorkerClient startWorker() throws InterruptedException {
        WorkerClient worker = new WorkerClient(
                "localhost", server.getLocalPort(), 2000, 10_000, generateToken());
        Thread t = Thread.ofVirtual().start(worker::start);

        // Wait for registration
        long deadline = System.currentTimeMillis() + 5_000;
        while (server.getRegistry().isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertFalse(server.getRegistry().isEmpty(), "Worker should have registered");

        workers.add(worker);
        workerThreads.add(t);
        return worker;
    }

    private void shutdownAllWorkers() throws InterruptedException {
        for (WorkerClient w : workers) {
            w.shutdown();
        }
        for (Thread t : workerThreads) {
            t.join(3_000);
        }
        workers.clear();
        workerThreads.clear();
    }

    /**
     * Enqueue a job and poll until it reaches one of the expected statuses.
     */
    private Job submitAndAwait(String payload, JobStatus... expected) throws InterruptedException {
        Job job = Job.newJob(payload).build();
        jobQueue.enqueue(job);

        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            for (JobStatus s : expected) {
                if (job.getStatus() == s) return job;
            }
            Thread.sleep(50);
        }
        fail("Job did not reach expected status within timeout. Final: " + job.getStatus());
        return job;
    }

    /**
     * Wait until ALL given jobs reach one of the expected statuses.
     */
    private boolean awaitAll(List<Job> jobs, long timeoutMs, JobStatus... expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            boolean allDone = jobs.stream().allMatch(j -> {
                for (JobStatus s : expected) {
                    if (j.getStatus() == s) return true;
                }
                return false;
            });
            if (allDone) return true;
            Thread.sleep(200);
        }
        return false;
    }

    // ─── tests ────────────────────────────────────────────────────────────────

    @AfterEach
    void cleanup() throws Exception {
        try { shutdownAllWorkers(); } catch (Exception ignored) {}
        // Ensure server is running for the next test (DirtiesContext handles isolation,
        // but we clean up workers to be safe)
        if (!server.isRunning()) {
            try {
                // If the test crashed the server, restart it so @DirtiesContext can tear down cleanly
                // The DB is already torn down by DirtiesContext, so this is just for cleanup
                recoveryService.recover();
                server.start();
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("10 jobs submitted, 3 workers running → kill Manager mid-flight → restart → all jobs complete, zero lost")
    void managerCrashMidExecution_restartRecoverAndComplete() throws Exception {
        // ── Phase 1: Normal operation — start workers and submit jobs ─────────
        // Start 3 workers
        startWorker();
        startWorker();
        startWorker();
        assertEquals(3, server.getRegistry().size(), "Should have 3 workers registered");

        // Submit 10 sleep jobs (long enough that some will be RUNNING when we crash)
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Job job = Job.newJob("{\"type\":\"sleep\",\"ms\":2000}").build();
            jobs.add(job);
            jobQueue.enqueue(job);
        }

        // Wait until at least some jobs are RUNNING (meaning workers are actively executing)
        long deadline = System.currentTimeMillis() + 8_000;
        long runningCount = 0;
        while (System.currentTimeMillis() < deadline) {
            runningCount = jobs.stream().filter(j -> j.getStatus() == JobStatus.RUNNING).count();
            if (runningCount >= 2) break; // At least 2 jobs should be running concurrently
            Thread.sleep(100);
        }
        assertTrue(runningCount >= 2,
                "Expected at least 2 jobs RUNNING, got " + runningCount + ". Job statuses: " +
                jobs.stream().map(j -> j.getStatus().name()).collect(Collectors.joining(", ")));

        // Capture DB state before crash — jobs that are ASSIGNED or RUNNING should be in the DB
        long inFlightBeforeCrash = jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.ASSIGNED || j.getStatus() == JobStatus.RUNNING)
                .count();
        assertTrue(inFlightBeforeCrash > 0, "Should have in-flight jobs before crash");

        // ── Phase 2: Simulate Manager crash — abrupt shutdown, no graceful cleanup ─
        // This simulates the Manager process being killed (SIGKILL, OOM, etc.)
        // Jobs in ASSIGNED/RUNNING state will remain in the DB as orphaned.
        server.shutdown();
        Thread.sleep(500); // Let the server fully stop

        assertFalse(server.isRunning(), "Manager should be stopped after crash");

        // Verify DB still has the in-flight jobs (they weren't cleaned up)
        List<JobEntity> dbJobs = jobRepository.findAll();
        long inFlightInDb = dbJobs.stream()
                .filter(e -> e.getStatus() == JobStatus.ASSIGNED || e.getStatus() == JobStatus.RUNNING)
                .count();
        assertTrue(inFlightInDb > 0,
                "DB should still contain in-flight jobs after crash. Found: " + inFlightInDb);

        // ── Phase 3: Restart Manager — recovery from DB ──────────────────────
        // On a real restart, Spring would fire ApplicationReadyEvent → StartupRecoveryService.
        // In our test the Spring context stays alive, so we call recover() manually.
        recoveryService.recover();

        // After recovery, orphaned jobs should be PENDING in DB and in the in-memory queue
        for (JobEntity e : jobRepository.findAll()) {
            if (e.getStatus() == JobStatus.ASSIGNED || e.getStatus() == JobStatus.RUNNING) {
                fail("After recovery, no jobs should be ASSIGNED or RUNNING in DB. Found: " + e.getId());
            }
        }
        assertEquals(inFlightBeforeCrash, jobQueue.size(),
                "Queue should contain exactly the recovered orphaned jobs");
        assertEquals(inFlightBeforeCrash, jobRegistry.size(),
                "Registry should contain exactly the recovered orphaned jobs");

        // Start the server again (scheduler loop needs to be running to assign jobs)
        server.start();
        assertTrue(server.isRunning(), "Manager should be running after restart");

        // ── Phase 4: Connect a worker and verify all jobs complete ────────────
        startWorker();
        assertEquals(1, server.getRegistry().size(), "Should have 1 worker registered on restarted server");

        // Wait for ALL 10 jobs to reach COMPLETED
        boolean allCompleted = awaitAll(jobs, 60_000, JobStatus.COMPLETED);
        assertTrue(allCompleted,
                "Not all jobs completed. Final statuses: " +
                jobs.stream().map(j -> j.getStatus().name()).collect(Collectors.joining(", ")));

        // Verify zero jobs lost: every job is either COMPLETED or FAILED (max retries exhausted)
        long completed = jobs.stream().filter(j -> j.getStatus() == JobStatus.COMPLETED).count();
        long failed = jobs.stream().filter(j -> j.getStatus() == JobStatus.FAILED).count();
        assertEquals(10, completed + failed,
                "All 10 jobs should be accounted for (COMPLETED + FAILED), got " + (completed + failed));

        // At least some jobs should have been recovered (retried after crash)
        long recoveredCount = jobs.stream().filter(j -> j.getRetryCount() > 0).count();
        assertTrue(recoveredCount > 0,
                "At least some jobs should have been retried after recovery. Recovered: " + recoveredCount);
    }
}
