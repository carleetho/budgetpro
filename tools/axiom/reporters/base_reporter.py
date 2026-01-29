from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from abc import ABC, abstractmethod
from tools.axiom.validators.base_validator import Violation

@dataclass(frozen=True)
class ReportResult:
    """Container for reporter execution results and metadata."""
    success: bool
    reporter_name: str
    execution_time_ms: float
    error_message: Optional[str] = None

class BaseReporter(ABC):
    """
    Abstract base class for all AXIOM reporters.
    
    Reporters take validation findings and output them to various sinks
    (stdout, file, GitHub PR, etc.). They should be designed to execute
    independently and in parallel.
    """
    
    def __init__(self, config: Dict[str, Any]):
        """
        Initialize the reporter with its specific configuration.
        
        Args:
            config: Dictionary containing reporter-specific settings
        """
        self.config = config

    @property
    @abstractmethod
    def name(self) -> str:
        """Returns the unique identifier for this reporter."""
        pass

    @abstractmethod
    def report(self, violations: List[Violation], total_execution_time_ms: float) -> ReportResult:
        """
        Processes and outputs the detected violations.
        
        Args:
            violations: List of violations detected by validators.
            total_execution_time_ms: Time taken by the validation phase.
            
        Returns:
            ReportResult containing reporting status and metadata.
        """
        pass
