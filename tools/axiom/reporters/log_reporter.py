import os
import json
import time
import sys
from datetime import datetime, timezone
from typing import List, Dict, Any
from tools.axiom.reporters.base_reporter import BaseReporter, ReportResult
from tools.axiom.validators.base_validator import Violation

class LogReporter(BaseReporter):
    """
    Reporter that persists validation results to a log file.
    
    Format:
    - ISO 8601 UTC Timestamp header
    - Human-readable violations (ordered by severity)
    - JSON block for programmatic parsing
    """

    def __init__(self, config: Dict[str, Any]):
        super().__init__(config)
        self.log_path = config.get("path", ".budgetpro/validation.log")

    @property
    def name(self) -> str:
        return "log_file"

    def report(self, violations: List[Violation], total_execution_time_ms: float) -> ReportResult:
        """Writes violations to the persistent log file."""
        start_time = time.time()
        
        try:
            # Ensure directory exists
            log_dir = os.path.dirname(self.log_path)
            if log_dir:
                os.makedirs(log_dir, exist_ok=True)
            
            # Prepare content
            timestamp = datetime.now(timezone.utc).isoformat() + "Z"
            
            # Group by severity for human-readable section
            severity_order = ["blocking", "warning", "info"]
            grouped = {s: [] for s in severity_order}
            for v in violations:
                grouped.setdefault(v.severity, []).append(v)
            
            lines = [f"=== Validation Run: {timestamp} ===\n"]
            lines.append(f"Summary: {len(violations)} violations found in {total_execution_time_ms:.1f}ms\n\n")
            
            if not violations:
                lines.append("No violations found.\n")
            else:
                for severity in severity_order:
                    sev_violations = grouped.get(severity, [])
                    if not sev_violations:
                        continue
                    
                    lines.append(f"--- {severity.upper()} ---\n")
                    for v in sev_violations:
                        line_info = f":{v.line_number}" if v.line_number else ""
                        lines.append(f"[{severity.upper()}] {v.file_path}{line_info} - {v.message}\n")
                        if v.detail:
                            lines.append(f"  Detail: {v.detail}\n")
                        if v.suggestion:
                            lines.append(f"  Suggestion: {v.suggestion}\n")
            
            # JSON block
            lines.append("\n[JSON_ENTRY]\n")
            json_data = {
                "timestamp": timestamp,
                "total_execution_time_ms": total_execution_time_ms,
                "violations": [self._violation_to_dict(v) for v in violations]
            }
            lines.append(json.dumps(json_data, indent=2))
            lines.append("\n[/JSON_ENTRY]\n")
            lines.append("-" * 40 + "\n\n")
            
            # Write to file (buffered append)
            with open(self.log_path, "a", encoding="utf-8") as f:
                f.writelines(lines)
                f.flush()

        except Exception as e:
            sys.stderr.write(f"Error writing to log file {self.log_path}: {str(e)}\n")
            sys.stderr.flush()
            return ReportResult(
                success=False,
                reporter_name=self.name,
                execution_time_ms=(time.time() - start_time) * 1000,
                error_message=str(e)
            )

        duration = (time.time() - start_time) * 1000
        return ReportResult(
            success=True,
            reporter_name=self.name,
            execution_time_ms=duration
        )

    def _violation_to_dict(self, v: Violation) -> Dict[str, Any]:
        """Converts a Violation dataclass to a dictionary for JSON serialization."""
        return {
            "file_path": v.file_path,
            "message": v.message,
            "severity": v.severity,
            "validator_name": v.validator_name,
            "line_number": v.line_number,
            "detail": v.detail,
            "suggestion": v.suggestion
        }
