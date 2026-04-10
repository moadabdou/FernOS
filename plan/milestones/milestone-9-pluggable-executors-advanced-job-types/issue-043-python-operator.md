# Issue 043: Python Operator

## Description
Implement a Python operator that executes Python scripts, functions, or notebooks with environment isolation (virtualenv/conda).

## Requirements
- Support executing Python scripts with configurable interpreter path
- Support virtualenv and conda environment isolation
- Capture stdout/stderr and return as job result
- Support passing arguments and environment variables to Python scripts
- Validate Python environment and dependencies before execution
- Support notebook (.ipynb) execution via papermill or similar

## Acceptance Criteria
- [ ] Python scripts execute successfully with isolated environment
- [ ] virtualenv and conda environments are supported
- [ ] stdout/stderr are captured and stored in job result
- [ ] Arguments and environment variables are passed correctly
- [ ] Missing dependencies are detected before execution
- [ ] Jupyter notebooks execute via papermill with output capture

## Dependencies
- Issue 042 Pluggable Executor SPI
