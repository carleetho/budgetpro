import subprocess
import os
import json
from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class SemgrepValidator(BaseValidator):
    """
    Adapter for Semgrep static analysis.
    Executes rules defined in .semgrep/rules and reports findings.
    """

    def __init__(self, config: Dict[str, Any], root_dir: str):
        super().__init__(config)
        self.root_dir = root_dir
        self.rules_path = config.get("rules_path", ".semgrep/rules")
        # Use local.yaml for pre-commit context (non-blocking, developer-friendly)
        self.config_path = config.get("config_path", ".semgrep/config/local.yaml")

    @property
    def name(self) -> str:
        return "semgrep_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        # Semgrep is more efficient when scanning directories, 
        # but AXIOM pattern is to scan staged files.
        # We'll pass the list of files to semgrep scan.
        
        if not files:
            return ValidationResult(validator_name=self.name, success=True, violations=[])

        try:
            # Check if semgrep is installed
            subprocess.run(["semgrep", "--version"], capture_output=True, check=True)
            
            # Execute semgrep scan
            # We use --json to parse results easily
            cmd = [
                "semgrep", "scan",
                "--config", self.rules_path,
                "--config", self.config_path,
                "--json",
                "--quiet"
            ] + files
            
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                cwd=self.root_dir
            )
            
            if result.returncode not in [0, 1]: # Semgrep returns 1 if findings are found
                # Possibly an execution error
                if "unknown option" in result.stderr:
                     # Fallback for older semgrep versions if needed
                     pass

            violations = self._parse_json_output(result.stdout)
            return ValidationResult(
                validator_name=self.name,
                violations=violations,
                success=len([v for v in violations if v.severity == "blocking"]) == 0
            )
            
        except FileNotFoundError:
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path="n/a",
                    message="Semgrep CLI not found. Please install it with 'pip install semgrep'.",
                    severity="warning",
                    validator_name=self.name
                )]
            )
        except Exception as e:
            return ValidationResult(
                validator_name=self.name,
                success=False,
                violations=[Violation(
                    file_path="n/a",
                    message=f"Failed to execute semgrep: {str(e)}",
                    severity="warning",
                    validator_name=self.name
                )]
            )

    def _parse_json_output(self, json_output: str) -> List[Violation]:
        violations = []
        try:
            data = json.loads(json_output)
            results = data.get("results", [])
            
            for item in results:
                path = item.get("path")
                start = item.get("start", {})
                line = start.get("line")
                check_id = item.get("check_id", "unknown")
                msg = item.get("extra", {}).get("message", "Semgrep finding")
                raw_severity = item.get("extra", {}).get("severity", "WARNING")
                
                # Filter/Map severity
                # In semgrep: ERROR, WARNING, INFO
                severity = "blocking" if raw_severity == "ERROR" else "warning"
                
                violations.append(Violation(
                    file_path=path,
                    message=f"[{check_id}] {msg}",
                    severity=severity,
                    validator_name=self.name,
                    line_number=line
                ))
        except Exception:
            # If JSON parsing fails, return empty or log error
            pass
                
        return violations
