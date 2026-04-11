# Issue 048: Result Passing & XComs

## Description
Implement a cross-communication mechanism (XComs) allowing jobs to exchange data between DAG nodes, enabling dynamic workflows.

## Requirements
- Database-backed XCom storage in PostgreSQL (survives restarts, queryable)
- API for jobs to push/pull XCom values by key
- Support for primitive types (strings, numbers, booleans) and JSON-serializable objects
- Configurable size limits per XCom value (with warnings for large payloads)
- XCom values scoped to DAG run, accessible by downstream jobs
- Integration with all operator types (Python, HTTP, SQL, Docker, Sensors)
- Cleanup policy for old XCom values (TTL-based or run-count-based)

## Acceptance Criteria
- [ ] Jobs can push and pull XCom values by key
- [ ] XCom values are accessible by downstream jobs in the same DAG run
- [ ] Large payloads are handled with configurable size limits
- [ ] All operator types can read/write XCom values
- [ ] Old XCom values are cleaned up based on configurable policy
- [ ] XCom values are visible in the dashboard (Milestone 8)

## Dependencies
- Issue 042 Pluggable Executor SPI
- Milestone 8 control plane
