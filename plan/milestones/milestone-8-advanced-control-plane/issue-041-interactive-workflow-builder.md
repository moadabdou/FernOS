# Issue 041: Interactive Workflow Builder

## Description
Build a drag-and-drop workflow builder that allows users to visually construct job chains and submit them to the orchestration engine.

## Requirements
- Drag-and-drop canvas for placing job nodes
- Ability to connect nodes with dependency edges
- Form-based job configuration (command, timeout, retries, tags, dependencies)
- Validation before submission (no cycles, all required fields filled)
- Export workflow as JSON for version control or template reuse
- Submit workflow directly to the job queue via backend API

## Acceptance Criteria
- [ ] Users can drag job nodes onto the canvas
- [ ] Users can draw connections between nodes to define dependencies
- [ ] Form validation prevents invalid submissions
- [ ] Workflows can be saved as reusable templates
- [ ] Submitted workflows appear in the Jobs view and execute correctly

## Dependencies
- Issue 038 DAG Visualizer (shared graph library)
- Milestone 3 REST API for job submission
