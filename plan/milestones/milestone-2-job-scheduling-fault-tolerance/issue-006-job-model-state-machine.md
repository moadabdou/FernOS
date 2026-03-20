# Issue #006 — Implement Job Model & State Machine

**Milestone:** 2 — Job Scheduling & Fault Tolerance  
**Labels:** `engine-core`, `data-model`, `priority:high`  
**Assignee:** —  
**Estimate:** 0.5 day  
**Depends on:** #001  

## Description

Define the `Job` domain model in `engine-core` with a strict state machine that prevents invalid transitions.

### State Machine

```
PENDING ──→ ASSIGNED ──→ RUNNING ──→ COMPLETED
                │            │
                │            └──→ FAILED
                │
                └──→ PENDING  (re-queue on timeout)
```

### Job Record/Class

```java
public class Job {
    UUID id;
    JobStatus status;          // PENDING, ASSIGNED, RUNNING, COMPLETED, FAILED
    String payload;            // JSON command/data
    String result;             // output from worker (nullable)
    UUID assignedWorkerId;     // nullable
    Instant createdAt;
    Instant updatedAt;
}
```

## Acceptance Criteria

- [ ] `JobStatus` enum: `PENDING`, `ASSIGNED`, `RUNNING`, `COMPLETED`, `FAILED`
- [ ] `Job` class with all fields, builder or factory method
- [ ] `Job.transition(JobStatus target)` enforces valid transitions, throws `IllegalStateException` on invalid
- [ ] Unit tests covering: valid transitions, invalid transitions (`COMPLETED → RUNNING` throws), re-queue (`RUNNING → PENDING`)
