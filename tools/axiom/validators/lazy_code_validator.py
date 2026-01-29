import os
import re
import logging
from typing import List, Dict, Any
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult

class LazyCodeValidator(BaseValidator):
    """
    Validator that detects lazy code anti-patterns to prevent incomplete 
    AI-generated code from entering the repository.
    """

    # Strictly defined regex patterns
    EMPTY_METHOD_REGEX = r"(public|private|protected)\s+[\w<>\[\], ]+\s+\w+\s*\([^)]*\)\s*\{\s*(?://.*|/\*[\s\S]*?\*/|\s)*\}"
    NULL_RETURN_REGEX = r"return\s+null;|return\s+Optional\.empty\(\);"
    TODO_FIXME_REGEX = r"//\s*(TODO|FIXME)"

    @property
    def name(self) -> str:
        """Returns the unique identifier for this validator."""
        return "lazy_code"

    def validate(self, files: List[str]) -> ValidationResult:
        """
        Executes lazy code detection rules against the provided files.
        """
        violations: List[Violation] = []
        
        for file_path in files:
            if not file_path.endswith(".java"):
                continue

            if not os.path.exists(file_path):
                logging.warning(f"File not found: {file_path}")
                continue

            try:
                with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                    content = f.read()

                # 1. Empty method detection
                for match in re.finditer(self.EMPTY_METHOD_REGEX, content):
                    line_num = self._get_line_number(content, match.start())
                    violations.append(self._create_violation(
                        file_path, line_num, content, "empty_method", match.start()
                    ))

                # 2. Null return detection (only in infrastructure/persistence/**)
                if self._matches_path_pattern(file_path, "infrastructure/persistence"):
                    for match in re.finditer(self.NULL_RETURN_REGEX, content):
                        line_num = self._get_line_number(content, match.start())
                        violations.append(self._create_violation(
                            file_path, line_num, content, "null_return", match.start()
                        ))

                # 3. TODO/FIXME detection (only in domain/presupuesto/** or domain/estimacion/**)
                if self._matches_path_pattern(file_path, "domain/presupuesto") or \
                   self._matches_path_pattern(file_path, "domain/estimacion"):
                    for match in re.finditer(self.TODO_FIXME_REGEX, content, re.IGNORECASE):
                        line_num = self._get_line_number(content, match.start())
                        violations.append(self._create_violation(
                            file_path, line_num, content, "todo_fixme", match.start()
                        ))

            except Exception as e:
                logging.warning(f"Error reading file {file_path}: {e}")
                continue

        return ValidationResult(
            validator_name=self.name,
            violations=violations,
            success=len(violations) == 0
        )

    def _create_violation(self, file_path: str, line_num: int, content: str, pattern_type: str, pos: int) -> Violation:
        """Helper to create a Violation with context and suggestion."""
        error_messages = {
            "empty_method": "CÓDIGO PEREZOSO: Método vacío detectado",
            "null_return": "CÓDIGO PEREZOSO: Retorno null o empty detectado",
            "todo_fixme": "CÓDIGO PEREZOSO: TODO o FIXME detectado en módulo crítico"
        }
        
        message = error_messages[pattern_type]
        context = self._extract_code_context(content, line_num)
        suggestion = self._get_suggestion(pattern_type)
        
        full_message = f"{message}\n\nContexto:\n{context}\n\nSugerencia: {suggestion}"
        
        return Violation(
            file_path=file_path,
            message=full_message,
            severity="blocking",
            validator_name=self.name,
            line_number=line_num
        )

    def _extract_code_context(self, content: str, line_number: int, context_lines: int = 2) -> str:
        """Extracts ±2 lines of context around the violation."""
        lines = content.splitlines()
        total_lines = len(lines)
        
        start = max(0, line_number - context_lines - 1)
        end = min(total_lines, line_number + context_lines)
        
        context_parts = []
        for i in range(start, end):
            current_line_num = i + 1
            marker = "→" if current_line_num == line_number else " "
            context_parts.append(f"{marker} {current_line_num:4} | {lines[i]}")
            
        return "\n".join(context_parts)

    def _get_suggestion(self, pattern_type: str) -> str:
        """Returns pattern-specific actionable suggestions."""
        suggestions = {
            "empty_method": "Implementa la lógica del método o elimínalo si no es necesario",
            "null_return": "Implementa la lógica real o lanza excepción específica",
            "todo_fixme": "Completa la implementación antes de commit"
        }
        return suggestions.get(pattern_type, "")

    def _matches_path_pattern(self, file_path: str, pattern: str) -> bool:
        """Checks if file_path contains the pattern fragment."""
        normalized_path = file_path.replace("\\", "/")
        return pattern.rstrip("/") + "/" in normalized_path or normalized_path.startswith(pattern.rstrip("/") + "/")

    def _get_line_number(self, content: str, match_position: int) -> int:
        """Calculates the 1-indexed line number from a position."""
        return content.count('\n', 0, match_position) + 1
