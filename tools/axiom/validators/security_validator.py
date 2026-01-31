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

    def __init__(self, config: Dict[str, Any], global_config: Dict[str, Any] = None):
        super().__init__(config)
        self.global_config = global_config or {}

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
            
        if self.config.get("semantic_integrity", True):
            violations.extend(self._check_semantic_integrity(files))
        
        if self.config.get("entropy_tracking", True):
            violations.extend(self._check_domain_entropy(files))
        
        return ValidationResult(
            validator_name=self.name,
            violations=violations,
            success=len([v for v in violations if v.severity == "blocking"]) == 0
        )

    def _check_semantic_integrity(self, files: List[str]) -> List[Violation]:
        """
        Enforces stricter semantic rules for Red Zones.
        specifically preventing modification of Enums/Classes signatures without override.
        """
        violations = []
        import logging
        
        # 1. Get Red Zones
        # Access protection_zones directly from global_config root (dataclass structure)
        zones = self.global_config.get("protection_zones", {})
        red_zones = zones.get("red", [])
        
        red_paths = [item.get("path") for item in red_zones if item.get("path")]
        logging.info(f"DEBUG: Red paths loaded: {red_paths}")
        
        for file_path in files:
            # Check if file is in Red Zone
            is_red = False
            for rp in red_paths:
                if rp in file_path or file_path.startswith(rp):
                    is_red = True
                    break
            
            if not is_red:
                continue

            # If Red Zone, check diff for dangerous changes
            try:
                # Get staged diff
                # Use --no-color to avoid ANSI codes breaking regex
                # Use --no-ext-diff to skip external diff tools
                cmd = ["git", "diff", "--cached", "--no-color", "--no-ext-diff", file_path]
                result = subprocess.run(cmd, capture_output=True, text=True, check=False)
                
                if result.returncode != 0:
                    logging.warning(f"DEBUG: git diff failed for {file_path}: {result.stderr}")
                    continue
                    
                diff = result.stdout
                
                # Check for removed/modified Enums or Class definitions
                # We look for lines starting with '-' that contain 'enum ' or 'class ' or 'interface '
                # This is a heuristic.
                for line in diff.splitlines():
                    if line.startswith("-") and not line.startswith("---"):
                        content = line[1:].strip()
                        # Dangerous patterns
                        if "enum " in content or "class " in content or "interface " in content:
                            violations.append(Violation(
                                file_path=file_path,
                                message="SEMANTIC LOCK: Modifying Core Domain Type Definitions (Enum/Class/Interface) in Red Zone is PROHIBITED.\nTo bypass this safety lock, you must use 'git commit --no-verify' (Emergency Override).",
                                severity="blocking",
                                validator_name=self.name
                            ))
                            break
                            
                        # Also check for Enum Value modification (simplistic)
                        # If inside an enum file (usually matches *Status.java or *Type.java or State*.java)
                        if "enum" in file_path.lower() or "estado" in file_path.lower() or "type" in file_path.lower():
                             # If we see a removed line that looks like an enum value (UPPERCASE)
                             logging.info(f"DEBUG: Checking enum value match for content: '{content}'")
                             if re.match(r"^[A-Z_]+[A-Z0-9_]*", content):
                                  logging.info(f"DEBUG: MATCHED ENUM VALUE CHANGE: {content}")
                                  violations.append(Violation(
                                    file_path=file_path,
                                    message="SEMANTIC LOCK: Removing/Renaming Enum Values in Red Zone is PROHIBITED.",
                                    severity="blocking",
                                    validator_name=self.name
                                  ))
                                  break

            except Exception as e:
                logging.warning(f"Error checking semantic integrity for {file_path}: {e}")
                
        return violations
        
    def _check_domain_entropy(self, files: List[str]) -> List[Violation]:
        """
        Detects 'Hotspots' in Red Zones by looking at historical frequency of changes.
        """
        violations = []
        import json
        import logging
        from pathlib import Path
        
        # 1. Load historical metrics
        metrics_path = Path(".budgetpro/metrics.json")
        if not metrics_path.exists():
            return []
            
        try:
            with open(metrics_path, 'r') as f:
                data = json.load(f)
            
            history = data.get("history", [])
            if len(history) < 3: # Not enough history to detect hotspots
                return []
                
            # 2. Get Red Zones
            zones = self.global_config.get("protection_zones", {})
            red_zones = zones.get("red", [])
            red_paths = [item.get("path") for item in red_zones if item.get("path")]
            
            for file_path in files:
                # Only check Red Zone files
                is_red = False
                for rp in red_paths:
                    if rp in file_path or file_path.startswith(rp):
                        is_red = True
                        break
                if not is_red: continue
                
                # Check git log for frequency in the last 2 weeks
                cmd = ["git", "log", "--since='2 weeks ago'", "--oneline", "--", file_path]
                result = subprocess.run(cmd, capture_output=True, text=True, check=False)
                if result.returncode == 0:
                    lines = result.stdout.strip().splitlines()
                    if len(lines) >= 3:
                        violations.append(Violation(
                            file_path=file_path,
                            message=f"DOMAIN ENTROPY WARNING: High frequency of changes in Red Zone ({len(lines)} times in 2 weeks).\nThis file is becoming an architectural HOTSPOT. Consider refactoring to stabilize the core.",
                            severity="warning",
                            validator_name=self.name
                        ))
                        
        except Exception as e:
            logging.warning(f"Error checking domain entropy: {e}")
            
        return violations

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
        import logging
        
        java_relevant = any(f.endswith(".java") or "pom.xml" in f for f in files)
        if not java_relevant:
            return []

        # Find backend directory containing mvnw
        # Use absolute path to avoid CWD issues
        repo_root = os.path.abspath(".")
        mvnw_path = os.path.join(repo_root, "backend", "mvnw")
        backend_dir = os.path.join(repo_root, "backend")
        
        if not os.path.exists(mvnw_path):
            if os.path.exists(os.path.join(repo_root, "mvnw")):
                 mvnw_path = os.path.join(repo_root, "mvnw")
                 backend_dir = repo_root
            else:
                 # No mvnw found, cannot check compilation
                 logging.warning("DEBUG: mvnw not found in backend or root. Skipping compilation check.")
                 return []
        
        if not os.path.exists(mvnw_path):
            return []

        try:
            logging.info(f"DEBUG: Executing compilation check: {mvnw_path} in {backend_dir}")
            
            # Force re-evaluation of staged files to bypass incremental build cache
            for f in files:
                abs_f = os.path.join(repo_root, f)
                if f.endswith(".java") and os.path.exists(abs_f):
                    try:
                        os.utime(abs_f, None)
                    except:
                        pass
            
            result = subprocess.run(
                [mvnw_path, "compile", "-DskipTests"],
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
