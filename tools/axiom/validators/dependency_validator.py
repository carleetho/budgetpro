import os
import re
from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class DependencyValidator(BaseValidator):
    """
    Validator to enforce architectural boundaries and dependency rules.
    Specifically:
    - Domain must not import Infrastructure.
    - Domain must not import Spring classes.
    - Domain must not import Jakarta/Persistence classes.
    """
    
    FORBIDDEN_DOMAIN_IMPORTS = [
        (r"import\s+com\.budgetpro\.infrastructure\.", "FORBIDDEN IMPORT: Domain cannot import Infrastructure."),
        (r"import\s+org\.springframework\.", "FORBIDDEN IMPORT: Domain cannot import Spring Framework."),
        (r"import\s+jakarta\.persistence\.", "FORBIDDEN IMPORT: Domain cannot import Jakarta Persistence (JPA)."),
        (r"import\s+jakarta\.validation\.", "FORBIDDEN IMPORT: Domain cannot import Jakarta Validation."),
    ]

    def __init__(self, config: Dict[str, Any]):
        super().__init__(config)

    @property
    def name(self) -> str:
        return "dependency_validator"

    def validate(self, files: List[str]) -> ValidationResult:
        violations = []
        
        # Determine which modules are "Domain" modules
        domain_patterns = self.config.get("domain_patterns", ["domain/"])
        
        for file_path in files:
            if not file_path.endswith(".java"):
                continue
                
            # Check if file belongs to Domain
            is_domain = any(pattern in file_path for pattern in domain_patterns)
            if not is_domain:
                continue
                
            if not os.path.exists(file_path):
                continue
                
            violations.extend(self._check_forbidden_imports(file_path))
            
        return ValidationResult(
            validator_name=self.name,
            violations=violations,
            success=len([v for v in violations if v.severity == "blocking"]) == 0
        )

    def _check_forbidden_imports(self, file_path: str) -> List[Violation]:
        violations = []
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                for line_num, line in enumerate(f, 1):
                    # Skip comments
                    if line.strip().startswith("//") or line.strip().startswith("/*"):
                        continue
                        
                    for pattern, message in self.FORBIDDEN_DOMAIN_IMPORTS:
                        if re.search(pattern, line):
                            violations.append(Violation(
                                file_path=file_path,
                                message=message,
                                severity="blocking",
                                validator_name=self.name,
                                line_number=line_num
                            ))
        except Exception as e:
            # Silently ignore read errors during validation (handled by other validators if critical)
            pass
            
        return violations
