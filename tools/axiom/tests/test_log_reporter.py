import unittest
import os
import shutil
import json
from unittest.mock import patch, mock_open
from tools.axiom.reporters.log_reporter import LogReporter
from tools.axiom.validators.base_validator import Violation

class TestLogReporter(unittest.TestCase):

    def setUp(self):
        self.test_log_dir = ".test_budgetpro"
        self.test_log_path = os.path.join(self.test_log_dir, "validation.log")
        self.config = {"path": self.test_log_path}
        self.reporter = LogReporter(self.config)
        
        # Clean up any existing test log
        if os.path.exists(self.test_log_dir):
            shutil.rmtree(self.test_log_dir)

    def tearDown(self):
        # Clean up test log
        if os.path.exists(self.test_log_dir):
            shutil.rmtree(self.test_log_dir)

    def test_report_creates_file_and_directory(self):
        v = Violation("f.py", "msg", "blocking", "val")
        result = self.reporter.report([v], 10.5)
        
        self.assertTrue(result.success)
        self.assertTrue(os.path.exists(self.test_log_path))
        
        with open(self.test_log_path, "r") as f:
            content = f.read()
            self.assertIn("=== Validation Run:", content)
            self.assertIn("[BLOCKING] f.py - msg", content)
            self.assertIn("[JSON_ENTRY]", content)

    def test_report_appends_to_file(self):
        v1 = Violation("f1.py", "m1", "blocking", "val")
        self.reporter.report([v1], 5.0)
        
        v2 = Violation("f2.py", "m2", "warning", "val")
        self.reporter.report([v2], 5.0)
        
        with open(self.test_log_path, "r") as f:
            lines = f.readlines()
            # Count headers
            headers = [l for l in lines if "=== Validation Run:" in l]
            self.assertEqual(len(headers), 2)

    def test_json_block_integrity(self):
        v = Violation(
            "test.py", "message", "info", "validator",
            line_number=42, detail="detail text", suggestion="suggest text"
        )
        self.reporter.report([v], 1.0)
        
        with open(self.test_log_path, "r") as f:
            content = f.read()
            
            # Extract JSON
            start_marker = "[JSON_ENTRY]"
            end_marker = "[/JSON_ENTRY]"
            start_idx = content.find(start_marker) + len(start_marker)
            end_idx = content.find(end_marker)
            
            json_str = content[start_idx:end_idx].strip()
            data = json.loads(json_str)
            
            self.assertEqual(len(data["violations"]), 1)
            violation = data["violations"][0]
            self.assertEqual(violation["file_path"], "test.py")
            self.assertEqual(violation["line_number"], 42)
            self.assertEqual(violation["detail"], "detail text")
            self.assertEqual(violation["suggestion"], "suggest text")

    @patch("builtins.open", side_effect=PermissionError("Fake permission error"))
    def test_report_handles_io_errors(self, mock_f):
        v = Violation("f.py", "m", "blocking", "v")
        
        # Test should not crash and return success=False
        result = self.reporter.report([v], 10.0)
        
        self.assertFalse(result.success)
        self.assertIn("Fake permission error", result.error_message)

if __name__ == "__main__":
    unittest.main()
