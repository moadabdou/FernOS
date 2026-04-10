# Issue 039: Visual Job Scheduling & Management

## Description
Enable interactive job lifecycle management from the dashboard, allowing users to schedule, pause, resume, cancel, or retry jobs directly from the UI.

## Requirements
- Add action buttons (Pause, Resume, Cancel, Retry) to job detail views
- Implement backend API endpoints for each action with proper validation
- Ensure idempotent operations to prevent duplicate actions from double-clicks
- Display confirmation dialogs for destructive actions (Cancel, Retry)
- Show real-time feedback as jobs transition states

## Acceptance Criteria
- [ ] Users can pause running jobs and resume them successfully
- [ ] Users can cancel pending or running jobs
- [ ] Users can retry failed jobs, creating a new execution
- [ ] All actions are idempotent (safe to retry)
- [ ] UI provides clear success/error feedback for each action

## Dependencies
- Milestone 2 job state machine
- Milestone 3 REST API controllers
