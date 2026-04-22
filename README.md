# Fern-OS — Distributed Job Orchestration Platform

![Fern-OS Cover](assets/Fern-OS.png)

A distributed job orchestration platform that schedules workloads across TCP-connected worker nodes with crash recovery, real-time monitoring, and a React dashboard. Built with Java 21 virtual threads, Spring Boot 3.3, PostgreSQL, and React 19.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Documentation & Guides](#documentation--guides)
- [Quickstart](#quickstart)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)

---

## Architecture Overview

flowchart TB
    subgraph Client
        Browser[Browser / cURL]
        PythonSDK[Python SDK / Workflows]
    end

    subgraph Dashboard
        ReactApp[React 19 SPA]
    end

    subgraph Manager["Manager Node (Spring Boot)"]
        REST[REST API]
        TCPServer[TCP Server]
        Scheduler[Job Scheduler]
        JobQueue[Job Queue]
    end

    subgraph Workers["Worker Nodes (Plain Java)"]
        W1[Worker 1]
        W2[Worker N]
    end

    subgraph Database
        PG[(PostgreSQL 16)]
    end

    subgraph Storage[MinIO Object Storage]
        S3[(S3 Buckets)]
    end

    PythonSDK -->|submit workflows| REST
    Browser -->|HTTP requests| REST
    Browser -->|static files| ReactApp
    ReactApp -->|REST API| REST
    REST -->|enqueue| JobQueue
    JobQueue -->|dequeue| Scheduler
    Scheduler -->|dispatch| TCPServer
    TCPServer <-->|TCP| W1
    TCPServer <-->|TCP| W2
    
    REST <-->|persist| PG
    W1 <-->|XCom / Data| S3
    W2 <-->|XCom / Data| S3
    REST -.->|cleanup| S3

### Component Summary

| Component | Technology | Role |
|-----------|------------|------|
| **engine-core** | Java 21 | Shared library: domain models, protocol codec, registries |
| **manager-node** | Spring Boot 3.3 | Central orchestrator: REST API, TCP server, scheduler, PostgreSQL |
| **worker-node** | Plain Java 21 | Lightweight client: executes tasks (Echo, Python, Shell, etc.) |
| **python-sdk** | Python 3.10+ | Developer tool: define DAGs, submit workflows, interact with XCom |
| **dashboard** | React 19, Vite | Real-time UI: monitoring and job management |
| **PostgreSQL** | Docker | Persistent storage for jobs and worker states |
| **MinIO** | Docker | S3-compatible object storage for XCom and large payloads |

### Communication Protocol

Manager ↔ Workers communicate over raw TCP sockets using a binary wire format:

```
[1 byte: MessageType][4 bytes: payload length (big-endian)][N bytes: JSON payload]
```

Supported message types: `REGISTER_WORKER`, `REGISTER_ACK`, `HEARTBEAT`, `ASSIGN_JOB`, `JOB_RUNNING`, `JOB_RESULT`, `CANCEL_JOB`.

---

## Documentation & Guides

For detailed information on everything Fern-OS, check out our comprehensive documentation:

- [**🚀 Project Setup**](docs/setup/index.md): Detailed guides for Docker, local development, and environment variables.
- [**📖 API Reference**](docs/api/introduction.md): Complete REST API documentation for managing jobs, workers, and workflows.
- [**🐍 Python SDK**](docs/python-sdk/index.md): Learn how to build and submit complex workflows using our Python library.
- [**🖥️ UI Dashboard**](docs/ui/index.md): Overview of the real-time monitoring and management interface.

---

## Quickstart

### Prerequisites

For a full breakdown of requirements, see the [Setup Guide](docs/setup/index.md#requirements).

| Requirement | Version |
|-------------|---------|
| Java | 21+ |
| Maven | 3.8+ |
| Docker & Docker Compose | v26+ / v2.24+ |
| Node.js (for dashboard dev) | 20+ |

### Running with Docker Compose (Recommended)

Get a fully operational cluster in **3 commands**:

```bash
# 1. Clone and enter the project
git clone https://github.com/moadabdou/FernOS.git&& cd FernOS

# 2. Copy environment config
cp .env.example .env
### Running with Docker Compose (Recommended)

Get a fully operational cluster in **3 commands**:

```bash
# 1. Clone and enter the project
git clone https://github.com/moadabdou/FernOS.git&& cd FernOS

# 2. Copy environment config
cp .env.example .env

# 3. Launch everything
docker compose up -d --build
```

For detailed deployment options, scaling workers, and verification steps, see the [**Docker Deployment Guide**](docs/setup/docker.md).

### Running Locally (Development)

For instructions on running the manager, workers, and dashboard locally for development, see the [**Local Development Guide**](docs/setup/index.md#running-locally-development).

---

## API Reference

## API Reference

The Fern-OS REST API allows you to manage jobs, workers, and workflows.

- **Jobs**: Submit, list, get, and cancel jobs.
- **Workers**: Monitor worker health and capacity.
- **Workflows**: Support for complex DAGs and XCom.

> [!IMPORTANT]
> For the complete API specification, including request formats and error codes, please refer to the [**Full API Documentation**](docs/api/introduction.md).

---

---

## Configuration

Fern-OS is highly configurable via environment variables and `application.yml`.

- **Environment Variables**: Port settings, database credentials, and security tokens.
- **Manager Configuration**: Scheduler behavior, heartbeat timeouts, and queue capacity.

> [!TIP]
> For a full list of configuration options, see the [**Environment Variables**](docs/setup/environment-variables.md) and [**Manager Configuration**](docs/setup/manager-config.md) guides.

---

## Testing

The project includes a comprehensive test suite covering unit, integration, and end-to-end tests.

- **Unit Tests**: Java (JUnit 5) and Dashboard (Vitest).
- **Integration Tests**: Spring Boot + Testcontainers (PostgreSQL).
- **E2E Tests**: Python-based automation scripts.

---

## Project Structure

```
distributed_orchestration_engine/
├── engine-core/                  # Shared library (no Spring)
├── manager-node/                 # Spring Boot orchestrator
├── worker-node/                  # Plain Java TCP client
├── python-sdk/                   # Python library for DAGs and workflows
├── dashboard/                    # React 19 SPA
├── docs/                         # Comprehensive documentation
├── automated_tests/              # E2E test scripts
├── docker-compose.yml            # Full cluster orchestration
└── .env.example                  # Environment template
```

### Module Dependency Graph

```
engine-core  (shared library, zero Spring dependencies)
    ↑
    ├── manager-node  (Spring Boot, depends on engine-core)
    └── worker-node   (plain Java, depends on engine-core)

dashboard  (React SPA, independent — communicates via REST)
python-sdk (Python library, independent — communicates via REST)
```

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

### Quick Summary

- **Branch naming:** `<type>/<short-description>` — e.g., `feat/shell-plugin`, `fix/heartbeat-timeout`
- **Types:** `feat`, `fix`, `docs`, `refactor`, `test`, `chore`
- **Commits:** [Conventional Commits](https://www.conventionalcommits.org/) format
- **Code style:** Google Java Format (4-space indent), run `./mvnw checkstyle:check` before PR
- **Tests:** All PRs must pass `./mvnw test` + `cd dashboard && npm test`
- **PR process:** Branch → PR → at least 1 approval → merge to `main`

---

## License

[Specify your license here]

---

*Built with ☕ Java 21 Virtual Threads and 🌱 Spring Boot 3.3*
