from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult
from tools.axiom.lib.blast_radius_adapter import BlastRadiusAdapter

class BlastRadiusValidator(BaseValidator):
    def __init__(self, config: Dict[str, Any], adapter: BlastRadiusAdapter):
        super().__init__(config)
        self.adapter = adapter
        
    @property
    def name(self) -> str:
        return "blast_radius"

    def validate(self, files: List[str]) -> ValidationResult:
        violations = []
        
        # Delegate logic to adapter
        raw_violations = self.adapter.check_limits(files)
        
        for v in raw_violations:
            violations.append(Violation(
                file_path=v.get("file_path", "Global"),
                message=v.get("message"),
                severity=v.get("severity", "blocking"),
                validator_name=self.name
            ))
            
        return ValidationResult(
            validator_name=self.name,
            violations=violations,
            success=len(violations) == 0
        )
