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

if __name__ == "__main__":
    unittest.main()
