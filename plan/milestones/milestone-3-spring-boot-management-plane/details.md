# Milestone 3: Spring Boot Management Plane & Persistence

## Overview

Wrap the raw Java Manager engine inside a Spring Boot application, exposing a RESTful API for job submission and worker monitoring, and persisting all state to PostgreSQL via Spring Data JPA.

After this milestone, users interact with the cluster through standard HTTP/JSON — no more in-code job submission.

## Goals

1. **Spring Boot integration** — Refactor `ManagerServer` to run as a `@Service` bean, started on application boot.
2. **JPA entities & repositories** — Map `Job` and `Worker` to PostgreSQL tables with proper indexing.
3. **REST API** — `POST /api/v1/jobs`, `GET /api/v1/jobs`, `GET /api/v1/workers` with pagination and filtering.
4. **Event synchronization** — Wire the internal engine events (job completed, worker registered) to database writes.
5. **Manager crash recovery** — On startup, query DB for `RUNNING` jobs and reset to `PENDING`.

## Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Database | PostgreSQL 15+ | Industry standard, JSONB for payloads, battle-tested |
| ORM | Spring Data JPA + Hibernate | Convention over configuration, repository pattern |
| API style | RESTful JSON, versioned `/api/v1/` | Standard, frontend-friendly |
| Payload storage | JSONB column | Flexible schema, queryable |
| Migration tool | Flyway | Version-controlled schema migrations |

## Success Criteria

- [ ] `POST /api/v1/jobs` with JSON body creates a job in DB and enqueues it
- [ ] `GET /api/v1/jobs` returns paginated list with status filtering
- [ ] `GET /api/v1/workers` returns live worker status
- [ ] Jobs submitted via Postman/curl are picked up by connected workers
- [ ] Manager restart: `RUNNING` jobs reset to `PENDING`, workers reconnect

## Dependencies

- Milestones 1 and 2 fully complete
- PostgreSQL instance (local or Docker)

## Estimated Effort

**5–6 working days**
