# Milestone 1: Core Engine — TCP Protocol & Worker/Manager Communication

## Overview

Build the foundational distributed compute layer using raw Java 21. This milestone establishes the custom binary TCP protocol, implements the Manager server and Worker clients, and proves that multiple workers can concurrently connect, register, and exchange heartbeats with a central manager node.

**This is the most critical milestone** — every subsequent layer (Spring Boot, Dashboard, Docker) depends on a rock-solid, non-blocking core engine.

## Goals

1. **Multi-module project scaffold** — Establish a clean Maven/Gradle project with `engine-core`, `manager-node`, and `worker-node` modules.
2. **Custom binary protocol** — Implement a compact wire protocol (`[1B Type][4B Length][NB Payload]`) with encoder/decoder utilities shared across modules.
3. **Manager server** — Accept concurrent TCP connections using `ServerSocket` + Java 21 Virtual Threads (`Thread.ofVirtual()`). Maintain a thread-safe registry of connected workers.
4. **Worker client** — Connect to the manager, send `REGISTER_WORKER`, and run a periodic heartbeat loop.
5. **Heartbeat mechanism** — Workers emit `HEARTBEAT` every 5 seconds; the Manager tracks `lastSeenAt` per worker and logs disconnection after 3 missed beats (15 s).

## Architecture Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Concurrency model | Virtual Threads (Project Loom) | Scales to thousands of workers without thread-pool tuning |
| Protocol format | Custom binary header + JSON payload | Low overhead, easy debugging with JSON body |
| Build tool | Gradle (Kotlin DSL) or Maven | Multi-module support, widely understood |
| Worker registry | `ConcurrentHashMap<UUID, WorkerConnection>` | Lock-free reads for high-throughput heartbeat tracking |

## Success Criteria

- [ ] Run 1 Manager + 3 Workers from separate terminal sessions on localhost
- [ ] Manager logs each worker registration with UUID and IP
- [ ] Manager detects a killed worker within 15 seconds and logs `Worker <UUID> disconnected`
- [ ] Zero jobs are assigned in this milestone — communication-only proof of concept

## Dependencies

- Java 21 SDK
- Gradle 8.x or Maven 3.9+

## Estimated Effort

**5–7 working days** for a solo developer.
