# Issue 044: HTTP/API Operator

## Description
Implement an HTTP operator that makes HTTP requests, handles retries, validates responses, and supports chaining API calls.

## Requirements
- Support GET, POST, PUT, DELETE, PATCH methods
- Configurable request headers, query parameters, and request body (JSON, form-data, raw)
- Response validation via status codes, JSON schema, or custom predicates
- Retry policies with exponential backoff for transient failures
- Timeout configuration per request
- Response body capture and storage as job result (with size limits)
- Support for authentication (Bearer tokens, Basic Auth, API keys)

## Acceptance Criteria
- [ ] HTTP requests execute with all supported methods
- [ ] Headers, query parameters, and request bodies are configurable
- [ ] Response validation rejects invalid responses and triggers retries
- [ ] Exponential backoff retry policy works correctly
- [ ] Response body is captured and stored (up to configurable size limit)
- [ ] Authentication methods work correctly

## Dependencies
- Issue 042 Pluggable Executor SPI
