import unittest
from tools.axiom.validators import BaseValidator, Violation, ValidationResult
from typing import List

class ConcreteValidator(BaseValidator):
    @property
    def name(self) -> str:
        return "concrete_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        violations = [
            Violation(
                file_path=files[0],
                message="Testing violation",
                severity="info",
                validator_name=self.name
            )
        ]
        return ValidationResult(
            validator_name=self.name,
            violations=violations,
            success=False
        )

class TestBaseValidator(unittest.TestCase):
    def test_cannot_instantiate_abstract_class(self):
        with self.assertRaises(TypeError):
            BaseValidator({})

    def test_concrete_validator_implementation(self):
        config = {"key": "value"}
        validator = ConcreteValidator(config)
        
        self.assertEqual(validator.name, "concrete_validator")
        self.assertEqual(validator.config, config)
        
        result = validator.validate(["dummy.py"])
        self.assertEqual(result.validator_name, "concrete_validator")
        self.assertEqual(len(result.violations), 1)
        self.assertEqual(result.violations[0].message, "Testing violation")
        self.assertFalse(result.success)

    def test_violation_auto_fix_fields(self):
        violation = Violation(
            file_path="/path/to/file.py",
            message="Auto-fixable violation",
            severity="warning",
            validator_name="test_validator",
            auto_fixable=True,
            fix_data={"action": "replace", "new_content": "fixed"}
        )
        
        self.assertTrue(violation.auto_fixable)
        self.assertEqual(violation.fix_data, {"action": "replace", "new_content": "fixed"})

    def test_violation_defaults(self):
        violation = Violation(
            file_path="/path/to/file.py",
            message="Default violation",
            severity="info",
            validator_name="test_validator"
        )
        
        self.assertFalse(violation.auto_fixable)
        self.assertIsNone(violation.fix_data)

    def test_violation_frozen(self):
        violation = Violation(
            file_path="/path/to/file.py",
            message="Frozen violation",
            severity="info",
            validator_name="test_validator"
        )
        with self.assertRaises(Exception): # dataclasses.FrozenInstanceError
            violation.auto_fixable = True

if __name__ == "__main__":
    unittest.main()
