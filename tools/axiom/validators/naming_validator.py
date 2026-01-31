import subprocess
import os
import re
from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class NamingValidator(BaseValidator):
    """
    Adapter for the Java-based naming-validator tool.
    
    This validator runs a compiled JAR that checks naming conventions 
    for Java classes based on architectural layers.
    """

    def __init__(self, config: Dict[str, Any], root_dir: str):
        super().__init__(config)
        self.root_dir = root_dir
        self.jar_path = config.get("jar_path", "tools/naming-validator/target/naming-validator-1.0.0-SNAPSHOT.jar")
        self.source_path = config.get("source_path", "backend/src/main/java")
        self.config_path = config.get("config_path")

    @property
    def name(self) -> str:
        return "naming_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        """
        Runs the naming-validator JAR against the source directory.
        Note: The current Java tool scans the whole directory rather than specific files,
        but we can filter the results to only include staged files if needed.
        """
        full_jar_path = os.path.join(self.root_dir, self.jar_path)
        full_source_path = os.path.join(self.root_dir, self.source_path)
        
        if not os.path.exists(full_jar_path):
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path=full_jar_path,
                    message="Naming validator JAR not found. Run 'cd tools/naming-validator && ./mvnw package'",
                    severity="warning",
                    validator_name=self.name
                )]
            )

        try:
            # Prepare command
            cmd = ["java", "-jar", full_jar_path, full_source_path]
            
            if self.config_path:
                full_config_path = os.path.join(self.root_dir, self.config_path)
                if os.path.exists(full_config_path):
                    cmd.extend(["--config", full_config_path])
            
            # Execute the JAR
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                cwd=self.root_dir
            )
            
            violations = self._parse_output(result.stderr + result.stdout, files)
            return ValidationResult(
                validator_name=self.name,
                violations=violations,
                success=True
            )
            
        except Exception as e:
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path="n/a",
                    message=f"Failed to execute naming-validator: {str(e)}",
                    severity="warning",
                    validator_name=self.name
                )]
            )

    def _parse_output(self, output: str, staged_files: List[str]) -> List[Violation]:
        """
        Parses the output of the Java tool.
        Example line: [WARNING] ../../backend/src/main/java/com/budgetpro/JpaEntity.java: NAMING: ...
        """
        violations = []
        # Pattern to match: [SEVERITY] path: message\n  Sugerencia: suggestion
        pattern = r"\[(BLOCKING|WARNING)\] (.*?): (.*?)\n\s+Sugerencia: (.*?)$"
        
        matches = re.finditer(pattern, output, re.MULTILINE)
        
        staged_files_set = set(os.path.abspath(f) for f in staged_files)
        
        for match in matches:
            severity_raw = match.group(1)
            rel_path = match.group(2)
            message = match.group(3)
            suggestion = match.group(4)
            
            # Normalize path
            # The Java tool output paths relative to its target/ execution dir or as passed.
            # In our case it was ../../backend/...
            abs_path = os.path.abspath(os.path.join(self.root_dir, "tools/naming-validator", rel_path))
            
            # Only report if it's one of the files we are currently validating (staged)
            if abs_path in staged_files_set:
                violations.append(Violation(
                    file_path=abs_path,
                    message=message,
                    severity="blocking" if severity_raw == "BLOCKING" else "warning",
                    validator_name=self.name,
                    suggestion=suggestion
                ))
                
        return violations
