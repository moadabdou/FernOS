# Issue 039: Visual Job Scheduling & Management

## Description
Enable interactive job lifecycle management from the dashboard, allowing users to schedule, pause, resume, cancel, or retry jobs directly from the UI.

**Note:** This issue is now decoupled from the DAG Visualizer (Issue 038 series) and focuses on **individual job controls** that work for both standalone jobs and jobs within workflows.

## Requirements
- Add action buttons (Pause, Resume, Cancel, Retry) to job detail views
- Implement backend API endpoints for each action with proper validation
- Ensure idempotent operations to prevent duplicate actions from double-clicks
- Display confirmation dialogs for destructive actions (Cancel, Retry)
- Show real-time feedback as jobs transition states
- Integrate with existing `/jobs` endpoints (backward compatible)

## Acceptance Criteria
- [ ] Users can pause running jobs and resume them successfully
- [ ] Users can cancel pending or running jobs
- [ ] Users can retry failed jobs, creating a new execution
- [ ] All actions are idempotent (safe to retry)
- [ ] UI provides clear success/error feedback for each action
- [ ] Actions update the parent workflow state correctly (if job belongs to a workflow)

## Dependencies
- Milestone 2 job state machine
- Milestone 3 REST API controllers
- Issue 038.8 (Workflow REST API) — for workflow state sync when jobs are part of workflows

## Implementation Order
This issue should be implemented **after Issue 038.9 (Phase 3 API)** because:
1. Workflow engine must exist to handle job state changes within workflows
2. Job actions must trigger workflow state re-evaluation (via `JobResultListener`)
3. Dashboard already has polling infrastructure from Phase 4 setup

## Deliverables
```
manager-node/
  src/main/java/com/doe/manager/api/controller/
    JobActionController.java          -- NEW (pause, resume, cancel, retry endpoints)

dashboard/
  src/components/
    JobActionButtons.tsx              -- NEW (pause, resume, cancel, retry buttons)
  src/api/
    jobActions.ts                     -- NEW (API functions for job actions)
```

## Notes
- Job actions on standalone jobs (auto-workflows) work the same as workflow jobs
- When a job action affects workflow state (e.g., job failure → workflow failure), the `JobResultListener` handles the cascade
- Idempotency: use job status checks before performing actions to prevent duplicate state transitions
