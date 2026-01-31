import subprocess
import os
import re
from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class BoundaryValidator(BaseValidator):
    """
    Adapter for the Domain Hexagonal Boundary validator.
    Ensures domain relies only on pure Java/DDD objects.
    """

    def __init__(self, config: Dict[str, Any], root_dir: str):
        super().__init__(config)
        self.root_dir = root_dir
        self.jar_path = config.get("jar_path", "tools/domain-validator/target/domain-validator-1.0.0-SNAPSHOT.jar")
        self.domain_path = config.get("domain_path", "backend/src/main/java/com/budgetpro/domain")
        self.config_path = config.get("config_path", ".budgetpro/boundary-rules.json")

    @property
    def name(self) -> str:
        return "boundary_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        full_jar_path = os.path.join(self.root_dir, self.jar_path)
        full_domain_path = os.path.join(self.root_dir, self.domain_path)
        full_config_path = os.path.join(self.root_dir, self.config_path) if self.config_path else None
        
        if not os.path.exists(full_jar_path):
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path=full_jar_path,
                    message="Domain validator JAR not found. Run 'cd tools/domain-validator && ./mvnw package'",
                    severity="warning",
                    validator_name=self.name
                )]
            )

        try:
            # Prepare command
            cmd = ["java", "-jar", full_jar_path, "validate-boundary", "-r", full_domain_path]
            if full_config_path and os.path.exists(full_config_path):
                cmd.extend(["-c", full_config_path])

            # Execute the JAR
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                cwd=self.root_dir
            )
            
            # Boundary validation is usually exhaustive, but we filter for efficiency
            violations = self._parse_output(result.stdout + result.stderr, files)
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
                    message=f"Failed to execute boundary-validator: {str(e)}",
                    severity="warning",
                    validator_name=self.name
                )]
            )

    def _parse_output(self, output: str, staged_files: List[str]) -> List[Violation]:
        """
        Parses the output of the Java tool.
        New format:   - [SEVERITY] [path]: item -> message
        Example:   - [CRITICAL] [/path/to/file.java]: org.springframework.Service -> Violación...
        """
        violations = []
        # Updated pattern to catch severity, path, forbidden item and message
        pattern = r"^\s+-\s+\[(\w+)\]\s+\[(.*?)\]:\s+(.*?)\s+->\s+(.*?)$"
        
        matches = re.finditer(pattern, output, re.MULTILINE)
        staged_files_set = set(os.path.abspath(f) for f in staged_files)
        
        for match in matches:
            severity_str = match.group(1).upper()
            abs_path = os.path.abspath(match.group(2))
            item = match.group(3)
            message = match.group(4)

            # Map Java severity to AXIOM severity
            if severity_str in ["CRITICAL", "ERROR"]:
                severity = "blocking"
            else:
                severity = "warning"
            
            # Only report if it's one of the files we are currently validating (staged)
            if abs_path in staged_files_set:
                violations.append(Violation(
                    file_path=abs_path,
                    message=f"BOUNDARY: {message} ({item})",
                    severity=severity,
                    validator_name=self.name,
                    suggestion=f"Elimina la referencia a {item} del dominio."
                ))
                
        return violations
