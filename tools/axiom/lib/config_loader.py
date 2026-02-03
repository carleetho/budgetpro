import yaml
import json
import jsonschema
import os
import logging

logger = logging.getLogger("AxiomConfigLoader")

class ConfigLoader:
    """
    Handles loading and validation of AXIOM configuration files.
    """
    
    @staticmethod
    def load_config(config_path):
        """
        Loads the YAML configuration file.
        
        Args:
            config_path (str): Path to axiom.config.yaml
            
        Returns:
            dict: The parsed configuration data.
            
        Raises:
            FileNotFoundError: If file doesn't exist.
            yaml.YAMLError: If file contains invalid YAML.
        """
        if not os.path.exists(config_path):
            raise FileNotFoundError(f"Configuration file not found at: {config_path}")
            
        with open(config_path, 'r', encoding='utf-8') as f:
            try:
                data = yaml.safe_load(f)
                return data
            except yaml.YAMLError as e:
                logger.error(f"Failed to parse YAML: {e}")
                raise

    @staticmethod
    def validate_config(config_data, schema_path):
        """
        Validates configuration data against a JSON schema.
        
        Args:
            config_data (dict): The configuration data.
            schema_path (str): Path to the JSON schema file.
            
        Raises:
            jsonschema.ValidationError: If validation fails.
            FileNotFoundError: If schema file missing.
        """
        if not os.path.exists(schema_path):
            raise FileNotFoundError(f"Schema file not found at: {schema_path}")
            
        with open(schema_path, 'r', encoding='utf-8') as f:
            schema = json.load(f)
            
        try:
            jsonschema.validate(instance=config_data, schema=schema)
        except jsonschema.ValidationError as e:
            logger.error(f"Config validation failed: {e.message}")
            raise
