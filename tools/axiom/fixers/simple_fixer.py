import os
import time
import shutil
import logging
from typing import List
from tools.axiom.fixers.base_fixer import BaseFixer, FixResult
from tools.axiom.validators.base_validator import Violation

logger = logging.getLogger(__name__)

class SimpleFixer(BaseFixer):
    """
    Implementation of BaseFixer that handles simple file-append corrections.
    Currently focused on .gitignore improvements.
    """

    @property
    def name(self) -> str:
        return "simple_fixer"

    def fix(self, violations: List[Violation]) -> FixResult:
        """
        Attempts to fix the provided violations by appending missing entries to files.
        """
        start_time = time.time()
        
        if not self.config.get("enabled", False):
            return FixResult(success=True, execution_time_ms=0.0)

        fixable_violations = [
            v for v in violations 
            if v.auto_fixable and v.severity != "blocking" and v.fix_data and "missing_entries" in v.fix_data
        ]

        if not fixable_violations:
            return FixResult(success=True, execution_time_ms=(time.time() - start_time) * 1000)

        fixed_files = set()
        backups = {}

        try:
            for violation in fixable_violations:
                file_path = violation.file_path
                missing_entries = violation.fix_data["missing_entries"]

                if not os.path.exists(file_path):
                    # For .gitignore, we might want to create it if missing, 
                    # but current logic assumes it exists or is being reported as missing.
                    # Base on Task 2, it marks .gitignore as missing (CRITICAL/Blocking)
                    # and MISSING EXCLUSION (HIGH/Warning). 
                    # Blocking is skipped, so we only handle existing .gitignore here.
                    continue

                # Create backup if not already done for this file
                if file_path not in backups:
                    backup_path = f"{file_path}.backup.{int(time.time())}"
                    try:
                        shutil.copy2(file_path, backup_path)
                        backups[file_path] = backup_path
                    except Exception as e:
                        logger.error(f"Failed to create backup for {file_path}: {e}")
                        return FixResult(success=False, error_message=f"Backup failed: {e}")

                # Append missing entries
                try:
                    with open(file_path, "a") as f:
                        f.write("\n# Auto-added by AXIOM Sentinel\n")
                        for entry in missing_entries:
                            f.write(f"{entry}\n")
                    fixed_files.add(file_path)
                except Exception as e:
                    logger.error(f"Failed to write to {file_path}: {e}")
                    # Rollback all changes if any write fails
                    self._rollback(backups)
                    return FixResult(success=False, error_message=f"Write failed on {file_path}: {e}")

            return FixResult(
                success=True, 
                fixed_files=list(fixed_files), 
                execution_time_ms=(time.time() - start_time) * 1000
            )

        except Exception as e:
            logger.error(f"Unexpected error in SimpleFixer: {e}")
            self._rollback(backups)
            return FixResult(success=False, error_message=str(e))

    def _rollback(self, backups: dict):
        """Restores files from backups."""
        for original_path, backup_path in backups.items():
            try:
                shutil.move(backup_path, original_path)
                logger.info(f"Rolled back {original_path} from {backup_path}")
            except Exception as e:
                logger.error(f"FAILED ROLLBACK for {original_path}: {e}")
