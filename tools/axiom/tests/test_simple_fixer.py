import unittest
import os
import shutil
import tempfile
from tools.axiom.fixers.simple_fixer import SimpleFixer
from tools.axiom.validators.base_validator import Violation

class TestSimpleFixer(unittest.TestCase):
    def setUp(self):
        self.test_dir = tempfile.mkdtemp()
        self.gitignore_path = os.path.join(self.test_dir, ".gitignore")
        with open(self.gitignore_path, "w") as f:
            f.write("node_modules\ntarget\n")
        
        self.config = {"enabled": True}
        self.fixer = SimpleFixer(self.config)

    def tearDown(self):
        shutil.rmtree(self.test_dir)

    def test_fix_gitignore_success(self):
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="warning",
            validator_name="security_validator",
            auto_fixable=True,
            fix_data={"missing_entries": [".env", ".gemini"]}
        )
        
        result = self.fixer.fix([violation])
        
        self.assertTrue(result.success)
        self.assertIn(self.gitignore_path, result.fixed_files)
        
        with open(self.gitignore_path, "r") as f:
            content = f.read()
            self.assertIn(".env", content)
            self.assertIn(".gemini", content)
            self.assertIn("# Auto-added by AXIOM Sentinel", content)
        
        # Check if backup exists
        backups = [f for f in os.listdir(self.test_dir) if ".backup." in f]
        self.assertEqual(len(backups), 1)

    def test_fix_disabled(self):
        self.fixer.config["enabled"] = False
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="warning",
            validator_name="security_validator",
            auto_fixable=True,
            fix_data={"missing_entries": [".env"]}
        )
        
        result = self.fixer.fix([violation])
        
        self.assertTrue(result.success)
        self.assertEqual(len(result.fixed_files), 0)
        
        with open(self.gitignore_path, "r") as f:
            content = f.read()
            self.assertNotIn(".env", content)

    def test_fix_skips_blocking(self):
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="blocking",
            validator_name="security_validator",
            auto_fixable=True,
            fix_data={"missing_entries": [".env"]}
        )
        
        result = self.fixer.fix([violation])
        
        self.assertTrue(result.success)
        self.assertEqual(len(result.fixed_files), 0)

    def test_rollback_on_write_failure(self):
        # Create a violation for a file that we will make unreadable/unwritable
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="warning",
            validator_name="security_validator",
            auto_fixable=True,
            fix_data={"missing_entries": [".env"]}
        )
        
        # Mocking a write failure is tricky without mock, but we can try making it read-only
        os.chmod(self.gitignore_path, 0o444)
        
        try:
            result = self.fixer.fix([violation])
            self.assertFalse(result.success)
            self.assertIn("Write failed", result.error_message)
        finally:
            os.chmod(self.gitignore_path, 0o644)

    def test_fix_no_fixable_violations(self):
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="warning",
            validator_name="security_validator",
            auto_fixable=False,
            fix_data={"missing_entries": [".env"]}
        )
        
        result = self.fixer.fix([violation])
        self.assertTrue(result.success)
        self.assertEqual(len(result.fixed_files), 0)

if __name__ == "__main__":
    unittest.main()
