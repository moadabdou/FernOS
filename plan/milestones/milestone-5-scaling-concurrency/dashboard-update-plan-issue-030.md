# Frontend Dashboard Update Plan for #030 (Concurrent Job Assignments)

With the backend API now supporting capacity-based concurrent worker tracking, the React frontend must be updated to visualize these new capabilities. This includes modifying type definitions, adding capacity gauges, and introducing color-coded worker associations.

### 1. Update Type Definitions and API Client
**Target Files:** 
- `dashboard/src/types/api.ts`
- `dashboard/src/api/` (mock data or client if applicable)

**Actions:**
- Modify the `Worker` interface (or equivalent) to include the new fields: `maxCapacity: number` and `activeJobCount: number`.
- Ensure any mock data used for testing or development reflects these new fields.
- Review the `Job` interface to ensure it contains a property linking it to a `workerId` or `workerHostname`.

### 2. Implement Worker Color Generator Utility
**Target Files:** 
- Add a new file, e.g., `dashboard/src/utils/workerColors.ts`

**Actions:**
- Create a predictable color generation function (e.g., string-hashing the `workerId` or `hostname` to a predefined palette of Tailwind CSS colors).
- This ensures that a specific worker always retains the same distinct color across re-renders and across different components (WorkerCard and JobRow).

### 3. Update the `WorkerCard` Component
**Target Files:** 
- `dashboard/src/components/WorkerCard.tsx`

**Actions:**
- **Color Coding:** Apply the generated worker color to the card (e.g., a top border, background glow, or icon color).
- **Capacity Indicator:** Replaces older "status: idle/busy" text with a small circular progress bar (SVG-based or CSS conic-gradient). 
  - The bar will represent the percentage: `(activeJobCount / maxCapacity) * 100`.
  - Display the text fraction natively inside or next to the ring (e.g., `2 / 4`).

### 4. Update the `JobRow` Component
**Target Files:** 
- `dashboard/src/components/JobRow.tsx`

**Actions:**
- **Conditional Visibility:** Add logic to check if `job.status === 'ASSIGNED' || job.status === 'RUNNING'`. Only show the worker label if this condition is met (hide for `COMPLETED`, `FAILED`, etc.).
- **Worker Label Display:** 
  - Add a pill/badge element that says "Worker: {workerHostname}".
  - Use the color generator utility to apply the exact same background/text color payload assigned to that worker.

### 5. Final Integration & Testing
**Target Files:** 
- `dashboard/src/components/WorkerNodesPanel.tsx`
- `dashboard/src/components/JobQueuePanel.tsx`

**Actions:**
- Verify that state flows down properly.
- Ensure Tailwind classes used for dynamic colors are either safelisted in `tailwind.config.js` or statically mapped to avoid purging.
- Run frontend unit tests and verify the UI behavior locally against the updated Manager Node API.
