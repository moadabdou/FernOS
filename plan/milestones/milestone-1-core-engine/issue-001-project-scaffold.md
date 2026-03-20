# Issue #001 — Initialize Multi-Module Project Scaffold

**Milestone:** 1 — Core Engine  
**Labels:** `setup`, `build-system`, `priority:high`  
**Assignee:** —  
**Estimate:** 0.5 day  

## Description

Create the root project and three sub-modules using Gradle (Kotlin DSL) or Maven:

| Module | Purpose |
|---|---|
| `engine-core` | Shared protocol classes, message types, codec utilities |
| `manager-node` | Manager server application |
| `worker-node` | Worker client application |

## Acceptance Criteria

- [ ] Root `build.gradle.kts` (or `pom.xml`) compiles all three modules
- [ ] Java 21 source/target configured globally
- [ ] Each module has a placeholder `Main` class that prints its module name
- [ ] `./gradlew build` (or `mvn package`) succeeds with zero warnings
- [ ] `.gitignore` includes IDE files, build output, and OS artifacts

## Technical Notes

- Use `java-library` plugin for `engine-core` and `application` plugin for `manager-node` / `worker-node`
- Pin dependency versions in a central `libs.versions.toml` or BOM
