from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field
from abc import ABC, abstractmethod

@dataclass(frozen=True)
class Violation:
    """
    Represents a single validation rule violation.
    
    Attributes:
        file_path: Absolute path to the file containing the violation.
        message: Concise summary of the violation.
        severity: Level of severity ('blocking', 'warning', 'info').
        validator_name: Name of the validator that identified the violation.
        line_number: Optional line number where the violation occurs.
        detail: Optional extended explanation of why this is a violation.
        suggestion: Optional actionable guidance on how to fix the violation.
    """
    file_path: str
    message: str
    severity: str  # 'blocking', 'warning', 'info'
    validator_name: str
    line_number: Optional[int] = None
    detail: Optional[str] = None
    suggestion: Optional[str] = None

@dataclass(frozen=True)
class ValidationResult:
    """Container for all violations found by a single validator."""
    validator_name: str
    violations: List[Violation] = field(default_factory=list)
    execution_time_ms: float = 0.0
    success: bool = True

class BaseValidator(ABC):
    """
    Abstract base class for all AXIOM validators.
    
    All concrete validators must implement the validate() method and
    initialize with a configuration dictionary.
    """
    
    def __init__(self, config: Dict[str, Any]):
        """
        Initialize the validator with its specific configuration.
        
        Args:
            config: Dictionary containing validator-specific settings
                    (e.g., from axiom_config.yaml)
        """
        self.config = config

    @property
    @abstractmethod
    def name(self) -> str:
        """Returns the unique identifier for this validator."""
        pass

    @abstractmethod
    def validate(self, files: List[str]) -> ValidationResult:
        """
        Executes the validation logic against a set of files.
        
        Args:
            files: List of absolute file paths to validate.
            
        Returns:
            ValidationResult containing detected violations and metadata.
        """
        pass
