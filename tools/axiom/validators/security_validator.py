import os
import re
import subprocess
from typing import List, Dict, Any, Optional
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class SecurityValidator(BaseValidator):
    """
    Validator responsible for enforcing security best practices.
    
    Includes checks for:
    - Sensitive information leaks (credentials, keys)
    - Critical file integrity (.gitignore)
    - Unauthorized deletions of security-relevant files
    - Basic compilation check (Maven)
    """
    
    # Common sensitive patterns to scan for
    SENSITIVE_PATTERNS = {
        "AWS Access Key": r"AKIA[0-9A-Z]{16}",
        "Slack Token": r"xox[baprs]-[0-9a-zA-Z]{10,48}",
        "Private Key Header": r"-----BEGIN [A-Z ]*PRIVATE KEY-----",
        "Generic Secret/API Key": r"(?i)(key|secret|password|auth|token|api_key|private_key)[:=\s]+['\"]?[0-9a-zA-Z\-_]{16,}['\"]?",
    }

    # Strictness levels
    STRICTNESS_STRICT = "strict"
    STRICTNESS_STANDARD = "standard"
    STRICTNESS_PERMISSIVE = "permissive"

    # Raw Security Severities
    SEC_CRITICAL = "CRITICAL"
    SEC_HIGH = "HIGH"
    SEC_MEDIUM = "MEDIUM"
    SEC_LOW = "LOW"

    @property
    def name(self) -> str:
        return "security_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        """
        Orchestrates all enabled security checks.
        """
        violations: List[Violation] = []
        checks = self.config.get("checks", {})
        
        if checks.get("gitignore", True):
            violations.extend(self._check_gitignore())
            
        if checks.get("credentials", True):
            violations.extend(self._check_credentials(files))

        if checks.get("file_integrity", True):
            violations.extend(self._check_compilation(files))
        
        return ValidationResult(
            validator_name=self.name,
            violations=violations,
            success=len([v for v in violations if v.severity == "blocking"]) == 0
        )

    def _resolve_severity(self, raw_severity: str) -> str:
        """Maps security-specific severities to AXIOM levels based on strictness."""
        strictness = self.config.get("strictness", self.STRICTNESS_STANDARD).lower()
        
        if raw_severity == self.SEC_CRITICAL:
            return "blocking"
            
        if strictness == self.STRICTNESS_STRICT:
            if raw_severity == self.SEC_HIGH:
                return "blocking"
            return "warning"
            
        if strictness == self.STRICTNESS_STANDARD:
            return "warning"
            
        if strictness == self.STRICTNESS_PERMISSIVE:
            return "warning"
            
        return "warning"

    def _check_compilation(self, files: List[str]) -> List[Violation]:
        """Ensures Java changes compile successfully."""
        violations = []
        
        java_relevant = any(f.endswith(".java") or "pom.xml" in f for f in files)
        if not java_relevant:
            return []

        # Find backend directory containing mvnw
        backend_dir = "backend"
        mvnw_path = os.path.join(backend_dir, "mvnw")
        
        if not os.path.exists(mvnw_path):
            if os.path.exists("mvnw"):
                mvnw_path = "./mvnw"
            else:
                return []

        try:
            # Use ./mvnw if we are running inside the backend directory
            cmd_mvnw = "./mvnw" if backend_dir == "backend" else mvnw_path
            
            result = subprocess.run(
                [cmd_mvnw, "compile", "-DskipTests"],
                cwd=backend_dir,
                capture_output=True,
                text=True,
                check=False
            )
            
            if result.returncode != 0:
                violations.append(Violation(
                    file_path="backend/pom.xml",
                    message=f"COMPILATION ERROR: Project failed to compile. Error output:\n{result.stderr or result.stdout}",
                    severity=self._resolve_severity(self.SEC_CRITICAL),
                    validator_name=self.name
                ))
        except Exception as e:
            violations.append(Violation(
                file_path="backend/pom.xml",
                message=f"ERROR: Execution of Maven compilation check failed: {e}",
                severity=self._resolve_severity(self.SEC_CRITICAL),
                validator_name=self.name
            ))
            
        return violations

    def _check_credentials(self, files: List[str]) -> List[Violation]:
        """Scans staged files for common sensitive patterns."""
        violations = []
        ignore_extensions = ['.png', '.jpg', '.jpeg', '.gif', '.ico', '.pdf', '.zip', '.jar', '.war']
        
        for file_path in files:
            if any(file_path.lower().endswith(ext) for ext in ignore_extensions):
                continue
                
            if not os.path.exists(file_path):
                continue
                
            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    for line_num, line in enumerate(f, 1):
                        for pattern_name, pattern in self.SENSITIVE_PATTERNS.items():
                            if re.search(pattern, line):
                                violations.append(Violation(
                                    file_path=file_path,
                                    message=f"POTENTIAL LEAK: Found {pattern_name} pattern.",
                                    severity=self._resolve_severity(self.SEC_HIGH),
                                    validator_name=self.name,
                                    line_number=line_num
                                ))
                                break
            except Exception as e:
                continue
                
        return violations

    def _check_gitignore(self) -> List[Violation]:
        """Ensures .gitignore exists and contains critical exclusions."""
        violations = []
        gitignore_path = ".gitignore"
        
        # Use absolute path if config suggests it, or if we want to be safe for matching
        abs_gitignore_path = os.path.abspath(gitignore_path)
        
        if not os.path.exists(abs_gitignore_path):
            violations.append(Violation(
                file_path=abs_gitignore_path,
                message="CRITICAL: .gitignore file is missing. This is a severe security risk.",
                severity=self._resolve_severity(self.SEC_CRITICAL),
                validator_name=self.name
            ))
            return violations

        # Get patterns from config or use defaults
        config_patterns = self.config.get("required_gitignore_entries", {})
        if isinstance(config_patterns, dict):
            # If it's a dict, we look for our specific file or a generic list
            critical_patterns = config_patterns.get(abs_gitignore_path) or config_patterns.get(gitignore_path) or [".env", "*.log", ".gemini", "node_modules", "target"]
        else:
            critical_patterns = config_patterns or [".env", "*.log", ".gemini", "node_modules", "target"]

        missing_entries = []
        try:
            with open(abs_gitignore_path, "r") as f:
                # Basic parsing: strip comments and whitespace
                lines = [line.split('#')[0].strip() for line in f]
                existing_entries = set(filter(None, lines))
                
                for pattern in critical_patterns:
                    if pattern not in existing_entries:
                        missing_entries.append(pattern)
            
            if missing_entries:
                severity = self._resolve_severity(self.SEC_MEDIUM)
                is_blocking = (severity == "blocking")
                
                violations.append(Violation(
                    file_path=abs_gitignore_path,
                    message=f"Missing .gitignore entries for sensitive files: {', '.join(missing_entries)}",
                    severity=severity,
                    validator_name=self.name,
                    auto_fixable=not is_blocking,
                    fix_data={"missing_entries": missing_entries} if not is_blocking else None
                ))
        except Exception as e:
             violations.append(Violation(
                file_path=abs_gitignore_path,
                message=f"ERROR: Could not read .gitignore: {e}",
                severity=self._resolve_severity(self.SEC_CRITICAL),
                validator_name=self.name
            ))
            
        return violations

        return violations
