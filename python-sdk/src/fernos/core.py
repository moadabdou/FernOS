from typing import List, Optional, Union, Dict, Any
import threading
import json

class DAGContext(threading.local):
    """Thread-local storage for the active DAG context."""
    active_dag: Optional['DAG'] = None

_context = DAGContext()

def _get_current_dag() -> Optional['DAG']:
    """Retrieves the current active DAG from the thread-local context."""
    return _context.active_dag

def _set_current_dag(dag: Optional['DAG']):
    """Sets the active DAG in the thread-local context."""
    _context.active_dag = dag

class Job:
    """
    Represents a single job within a Fern-OS workflow.
    
    Attributes:
        label (str): A unique identifier for the job within the DAG.
        path (str): The path to the Python script to execute.
        timeout_ms (int): Maximum execution time in milliseconds.
    """
    def __init__(self, label: str, path: str, timeout_ms: int = 300000):
        self.label = label
        self.path = path
        self.timeout_ms = timeout_ms
        self.upstream: set[str] = set()
        
        # Register with the current DAG context if available
        current_dag = _get_current_dag()
        if current_dag:
            current_dag.add_job(self)

    def __eq__(self, other: object) -> bool:
        """Jobs are considered equal if they have the same label."""
        if not isinstance(other, Job):
            return NotImplemented
        return self.label == other.label

    def __hash__(self) -> int:
        """Hash based on job label to support set operations."""
        return hash(self.label)

    def __rshift__(self, other: Union['Job', List['Job']]) -> Union['Job', List['Job']]:
        """
        Defines a downstream dependency using the bitshift operator (>>).
        Usage: task1 >> task2 or task1 >> [task2, task3]
        """
        if isinstance(other, list):
            for job in other:
                job.upstream.add(self.label)
        else:
            other.upstream.add(self.label)
        return other

    def __lshift__(self, other: Union['Job', List['Job']]) -> Union['Job', List['Job']]:
        """
        Defines an upstream dependency using the bitshift operator (<<).
        Usage: task2 << task1 or task2 << [task1, task0]
        """
        if isinstance(other, list):
            for job in other:
                self.upstream.add(job.label)
        else:
            self.upstream.add(other.label)
        return other

    def to_dict(self) -> Dict[str, Any]:
        """
        Converts the Job to a dictionary format compatible with backend JobDefinition.
        """
        return {
            "label": self.label,
            "type": "PYTHON",
            "payload": json.dumps({"scriptPath": self.path}),
            "timeoutMs": self.timeout_ms,
            "retryCount": 0
        }

class DAG:
    """
    Context manager for defining Fern-OS workflows.
    
    Attributes:
        name (str): The human-readable name of the workflow.
        description (str): A brief description of the workflow.
    """
    def __init__(self, name: str, description: str = ""):
        self.name = name
        self.description = description
        self.jobs: set[Job] = set()
        self._prev_dag: Optional[DAG] = None

    def __enter__(self):
        """Activates the DAG context."""
        self._prev_dag = _get_current_dag()
        _set_current_dag(self)
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Deactivates the DAG context."""
        _set_current_dag(self._prev_dag)

    def add_job(self, job: Job):
        """Adds a job to the DAG if it doesn't already exist."""
        self.jobs.add(job)

    def to_dict(self) -> Dict[str, Any]:
        """
        Serializes the entire DAG into the format expected by CreateWorkflowRequest.
        """
        jobs_data = [job.to_dict() for job in self.jobs]
        
        dependencies = []
        for job in self.jobs:
            for up in job.upstream:
                dependencies.append({
                    "fromJobLabel": up,
                    "toJobLabel": job.label
                })
                
        return {
            "name": self.name,
            "jobs": jobs_data,
            "dependencies": dependencies
        }
