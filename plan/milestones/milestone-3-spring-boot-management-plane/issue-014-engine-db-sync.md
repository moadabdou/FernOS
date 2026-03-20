# Issue #014 — Engine ↔ Database Event Synchronization

**Milestone:** 3 — Spring Boot Management Plane  
**Labels:** `manager-node`, `persistence`, `integration`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #011, #012, #013  

## Description

Wire the internal engine events to database writes so that every state change is persisted.

### Events to Persist

| Engine Event | Database Action |
|---|---|
| Worker registers (TCP) | Insert into `workers` table |
| Worker sends heartbeat | Update `last_heartbeat` in `workers` |
| Worker marked dead | Set `workers.status = OFFLINE` |
| Job assigned to worker | Update `jobs.status = ASSIGNED`, set `worker_id` |
| Job result received | Update `jobs.status`, `result`, `updated_at` |
| Job re-queued (crash recovery) | Update `jobs.status = PENDING`, clear `worker_id` |

## Acceptance Criteria

- [ ] `EngineEventListener` interface with methods for each event type
- [ ] `DatabaseEventListener implements EngineEventListener` — persists events via repositories
- [ ] Engine components (ManagerServer, HeartbeatMonitor, JobScheduler) publish events through the listener
- [ ] All DB writes use `@Transactional` where appropriate
- [ ] `GET /api/v1/jobs` reflects real-time status changes from the engine
- [ ] Worker registration over TCP → appears in `GET /api/v1/workers` within 1 second
- [ ] Batch heartbeat updates (buffer for 2 s) to avoid DB write amplification

## Technical Notes

- Use Spring's `ApplicationEventPublisher` or a simple observer pattern
- Be careful with thread safety: engine threads (Virtual Threads) calling JPA repositories must be within a Spring-managed transaction context
- Consider using `@Async` for non-critical DB writes (heartbeat updates) to avoid blocking the engine loop
