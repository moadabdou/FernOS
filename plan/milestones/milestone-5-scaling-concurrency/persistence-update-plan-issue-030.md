# Persistence Layer Update Plan for #030 (Concurrent Job Assignments)

With the core engine already updated to support capacity-based concurrent worker tracking, the persistence layer in `manager-node` must now be aligned to cleanly mirror this new architecture. The shift from a binary `IDLE`/`BUSY` state to a concurrent capacity model requires database schema evolution and entity mapping updates.

---

### 1. Database Schema Migration (Flyway)
**Action:** Create a new migration script `V2__concurrent_worker_capacity.sql` in `manager-node/src/main/resources/db/migration/`.

- **Schema Updates:**
  - Add integer column `max_capacity` (default 4) to the `workers` table.
  - Add integer column `active_job_count` (default 0) to the `workers` table.
- **Constraints Updates:**
  - Drop the existing `CHECK` constraint on `status` (`IDLE`, `BUSY`, `OFFLINE`).
  - Add a new `CHECK` constraint supporting the new capacity states (e.g., `ONLINE`, `OFFLINE`).
- **Data Migration:**
  - Update existing rows where `status` is `'IDLE'` or `'BUSY'` to the new state `'ONLINE'`.
  - Calculate existing `active_job_count` for each worker by counting associated jobs in the `jobs` table that are still `RUNNING`, or just reset default gracefully.

### 2. Update `WorkerStatus` Enum (Core & Manager)
**Action:** Refactor the `WorkerStatus` enumeration.
- Deprecate/remove `IDLE` and `BUSY` states.
- Introduce `ONLINE` (or similar unified active state) and preserve `OFFLINE`.

### 3. Update `WorkerEntity` JPA Model
**Action:** Modify `WorkerEntity.java` in `manager-node/src/main/java/com/doe/manager/persistence/entity/`.
- **Fields:** Add `private int maxCapacity;` and `private int activeJobCount;`.
- **Annotations:** Map `max_capacity` and `active_job_count` to their respective `@Column` fields.
- **Methods:** Update constructive parameters, getters, and setters to incorporate the capacity metrics.

### 4. Update the Event Listener (`DatabaseEventListener.java`)
**Action:** Integrate capacity awareness into the boundary between domain events and JPA saves.
- **Worker Registration (`onWorkerRegistered` / Hearbeats):** Update the handler so that when workers connect or pulse, their specific `maxCapacity` configuration is persisted/updated on the `WorkerEntity`.
- **Job Execution Tracking (`onJobAssigned`, `onJobCompleted`, `onJobFailed`):**
  - When a job is assigned to a worker, fetch the `WorkerEntity` and increment `activeJobCount`.
  - When a job terminates (success, failure, or engine recovery re-queues), decrement the worker's `activeJobCount`.

### 5. Repository Adjustments (`WorkerRepository.java`)
**Action:** Refactor custom Spring Data JPA queries referencing outdated states.
- Find and replace usages of hardcoded `findByStatus(WorkerStatus.IDLE)`.
- If the Manager Node queries the persistence layer for available workers (though likely relies on the Engine's fast memory bounds), replace standard status queries with predicates validating availability: `WHERE w.status = 'ONLINE' AND w.activeJobCount < w.maxCapacity`.

### 6. Refactor Integration Tests
**Action:** Fix breaking tests impacted by schema and lifecycle changes.
- Update `DatabaseEventListenerTest.java` to assert `activeJobCount` appropriately increments/decrements alongside jobs transitioning states.
- Refactor `WorkerRepositoryTest` to validate constraints matching the updated Flyway specifications.
- Adapt `StartupRecoveryIntegrationTest` regarding the recovery of workers handling multiple stranded jobs on shutdown, ensuring capacity bounds remain valid through the mock data generation.