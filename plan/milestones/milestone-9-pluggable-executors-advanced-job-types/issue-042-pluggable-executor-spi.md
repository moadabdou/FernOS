# Issue 042: Pluggable Executor SPI

## Description
Design and implement a standardized Service Provider Interface (SPI) for task executors, allowing new executor types to be added without modifying core engine code.

## Requirements
- Define a `TaskExecutor` interface with methods: `execute(JobDefinition, ExecutionContext)`, `cancel(JobId)`, `validate(JobDefinition)`
- Implement Java SPI (ServiceLoader) for automatic executor discovery and registration
- Create an `ExecutionContext` containing environment variables, secrets, XCom storage client, and logging utilities
- Add executor registry with health checks and capability reporting
- Provide documentation for developing custom executors

## Acceptance Criteria
- [ ] `TaskExecutor` interface supports synchronous and asynchronous execution
- [ ] New executors are auto-discovered at startup via ServiceLoader
- [ ] Executor validation prevents invalid job submissions
- [ ] Executor registry reports available executor types and their capabilities
- [ ] Custom executor can be developed in a separate JAR and loaded at runtime

## Dependencies
- Milestone 3 pluggable task executor foundation (issue-019)
