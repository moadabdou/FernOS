import pytest
import json
from fernos import DAG, Job

def test_dag_context_manager():
    with DAG(name="test_dag") as dag:
        Job(label="job1", path="script1.py")
        Job(label="job2", path="script2.py")
        
    assert len(dag.jobs) == 2
    labels = {j.label for j in dag.jobs}
    assert labels == {"job1", "job2"}
    assert dag.name == "test_dag"

def test_job_equality():
    j1 = Job(label="job1", path="p1.py")
    j1_dup = Job(label="job1", path="p1_different.py")
    j2 = Job(label="job2", path="p2.py")
    
    assert j1 == j1_dup
    assert j1 != j2
    assert hash(j1) == hash(j1_dup)

def test_dag_set_behavior():
    with DAG(name="test_set") as dag:
        j1 = Job(label="job1", path="p1.py")
        Job(label="job1", path="p1_dup.py") # Duplicate label
        
    assert len(dag.jobs) == 1
    assert list(dag.jobs)[0].label == "job1"

def test_job_dependencies():
    with DAG(name="test_deps") as dag:
        job1 = Job(label="job1", path="script1.py")
        job2 = Job(label="job2", path="script2.py")
        job3 = Job(label="job3", path="script3.py")
        
        job1 >> job2
        job2 >> job3
        
    assert "job1" in job2.upstream
    assert "job2" in job3.upstream
    assert len(job1.upstream) == 0

def test_duplicate_dependencies():
    with DAG(name="test_dups") as dag:
        j1 = Job(label="j1", path="p1.py")
        j2 = Job(label="j2", path="p2.py")
        
        # Multiple links should be idempotent
        j1 >> j2
        j1 >> j2
        j2 << j1
        
    assert len(j2.upstream) == 1
    assert "j1" in j2.upstream

def test_multi_dependency_chain():
    with DAG(name="test_chain") as dag:
        j1 = Job(label="j1", path="p1.py")
        j2 = Job(label="j2", path="p2.py")
        j3 = Job(label="j3", path="p3.py")
        
        j1 >> j2 >> j3
        
    assert "j1" in j2.upstream
    assert "j2" in j3.upstream

def test_list_dependencies():
    with DAG(name="test_list") as dag:
        j1 = Job(label="j1", path="p1.py")
        j2 = Job(label="j2", path="p2.py")
        j3 = Job(label="j3", path="p3.py")
        
        j1 >> [j2, j3]
        
    assert "j1" in j2.upstream
    assert "j1" in j3.upstream

def test_serialization():
    with DAG(name="test_data", description="My test DAG") as dag:
        job1 = Job(label="downloader", path="down.py")
        job2 = Job(label="processor", path="proc.py", timeout_ms=60000)
        job1 >> job2
        
    data = dag.to_dict()
    assert data["name"] == "test_data"
    assert len(data["jobs"]) == 2
    assert len(data["dependencies"]) == 1
    
    # Check job definition
    proc_job = next(j for j in data["jobs"] if j["label"] == "processor")
    assert proc_job["type"] == "PYTHON"
    payload = json.loads(proc_job["payload"])
    assert payload["scriptPath"] == "proc.py"
    assert proc_job["timeoutMs"] == 60000
    
    # Check dependency edge
    edge = data["dependencies"][0]
    assert edge["fromJobLabel"] == "downloader"
    assert edge["toJobLabel"] == "processor"
