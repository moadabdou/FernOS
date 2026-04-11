# Future Improvements & Enhancements

This document tracks ideas and features that fall outside the main scope of the current 7 development milestones, but are highly valuable for the long-term maturity of the engine.

## Milestone 9: Pluggable Executors & Advanced Job Types
*Timeline: Scheduled after Milestone 8, once the control plane is mature.*

Currently, the engine executes generic shell commands or simple tasks. Modern orchestration engines (Airflow, Prefect, Dagster) excel because they support **domain-specific operators** and **pluggable executors**. This milestone transforms the engine from a "shell command runner" into a **versatile data & infrastructure orchestration platform**.

### Planned Features:
- **Pluggable Task Executor Interface:** A standardized SPI (Service Provider Interface) allowing custom executors for different workload types without modifying core engine code.
- **Advanced Job Types / Operators:**
  - **Python Operator:** Execute Python scripts, functions, or notebooks with environment isolation (virtualenv/conda).
  - **HTTP/API Operator:** Make HTTP requests, handle retries, validate responses, and chain API calls.
  - **SQL/Database Operator:** Execute SQL queries against various databases (PostgreSQL, MySQL, BigQuery) with result handling.
  - **Docker/Kubernetes Operator:** Run containers, manage container lifecycle, and deploy to K8s clusters.
  - **Sensor/Trigger Operator:** Wait for external events (file arrival, API response, time-based triggers) before proceeding.
- **Environment & Dependency Management:** Per-job dependency injection, custom Docker images, or virtual environments.
- **Result Passing & XComs:** A mechanism for jobs to exchange data between DAG nodes (like Airflow's XCom), enabling dynamic workflows.
- **Retries & Alerting per Operator Type:** Configurable retry policies specific to each operator (e.g., exponential backoff for HTTP, fixed delay for SQL).

### Rationale for Deferment:
1. **Core Engine First:** Before supporting diverse workload types, the core scheduling, distribution, and fault-tolerance mechanisms must be rock-solid (Milestones 1-7).
2. **UI Foundation Needed:** Managing advanced operators requires the control plane from Milestone 8 (DAG visualizer, workflow builder, job configuration forms). Building operators without the UI to configure them would result in a poor developer experience.
3. **Complexity:** Each operator type (Python, SQL, Docker, K8s) is essentially a mini-project requiring its own testing, security considerations, and error handling. Deferring ensures we don't dil focus from the orchestration engine's core reliability.

## Milestone 8: Advanced Control Plane (Dashboard v2.0)
*Timeline: Scheduled for after the completion of Milestones 1-7 (specifically after Testing, Hardening, Scaling, and DevOps are finalized).*

While Milestone 4 delivered solid read-only observability and monitoring, the frontend should eventually support a full **Control Plane** to manage workflows interactively.

### Planned Features:
- **Dependency Graphs (DAG Visualizer):** A visual, interactive representation of job workflows and their dependencies (similar to Airflow's DAG UI).
- **Visual Job Scheduling & Management:** The ability to schedule, pause, resume, cancel, or retry jobs interactively from the dashboard.
- **Full System Controls:** Granular write-access to manage worker nodes (e.g., manually draining a node, forcing disconnects/reconnections, or updating worker tags directly from the UI).
- **Interactive Workflow Builder:** Drag-and-drop builders or advanced form-based job submission capabilities.

### Rationale for Deferment:
1. **MVP Focus:** The primary goal of the current roadmap is to get a robust, distributed orchestration engine deployed and hardened. Read-only observability is sufficient to monitor and debug the MVP.
2. **Backend Stability First:** Features like DAG scheduling and system control require extremely robust backend APIs, transaction management, and concurrency handling. Developing the UI for this before the backend is scaled (Milestone 5) and hardened (Milestone 7) risks significant frontend rework if backend architectural changes occur.
3. **Scope Control:** Building interactive DAG visualizers is a complex, time-consuming frontend task. Deferring this prevents it from becoming a distraction that stalls the core engine's architectural development.
