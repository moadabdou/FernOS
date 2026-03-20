# Distributed Job Orchestration Platform

A high-performance, fault-tolerant distributed compute engine that accepts computational tasks via a REST API, distributes them across a scalable pool of worker nodes over a custom TCP protocol, and provides real-time observability through a TypeScript dashboard — all deployable with a single `docker-compose up`.

---

## The Problem

Modern applications increasingly need to distribute heavy computational work across multiple machines — batch data processing, parallel image rendering, ML inference, report generation. Building a reliable system that queues work, assigns it to available workers, handles crashes gracefully, and gives operators full visibility is a core distributed systems challenge.

## The Solution

A four-layer platform that tackles each concern with the right tool:

### Layer 1 — Core Engine (Java 21)

The high-performance heart of the system. A central **Manager** node communicates with a fleet of **Worker** nodes over raw TCP sockets using a custom binary protocol. Java 21 Virtual Threads (Project Loom) allow the Manager to handle thousands of concurrent worker connections without traditional thread-pool limitations.

- Custom binary wire protocol: `[1B Type][4B Length][NB JSON Payload]`
- Worker registration, heartbeat monitoring, and automatic dead-worker detection
- In-memory job queue with FIFO scheduling and idle-worker assignment
- Crash recovery: jobs on dead workers are automatically re-queued

### Layer 2 — Management Plane (Spring Boot & PostgreSQL)

The Manager engine is wrapped in a Spring Boot application, exposing a RESTful API for external interaction and persisting all state to PostgreSQL.

- `POST /api/v1/jobs` — Submit tasks as JSON payloads
- `GET /api/v1/jobs` — Paginated, filterable job listing with live status
- `GET /api/v1/workers` — Real-time view of connected workers
- Full crash recovery on Manager restart via database state reconciliation

### Layer 3 — Observability Dashboard (React + TypeScript)

A modern, real-time SPA that visualizes the entire cluster — worker health, job lifecycle, system metrics.

- Live-polling worker grid with status indicators (IDLE / BUSY / OFFLINE)
- Filterable job list with status badges and duration tracking
- Dashboard overview with summary metrics and activity feed
- Submit jobs directly from the UI

### Layer 4 — DevOps Infrastructure (Docker)

One-command deployment via Docker Compose. Multi-stage Dockerfiles keep images lean. Workers scale horizontally with `--scale worker=N`.

- Containerized Manager, Workers, Dashboard, and PostgreSQL
- Internal Docker networking with service DNS resolution
- Persistent database volumes across restarts

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Core Engine | Java 21, Virtual Threads, Raw TCP Sockets |
| Management API | Spring Boot, Spring Data JPA, Hibernate, Flyway |
| Database | PostgreSQL 15 |
| Dashboard | React 18, TypeScript, Vite, TanStack Query |
| Infrastructure | Docker, Docker Compose, Nginx |
| Observability | SLF4J + Logback (structured JSON), Spring Actuator, Micrometer |