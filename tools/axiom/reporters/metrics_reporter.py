import os
import json
import time
import sys
from datetime import datetime, timezone
from typing import List, Dict, Any, Optional
from tools.axiom.reporters.base_reporter import BaseReporter, ReportResult
from tools.axiom.validators.base_validator import Violation

class MetricsReporter(BaseReporter):
    """
    Reporter that aggregates validation statistics over time.
    
    Features:
    - Maintains last 100 runs in history.
    - Calculates total runs, pass rate, and averages.
    - Identifies most common issues (validators/modules).
    - Calculates quality trends (improving/degrading/stable).
    - Ensures integrity via atomic writes.
    """

    def __init__(self, config: Dict[str, Any]):
        super().__init__(config)
        self.metrics_path = config.get("path", ".budgetpro/metrics.json")
        self.max_history = 100

    @property
    def name(self) -> str:
        return "metrics"

    def report(self, violations: List[Violation], total_execution_time_ms: float) -> ReportResult:
        """Processes and aggregates metrics from the current run."""
        start_time = time.time()
        
        try:
            # Load existing metrics
            metrics_data = self._load_metrics()
            
            # Create new entry
            entry = self._build_history_entry(violations, total_execution_time_ms)
            
            # Update history
            history = metrics_data.get("history", [])
            history.append(entry)
            
            # Enforce limit
            if len(history) > self.max_history:
                history = history[-self.max_history:]
            
            metrics_data["history"] = history
            
            # Calculate statistics and trends
            metrics_data["statistics"] = self._calculate_statistics(history)
            
            # Atomic write
            self._save_metrics(metrics_data)

        except Exception as e:
            sys.stderr.write(f"Error updating metrics at {self.metrics_path}: {str(e)}\n")
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

    def _load_metrics(self) -> Dict[str, Any]:
        """Loads metrics from file, or returns default structure if missing/invalid."""
        if not os.path.exists(self.metrics_path):
            return {"history": [], "statistics": {}}
        
        try:
            with open(self.metrics_path, "r", encoding="utf-8") as f:
                return json.load(f)
        except (json.JSONDecodeError, IOError) as e:
            sys.stderr.write(f"Warning: Could not read metrics file {self.metrics_path}. Starting fresh. Error: {str(e)}\n")
            return {"history": [], "statistics": {}}

    def _save_metrics(self, data: Dict[str, Any]):
        """Saves metrics using an atomic write pattern."""
        log_dir = os.path.dirname(self.metrics_path)
        if log_dir:
            os.makedirs(log_dir, exist_ok=True)
            
        temp_path = f"{self.metrics_path}.tmp"
        with open(temp_path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
            f.flush()
            os.fsync(f.fileno())
            
        # Atomic rename
        os.replace(temp_path, self.metrics_path)

    def _build_history_entry(self, violations: List[Violation], total_execution_time_ms: float) -> Dict[str, Any]:
        """Creates a single history record from current violations."""
        by_severity = {}
        by_validator = {}
        by_module = {}
        
        for v in violations:
            by_severity[v.severity] = by_severity.get(v.severity, 0) + 1
            by_validator[v.validator_name] = by_validator.get(v.validator_name, 0) + 1
            
            module = self._extract_module(v.file_path)
            by_module[module] = by_module.get(module, 0) + 1
            
        return {
            "timestamp": datetime.now(timezone.utc).isoformat() + "Z",
            "execution_time_ms": total_execution_time_ms,
            "total_violations": len(violations),
            "by_severity": by_severity,
            "by_validator": by_validator,
            "by_module": by_module
        }

    def _extract_module(self, file_path: str) -> str:
        """Extracts the first directory component as module name."""
        file_path = file_path.replace("\\", "/") # Normalize separators
        parts = file_path.lstrip("/").split("/")
        return parts[0] if len(parts) > 1 else "root"

    def _calculate_statistics(self, history: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Aggregates historical data into high-level statistics."""
        if not history:
            return {}
            
        total_runs = len(history)
        total_violations_cumulative = sum(h["total_violations"] for h in history)
        runs_with_blocking = sum(1 for h in history if h["by_severity"].get("blocking", 0) > 0)
        
        # Aggregations for most common issues
        all_validators = {}
        all_modules = {}
        for h in history:
            for val, count in h["by_validator"].items():
                all_validators[val] = all_validators.get(val, 0) + count
            for mod, count in h["by_module"].items():
                all_modules[mod] = all_modules.get(mod, 0) + count
                
        most_common_validator = max(all_validators, key=all_validators.get) if all_validators else None
        most_problematic_module = max(all_modules, key=all_modules.get) if all_modules else None
        
        return {
            "total_runs": total_runs,
            "avg_violations_per_run": round(total_violations_cumulative / total_runs, 2),
            "pass_rate": round((total_runs - runs_with_blocking) / total_runs * 100, 1),
            "most_common_validator": most_common_validator,
            "most_problematic_module": most_problematic_module,
            "trend": self._calculate_trend(history)
        }

    def _calculate_trend(self, history: List[Dict[str, Any]]) -> str:
        """Compares moving averages to identify quality trends."""
        if len(history) < 2:
            return "stable"
            
        # Compare last 5 runs with up to 5 runs before that
        last_n = 5
        recent_history = history[-last_n:]
        previous_history = history[-(last_n*2):-last_n]
        
        if not previous_history:
            return "stable"
            
        avg_recent = sum(h["total_violations"] for h in recent_history) / len(recent_history)
        avg_previous = sum(h["total_violations"] for h in previous_history) / len(previous_history)
        
        if avg_previous == 0:
            return "degrading" if avg_recent > 0 else "stable"
            
        change_pct = (avg_recent - avg_previous) / avg_previous
        
        if change_pct < -0.10:
            return "improving"
        elif change_pct > 0.10:
            return "degrading"
        else:
            return "stable"
