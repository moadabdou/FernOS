# Contributing to Fern-OS

Thank you for your interest in contributing to Fern-OS! This document outlines the coding standards, development workflow, and pull request process.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Development Environment Setup](#development-environment-setup)
- [Branch Naming Conventions](#branch-naming-conventions)
- [Commit Message Format](#commit-message-format)
- [Code Style](#code-style)
- [Java Code Standards](#java-code-standards)
- [TypeScript Code Standards](#typescript-code-standards)
- [Testing Guidelines](#testing-guidelines)
- [Architecture Principles](#architecture-principles)
- [Pull Request Process](#pull-request-process)
- [Adding a New Task Plugin](#adding-a-new-task-plugin)

---

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone <your-fork-url>`
3. Create a feature branch (see [Branch Naming](#branch-naming-conventions))
4. Make your changes
5. Write or update tests
6. Open a Pull Request

---

## Development Environment Setup

### Requirements

| Tool | Version |
|------|---------|
| JDK | 21+ |
| Maven | 3.8+ |
| Node.js | 20+ |
| Docker & Docker Compose | v26+ / v2.24+ |

### First-Time Setup

> [!NOTE]
> For a more comprehensive guide on setting up the cluster and environment variables, see the [**Project Setup Documentation**](docs/setup/index.md).

```bash
# 1. Copy environment config
cp .env.example .env

# 2. Build all Java modules
./mvnw clean install -DskipTests

# 3. Start the database
docker compose up -d postgres

# 4. Run tests to verify setup
./mvnw test
```

### IDE Recommendations

- **IntelliJ IDEA** (Community or Ultimate) — recommended for Java/Spring development
  - Enable Google Java Format plugin
  - Enable Lombok plugin (if applicable)
  - Configure code style: Settings → Editor → Code Style → Java → Google Java Format
- **VS Code** — for dashboard TypeScript development
  - Extensions: ESLint, Prettier, Tailwind CSS IntelliSense

---

## Branch Naming Conventions

Format: `<type>/<short-description>`

| Type | Use Case | Example |
|------|----------|---------|
| `feat` | New feature or capability | `feat/shell-plugin` |
| `fix` | Bug fix | `fix/heartbeat-timeout` |
| `docs` | Documentation changes | `docs/api-reference` |
| `refactor` | Code restructuring (no behavior change) | `refactor/worker-registry-lock` |
| `test` | Adding or fixing tests | `test/job-scheduler-mock` |
| `chore` | Build, CI, dependency changes | `chore/upgrade-testcontainers` |
| `perf` | Performance improvements | `perf/heartbeat-batch-write` |

Rules:
- Use **lowercase kebab-case** for the description
- Keep descriptions **short** (2-4 words)
- Reference issue numbers when applicable: `feat/42-worker-metrics`

---

## Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only |
| `style` | Code style changes (formatting, no logic change) |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `perf` | Performance improvement |
| `test` | Adding or correcting tests |
| `chore` | Build, CI, or tooling changes |

### Examples

```
feat(worker): add ShellScriptPlugin for arbitrary command execution

fix(manager): rollback job state to PENDING when TCP send fails

test(core): add 14 test cases for JobStateMachine transitions

chore(deps): bump testcontainers from 1.19.8 to 1.20.6
```

### Body & Footer

Use the body for context and the footer for breaking changes or issue references:

```
fix(scheduler): prevent duplicate job assignment on reconnect

When a worker reconnects during job execution, the scheduler was
sending a duplicate ASSIGN_JOB. Now it checks JobRegistry for
existing active jobs before dispatching.

Fixes #42
```

---

## Code Style

### General Rules

- **4 spaces** for indentation (no tabs)
- **Maximum line length:** 120 characters
- **No trailing whitespace**
- **UTF-8** encoding for all files
- **Unix line endings** (LF)

---

## Java Code Standards

### Formatting

- Use **Google Java Format** style
- Run `./mvnw checkstyle:check` before submitting a PR
- Organize imports: standard library → third-party → project imports (blank line between groups)

### Naming

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `JobScheduler`, `WorkerClient` |
| Interfaces | PascalCase | `TaskExecutor`, `EngineEventListener` |
| Methods | camelCase | `submitJob()`, `findAvailableWorker()` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase, dot-separated | `com.doe.manager.api.controller` |

### Design Principles

1. **Prefer records** for immutable DTOs and data carriers
2. **Use virtual threads** for I/O-bound operations (`Thread.startVirtualThread()`, `Executors.newVirtualThreadPerTaskExecutor()`)
3. **Thread safety first**: Use `ConcurrentHashMap`, `AtomicInteger`, `LinkedBlockingQueue` — avoid `synchronized` blocks where lock-free alternatives exist
4. **No Spring in `engine-core` or `worker-node`**: These modules must remain framework-free for portability
5. **Dependency injection** via constructor injection (no field injection with `@Autowired`)
6. **Use the event system** (`EngineEventListener`) for cross-cutting concerns — don't couple components directly

### Exception Handling

- Use **specific exception types** — never catch `Exception` or `Throwable` broadly
- Log at the **appropriate level**: `ERROR` for failures, `WARN` for recoverable issues, `DEBUG` for diagnostics
- Include **context in log messages**: `log.error("Failed to send ASSIGN_JOB to worker {}", workerId, e)`

### Testing

- Name test methods descriptively: `shouldTransitionToCompletedWhenJobSucceeds()`
- Use **JUnit 5** lifecycle (`@BeforeEach`, `@Nested`, `@ParameterizedTest`)
- Use **Testcontainers** for integration tests requiring PostgreSQL
- Mock external dependencies; don't rely on real infrastructure for unit tests
- Test **edge cases and failure modes**, not just the happy path

---

## TypeScript Code Standards

### Formatting

- Use **Prettier** with project defaults
- Run `npm run lint` before submitting dashboard PRs

### Naming

| Element | Convention | Example |
|---------|------------|---------|
| Components | PascalCase | `JobQueuePanel`, `SubmitJobModal` |
| Hooks | camelCase, `use` prefix | `useSystemStats`, `useJobPolling` |
| Types/Interfaces | PascalCase | `JobResponse`, `WorkerAPI` |
| Constants | UPPER_SNAKE_CASE | `API_BASE_URL` |

### Patterns

- Use **functional components** with hooks (no class components)
- Prefer **TypeScript interfaces** for API response shapes
- Use **TanStack React Query** for server state — avoid raw `useEffect` for data fetching
- Keep components **small and focused** — extract sub-components when a file exceeds 200 lines

---

## Testing Guidelines

### Test Pyramid

```
        /\
       /  \  E2E (automated_tests/)
      /────\
     /      \  Integration (Testcontainers, TestManagerServerBuilder)
    /────────\
   /          \  Unit (./mvnw test)
  /────────────\
```

### Running Tests

```bash
# All Java tests
./mvnw test

# Specific module
./mvnw test -pl engine-core

# Integration tests only
./mvnw verify -pl manager-node -Dtest="*IntegrationTest"

# Dashboard tests
cd dashboard && npm test

# E2E tests (requires running cluster)
python automated_tests/test_sleep_jobs.py
```

### Coverage Expectations

- **New features** must include unit tests
- **Bug fixes** must include regression tests
- **Aim for >70% line coverage** on new code
- **Critical paths** (job scheduling, crash recovery, protocol codec) must have >90% coverage

---

## Architecture Principles

### Module Boundaries

| Rule | Rationale |
|------|-----------|
| `engine-core` has zero Spring dependencies | Must be usable by any Java application |
| `worker-node` has zero Spring dependencies | Must stay lightweight and fast |
| `manager-node` depends on `engine-core` | Shares domain models and protocol |
| `dashboard` is independent of Java modules | Communicates only via HTTP REST (see [Dashboard Docs](docs/ui/index.md)) |

### Adding Dependencies

- **engine-core**: Avoid new dependencies. Only `gson`, `slf4j`, and `junit` are acceptable.
- **manager-node**: Spring Boot ecosystem only, unless approved in a PR discussion.
- **worker-node**: Minimal. Only `gson`, `slf4j`, and `junit`.
- **dashboard**: Discuss in PR. Prefer established, well-maintained libraries. See [UI Architecture](docs/ui/index.md) for more info.

### Protocol Changes

The TCP protocol between manager and worker is defined in `engine-core/protocol/`. Changes to `MessageType` or wire format require:

1. Backward compatibility consideration (version negotiation)
2. Tests for both encoder and decoder
3. Documentation update in README

---

## Pull Request Process

1. **Create a branch** from `main` using the naming convention above
2. **Make your changes** following the code standards
3. **Write or update tests** — all tests must pass
4. **Self-review** your diff before opening the PR
5. **Open the PR** with:
   - A clear title following the commit message format
   - A description explaining **what** changed and **why**
   - Screenshots/GIFs for UI changes
   - Reference related issues: `Fixes #42`
6. **At least 1 approval** is required before merging
7. **Address review feedback** promptly
8. **Merge to `main`** (squash merge preferred)

### PR Checklist

- [ ] Tests pass (`./mvnw test` and/or `cd dashboard && npm test`)
- [ ] Code style checks pass (`./mvnw checkstyle:check`, `npm run lint`)
- [ ] Commit messages follow Conventional Commits
- [ ] No debug logging or commented-out code
- [ ] README updated if API/configuration changed
- [ ] `.env.example` updated if new environment variables added

---

## Adding a New Task Plugin

Task plugins allow workers to execute different types of work. Here's how to add one:

### 1. Create the Plugin Class

In `worker-node/src/main/java/com/doe/worker/executor/`:

```java
public class MyPlugin implements TaskExecutor {
    @Override
    public String getType() {
        return "my-task";  // unique type identifier
    }

    @Override
    public String execute(String payloadData) {
        // Parse payloadData (JSON), do work, return result string
    }
}
```

### 2. Register the Plugin

Add the plugin to the `TaskPluginRegistry` in the worker's `Main.java`:

```java
registry.register(new MyPlugin());
```

### 3. Write Tests

Create a unit test for your plugin covering:
- Successful execution
- Error handling
- Edge cases (empty input, large input, etc.)

### 4. Document

Add your plugin to the README API Reference section with a payload example.

---

## Questions?

Open a GitHub Discussion or tag the maintainers in a PR. We're happy to help!
