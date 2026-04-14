# Issue 038.13: DAG Editor — Interactive Node & Edge Management

## Phase
**Phase 4: Frontend — DAG Visualizer & Graphical Control**

## Description
Implement interactive editing capabilities in the DAG editor modal: add/delete/edit nodes and edges with real-time cycle prevention and save functionality.

## Scope

### 1. Node Management
- **Add node:** Click "Add Job" → placeholder node appears → user edits label/payload → save
- **Delete node:** Select node → click "Delete" → confirmation → removes node and its edges
- **Edit node:** Double-click → inline editor → save updates

### 2. DagNodeEditor Component
- Inline node editor triggered by double-clicking a node
- Form fields: label, payload (JSON textarea with validation), timeout, retry count
- Save/Cancel buttons
- Used for both creating new nodes and editing existing ones

### 3. Edge Management
- **Add edge:** Drag from output handle of node A to input handle of node B
- **Delete edge:** Click edge → press Delete key or click delete button
- **Cycle prevention:** Real-time cycle check on edge creation → error toast if cycle detected
  - Uses `dagCycleCheck` utility from frontend
  - Backend also validates on save (source of truth)

### 4. Save Behavior
- Save button enabled only when there are unsaved changes AND editing is allowed
- On save: `PUT /api/v1/workflows/{id}` with updated definition
- Shows loading → success/error feedback
- On success: invalidates React Query cache, closes modal
- On error: shows error message, keeps modal open

### 5. Create Mode
- If opened with no workflow ID, creates a new workflow via `POST /api/v1/workflows`
- Starts with empty canvas, user adds nodes and edges
- Save creates the workflow and returns the ID

### 6. Unsaved Changes Tracking
- Track modifications to nodes and edges
- Warn user on modal close if there are unsaved changes
- Reset tracking after successful save

## Acceptance Criteria
- [ ] Users can add new nodes to the canvas
- [ ] Users can delete nodes (with confirmation) and their edges are removed
- [ ] Users can edit node properties via inline editor
- [ ] Users can create edges by dragging between node handles
- [ ] Users can delete edges via click + Delete key or button
- [ ] Real-time cycle detection prevents invalid edge creation (error toast)
- [ ] Save button is only enabled when there are unsaved changes and editing is allowed
- [ ] Save triggers correct API call (`POST` for new, `PUT` for existing)
- [ ] Save shows loading/success/error feedback
- [ ] Unsaved changes warning on modal close
- [ ] `DagEditorModal.test.tsx` updated to cover editing scenarios
- [ ] `DagNodeEditor.test.tsx` covers inline editing

## Deliverables
```
dashboard/
  src/components/
    DagNodeEditor.tsx                   -- NEW

  src/test/
    components/DagNodeEditor.test.tsx
```

(Also updates existing `DagEditorModal.tsx` and its test to cover editing scenarios)

## Dependencies
- Issue 038.12 (DAG Editor Modal — Read-Only & Visual Controls)
- Issue 038.10 (Frontend Foundation — Types, API Functions & Utilities) — specifically `dagCycleCheck`

## Notes
- Start simple: basic inline editor first, refine UX later.
- Frontend cycle check is a UX nicety; backend is the source of truth and will reject invalid DAGs.
- Payload validation: use JSON schema validation if available, or try/catch `JSON.parse`.
