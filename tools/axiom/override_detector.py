from typing import Dict, Any, Set, List
from dataclasses import dataclass, field
from tools.axiom.config_loader import AxiomConfig

@dataclass
class OverrideResult:
    bypass_zones: Set[str] = field(default_factory=set)
    bypass_blast_radius: bool = False

def detect_overrides(commit_message: str, config: AxiomConfig = None) -> OverrideResult:
    """
    Scans the commit message for override keywords and returns the detected bypasses.
    
    Args:
        commit_message: The commit message to scan.
        config: Optional AxiomConfig. Can be used for dynamic keyword configuration in the future.
                Currently used to ensure function signature anticipates config usage.
    
    Returns:
        OverrideResult containing the set of zones to bypass and blast radius bypass flag.
    """
    if not commit_message:
        return OverrideResult()

    message_upper = commit_message.upper()
    result = OverrideResult()

    # Domain specific overrides
    if "OVERRIDE_ESTIMACION" in message_upper:
        result.bypass_zones.add("domain/estimacion")
    
    if "OVERRIDE_PRESUPUESTO" in message_upper:
        result.bypass_zones.add("domain/presupuesto")
        
    if "OVERRIDE_DOMAIN_CORE" in message_upper:
        result.bypass_zones.add("domain/valueobjects")
        result.bypass_zones.add("domain/entities")

    # Global overrides
    if "BIGBANG_APPROVED" in message_upper:
        result.bypass_blast_radius = True
        
    return result
