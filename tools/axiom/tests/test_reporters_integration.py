import unittest
import os
import shutil
import json
import sys
import io
import time
import re
from unittest.mock import patch, MagicMock
from tools.axiom.axiom_sentinel import AxiomSentinel
from tools.axiom.validators.base_validator import Violation, ValidationResult
from tools.axiom.config_loader import AxiomConfig
from tools.axiom.reporters.console_reporter import ConsoleReporter
from tools.axiom.reporters.log_reporter import LogReporter
from tools.axiom.reporters.metrics_reporter import MetricsReporter

def strip_ansi(text: str) -> str:
    """Removes ANSI escape sequences from a string."""
    ansi_escape = re.compile(r'\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])')
    return ansi_escape.sub('', text)

class TestReportersIntegration(unittest.TestCase):

    def setUp(self):
        self.test_dir = ".test_axiom_integration"
        if os.path.exists(self.test_dir):
            shutil.rmtree(self.test_dir)
        os.makedirs(self.test_dir, exist_ok=True)
        
        self.log_path = os.path.join(self.test_dir, "validation.log")
        self.metrics_path = os.path.join(self.test_dir, "metrics.json")
        
        self.config = AxiomConfig(reporters={
            "console": {"enabled": True},
            "log_file": {"enabled": True, "path": self.log_path},
            "metrics": {"enabled": True, "path": self.metrics_path}
        })
        
        # Prepare sample violations
        self.violations = [
            Violation(
                file_path="src/domain/entity.py",
                message="Naming mismatch",
                severity="blocking",
                validator_name="naming_checker",
                line_number=15,
                detail="Entity name should match file name",
                suggestion="Rename class to Entity"
            ),
            Violation(
                file_path="src/infrastructure/db.py",
                message="Slow query pattern",
                severity="warning",
                validator_name="perf_checker"
            )
        ]
        
        # Silence AxiomSentinel logging
        import logging
        logging.getLogger("AxiomSentinel").setLevel(logging.CRITICAL)

    def tearDown(self):
        if os.path.exists(self.test_dir):
            shutil.rmtree(self.test_dir)

    def test_full_pipeline_execution(self):
        """Test that all three reporters execute and produce expected files/output."""
        sentinel = AxiomSentinel()
        sentinel.config = self.config
        sentinel._initialize_components()
        
        # Capture stdout for ConsoleReporter
        captured_output = io.StringIO()
        original_stdout = sys.stdout
        sys.stdout = captured_output
        
        try:
            # Prepare internal aggregated result
            val_results = [ValidationResult("naming_checker", [self.violations[0]]),
                           ValidationResult("perf_checker", [self.violations[1]])]
            aggregated = sentinel._aggregate_violations(val_results)
            
            # Run reporters
            sentinel._invoke_reporters(aggregated, 150.0)
            
            output = captured_output.getvalue()
            
            # 1. Verify Console Output
            stripped_output = strip_ansi(output)
            self.assertIn("--- BLOCKING ---", stripped_output)
            self.assertIn("--- WARNING ---", stripped_output)
            self.assertIn("Detalle: Entity name should match file name", stripped_output)
            self.assertIn("Sugerencia: Rename class to Entity", stripped_output)
            
            # Verify that some ANSI codes were actually present
            self.assertNotEqual(output, stripped_output)
            
            # 2. Verify Log File
            self.assertTrue(os.path.exists(self.log_path))
            with open(self.log_path, "r", encoding="utf-8") as f:
                log_content = f.read()
                self.assertIn("=== Validation Run:", log_content)
                self.assertIn("[JSON_ENTRY]", log_content)
                self.assertIn("Naming mismatch", log_content)
                
            # 3. Verify Metrics File
            self.assertTrue(os.path.exists(self.metrics_path))
            with open(self.metrics_path, "r", encoding="utf-8") as f:
                metrics_data = json.load(f)
                self.assertEqual(len(metrics_data["history"]), 1)
                self.assertEqual(metrics_data["statistics"]["total_runs"], 1)
                self.assertEqual(metrics_data["history"][0]["total_violations"], 2)
                
        finally:
            sys.stdout = original_stdout

    def test_reporter_error_isolation(self):
        """Test that failure in one reporter does not block others."""
        sentinel = AxiomSentinel()
        sentinel.config = self.config
        sentinel._initialize_components()
        
        # Mock LogReporter to fail
        for r in sentinel.reporters:
            if isinstance(r, LogReporter):
                r.report = MagicMock(side_effect=RuntimeError("Disk failure"))
        
        captured_output = io.StringIO()
        original_stdout = sys.stdout
        sys.stdout = captured_output
        
        try:
            val_results = [ValidationResult("v", [self.violations[0]])]
            aggregated = sentinel._aggregate_violations(val_results)
            
            # Should not raise exception
            sentinel._invoke_reporters(aggregated, 10.0)
            
            stripped_output = strip_ansi(captured_output.getvalue())
            # Console should still have worked
            self.assertIn("AXIOM: Se encontraron 1 violaciones", stripped_output)
            
            # Metrics should still have worked
            self.assertTrue(os.path.exists(self.metrics_path))
            with open(self.metrics_path, "r") as f:
                data = json.load(f)
                self.assertEqual(len(data["history"]), 1)
                
        finally:
            sys.stdout = original_stdout

    def test_selective_enabling(self):
        """Test that reporters can be disabled via config."""
        config = AxiomConfig(reporters={
            "console": {"enabled": False},
            "log_file": {"enabled": True, "path": self.log_path},
            "metrics": {"enabled": False, "path": self.metrics_path}
        })
        
        sentinel = AxiomSentinel()
        sentinel.config = config
        sentinel._initialize_components()
        
        self.assertEqual(len(sentinel.reporters), 1)
        self.assertIsInstance(sentinel.reporters[0], LogReporter)

    def test_metrics_persistence_over_runs(self):
        """Test that metrics and logs accumulate correctly."""
        sentinel = AxiomSentinel()
        sentinel.config = self.config
        sentinel._initialize_components()
        
        # Run 3 times
        for _ in range(3):
            val_results = [ValidationResult("v", [self.violations[0]])]
            aggregated = sentinel._aggregate_violations(val_results)
            sentinel._invoke_reporters(aggregated, 10.0)
            
        # Check counts
        with open(self.metrics_path, "r") as f:
            data = json.load(f)
            self.assertEqual(len(data["history"]), 3)
            self.assertEqual(data["statistics"]["total_runs"], 3)
            
        with open(self.log_path, "r") as f:
            content = f.read()
            self.assertEqual(content.count("=== Validation Run:"), 3)

    def test_console_performance_integration(self):
        """Quick check that performance is still acceptable in integration context."""
        sentinel = AxiomSentinel()
        sentinel.config = self.config
        sentinel._initialize_components()
        
        # 50 violations
        many_violations = [Violation("f", "m", "info", "v") for _ in range(50)]
        aggregated = AggregatedViolations(info=many_violations)
        
        start = time.time()
        # Mock stdout for no output delay
        with patch('sys.stdout', new=io.StringIO()):
            sentinel._invoke_reporters(aggregated, 10.0)
        duration = (time.time() - start) * 1000
        
        self.assertLess(duration, 200.0) # More lenient than unit test due to disk I/O for others

from tools.axiom.axiom_sentinel import AggregatedViolations

if __name__ == "__main__":
    unittest.main()
