# Issue #009 — Manager-Side Result Handling & Job Completion

**Milestone:** 2 — Job Scheduling & Fault Tolerance  
**Labels:** `manager-node`, `lifecycle`, `priority:high`  
**Assignee:** —  
**Estimate:** 0.5 day  
**Depends on:** #007, #008  

## Description

Handle incoming `JOB_RESULT` messages on the Manager side: update job status and free the worker for new tasks.

## Acceptance Criteria

- [ ] Manager's per-worker message handler processes `JOB_RESULT` messages
- [ ] On `COMPLETED` → job transitions to `COMPLETED`, result stored in `Job.result`
- [ ] On `FAILED` → job transitions to `FAILED`, error stored in `Job.result`
- [ ] Worker status set back to `IDLE` after result received
- [ ] Log full lifecycle: `Job <UUID> COMPLETED by Worker <UUID> in <duration>ms`
- [ ] `JobRegistry` (in-memory map of all jobs) tracks all jobs by UUID for lookup
- [ ] Integration test: enqueue 3 jobs → 1 worker processes all 3 sequentially → all `COMPLETED`
