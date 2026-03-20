# Issue #013 — REST API Controllers (Jobs & Workers)

**Milestone:** 3 — Spring Boot Management Plane  
**Labels:** `api`, `spring-boot`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #012  

## Description

Implement RESTful endpoints for submitting jobs and querying system state.

### API Endpoints

| Method | Path | Description | Request Body | Response |
|--------|------|-------------|-------------|----------|
| `POST` | `/api/v1/jobs` | Submit a new job | `{ "payload": { ... } }` | `201 Created` + Job JSON |
| `GET` | `/api/v1/jobs` | List all jobs | — | Paginated list, filterable by `?status=` |
| `GET` | `/api/v1/jobs/{id}` | Get job details | — | Job JSON or `404` |
| `GET` | `/api/v1/workers` | List all workers | — | Array of worker status objects |

### Response DTOs

```json
// Job Response
{
  "id": "uuid",
  "status": "RUNNING",
  "payload": { ... },
  "result": null,
  "workerId": "uuid",
  "createdAt": "2025-...",
  "updatedAt": "2025-..."
}

// Worker Response
{
  "id": "uuid",
  "hostname": "worker-1",
  "ipAddress": "192.168.1.10",
  "status": "BUSY",
  "lastHeartbeat": "2025-..."
}
```

## Acceptance Criteria

- [ ] `JobController` and `WorkerController` with `@RestController` + `@RequestMapping("/api/v1")`
- [ ] `POST /jobs` → validates payload, saves to DB, enqueues in `JobQueue`, returns `201`
- [ ] `GET /jobs` → paginated (`?page=0&size=20`), optional `?status=PENDING` filter
- [ ] `GET /jobs/{id}` → returns job or `404`
- [ ] `GET /workers` → returns live worker list from registry + DB
- [ ] Request/Response DTOs separate from JPA entities (use `JobRequest`, `JobResponse`, `WorkerResponse`)
- [ ] Global `@ControllerAdvice` exception handler for validation errors → `400`, not-found → `404`
- [ ] Tested with `curl` or Postman: submit job → appears in GET → worker picks it up → status updates
