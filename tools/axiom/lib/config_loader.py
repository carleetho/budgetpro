import os
import yaml
import json
import logging
from jsonschema import validate, ValidationError

class ConfigLoader:
    """
    Handles loading and validation of AXIOM configuration files.
    """
    
    def __init__(self):
        self.logger = logging.getLogger(__name__)

    def load_config(self, config_path):
        """
        Load YAML configuration from file.
        
        Args:
            config_path (str): Path to yaml config file
            
        Returns:
            dict: Parsed configuration dictionary
            
        Raises:
            FileNotFoundError: If file doesn't exist
            yaml.YAMLError: If YAML parsing fails
        """
        if not os.path.exists(config_path):
            raise FileNotFoundError(f"Configuration file not found at {config_path}")
            
        with open(config_path, 'r', encoding='utf-8') as f:
            try:
                return yaml.safe_load(f)
            except yaml.YAMLError as e:
                self.logger.error(f"Failed to parse YAML configuration: {e}")
                raise

    def validate_config(self, config, schema_path):
        """
        Validate configuration against JSON schema.
        
        Args:
            config (dict): Configuration dictionary to validate
            schema_path (str): Path to JSON schema file
            
        Returns:
            bool: True if valid
            
        Raises:
            ValidationError: If validation fails
            FileNotFoundError: If schema file missing
        """
        if not os.path.exists(schema_path):
            raise FileNotFoundError(f"Schema file not found at {schema_path}")
            
        with open(schema_path, 'r', encoding='utf-8') as f:
            try:
                schema = json.load(f)
            except json.JSONDecodeError as e:
                self.logger.error(f"Failed to parse JSON schema: {e}")
                raise
                
        try:
            validate(instance=config, schema=schema)
            return True
        except ValidationError as e:
            self.logger.error(f"Configuration validation failed: {e.message}")
            raise
