# Issue 038.2: WorkflowManager — Lifecycle Service

## Phase
**Phase 1: Engine — In-Memory Workflow Management**

## Description
Implement the core `WorkflowManager` service that manages workflow lifecycle operations in memory with thread-safe state management.

## Scope

### WorkflowManager Service
Located in: `manager-node/src/main/java/com/doe/manager/workflow/WorkflowManager.java`

**Operations:**
- `registerWorkflow(Workflow)` — validates DAG, stores in memory, sets status to DRAFT
- `deleteWorkflow(UUID)` — removes from memory (only if not RUNNING)
- `updateWorkflow(UUID, Workflow)` — replaces workflow definition (only if editable; recalculates topological order)
- `executeWorkflow(UUID)` — transitions DRAFT → RUNNING, begins scheduling eligible jobs
- `pauseWorkflow(UUID)` — transitions RUNNING → PAUSED, stops scheduling new jobs
- `resumeWorkflow(UUID)` — transitions PAUSED → RUNNING, resumes scheduling
- `resetWorkflow(UUID)` — transitions any non-RUNNING state back to DRAFT, resets all job statuses to PENDING
- `getWorkflow(UUID)` — returns workflow snapshot
- `listWorkflows()` — returns all workflows (optionally filtered by status)

### Thread Safety
- Use `ConcurrentHashMap<UUID, Workflow>` for storage
- Synchronize state mutations with `ReentrantReadWriteLock` or `synchronized` blocks
- Prevent race conditions between API calls, scheduler thread, and listener callbacks

### Lifecycle State Machine
Enforce valid transitions:
```
DRAFT → RUNNING (execute)
DRAFT → DELETED (delete)
DRAFT → DRAFT (update)
RUNNING → PAUSED (pause)
PAUSED → RUNNING (resume)
PAUSED → DRAFT (reset)
FAILED → DRAFT (reset)
COMPLETED → DRAFT (reset)
```

**Editing rules:**
- Allowed when: DRAFT or PAUSED
- Blocked when: RUNNING, COMPLETED, or FAILED

## Acceptance Criteria
- [ ] All lifecycle operations implemented and thread-safe
- [ ] State machine enforces valid transitions (rejects invalid ones)
- [ ] `WorkflowManagerTest` covers all operations and edge cases
- [ ] `WorkflowStatusMachineTest` validates all valid/invalid transitions
- [ ] Concurrent access handled without race conditions

## Deliverables
```
manager-node/
  src/main/java/com/doe/manager/workflow/
    WorkflowManager.java
  src/test/java/com/doe/manager/workflow/
    WorkflowManagerTest.java
    WorkflowStatusMachineTest.java
```

## Dependencies
- Issue 038.1 (Workflow Domain Models & DAG Validator)
- Existing `JobQueue` and `JobExecutor` (to be wrapped later)

## Notes
- No persistence yet — workflows lost on restart (acceptable for Phase 1)
- Legacy compatibility: existing `JobScheduler` (FIFO) can coexist; `DagScheduler` will wrap it later
