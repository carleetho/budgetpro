import subprocess
import os
import re
from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class StateMachineValidator(BaseValidator):
    """
    Adapter for the State Machine transition validator.
    Ensures state transitions in domain objects follow allowed paths.
    """

    def __init__(self, config: Dict[str, Any], root_dir: str):
        super().__init__(config)
        self.root_dir = root_dir
        self.jar_path = config.get("jar_path", "tools/domain-validator/target/domain-validator-1.0.0-SNAPSHOT.jar")
        self.domain_path = config.get("domain_path", "backend/src/main/java/com/budgetpro/domain")

    @property
    def name(self) -> str:
        return "state_machine_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        full_jar_path = os.path.join(self.root_dir, self.jar_path)
        full_domain_path = os.path.join(self.root_dir, self.domain_path)
        
        if not os.path.exists(full_jar_path):
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path=full_jar_path,
                    message="Domain validator JAR not found.",
                    severity="warning",
                    validator_name=self.name
                )]
            )

        try:
            # For state machine, we always use --cached since AXIOM runs on staged files
            result = subprocess.run(
                ["java", "-jar", full_jar_path, "validate-state-machine", "-d", full_domain_path, "--strict"],
                capture_output=True,
                text=True,
                cwd=self.root_dir
            )
            
            violations = self._parse_output(result.stdout + result.stderr)
            return ValidationResult(
                validator_name=self.name,
                violations=violations,
                success=len(violations) == 0
            )
            
        except Exception as e:
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path="n/a",
                    message=f"Failed to execute state-machine-validator: {str(e)}",
                    severity="warning",
                    validator_name=self.name
                )]
            )

    def _parse_output(self, output: str) -> List[Violation]:
        """
        Parses state machine violations.
        Format from source (estimated): [ERROR/WARNING] class: transition error -> suggestion
        """
        violations = []
        # Pattern based on common domain-validator patterns
        pattern = r"\[(ERROR|WARNING)\] (.*?): (.*?) -> (.*?)$"
        
        matches = re.finditer(pattern, output, re.MULTILINE)
        
        for match in matches:
            severity_raw = match.group(1)
            class_name = match.group(2)
            message = match.group(3)
            suggestion = match.group(4)
            
            violations.append(Violation(
                file_path=class_name, # Usually the class name is enough for context here
                message=f"STATE-MACHINE: {message}",
                severity="blocking" if severity_raw == "ERROR" else "warning",
                validator_name=self.name,
                suggestion=suggestion
            ))
                
        return violations
