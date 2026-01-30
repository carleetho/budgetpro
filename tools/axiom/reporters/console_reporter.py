import logging
from typing import List, Dict, Any
from tools.axiom.reporters.base_reporter import BaseReporter, ReportResult
from tools.axiom.validators.base_validator import Violation

class ConsoleReporter(BaseReporter):
    """Simple reporter that outputs violations to the console."""

    @property
    def name(self) -> str:
        return "console"

    def report(self, violations: List[Violation], total_execution_time_ms: float) -> ReportResult:
        """Prints violations to the standard output using logging/print."""
        import time
        start_time = time.time()
        
        if not violations:
            print("\n✅ AXIOM: No se encontraron violaciones.")
        else:
            print(f"\n❌ AXIOM: Se encontraron {len(violations)} violaciones.")
            
            # Grouping by validator for better display
            by_validator = {}
            for v in violations:
                by_validator.setdefault(v.validator_name, []).append(v)
            
            for val_name, val_violations in by_validator.items():
                print(f"\n--- Validador: {val_name} ---")
                for v in val_violations:
                    header = f"[{v.severity.upper()}] {v.file_path}:{v.line_number}"
                    print(f"\n{header}")
                    print("-" * len(header))
                    print(v.message)
        
        duration = (time.time() - start_time) * 1000
        return ReportResult(
            success=True,
            reporter_name=self.name,
            execution_time_ms=duration
        )
