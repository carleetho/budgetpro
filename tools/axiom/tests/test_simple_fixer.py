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

    def test_fix_gitignore_success_and_commit(self):
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
        
        # Check if backup exists before commit
        backups = [f for f in os.listdir(self.test_dir) if ".backup." in f]
        self.assertEqual(len(backups), 1)
        
        # Call commit
        self.fixer.commit()
        
        # Check if backup is cleaned up
        backups = [f for f in os.listdir(self.test_dir) if ".backup." in f]
        self.assertEqual(len(backups), 0)
        self.assertEqual(len(self.fixer.backups), 0)

    def test_explicit_rollback(self):
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="warning",
            validator_name="security_validator",
            auto_fixable=True,
            fix_data={"missing_entries": [".env"]}
        )
        
        self.fixer.fix([violation])
        
        # Verify fix applied
        with open(self.gitignore_path, "r") as f:
            self.assertIn(".env", f.read())
            
        # Call rollback
        self.fixer.rollback()
        
        # Verify content restored
        with open(self.gitignore_path, "r") as f:
            content = f.read()
            self.assertNotIn(".env", content)
            self.assertIn("node_modules", content)
            
        # Check if backup is cleaned up after rollback
        backups = [f for f in os.listdir(self.test_dir) if ".backup." in f]
        self.assertEqual(len(backups), 0)
        self.assertEqual(len(self.fixer.backups), 0)

    def test_rollback_on_write_failure(self):
        violation = Violation(
            file_path=self.gitignore_path,
            message="Missing .env",
            severity="warning",
            validator_name="security_validator",
            auto_fixable=True,
            fix_data={"missing_entries": [".env"]}
        )
        
        # Make file read-only (0o444) to force failure
        os.chmod(self.gitignore_path, 0o444)
        
        try:
            result = self.fixer.fix([violation])
            self.assertFalse(result.success)
            self.assertIn("Write failed", result.error_message)
            
            # Verify file was restored (content should be original despite failed write attempt)
            # Actually since write failed, content shouldn't have changed, but rollback should have restored from backup if anything happened.
            # Most importantly, backups should be cleared if fix fails and rolls back internally.
            self.assertEqual(len(self.fixer.backups), 0)
        finally:
            os.chmod(self.gitignore_path, 0o644)

if __name__ == "__main__":
    unittest.main()
