# Issue #002 — Implement Custom Binary Protocol Encoder/Decoder

**Milestone:** 1 — Core Engine  
**Labels:** `engine-core`, `networking`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #001  

## Description

Design and implement the wire protocol that all Manager ↔ Worker communication will use. Every message on the TCP socket conforms to:

```
+----------+----------------+---------------------+
| 1 byte   | 4 bytes        | N bytes             |
| Msg Type | Payload Length  | Payload (JSON/Raw)  |
+----------+----------------+---------------------+
```

### Message Types (enum `MessageType`)

| Code | Name | Direction | Payload |
|------|------|-----------|---------|
| `0x01` | `REGISTER_WORKER` | Worker → Manager | `{ "workerId": "<UUID>", "hostname": "..." }` |
| `0x02` | `HEARTBEAT` | Worker → Manager | `{ "workerId": "<UUID>", "timestamp": <epoch> }` |
| `0x03` | `ASSIGN_JOB` | Manager → Worker | `{ "jobId": "<UUID>", "payload": { ... } }` |
| `0x04` | `JOB_RESULT` | Worker → Manager | `{ "jobId": "<UUID>", "status": "COMPLETED\|FAILED", "output": "..." }` |

## Acceptance Criteria

- [ ] `MessageType` enum with byte-code mapping
- [ ] `ProtocolEncoder.encode(MessageType, byte[] payload) → byte[]` writes header + payload
- [ ] `ProtocolDecoder.decode(InputStream) → Message` reads header, then exactly N payload bytes
- [ ] Round-trip unit test: encode → decode produces identical `Message` object
- [ ] Edge-case test: empty payload (`N = 0`) handled correctly
- [ ] Edge-case test: maximum payload size guard (e.g., 10 MB limit)

## Technical Notes

- Use `java.io.DataOutputStream` / `DataInputStream` for big-endian integer encoding
- `Message` record: `record Message(MessageType type, byte[] payload) {}`
- Keep codec stateless and thread-safe — no mutable fields
