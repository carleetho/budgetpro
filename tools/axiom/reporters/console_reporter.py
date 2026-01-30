import sys
import time
from typing import List, Dict, Any
from tools.axiom.reporters.base_reporter import BaseReporter, ReportResult
from tools.axiom.validators.base_validator import Violation

class ConsoleReporter(BaseReporter):
    """
    Enhanced reporter that outputs violations to the console with:
    - Severity-based grouping
    - ANSI color coding
    - Detail and suggestion display
    - Spanish-language summary
    """

    # ANSI Color Codes
    COLOR_RED = "\033[91m"
    COLOR_YELLOW = "\033[93m"
    COLOR_BLUE = "\033[94m"
    COLOR_GREEN = "\033[92m"
    COLOR_RESET = "\033[0m"
    COLOR_BOLD = "\033[1m"

    @property
    def name(self) -> str:
        return "console"

    def report(self, violations: List[Violation], total_execution_time_ms: float) -> ReportResult:
        """Processes and prints violations with enhanced formatting."""
        start_time = time.time()
        
        # Group by severity
        severity_order = ["blocking", "warning", "info"]
        grouped = {s: [] for s in severity_order}
        for v in violations:
            if v.severity in grouped:
                grouped[v.severity].append(v)
            else:
                grouped.setdefault(v.severity, []).append(v)

        # Output violations
        if not violations:
            self._write(f"\n{self.COLOR_GREEN}{self.COLOR_BOLD}✅ AXIOM: No se encontraron violaciones.{self.COLOR_RESET}\n")
        else:
            self._write(f"\n{self.COLOR_BOLD}AXIOM: Se encontraron {len(violations)} violaciones en {total_execution_time_ms:.1f}ms.{self.COLOR_RESET}\n")
            
            for severity in severity_order:
                sev_violations = grouped.get(severity, [])
                if not sev_violations:
                    continue
                
                color = self._get_color(severity)
                self._write(f"\n{self.COLOR_BOLD}{color}--- {severity.upper()} ---{self.COLOR_RESET}\n")
                
                for v in sev_violations:
                    line_info = f":{v.line_number}" if v.line_number else ""
                    self._write(f"{color}[{severity.upper()}]{self.COLOR_RESET} {v.file_path}{line_info} - {v.message}\n")
                    
                    if v.detail:
                        self._write(f"  {self.COLOR_BOLD}Detalle:{self.COLOR_RESET} {v.detail}\n")
                    if v.suggestion:
                        self._write(f"  {self.COLOR_BOLD}Sugerencia:{self.COLOR_RESET} {v.suggestion}\n")

        # Summary line
        self._print_summary(grouped)
        
        duration = (time.time() - start_time) * 1000
        return ReportResult(
            success=True,
            reporter_name=self.name,
            execution_time_ms=duration
        )

    def _get_color(self, severity: str) -> str:
        if severity == "blocking":
            return self.COLOR_RED
        elif severity == "warning":
            return self.COLOR_YELLOW
        elif severity == "info":
            return self.COLOR_BLUE
        return self.COLOR_RESET

    def _write(self, msg: str):
        sys.stdout.write(msg)
        sys.stdout.flush()

    def _print_summary(self, grouped: Dict[str, List[Violation]]):
        blocking = len(grouped.get("blocking", []))
        warning = len(grouped.get("warning", []))
        info = len(grouped.get("info", []))
        
        color = self.COLOR_GREEN
        if blocking > 0:
            color = self.COLOR_RED
        elif warning > 0:
            color = self.COLOR_YELLOW
        elif info > 0:
            color = self.COLOR_BLUE
            
        summary = f"\n{color}{self.COLOR_BOLD}Resumen: {blocking} bloqueantes, {warning} advertencias, {info} info.{self.COLOR_RESET}\n"
        self._write(summary)
