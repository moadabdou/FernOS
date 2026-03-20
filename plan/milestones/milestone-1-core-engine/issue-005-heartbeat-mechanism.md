# Issue #005 — Implement Heartbeat Mechanism & Dead Worker Detection

**Milestone:** 1 — Core Engine  
**Labels:** `engine-core`, `manager-node`, `worker-node`, `reliability`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #003, #004  

## Description

Add a bidirectional health-check system: workers send periodic heartbeats, and the manager detects dead workers when heartbeats stop arriving.

### Worker Side

- A dedicated thread (or virtual thread) sends a `HEARTBEAT` message every **5 seconds** on the existing TCP connection.

### Manager Side

- A `HeartbeatMonitor` scheduled task runs every **5 seconds**, iterating over `WorkerRegistry`.
- If `Instant.now() - worker.lastHeartbeat > 15 seconds` → mark worker `DEAD`, close socket, remove from registry.

## Acceptance Criteria

- [ ] Worker sends `HEARTBEAT` every 5 s (configurable)
- [ ] Manager updates `lastHeartbeat` on each received heartbeat
- [ ] `HeartbeatMonitor` runs as a `ScheduledExecutorService` with a 5 s fixed-rate
- [ ] Worker killed with `kill -9` → Manager detects within 15 s and logs `Worker <UUID> marked DEAD`
- [ ] Dead worker removed from `WorkerRegistry`
- [ ] Integration test: start worker → wait 10 s (2 heartbeats) → kill worker → wait 20 s → assert registry is empty

## Technical Notes

- Use `Executors.newSingleThreadScheduledExecutor()` for the monitor — keep it on a platform thread for reliability
- Heartbeat interval and timeout should be configurable via constants or a config class for easy tuning
