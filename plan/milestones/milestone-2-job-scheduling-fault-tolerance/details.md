# Milestone 2: Job Scheduling & Fault Tolerance

## Overview

With reliable Worker ↔ Manager communication in place (Milestone 1), this milestone adds the **job lifecycle**: the ability for the Manager to queue tasks, assign them to idle workers, receive results, and automatically recover from worker crashes mid-execution.

This is the milestone where the system transitions from a "chat protocol demo" to a **real distributed compute engine**.

## Goals

1. **Job queue** — In-memory `ConcurrentLinkedQueue` on the Manager that holds pending tasks.
2. **Job state machine** — Enforce the strict lifecycle: `PENDING → ASSIGNED → RUNNING → COMPLETED | FAILED`.
3. **Job assignment** — Scheduler loop picks idle workers, sends `ASSIGN_JOB`, and transitions jobs to `ASSIGNED`.
4. **Result handling** — Workers execute dummy tasks, return `JOB_RESULT`, manager transitions jobs to `COMPLETED` or `FAILED`.
5. **Crash recovery** — When a worker dies while running a job, the job is automatically re-queued as `PENDING`.

## Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Queue implementation | `ConcurrentLinkedQueue<Job>` | Non-blocking, unbounded, ideal for producer-consumer |
| Scheduling strategy | FIFO with idle-worker polling | Simple, deterministic, easy to reason about |
| Job execution | Dummy CPU-bound tasks (e.g., compute Fibonacci, sleep) | Proves the pipeline without requiring real payloads |
| Crash recovery trigger | Heartbeat monitor (from M1) sets job back to PENDING | Single responsibility — reuses existing detection |

## Success Criteria

- [ ] Submit 10 dummy jobs via in-code API → all 10 reach `COMPLETED` across 3 workers
- [ ] Kill a worker mid-job → the job reappears as `PENDING` and is re-assigned to another worker
- [ ] No jobs are ever lost under normal or crash scenarios
- [ ] Manager logs full job lifecycle trace: `PENDING → ASSIGNED → RUNNING → COMPLETED`

## Dependencies

- Milestone 1 fully complete (protocol, manager, worker, heartbeat)

## Estimated Effort

**4–5 working days**
