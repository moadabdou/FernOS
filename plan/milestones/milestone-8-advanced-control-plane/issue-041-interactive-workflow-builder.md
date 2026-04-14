# Issue 041: Interactive Workflow Builder

## Description
Build a drag-and-drop workflow builder that allows users to visually construct job chains and submit them to the orchestration engine.

**Note:** This issue extends the DAG Editor functionality (Issue 038.12/038.13) with additional **template management** and **advanced workflow creation** features. It should be implemented **after Issue 038.14 (Phase 4 Frontend)**.

## Requirements
- Drag-and-drop canvas for placing job nodes
- Ability to connect nodes with dependency edges
- Form-based job configuration (command, timeout, retries, tags, dependencies)
- Validation before submission (no cycles, all required fields filled)
- Export workflow as JSON for version control or template reuse
- Submit workflow directly to the job queue via backend API
- **Workflow templates:** save reusable workflow definitions for quick creation

## Acceptance Criteria
- [ ] Users can drag job nodes onto the canvas from a node palette
- [ ] Users can draw connections between nodes to define dependencies
- [ ] Form validation prevents invalid submissions
- [ ] Workflows can be saved as reusable templates
- [ ] Submitted workflows appear in the Jobs view and execute correctly
- [ ] Workflow templates can be loaded and instantiated

## Dependencies
- Issue 038.8 (Workflow REST API) — backend workflow CRUD
- Issue 038.10 (Frontend Foundation) — React Flow, dagre, types, API functions
- Issue 038.12 (DAG Editor Modal) — shared canvas infrastructure
- Issue 038.13 (DAG Interactive Editing) — shared node/edge management

## Implementation Order
This issue should be implemented **after Issue 038.14 (Phase 4 Frontend)** because:
1. DAG Editor modal already provides the canvas infrastructure (React Flow setup, node/edge handling)
2. Workflow builder extends the editor with **template management** and **quick-create workflows**
3. Shared components (DagJobNode, DagNodeEditor, dagCycleCheck) can be reused

## Deliverables
```
manager-node/
  src/main/java/com/doe/manager/api/controller/
    WorkflowTemplateController.java   -- NEW (save/load/delete templates)

dashboard/
  src/components/
    WorkflowBuilder.tsx               -- NEW (extends DagEditorModal with builder features)
    NodePalette.tsx                   -- NEW (draggable node types sidebar)
    TemplateSelector.tsx              -- NEW (browse/load saved templates)
    WorkflowExportButton.tsx          -- NEW (export as JSON)
  src/api/
    workflowTemplates.ts              -- NEW (template API functions)
  src/types/
    templates.ts                      -- NEW (template types)
  src/pages/
    WorkflowBuilderView.tsx           -- NEW (dedicated builder page)
```

## Relationship to Issue 038.12/038.13
The DAG Editor modal (038.12/038.13) provides:
- React Flow canvas setup
- Node/edge rendering and interaction
- Auto-layout with dagre
- Cycle detection
- Save functionality

The Workflow Builder (041) extends this with:
- **Node palette** — predefined job types users can drag onto the canvas
- **Template management** — save, load, and instantiate workflow templates
- **Export/Import** — JSON export for version control, import from JSON files
- **Quick-create workflows** — pre-filled templates for common patterns (linear pipeline, fan-out, etc.)

## Notes
- Start by reusing the DAG Editor modal infrastructure; add builder features incrementally
- Template storage: initially in-memory or simple DB table; can evolve to file-based storage later
- Export format: JSON matching the `CreateWorkflowRequest` DTO structure
- Consider adding a "template gallery" with pre-built workflow patterns for common use cases
