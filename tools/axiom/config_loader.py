import os
import re
import yaml
import logging
from typing import Dict, Any, List, Optional
from dataclasses import dataclass, field

# Use environment variable for config path or default to project root
DEFAULT_CONFIG_PATH = "axiom.config.yaml"

# Default configuration structure
DEFAULT_CONFIG = {
    "protection_zones": {
        "red": [
            {"path": "domain/presupuesto", "max_files": 1},
            {"path": "domain/estimacion", "max_files": 1},
            {"path": "domain/valueobjects", "max_files": 1},
            {"path": "domain/entities", "max_files": 1}
        ],
        "yellow": [
            {"path": "infrastructure/persistence", "max_files": 3}
        ],
        "green": [
            {"path": "application", "max_files": 10},
            {"path": "infrastructure/web", "max_files": 10},
            {"path": "tests", "max_files": 10}
        ]
    },
    "validators": {
        "blast_radius": {
            "enabled": True,
            "threshold": 10,
            "strictness": "blocking"
        }
    },
    "reporters": {
        "console": {"enabled": True},
        "log_file": {"enabled": True, "path": ".budgetpro/validation.log"},
        "metrics": {"enabled": True, "path": ".budgetpro/metrics.json"}
    },
    "auto_fix": {
        "enabled": False,
        "safe_only": True
    },
    "overrides": {
        "enabled_keywords": []
    }
}

class ConfigurationError(Exception):
    """Raised when configuration validation fails."""
    pass

@dataclass
class AxiomConfig:
    protection_zones: Dict[str, List[Dict[str, Any]]] = field(default_factory=dict)
    validators: Dict[str, Any] = field(default_factory=dict)
    reporters: Dict[str, Any] = field(default_factory=dict)
    auto_fix: Dict[str, Any] = field(default_factory=dict)
    overrides: Dict[str, Any] = field(default_factory=dict)

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> 'AxiomConfig':
        return cls(
            protection_zones=data.get("protection_zones", {}),
            validators=data.get("validators", {}),
            reporters=data.get("reporters", {}),
            auto_fix=data.get("auto_fix", {}),
            overrides=data.get("overrides", {})
        )

# Module-level cache for the configuration singleton
_config_cache: Optional[AxiomConfig] = None

def _interpolate_env_vars(value: Any) -> Any:
    """Recursively replaces ${VAR} with environment variable values."""
    if isinstance(value, str):
        # Regex to find ${VAR_NAME}
        pattern = re.compile(r'\$\{([A-Z_][A-Z0-9_]*)\}')
        
        def replace(match):
            var_name = match.group(1)
            return os.environ.get(var_name, match.group(0)) # Return original if not found
        
        return pattern.sub(replace, value)
    
    elif isinstance(value, dict):
        return {k: _interpolate_env_vars(v) for k, v in value.items()}
    
    elif isinstance(value, list):
        return [_interpolate_env_vars(v) for v in value]
    
    return value

def _validate_config(config: Dict[str, Any]) -> None:
    """Validates configuration schema and logical rules."""
    
    # 1. Protection Zones
    zones = config.get("protection_zones", {})
    if not isinstance(zones, dict):
        raise ConfigurationError("protection_zones must be a dictionary")
        
    all_paths = []
    for zone_name in ["red", "yellow", "green"]:
        if zone_name in zones:
            if not isinstance(zones[zone_name], list):
                raise ConfigurationError(f"protection_zones.{zone_name} must be a list")
            for item in zones[zone_name]:
                if "path" not in item:
                    raise ConfigurationError(f"Missing 'path' in protection_zones.{zone_name}")
                if "max_files" not in item:
                    raise ConfigurationError(f"Missing 'max_files' in protection_zones.{zone_name}")
                if not isinstance(item["max_files"], int) or item["max_files"] < 0:
                     raise ConfigurationError(f"Invalid max_files for {item['path']}: must be a positive integer")
                path = item["path"]
                if path.startswith("/"):
                     raise ConfigurationError(f"Invalid path {path}: Zone paths must be relative (no leading /)")
                
                # Check for overlaps
                # A path overlaps if it is a prefix of another or equal to another (excluding itself effectively, but list might have duplicates)
                # We interpret "path" as directory path. "domain" overlaps "domain/core".
                # To be precise, we need to handle path separators. "dom" does not overlap "domain".
                normalized_path = path.rstrip("/")
                all_paths.append(normalized_path)

    # Check for overlaps O(N^2) is fine for small N
    for i in range(len(all_paths)):
        for j in range(len(all_paths)):
            if i == j:
                continue
            p1 = all_paths[i]
            p2 = all_paths[j]
            # Check if p1 is a parent of p2 or same
            # Add implicit slash to ensure directory matching
            if p1 == p2 or p2.startswith(p1 + "/"):
                raise ConfigurationError(f"Overlapping protection zones: '{p1}' overlaps with '{p2}'")

    # 2. Validators
    validators = config.get("validators", {})
    blast_radius = validators.get("blast_radius", {})
    if blast_radius.get("enabled"):
        threshold = blast_radius.get("threshold")
        if not isinstance(threshold, int) or threshold < 0:
            raise ConfigurationError("blast_radius.threshold must be a positive integer")
        
        strictness = blast_radius.get("strictness")
        valid_strictness = ["blocking", "warning", "hybrid"]
        if strictness not in valid_strictness:
            raise ConfigurationError(f"blast_radius.strictness must be one of {valid_strictness}")

    # 3. Reporters
    reporters = config.get("reporters", {})
    if not any(r.get("enabled", False) for r in reporters.values()):
        raise ConfigurationError("At least one reporter must be enabled")

def _deep_merge(base: Dict[str, Any], update: Dict[str, Any]) -> Dict[str, Any]:
    """Recursively merges update dict into base dict."""
    for key, value in update.items():
        if isinstance(value, dict) and key in base and isinstance(base[key], dict):
            _deep_merge(base[key], value)
        else:
            base[key] = value
    return base

def load_axiom_config(config_path: str = DEFAULT_CONFIG_PATH) -> AxiomConfig:
    """
    Loads, interpolates, validates, and caches the AXIOM configuration.
    Falls back to defaults if file is missing, but logs a warning.
    """
    global _config_cache
    if _config_cache:
        return _config_cache

    # Start with a deep copy of defaults to ensure we don't mutate the constant
    import copy
    config_data = copy.deepcopy(DEFAULT_CONFIG)
    
    # Try to load from file
    if os.path.exists(config_path):
        try:
            with open(config_path, 'r') as f:
                file_data = yaml.safe_load(f)
                if file_data:
                    # Perform deep merge so partial configs work
                    _deep_merge(config_data, file_data)
        except yaml.YAMLError as e:
            raise ConfigurationError(f"Error parsing YAML config: {e}")
    else:
        logging.warning(f"Configuration file not found at {config_path}. Using default configuration.")

    # Interpolate Env Vars
    config_data = _interpolate_env_vars(config_data)

    # Validate
    _validate_config(config_data)

    # Convert to Object and Cache
    _config_cache = AxiomConfig.from_dict(config_data)
    return _config_cache

def reset_config_cache():
    """Resets the singleton cache. Useful for testing."""
    global _config_cache
    _config_cache = None
