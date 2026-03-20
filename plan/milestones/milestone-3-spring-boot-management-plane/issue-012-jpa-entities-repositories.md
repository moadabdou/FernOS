# Issue #012 — JPA Entities, Repositories & Database Schema

**Milestone:** 3 — Spring Boot Management Plane  
**Labels:** `persistence`, `database`, `priority:high`  
**Assignee:** —  
**Estimate:** 1 day  
**Depends on:** #011  

## Description

Create JPA entity classes and Spring Data repositories for `Job` and `Worker`, backed by PostgreSQL with Flyway migrations.

### Database Schema

**Table: `workers`**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `UUID` | PK |
| `hostname` | `VARCHAR(255)` | NOT NULL |
| `ip_address` | `VARCHAR(45)` | NOT NULL |
| `status` | `VARCHAR(20)` | NOT NULL, CHECK (IDLE, BUSY, OFFLINE) |
| `last_heartbeat` | `TIMESTAMP` | NOT NULL |
| `registered_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() |

**Table: `jobs`**

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | `UUID` | PK |
| `worker_id` | `UUID` | FK → workers.id, NULLABLE |
| `status` | `VARCHAR(20)` | NOT NULL, INDEX |
| `payload` | `JSONB` | NOT NULL |
| `result` | `TEXT` | NULLABLE |
| `retry_count` | `INTEGER` | NOT NULL, DEFAULT 0 |
| `created_at` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() |
| `updated_at` | `TIMESTAMP` | NOT NULL |

## Acceptance Criteria

- [ ] `JobEntity` and `WorkerEntity` JPA classes with proper annotations (`@Entity`, `@Id`, `@Enumerated`, `@Column`)
- [ ] `JobRepository extends JpaRepository<JobEntity, UUID>` with custom query: `findByStatus(JobStatus)`
- [ ] `WorkerRepository extends JpaRepository<WorkerEntity, UUID>`
- [ ] Flyway migration `V1__create_tables.sql` creates both tables with indexes
- [ ] `application.yml` with PostgreSQL datasource config, Flyway enabled
- [ ] `./gradlew bootRun` starts cleanly, tables created in PostgreSQL

## Technical Notes

- Use `@Enumerated(EnumType.STRING)` for status fields
- Add index on `jobs.status` for efficient filtering
- Use `@UpdateTimestamp` on `updatedAt` via Hibernate
