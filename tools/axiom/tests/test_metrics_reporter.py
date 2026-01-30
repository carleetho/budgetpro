import unittest
import os
import shutil
import json
from tools.axiom.reporters.metrics_reporter import MetricsReporter
from tools.axiom.validators.base_validator import Violation

class TestMetricsReporter(unittest.TestCase):

    def setUp(self):
        self.test_log_dir = ".test_metrics"
        self.test_path = os.path.join(self.test_log_dir, "metrics.json")
        self.config = {"path": self.test_path}
        self.reporter = MetricsReporter(self.config)
        
        if os.path.exists(self.test_log_dir):
            shutil.rmtree(self.test_log_dir)

    def tearDown(self):
        if os.path.exists(self.test_log_dir):
            shutil.rmtree(self.test_log_dir)

    def test_first_run_creation(self):
        v = Violation("src/domain/model.py", "msg", "blocking", "val1")
        result = self.reporter.report([v], 100.0)
        
        self.assertTrue(result.success)
        self.assertTrue(os.path.exists(self.test_path))
        
        with open(self.test_path, "r") as f:
            data = json.load(f)
            self.assertEqual(len(data["history"]), 1)
            self.assertEqual(data["statistics"]["total_runs"], 1)
            self.assertEqual(data["history"][0]["by_module"]["src"], 1)

    def test_history_limit(self):
        # Flood with 110 runs
        v = []
        for i in range(110):
            self.reporter.report(v, 1.0)
            
        with open(self.test_path, "r") as f:
            data = json.load(f)
            self.assertEqual(len(data["history"]), 100)

    def test_trend_calculation(self):
        # Improving trend: 15 violations -> 5 violations
        # Previous 5 runs: 15 violations each
        for _ in range(5):
            violations = [Violation("f", "m", "warn", "v") for _ in range(15)]
            self.reporter.report(violations, 1.0)
            
        # Recent 5 runs: 5 violations each
        for _ in range(5):
            violations = [Violation("f", "m", "warn", "v") for _ in range(5)]
            self.reporter.report(violations, 1.0)
            
        with open(self.test_path, "r") as f:
            data = json.load(f)
            self.assertEqual(data["statistics"]["trend"], "improving")

        # Degrading trend: 5 -> 20
        for _ in range(5):
            violations = [Violation("f", "m", "warn", "v") for _ in range(20)]
            self.reporter.report(violations, 1.0)
            
        with open(self.test_path, "r") as f:
            data = json.load(f)
            self.assertEqual(data["statistics"]["trend"], "degrading")

        # Stable trend: 20 -> 21 (5% change, < 10% threshold)
        for _ in range(5):
            violations = [Violation("f", "m", "warn", "v") for _ in range(21)]
            self.reporter.report(violations, 1.0)
            
        with open(self.test_path, "r") as f:
            data = json.load(f)
            self.assertEqual(data["statistics"]["trend"], "stable")

    def test_invalid_json_recovery(self):
        os.makedirs(self.test_log_dir, exist_ok=True)
        with open(self.test_path, "w") as f:
            f.write("{ invalid json content ]")
            
        v = [Violation("f", "m", "info", "v")]
        result = self.reporter.report(v, 1.0)
        
        self.assertTrue(result.success)
        with open(self.test_path, "r") as f:
            data = json.load(f)
            self.assertEqual(len(data["history"]), 1)

if __name__ == "__main__":
    unittest.main()
