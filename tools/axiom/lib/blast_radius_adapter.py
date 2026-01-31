import logging
import re
from typing import List, Dict, Any, Optional
from dataclasses import dataclass, field

@dataclass
class ZoneConfig:
    path: str
    max_files: int

@dataclass
class BlastRadiusConfig:
    enabled: bool = True
    threshold: int = 10
    strictness: str = "blocking"

class BlastRadiusAdapter:
    """
    Adapter logic for checking Blast Radius limits based on Protection Zones.
    Separates the logic from the validator wrapper.
    """
    
    def __init__(self, config: Any):
        """
        Args:
            config: Full AxiomConfig object (or dict behaving like one)
        """
        self.config = config
        self.logger = logging.getLogger("BlastRadiusAdapter")
        
    def check_limits(self, files: List[str]) -> List[Dict[str, Any]]:
        """
        Checks file counts against Global Threshold and Zone Limits.
        Returns a list of violation details (dict).
        """
        violations = []
        
        # 1. Global Threshold Check
        global_threshold = 10
        strictness = "blocking"
        if hasattr(self.config, 'validators'):
             # Handle config object vs dict
             val_conf = self.config.validators if isinstance(self.config.validators, dict) else {}
             br_conf = val_conf.get("blast_radius", {})
             global_threshold = br_conf.get("threshold", 10)
             strictness = br_conf.get("strictness", "blocking")
        
        if len(files) > global_threshold:
            violations.append({
                "message": f"BLAST RADIUS: Too many files changed ({len(files)} > {global_threshold}). Break this PR down.",
                "severity": strictness,
                "file_path": "Global Check" 
            })
            
        # 2. Zone Limits Check
        zones = self._load_zones()
        
        # Count files per zone type (red, yellow, green)
        # Actually, limits are per zone definition?
        # Usually checking if *any* file violates its zone limit?
        # Wait. "max_files: 1" in Red Zone means strict limit on number of Red Zone files in one commit?
        # Yes.
        
        red_files = []
        yellow_files = []
        green_files = []
        
        for f in files:
            # Check Red
            if self._is_in_zone(f, zones.get("red", [])):
                red_files.append(f)
            # Check Yellow
            elif self._is_in_zone(f, zones.get("yellow", [])):
                yellow_files.append(f)
            # Check Green
            elif self._is_in_zone(f, zones.get("green", [])):
                green_files.append(f)
                
        # Access config for limits (defaults if missing)
        # We need to find the specific limit for the matched zone if checking per-path?
        # A simplified approach: predefined limits for Red/Yellow/Green categories based on config?
        # Config structure:
        # protection_zones:
        #   red: [ {path: "...", max_files: 1}, ... ]
        
        # Iterate defined zones and check if specific zone constraint matches?
        # Or aggregate by color?
        # "Limits the number of files that can be changed per commit in these zones"
        
        # Let's check max_files from the FIRST matching zone for each color.
        # Since config defines `max_files` per path entry, we could have different limits.
        # But usually consistent per color.
        
        # Check Red
        self._check_zone_limit(red_files, zones.get("red", []), violations, "Red", "blocking")
        
        # Check Yellow
        self._check_zone_limit(yellow_files, zones.get("yellow", []), violations, "Yellow", "warning") # Yellow usually warning or less strict
        
        # Check Green
        self._check_zone_limit(green_files, zones.get("green", []), violations, "Green", "info")
        
        return violations

    def _load_zones(self) -> Dict[str, List[Dict]]:
        """Extracts zone definitions from config."""
        if hasattr(self.config, 'protection_zones'):
            return self.config.protection_zones if isinstance(self.config.protection_zones, dict) else {}
        return {}

    def _is_in_zone(self, file_path: str, zone_list: List[Dict]) -> bool:
        if not zone_list:
            return False
            
        for zone in zone_list:
            path_pattern = zone.get("path", "")
            if path_pattern and (path_pattern in file_path or file_path.startswith(path_pattern)):
                return True
        return False
        
    def _check_zone_limit(self, matched_files: List[str], zone_list: List[Dict], violations: List[Dict], zone_name: str, default_severity: str):
        if not matched_files or not zone_list:
            return
            
        # Determine strict limit from config (take the minimum limit found in matched zones or first?)
        # Let's verify against the limit of the first matched zone for valid files?
        # Or simply use the configured limit from the first entry of that color (assuming consistency).
        limit = zone_list[0].get("max_files", 999) 
        
        # Also could be overridden by specific path limit.
        # Ideally we map file -> limit.
        # But if multiple files, which limit applies? The strictest?
        # Let's assume uniform limit per color for simplicity unless config differs.
        
        if len(matched_files) > limit:
            violations.append({
                "message": f"ZONE LIMIT: {zone_name} Zone limit exceeded ({len(matched_files)} > {limit}). Files: {', '.join(matched_files[:3])}...",
                "severity": "blocking" if zone_name == "Red" else default_severity,
                "file_path": matched_files[0] # Blaming first file
            })
