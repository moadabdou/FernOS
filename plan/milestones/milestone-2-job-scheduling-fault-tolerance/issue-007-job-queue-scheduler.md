# Issue #007 — Implement Job Queue & Scheduler on the Manager

**Milestone:** 2 — Job Scheduling & Fault Tolerance  
**Labels:** `manager-node`, `scheduling`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #003, #006  

## Description

Add a `JobQueue` backed by `ConcurrentLinkedQueue<Job>` and a `JobScheduler` that continuously assigns pending jobs to idle workers.

### Scheduler Loop (pseudocode)

```
while (running) {
    Job job = jobQueue.poll();
    if (job == null) { sleep(100ms); continue; }
    
    WorkerConnection worker = workerRegistry.findIdle();
    if (worker == null) { jobQueue.addFirst(job); sleep(100ms); continue; }
    
    job.transition(ASSIGNED);
    worker.status = BUSY;
    protocolEncoder.send(worker.socket, ASSIGN_JOB, job.toPayload());
}
```

## Acceptance Criteria

- [ ] `JobQueue` wrapper around `ConcurrentLinkedQueue<Job>` with `enqueue()`, `dequeue()`, `requeue(Job)` (adds to head)
- [ ] `JobScheduler` runs on its own Virtual Thread, polling the queue
- [ ] Idle worker selection: iterate `WorkerRegistry`, find first with status `IDLE`
- [ ] On assignment: job → `ASSIGNED`, worker → `BUSY`, send `ASSIGN_JOB` message
- [ ] If no idle workers: job stays in queue, scheduler sleeps briefly and retries
- [ ] Enqueue 5 jobs with 2 workers → all 5 eventually assigned (logged)

## Technical Notes

- Use `Deque` (e.g., `ConcurrentLinkedDeque`) if `addFirst` is needed for re-queuing
- Keep scheduler thread responsive with short sleep intervals (50–100 ms)
