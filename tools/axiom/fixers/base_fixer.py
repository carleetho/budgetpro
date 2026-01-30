from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field
from abc import ABC, abstractmethod
from tools.axiom.validators.base_validator import Violation

@dataclass(frozen=True)
class FixResult:
    """Container for auto-fix execution results and metadata."""
    success: bool
    fixed_files: List[str] = field(default_factory=list)
    execution_time_ms: float = 0.0
    error_message: Optional[str] = None

class BaseFixer(ABC):
    """
    Abstract base class for all AXIOM auto-fixers.
    
    Fixers are responsible for automatically remediating violations
    detected by validators. They typically modify files in place.
    """
    
    def __init__(self, config: Dict[str, Any]):
        """
        Initialize the fixer with its specific configuration.
        
        Args:
            config: Dictionary containing fixer-specific settings
        """
        self.config = config

    @property
    @abstractmethod
    def name(self) -> str:
        """Returns the unique identifier for this fixer."""
        pass

    @abstractmethod
    def fix(self, violations: List[Violation]) -> FixResult:
        """
        Attempts to fix the provided violations.
        
        Args:
            violations: List of violations detected by validators that might be fixable.
            
        Returns:
            FixResult containing success status, list of modified files, and metadata.
        """
        pass

    @abstractmethod
    def commit(self) -> None:
        """
        Finalizes the fix operation. 
        Should be called if the fix is accepted (e.g., re-validation passed).
        Typical actions include deleting backup files.
        """
        pass

    @abstractmethod
    def rollback(self) -> None:
        """
        Reverts any changes made during the fix operation.
        Should be called if the fix is rejected (e.g., re-validation failed).
        Typical actions include restoring files from backups.
        """
        pass
