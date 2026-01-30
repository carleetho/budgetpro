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

    def __init__(self, config: dict):
        super().__init__(config)
        # Internal state to track file backups: original_path -> backup_path
        # The backups dictionary lifecycle is managed by fix(), commit(), and rollback().
        # fix() populates it, while commit() and rollback() clear it after their respective operations.
        self.backups = {}  

    @property
    def name(self) -> str:
        return "simple_fixer"

    def fix(self, violations: List[Violation]) -> FixResult:
        """
        Attempts to fix the provided violations by appending missing entries to files.
        
        Args:
            violations: List of violations to evaluate.
            
        Returns:
            FixResult indicating success, modified files, and execution time.
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

        try:
            for violation in fixable_violations:
                file_path = violation.file_path
                missing_entries = violation.fix_data["missing_entries"]

                if not os.path.exists(file_path):
                    logger.debug(f"Skipping fix for non-existent file: {file_path}")
                    continue

                # Create backup if not already done for this file in this session
                if file_path not in self.backups:
                    # Naming pattern: {original_file}.backup.{timestamp}
                    # This allows identifying related backups while avoiding name collisions.
                    backup_path = f"{file_path}.backup.{int(time.time())}"
                    try:
                        shutil.copy2(file_path, backup_path)
                        self.backups[file_path] = backup_path
                    except (IOError, OSError) as e:
                        logger.error(f"Failed to create backup for {file_path}: {e}")
                        return FixResult(success=False, error_message=f"Backup failed: {e}")

                # Append missing entries
                try:
                    with open(file_path, "a") as f:
                        f.write("\n# Auto-added by AXIOM Sentinel\n")
                        for entry in missing_entries:
                            f.write(f"{entry}\n")
                    fixed_files.add(file_path)
                except (IOError, OSError) as e:
                    logger.error(f"Failed to write to {file_path}: {e}")
                    # Local rollback for current session failure during fix()
                    self.rollback()
                    return FixResult(success=False, error_message=f"Write failed on {file_path}: {e}")

            return FixResult(
                success=True, 
                fixed_files=list(fixed_files), 
                execution_time_ms=(time.time() - start_time) * 1000
            )

        except Exception as e:
            logger.error(f"Unexpected error in SimpleFixer.fix: {e}")
            self.rollback()
            return FixResult(success=False, error_message=str(e))

    def commit(self) -> None:
        """
        Finalizes the fix by deleting backup files created during the fix operation.
        """
        for backup_path in self.backups.values():
            try:
                if os.path.exists(backup_path):
                    os.remove(backup_path)
                    logger.debug(f"Cleaned up backup file: {backup_path}")
            except (IOError, OSError) as e:
                logger.warning(f"Failed to clean up backup {backup_path}: {e}")
        
        self.backups.clear()

    def rollback(self) -> None:
        """
        Reverts any file modifications by restoring from backups and then cleaning them up.
        """
        if not self.backups:
            logger.debug("No backups to rollback in SimpleFixer.")
            return

        for original_path, backup_path in self.backups.items():
            try:
                if os.path.exists(backup_path):
                    shutil.move(backup_path, original_path)
                    logger.info(f"Rolled back {original_path} from {backup_path}")
            except (IOError, OSError, shutil.Error) as e:
                logger.error(f"CRITICAL: Failed to rollback {original_path} from {backup_path}: {e}")
        
        self.backups.clear()
