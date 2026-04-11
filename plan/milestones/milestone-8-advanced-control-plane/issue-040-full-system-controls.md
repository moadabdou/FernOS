# Issue 040: Full System Controls

## Description
Provide granular write-access controls in the dashboard to manage worker nodes interactively, including draining nodes, forcing disconnects/reconnections, and updating worker tags.

## Requirements
- Add worker management UI with action buttons (Drain, Disconnect, Reconnect)
- Implement backend endpoints for worker lifecycle operations
- Support updating worker tags/labels directly from the UI
- Require confirmation for destructive operations (drain, disconnect)
- Reflect worker state changes in real-time across the dashboard

## Acceptance Criteria
- [ ] Users can drain a worker node (complete current jobs, stop accepting new ones)
- [ ] Users can force disconnect a worker from the manager
- [ ] Users can trigger reconnection attempts for disconnected workers
- [ ] Worker tags can be edited via an inline form or modal
- [ ] All operations are logged and auditable

## Dependencies
- Milestone 3 worker registry
- Milestone 5 async worker client
