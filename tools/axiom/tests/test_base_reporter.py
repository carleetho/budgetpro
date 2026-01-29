import unittest
from tools.axiom.reporters import BaseReporter, ReportResult
from tools.axiom.validators import Violation
from typing import List

class ConcreteReporter(BaseReporter):
    @property
    def name(self) -> str:
        return "concrete_reporter"

    def report(self, violations: List[Violation], total_execution_time_ms: float) -> ReportResult:
        return ReportResult(
            success=True,
            reporter_name=self.name,
            execution_time_ms=10.0,
            error_message=None
        )

class TestBaseReporter(unittest.TestCase):
    def test_cannot_instantiate_abstract_class(self):
        with self.assertRaises(TypeError):
            BaseReporter({})

    def test_concrete_reporter_implementation(self):
        config = {"dest": "stdout"}
        reporter = ConcreteReporter(config)
        
        self.assertEqual(reporter.name, "concrete_reporter")
        self.assertEqual(reporter.config, config)
        
        violations = [
            Violation(
                file_path="file.py",
                message="Error",
                severity="blocking",
                validator_name="test_val"
            )
        ]
        
        result = reporter.report(violations, 100.0)
        self.assertTrue(result.success)
        self.assertEqual(result.reporter_name, "concrete_reporter")
        self.assertEqual(result.execution_time_ms, 10.0)

if __name__ == "__main__":
    unittest.main()
