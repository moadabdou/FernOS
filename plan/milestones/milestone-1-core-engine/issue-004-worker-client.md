# Issue #004 — Build the Worker Client with Registration

**Milestone:** 1 — Core Engine  
**Labels:** `worker-node`, `networking`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #002  

## Description

Implement the `WorkerClient` application that connects to the Manager, sends a `REGISTER_WORKER` message, and waits for further instructions.

### Lifecycle

```
1. Generate a unique UUID on startup
2. Connect to Manager at <host>:<port> via TCP Socket
3. Send REGISTER_WORKER { workerId, hostname }
4. Enter main loop: listen for ASSIGN_JOB messages
5. On disconnect / exception → log error, attempt reconnection (backoff)
```

## Acceptance Criteria

- [ ] Worker connects to a configurable `manager.host` and `manager.port`
- [ ] On startup, sends `REGISTER_WORKER` with a fresh `UUID` + system hostname
- [ ] Main loop blocks on `ProtocolDecoder.decode(inputStream)` waiting for commands
- [ ] On socket close by Manager → log and exit gracefully
- [ ] Connection failure on startup → retry with exponential backoff (1s, 2s, 4s … max 30s)
- [ ] CLI args or config file for host/port override

## Technical Notes

- Use `InetAddress.getLocalHost().getHostName()` for hostname
- Reconnection logic should be extracted to a reusable `RetryPolicy` utility in `engine-core`
- Avoid catching generic `Exception`; handle `IOException`, `ConnectException` distinctly
