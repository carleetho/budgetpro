import os
import subprocess
import tempfile
import shutil
import pytest
from pathlib import Path

# --- Fixtures ---

@pytest.fixture
def temp_git_repo():
    """
    Fixture that creates a temporary directory, initializes a git repository,
    configures a test user, and yields the repository path.
    Cleans up after the test.
    """
    temp_dir = tempfile.mkdtemp()
    repo_path = os.path.join(temp_dir, "test_repo")
    os.makedirs(repo_path)
    
    # Initialize Git
    subprocess.run(["git", "init"], cwd=repo_path, check=True, capture_output=True)
    subprocess.run(["git", "config", "user.email", "test@axiom.com"], cwd=repo_path, check=True)
    subprocess.run(["git", "config", "user.name", "Axiom Test"], cwd=repo_path, check=True)
    
    # Copy axiom.config.yaml to the repo root
    workspace_root = os.getcwd()
    shutil.copy(
        os.path.join(workspace_root, "axiom.config.yaml"),
        os.path.join(repo_path, "axiom.config.yaml")
    )
    
    # Add a mandatory .gitignore to satisfy SecurityValidator (avoid blocking on missing file)
    with open(os.path.join(repo_path, ".gitignore"), "w") as f:
        f.write(".env\n*.log\n.gemini\nnode_modules\ntarget\n")
    subprocess.run(["git", "add", ".gitignore"], cwd=repo_path, check=True)
    subprocess.run(["git", "commit", "-m", "initial commit"], cwd=repo_path, check=True)

    yield repo_path
    
    shutil.rmtree(temp_dir)

# --- Helper Methods ---

def create_and_stage_file(repo_path: str, file_path: str, content: str):
    """Creates a file at the given relative path and stages it in the git index."""
    full_path = os.path.join(repo_path, file_path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w") as f:
        f.write(content)
    subprocess.run(["git", "add", file_path], cwd=repo_path, check=True)

def attempt_commit(repo_path: str, message: str, no_verify: bool = False) -> subprocess.CompletedProcess:
    """Runs git commit and returns the result."""
    cmd = ["git", "commit", "-m", message]
    if no_verify:
        cmd.append("--no-verify")
    
    # Ensure PYTHONPATH is set so that the pre_commit_hook.py can import the tools package
    env = os.environ.copy()
    env["PYTHONPATH"] = os.getcwd()
    
    return subprocess.run(
        cmd,
        cwd=repo_path,
        capture_output=True,
        text=True,
        env=env
    )

def install_axiom_hook(repo_path: str):
    """
    Copies/installs the AXIOM pre-commit hook into the repository.
    Replicates the logic of install_hook.sh but adapted for test environment.
    """
    workspace_root = os.getcwd()
    hook_dest = os.path.join(repo_path, ".git", "hooks", "pre-commit")
    hook_source_path = os.path.join(workspace_root, "tools/axiom/pre_commit_hook.py")
    
    # Replicate the logic from install_hook.sh for the hook content
    # Note: We use the absolute path to the workspace tools for the test, 
    # but the logic follows the requirement to "Copy AXIOM hook files to .git/hooks/"
    # Actually, the requirement says "Copy AXIOM hook files to .git/hooks/" - usually referring to the hook script.
    
    hook_content = f"""#!/bin/bash
# Trigger AXIOM validation pipeline
python3 {hook_source_path} "$@"
"""
    with open(hook_dest, "w") as f:
        f.write(hook_content)
    os.chmod(hook_dest, 0o755)

# --- Test Methods ---

def test_commit_blocked_empty_method(temp_git_repo):
    """Test case: commit blocked when empty method detected."""
    install_axiom_hook(temp_git_repo)
    create_and_stage_file(temp_git_repo, "LazyClass.java", "public class Lazy { public void empty() {} }")
    
    result = attempt_commit(temp_git_repo, "test empty method")
    
    assert result.returncode == 1
    assert "CÓDIGO PEREZOSO: Método vacío detectado" in (result.stdout + result.stderr)

def test_commit_blocked_null_return_persistence(temp_git_repo):
    """Test case: commit blocked when null return in persistence detected."""
    install_axiom_hook(temp_git_repo)
    content = "public class Repo { public Object find() { return null; } }"
    create_and_stage_file(temp_git_repo, "infrastructure/persistence/TestAdapter.java", content)
    
    result = attempt_commit(temp_git_repo, "test null return")
    
    assert result.returncode == 1
    assert "CÓDIGO PEREZOSO: Retorno null o empty detectado" in (result.stdout + result.stderr)

def test_commit_blocked_todo_critical_module(temp_git_repo):
    """Test case: commit blocked when TODO in critical module detected."""
    install_axiom_hook(temp_git_repo)
    content = "// TODO: implement this logic"
    create_and_stage_file(temp_git_repo, "domain/presupuesto/TestService.java", content)
    
    result = attempt_commit(temp_git_repo, "test todo")
    
    assert result.returncode == 1
    assert "CÓDIGO PEREZOSO: TODO o FIXME detectado" in (result.stdout + result.stderr)

def test_commit_succeeds_clean_code(temp_git_repo):
    """Test case: commit succeeds with clean code."""
    install_axiom_hook(temp_git_repo)
    content = "public class Clean { public void work() { System.out.println(\"working\"); } }"
    create_and_stage_file(temp_git_repo, "Clean.java", content)
    
    result = attempt_commit(temp_git_repo, "clean commit")
    
    assert result.returncode == 0
    assert "No blocking violations found. Commit allowed." in (result.stdout + result.stderr)

def test_multiple_violations_reported(temp_git_repo):
    """Test case: multiple violations reported together."""
    install_axiom_hook(temp_git_repo)
    create_and_stage_file(temp_git_repo, "Lazy1.java", "public void a() {}")
    create_and_stage_file(temp_git_repo, "domain/estimacion/Logic.java", "// FIXME later")
    
    result = attempt_commit(temp_git_repo, "multiple violations")
    
    assert result.returncode == 1
    output = result.stdout + result.stderr
    assert "CÓDIGO PEREZOSO: Método vacío detectado" in output
    assert "CÓDIGO PEREZOSO: TODO o FIXME detectado" in output

def test_bypass_with_no_verify(temp_git_repo):
    """Test case: --no-verify bypass allows commit."""
    install_axiom_hook(temp_git_repo)
    create_and_stage_file(temp_git_repo, "LazyBypass.java", "public void lazy() {}")
    
    result = attempt_commit(temp_git_repo, "bypass", no_verify=True)
    
    assert result.returncode == 0
    # Capture git log to verify commit was created
    log_check = subprocess.run(["git", "log", "-1", "--pretty=%B"], cwd=temp_git_repo, capture_output=True, text=True)
    assert "bypass" in log_check.stdout
