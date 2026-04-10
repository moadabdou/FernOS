# Issue 038: DAG Visualizer

## Description
Implement an interactive dependency graph visualizer that displays job workflows as a Directed Acyclic Graph (DAG), similar to Airflow's UI.

## Requirements
- Render nodes representing jobs with status-based coloring (PENDING, RUNNING, COMPLETED, FAILED)
- Display edges showing dependencies between jobs
- Support zoom, pan, and click-to-inspect functionality
- Auto-refresh on job state changes via WebSocket
- Handle large workflows (100+ nodes) without performance degradation

## Acceptance Criteria
- [ ] DAG renders correctly for linear, branching, and fan-out workflows
- [ ] Clicking a node opens a detail panel with job metadata
- [ ] Real-time color updates when job states change
- [ ] Performance remains smooth with 100+ nodes

## Dependencies
- Milestone 4 frontend foundation
- Milestone 3 REST API for job/workflow data
