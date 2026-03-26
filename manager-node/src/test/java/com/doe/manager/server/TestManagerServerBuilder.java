package com.doe.manager.server;

import com.doe.core.registry.WorkerRegistry;
import com.doe.core.registry.JobRegistry;
import com.doe.manager.scheduler.JobQueue;
import com.doe.manager.scheduler.JobScheduler;
import com.doe.manager.scheduler.CrashRecoveryHandler;
import com.doe.manager.scheduler.JobTimeoutMonitor;
import java.util.List;

public class TestManagerServerBuilder {
    public static ManagerServer build(int port, long check, long timeout) {
        WorkerRegistry registry = new WorkerRegistry();
        JobRegistry jobRegistry = new JobRegistry();
        JobQueue jobQueue = new JobQueue(jobRegistry);
        JobScheduler jobScheduler = new JobScheduler(jobQueue, registry);
        CrashRecoveryHandler recoveryHandler = new CrashRecoveryHandler(jobRegistry, jobQueue);
        JobTimeoutMonitor jobTimeoutMonitor = new JobTimeoutMonitor(jobRegistry, recoveryHandler);
        return new ManagerServer(port, check, timeout, registry, jobRegistry, jobScheduler, jobTimeoutMonitor, List.of(recoveryHandler));
    }
    public static ManagerServer build(int port) {
        return build(port, 5000, 15000);
    }
}
