# Issue 038.12: DAG Editor Modal — Read-Only & Visual Controls

## Phase
**Phase 4: Frontend — DAG Visualizer & Graphical Control**

## Description
Implement the DAG editor modal with read-only visualization, custom node components, and the detail panel for inspecting job metadata.

## Scope

### 1. DagEditorModal Component
- `fixed inset-0 z-50` overlay with `bg-slate-900/60 backdrop-blur-md`
- Panel: **95vw × 95vh**, centered, glass-panel styled
- Full React Flow canvas with:
  - **Custom node** (`DagJobNode`) — glass-card with job label, status dot, index number
  - **Edges** — status-colored lines; animated for active jobs
  - **React Flow built-in controls** — zoom, pan, minimap, fit view
  - **Toolbar** — workflow name input, Add Job button, Delete button, Save button, Close button

**Editing restrictions:**
- If workflow is `RUNNING`, `COMPLETED`, or `FAILED` → all nodes/edges are read-only
- Show banner: "Editing is disabled while workflow is running"
- Nodes can still be clicked to inspect details

### 2. DagJobNode Custom Component
- Glass-panel styled card with:
  - Job label (top)
  - Status dot (colored: PENDING=gray, ASSIGNED=blue, RUNNING=purple+pulse, COMPLETED=emerald, FAILED=red, CANCELLED=slate)
  - DAG index number (bottom-right badge)
- Handle connectors for input (top) and output (bottom) edges
- Click → opens `DagDetailPanel`

### 3. DagDetailPanel Component
- Slide-in panel from the right side of the modal
- Shows full job metadata:
  - Job ID, status badge, label, DAG index
  - Payload (JSON, pretty-printed)
  - Result (if any, JSON, pretty-printed)
  - Assigned worker (if any)
  - Created/Updated timestamps
  - Link to full job detail page (`/jobs` with job ID filter)

### 4. Auto-Layout
- On initial load: run dagre topological layout (top-to-bottom direction)
- User can override positions by dragging
- "Reset Layout" button in toolbar
- Layout runs synchronously for now; optimize with Web Worker if needed later

## Acceptance Criteria
- [ ] Modal opens from `DagPreviewPanel` edit button
- [ ] Modal opens from `WorkflowsView` "view DAG" action
- [ ] React Flow canvas renders with custom `DagJobNode` components
- [ ] Nodes display correct labels, status colors, and DAG index
- [ ] Edges display with status-colored lines
- [ ] Clicking a node opens `DagDetailPanel` with job metadata
- [ ] Editing is disabled when workflow is RUNNING/COMPLETED/FAILED (read-only mode)
- [ ] Zoom, pan, minimap, and fit view controls work
- [ ] Auto-layout positions nodes in topological order
- [ ] `DagEditorModal.test.tsx` and `DagJobNode.test.tsx` and `DagDetailPanel.test.tsx` pass

## Deliverables
```
dashboard/
  src/components/
    DagEditorModal.tsx                  -- NEW
    DagJobNode.tsx                      -- NEW
    DagDetailPanel.tsx                  -- NEW

  src/test/
    components/DagEditorModal.test.tsx
    components/DagJobNode.test.tsx
    components/DagDetailPanel.test.tsx
```

## Dependencies
- Issue 038.11 (DAG Preview Panel & Workflows View Page)
- Issue 038.10 (Frontend Foundation — Types, API Functions & Utilities)
- `@xyflow/react` and `@dagrejs/dagre` installed

## Notes
- The modal is large and has complex state (unsaved changes, node selection, detail panel). Consider using a state machine or reducer to manage the modal's internal state cleanly.
- Modal state management: keep it simple first; refactor if state gets too complex.
