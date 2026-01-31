"""
Domain Validator for AXIOM
Validates domain layer purity and architectural boundaries.
"""

from typing import List, Dict, Any
import subprocess
import json
import os
from .base_validator import BaseValidator, ValidationResult, Violation


class DomainValidator(BaseValidator):
    """
    Validates domain layer purity and architectural boundaries.
    Integrates the domain-validator tool into AXIOM pipeline.
    
    This validator ensures:
    - No Spring/JPA imports in domain layer
    - No infrastructure dependencies in domain
    - Proper bounded context structure
    - Adherence to hexagonal architecture
    """
    
    def __init__(self, config: Dict[str, Any], root_dir: str):
        super().__init__(config)
        self.root_dir = root_dir
        self.validator_dir = os.path.join(root_dir, "tools/domain-validator")
        self.config_file = config.get("config_file", ".domain-validator.yaml")
        self.strict_mode = config.get("strict_mode", False)
        self.severity_threshold = config.get("severity_threshold", "HIGH")
    
    @property
    def name(self) -> str:
        return "domain"
    
    def validate(self, files: List[str]) -> ValidationResult:
        """
        Execute domain validation using the domain-validator tool.
        
        Args:
            files: List of file paths to validate
            
        Returns:
            ValidationResult with violations found
        """
        # Only validate if domain files are in the changeset
        domain_files = [f for f in files if '/domain/' in f and f.endswith('.java')]
        
        if not domain_files:
            return ValidationResult(
                validator_name=self.name,
                passed=True,
                violations=[],
                message="No domain files in changeset"
            )
        
        # Run domain purity analysis
        try:
            violations = self._run_purity_analysis()
            
            # Filter violations by severity threshold
            if self.severity_threshold == "CRITICAL":
                blocking_violations = [v for v in violations if v.severity == "blocking"]
            else:  # HIGH or lower
                blocking_violations = violations
            
            passed = len(blocking_violations) == 0
            
            return ValidationResult(
                validator_name=self.name,
                passed=passed,
                violations=violations,
                message=f"Found {len(violations)} domain purity violations ({len(blocking_violations)} blocking)"
            )
            
        except Exception as e:
            return ValidationResult(
                validator_name=self.name,
                passed=False,
                violations=[],
                message=f"Domain validator failed: {str(e)}"
            )
    
    def _run_purity_analysis(self) -> List[Violation]:
        """
        Run domain purity analysis using Python scripts.
        
        Returns:
            List of Violation objects
        """
        violations = []
        
        # Step 1: Discover domain files
        inventory_file = "/tmp/domain-inventory.json"
        discover_cmd = [
            "python3",
            os.path.join(self.validator_dir, "scripts/discover_domain.py"),
            "--repo-root", self.root_dir,
            "--config", self.config_file,
            "--output", inventory_file
        ]
        
        result = subprocess.run(
            discover_cmd,
            capture_output=True,
            text=True,
            cwd=self.validator_dir
        )
        
        if result.returncode != 0:
            raise Exception(f"Domain discovery failed: {result.stderr}")
        
        # Step 2: Analyze purity
        purity_file = "/tmp/domain-purity.json"
        analyze_cmd = [
            "python3",
            os.path.join(self.validator_dir, "scripts/analyze_purity.py"),
            "--input", inventory_file,
            "--output", purity_file
        ]
        
        result = subprocess.run(
            analyze_cmd,
            capture_output=True,
            text=True,
            cwd=self.validator_dir
        )
        
        if result.returncode != 0:
            raise Exception(f"Purity analysis failed: {result.stderr}")
        
        # Step 3: Parse results
        with open(purity_file, 'r') as f:
            data = json.load(f)
        
        violations = self._parse_violations(data)
        
        return violations
    
    def _parse_violations(self, data: Dict) -> List[Violation]:
        """
        Parse domain validator JSON output into AXIOM violations.
        
        Args:
            data: JSON data from purity analysis
            
        Returns:
            List of Violation objects
        """
        violations = []
        
        for file_record in data.get("violations", []):
            for v in file_record.get("violations", []):
                # Map severity: CRITICAL/HIGH -> blocking, others -> warning
                severity = "blocking" if v["severity"] in ["CRITICAL", "HIGH"] else "warning"
                
                violations.append(Violation(
                    file_path=file_record["path"],
                    message=f"[{v['type']}] {v['description']}",
                    severity=severity,
                    validator_name=self.name,
                    line_number=v.get("line_number")
                ))
        
        return violations
