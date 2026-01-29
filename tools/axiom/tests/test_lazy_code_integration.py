import unittest
import os
import subprocess
import tempfile
import shutil
from pathlib import Path

class TestLazyCodeIntegration(unittest.TestCase):
    """
    End-to-end integration tests for AXIOM pre-commit hook blocking 
    lazy code patterns (REQ-18).
    """

    def setUp(self):
        # 1. Create temporary repository
        self.workspace_root = os.getcwd()
        self.test_dir = tempfile.mkdtemp()
        self.repo_path = os.path.join(self.test_dir, "test_repo")
        os.makedirs(self.repo_path)
        
        # 2. Init git
        self._run_git(["init"], self.repo_path)
        self._run_git(["config", "user.email", "test@axiom.com"], self.repo_path)
        self._run_git(["config", "user.name", "Axiom Test"], self.repo_path)
        
        # 3. Copy axiom.config.yaml
        shutil.copy(
            os.path.join(self.workspace_root, "axiom.config.yaml"),
            os.path.join(self.repo_path, "axiom.config.yaml")
        )
        
        # 4. Add required .gitignore for SecurityValidator
        self._create_and_stage(".gitignore", ".env\n*.log\n.gemini\nnode_modules\ntarget\n")
        
        # 5. Install pre-commit hook wrapper
        # We point to the actual code in the workspace root to avoid copying everything
        hook_path = os.path.join(self.repo_path, ".git", "hooks", "pre-commit")
        with open(hook_path, "w") as f:
            f.write(f"#!/bin/bash\n")
            f.write(f"export PYTHONPATH={self.workspace_root}\n")
            f.write(f"python3 {os.path.join(self.workspace_root, 'tools/axiom/pre_commit_hook.py')} \"$@\"\n")
        os.chmod(hook_path, 0o755)

    def tearDown(self):
        shutil.rmtree(self.test_dir)

    def _run_git(self, args, cwd, input=None):
        return subprocess.run(
            ["git"] + args,
            cwd=cwd,
            input=input,
            capture_output=True,
            text=True
        )

    def _create_and_stage(self, relative_path, content):
        full_path = os.path.join(self.repo_path, relative_path)
        os.makedirs(os.path.dirname(full_path), exist_ok=True)
        with open(full_path, "w") as f:
            f.write(content)
        self._run_git(["add", relative_path], self.repo_path)

    # --- Test Cases ---

    def test_commit_blocked_empty_method(self):
        """Verifies that an empty method in a Java file blocks the commit."""
        content = "public class Test { public void lazy() {} }"
        self._create_and_stage("Test.java", content)
        
        result = self._run_git(["commit", "-m", "add lazy code"], self.repo_path)
        
        self.assertEqual(result.returncode, 1)
        self.assertIn("CÓDIGO PEREZOSO: Método vacío detectado", result.stdout + result.stderr)

    def test_commit_blocked_null_return_persistence(self):
        """Verifies that a null return in persistence layer blocks the commit."""
        content = "public class Repo { public Object find() { return null; } }"
        self._create_and_stage("infrastructure/persistence/TestRepo.java", content)
        
        result = self._run_git(["commit", "-m", "add null return"], self.repo_path)
        
        self.assertEqual(result.returncode, 1)
        self.assertIn("CÓDIGO PEREZOSO: Retorno null o empty detectado", result.stdout + result.stderr)

    def test_commit_blocked_todo_critical_module(self):
        """Verifies that a TODO in a critical domain module blocks the commit."""
        content = "// TODO: implement this logic"
        self._create_and_stage("domain/presupuesto/Service.java", content)
        
        result = self._run_git(["commit", "-m", "add todo"], self.repo_path)
        
        self.assertEqual(result.returncode, 1)
        self.assertIn("CÓDIGO PEREZOSO: TODO o FIXME detectado", result.stdout + result.stderr)

    def test_commit_succeeds_clean_code(self):
        """Verifies that clean code allows the commit to proceed."""
        content = "public class Success { public void work() { System.out.println(\"done\"); } }"
        self._create_and_stage("Success.java", content)
        
        result = self._run_git(["commit", "-m", "add clean code"], self.repo_path)
        
        self.assertEqual(result.returncode, 0)
        self.assertIn("No blocking violations found. Commit allowed.", result.stdout + result.stderr)

    def test_bypass_with_no_verify(self):
        """Verifies that --no-verify allows committing even with lazy code."""
        content = "public class Lazy { public void empty() {} }"
        self._create_and_stage("Lazy.java", content)
        
        # This should fail WITHOUT --no-verify, so let's check it can pass WITH it
        result = self._run_git(["commit", "-m", "bypass", "--no-verify"], self.repo_path)
        
        self.assertEqual(result.returncode, 0)
        # Git log check
        log = self._run_git(["log", "-1", "--pretty=%B"], self.repo_path)
        self.assertIn("bypass", log.stdout)

    def test_multiple_violations_reported(self):
        """Checks if multiple violations across files are all reported."""
        self._create_and_stage("Lazy1.java", "public void a() {}")
        self._create_and_stage("domain/estimacion/Logic.java", "// FIXME later")
        
        result = self._run_git(["commit", "-m", "multiple issues"], self.repo_path)
        
        self.assertEqual(result.returncode, 1)
        output = result.stdout + result.stderr
        self.assertIn("Método vacío detectado", output)
        self.assertIn("TODO o FIXME detectado", output)
        self.assertIn("Commit blocked", output)

if __name__ == "__main__":
    unittest.main()
