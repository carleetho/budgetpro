import unittest
from unittest.mock import patch, MagicMock, mock_open
import os
import sys
import logging
from tools.axiom.axiom_sentinel import AxiomSentinel, AggregatedViolations
from tools.axiom.validators.base_validator import BaseValidator, ValidationResult, Violation
from tools.axiom.reporters.base_reporter import BaseReporter, ReportResult
from tools.axiom.fixers.base_fixer import BaseFixer, FixResult
from tools.axiom.config_loader import AxiomConfig

class MockValidator(BaseValidator):
    def __init__(self, name="mock_val", violations=None, raise_exception=False):
        super().__init__({})
        self._name = name
        self.violations = violations or []
        self.raise_exception = raise_exception

    @property
    def name(self) -> str:
        return self._name

    def validate(self, files):
        if self.raise_exception:
            raise RuntimeError("Validator crash")
        return ValidationResult(self.name, self.violations, success=not any(v.severity == "blocking" for v in self.violations))

class MockReporter(BaseReporter):
    def __init__(self, name="mock_rep", raise_exception=False):
        super().__init__({})
        self._name = name
        self.raise_exception = raise_exception
        self.reported_violations = []

    @property
    def name(self) -> str:
        return self._name

    def report(self, violations, total_time_ms):
        if self.raise_exception:
            raise RuntimeError("Reporter crash")
        self.reported_violations = violations
        return ReportResult(True, self.name, 1.0)

class MockFixer(BaseFixer):
    def __init__(self, name="mock_fix", fixed_files=None, raise_exception=False):
        super().__init__({})
        self._name = name
        self.fixed_files = fixed_files or []
        self.raise_exception = raise_exception

    @property
    def name(self) -> str:
        return self._name

    def fix(self, violations):
        if self.raise_exception:
            raise RuntimeError("Fixer crash")
        return FixResult(True, self.fixed_files, 1.0)

class TestAxiomSentinel(unittest.TestCase):

    def setUp(self):
        # Silence logging during tests
        logging.getLogger("AxiomSentinel").setLevel(logging.CRITICAL)

    @patch('tools.axiom.axiom_sentinel.load_axiom_config')
    def test_load_configuration_success(self, mock_load):
        config = AxiomConfig(validators={"val": {}})
        mock_load.return_value = config
        
        sentinel = AxiomSentinel()
        loaded = sentinel._load_configuration()
        
        self.assertEqual(loaded, config)
        self.assertEqual(sentinel.config, config)

    @patch('tools.axiom.axiom_sentinel.load_axiom_config')
    def test_load_configuration_fallback(self, mock_load):
        mock_load.side_effect = Exception("File missing")
        
        sentinel = AxiomSentinel()
        loaded = sentinel._load_configuration()
        
        self.assertIsNotNone(loaded)
        self.assertIn("red", loaded.protection_zones) # Check default structure

    @patch('subprocess.run')
    def test_discover_staged_files_success(self, mock_run):
        mock_run.return_value.stdout = "a.py\nb.py\n"
        mock_run.return_value.returncode = 0
        
        sentinel = AxiomSentinel()
        files = sentinel._discover_staged_files()
        
        self.assertEqual(files, ["a.py", "b.py"])

    @patch('subprocess.run')
    def test_discover_staged_files_error(self, mock_run):
        from subprocess import CalledProcessError
        mock_run.side_effect = CalledProcessError(1, "git")
        
        sentinel = AxiomSentinel()
        files = sentinel._discover_staged_files()
        
        self.assertEqual(files, [])

    def test_execute_validators_isolation(self):
        v1 = MockValidator("v1", [Violation("x", "err", "blocking", "v1")])
        v2 = MockValidator("v2", raise_exception=True)
        v3 = MockValidator("v3", [Violation("y", "warn", "warning", "v3")])
        
        sentinel = AxiomSentinel()
        sentinel.validators = [v1, v2, v3]
        
        results = sentinel._execute_validators(["test.py"])
        
        self.assertEqual(len(results), 2) # v2 crashed, but v1 and v3 results should be there
        self.assertEqual(results[0].validator_name, "v1")
        self.assertEqual(results[1].validator_name, "v3")

    def test_aggregate_violations(self):
        res1 = ValidationResult("v1", [Violation("f1", "m1", "blocking", "v1")])
        res2 = ValidationResult("v2", [Violation("f2", "m2", "warning", "v2"), Violation("f3", "m3", "info", "v2")])
        
        sentinel = AxiomSentinel()
        aggregated = sentinel._aggregate_violations([res1, res2])
        
        self.assertEqual(len(aggregated.blocking), 1)
        self.assertEqual(len(aggregated.warning), 1)
        self.assertEqual(len(aggregated.info), 1)
        self.assertEqual(aggregated.total_count, 3)

    def test_violation_with_all_fields(self):
        v = Violation(
            file_path="f.py",
            message="msg",
            severity="blocking",
            validator_name="v",
            line_number=10,
            detail="detailed info",
            suggestion="how to fix"
        )
        self.assertEqual(v.detail, "detailed info")
        self.assertEqual(v.suggestion, "how to fix")
        self.assertEqual(v.line_number, 10)

    def test_violation_backward_compatibility(self):
        v = Violation("f.py", "msg", "blocking", "v")
        self.assertIsNone(v.line_number)
        self.assertIsNone(v.detail)
        self.assertIsNone(v.suggestion)

    def test_violation_immutability(self):
        v = Violation("f.py", "msg", "blocking", "v")
        from dataclasses import FrozenInstanceError
        with self.assertRaises(FrozenInstanceError):
            v.message = "new message"

    @patch('tools.axiom.axiom_sentinel.detect_overrides')
    @patch('subprocess.run')
    def test_apply_overrides_blast_radius(self, mock_run, mock_detect):
        # Mock commit message reading
        mock_run.return_value.stdout = "BIGBANG_APPROVED"
        mock_run.return_value.returncode = 0
        
        # Mock override result
        mock_detect.return_value = MagicMock(bypass_blast_radius=True, bypass_zones=set())
        
        v = Violation("f", "too many files", "blocking", "blast_radius")
        aggregated = AggregatedViolations(blocking=[v])
        
        sentinel = AxiomSentinel()
        modified = sentinel._apply_overrides(aggregated)
        
        self.assertEqual(len(modified.blocking), 0)
        self.assertEqual(len(modified.warning), 1)
        self.assertTrue("[OVERRIDDEN]" in modified.warning[0].message)

    def test_invoke_reporters_isolation(self):
        r1 = MockReporter("r1", raise_exception=True)
        r2 = MockReporter("r2")
        
        sentinel = AxiomSentinel()
        sentinel.reporters = [r1, r2]
        
        violations = [Violation("f", "m", "blocking", "v")]
        sentinel._invoke_reporters(AggregatedViolations(blocking=violations), 100)
        
        self.assertEqual(len(r2.reported_violations), 1)

    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._execute_validators')
    def test_execute_auto_fixer_enabled(self, mock_exec):
        sentinel = AxiomSentinel()
        sentinel.config = AxiomConfig(auto_fix={"enabled": True})
        
        f1 = MockFixer("f1", fixed_files=["a.py"])
        sentinel.fixers = [f1]
        
        # Mock re-validation
        mock_exec.return_value = []
        
        aggregated = AggregatedViolations(blocking=[Violation("a.py", "m", "blocking", "v")])
        sentinel._execute_auto_fixer(aggregated)
        
        self.assertEqual(len(f1.fixed_files), 1)

    def test_make_exit_decision(self):
        sentinel = AxiomSentinel()
        
        # No blocking
        self.assertEqual(sentinel._make_exit_decision(AggregatedViolations(warning=[Violation("f", "m", "warning", "v")])), 0)
        
        # Blocking
        self.assertEqual(sentinel._make_exit_decision(AggregatedViolations(blocking=[Violation("f", "m", "blocking", "v")])), 1)
        
        # Dry-run
        sentinel.dry_run = True
        self.assertEqual(sentinel._make_exit_decision(AggregatedViolations(blocking=[Violation("f", "m", "blocking", "v")])), 0)

    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._load_configuration')
    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._discover_staged_files')
    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._execute_validators')
    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._invoke_reporters')
    def test_run_pipeline_flow(self, mock_rep, mock_val, mock_files, mock_config):
        mock_files.return_value = ["changed.py"]
        mock_val.return_value = [ValidationResult("v", [])]
        
        sentinel = AxiomSentinel()
        exit_code = sentinel.run()
        
        self.assertEqual(exit_code, 0)
        mock_config.assert_called_once()
        mock_files.assert_called_once()
        mock_val.assert_called_once()
        mock_rep.assert_called_once()

if __name__ == "__main__":
    unittest.main()
