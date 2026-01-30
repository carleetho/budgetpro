import unittest
from unittest.mock import patch
import io
import sys
import time
from tools.axiom.reporters.console_reporter import ConsoleReporter
from tools.axiom.validators.base_validator import Violation

class TestConsoleReporter(unittest.TestCase):

    def setUp(self):
        self.reporter = ConsoleReporter({})
        self.held_output = io.StringIO()
        # Redirect stdout to capture output
        self.original_stdout = sys.stdout
        sys.stdout = self.held_output

    def tearDown(self):
        sys.stdout = self.original_stdout

    def test_report_no_violations(self):
        result = self.reporter.report([], 10.0)
        output = self.held_output.getvalue()
        
        self.assertTrue(result.success)
        self.assertIn("No se encontraron violaciones", output)
        self.assertIn("\033[92m", output) # Green color

    def test_report_mixed_severities_grouping(self):
        v1 = Violation("f1", "m1", "warning", "v1")
        v2 = Violation("f2", "m2", "blocking", "v2")
        v3 = Violation("f3", "m3", "info", "v3")
        
        self.reporter.report([v1, v2, v3], 15.5)
        output = self.held_output.getvalue()
        
        # Verify order in output: BLOCKING should come before WARNING which comes before INFO
        blocking_pos = output.find("--- BLOCKING ---")
        warning_pos = output.find("--- WARNING ---")
        info_pos = output.find("--- INFO ---")
        
        self.assertTrue(blocking_pos < warning_pos < info_pos)
        self.assertIn("\033[91m", output) # Red
        self.assertIn("\033[93m", output) # Yellow
        self.assertIn("\033[94m", output) # Blue

    def test_report_detail_and_suggestion(self):
        v = Violation(
            "f.py", "msg", "blocking", "v", 
            detail="Detailed explanation", 
            suggestion="Fix it this way"
        )
        
        self.reporter.report([v], 5.0)
        output = self.held_output.getvalue()
        
        # Labels are bolded, so we need to account for ANSI bold codes in assertions
        self.assertIn("\033[1mDetalle:\033[0m Detailed explanation", output)
        self.assertIn("\033[1mSugerencia:\033[0m Fix it this way", output)

    def test_performance_under_100ms(self):
        violations = [
            Violation(f"file_{i}.py", f"msg_{i}", "warning", "val") 
            for i in range(50)
        ]
        
        start = time.time()
        result = self.reporter.report(violations, 100.0)
        end = time.time()
        
        execution_time = (end - start) * 1000
        self.assertLess(execution_time, 100.0)
        self.assertTrue(result.success)

if __name__ == "__main__":
    unittest.main()
