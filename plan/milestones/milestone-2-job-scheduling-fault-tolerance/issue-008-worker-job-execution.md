# Issue #008 — Worker Job Execution & Result Reporting

**Milestone:** 2 — Job Scheduling & Fault Tolerance  
**Labels:** `worker-node`, `execution`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #004, #006  

## Description

Extend the `WorkerClient` to receive `ASSIGN_JOB` messages, execute a dummy computational task, and send back a `JOB_RESULT`.

### Execution Flow

```
1. Worker receives ASSIGN_JOB { jobId, payload }
2. Log: "Executing job <jobId>"
3. Execute payload (initially: dummy tasks — sleep, compute Fibonacci, etc.)
4. On success → send JOB_RESULT { jobId, status: "COMPLETED", output: "..." }
5. On exception → send JOB_RESULT { jobId, status: "FAILED", output: "<error>" }
6. Worker marks itself IDLE again (ready for next job)
```

## Acceptance Criteria

- [ ] Worker handles `ASSIGN_JOB` in its message loop
- [ ] `TaskExecutor` interface: `String execute(String payload)` — pluggable execution strategy
- [ ] `DummyTaskExecutor`: interprets payload JSON, runs: `sleep(N)`, `fibonacci(N)`, or `echo`
- [ ] On success → sends `JOB_RESULT` with `COMPLETED` + output string
- [ ] On exception → catches, sends `JOB_RESULT` with `FAILED` + exception message
- [ ] Worker stays alive after job completion and accepts more jobs

## Technical Notes

- Execute tasks on the connection thread (Virtual Thread) — no need for a separate thread pool
- Set a max execution timeout (e.g., 60 s) using `CompletableFuture.orTimeout()` to prevent hanging
