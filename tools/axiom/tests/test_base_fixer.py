import unittest
from tools.axiom.fixers import BaseFixer, FixResult
from tools.axiom.validators import Violation
from typing import List

class ConcreteFixer(BaseFixer):
    @property
    def name(self) -> str:
        return "concrete_fixer"

    def fix(self, violations: List[Violation]) -> FixResult:
        fixed = []
        for v in violations:
            if v.file_path not in fixed:
                fixed.append(v.file_path)
        
        return FixResult(
            success=True,
            fixed_files=fixed,
            execution_time_ms=50.0
        )

class TestBaseFixer(unittest.TestCase):
    def test_cannot_instantiate_abstract_class(self):
        with self.assertRaises(TypeError):
            BaseFixer({})

    def test_concrete_fixer_implementation(self):
        config = {"auto_commit": True}
        fixer = ConcreteFixer(config)
        
        self.assertEqual(fixer.name, "concrete_fixer")
        self.assertEqual(fixer.config, config)
        
        violations = [
            Violation(
                file_path="src/app.py",
                message="Formatting error",
                severity="warning",
                validator_name="fmt"
            )
        ]
        
        result = fixer.fix(violations)
        self.assertTrue(result.success)
        self.assertEqual(result.fixed_files, ["src/app.py"])
        self.assertEqual(result.execution_time_ms, 50.0)

if __name__ == "__main__":
    unittest.main()
