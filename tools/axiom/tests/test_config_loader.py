import unittest
import os
import tempfile
import yaml
from unittest.mock import patch
from tools.axiom.config_loader import load_axiom_config, reset_config_cache, ConfigurationError, AxiomConfig

class TestAxiomConfigLoader(unittest.TestCase):
    
    def setUp(self):
        reset_config_cache()
        self.test_dir = tempfile.TemporaryDirectory()
        self.config_path = os.path.join(self.test_dir.name, "axiom.config.yaml")

    def tearDown(self):
        self.test_dir.cleanup()
        reset_config_cache()

    def create_config_file(self, content):
        with open(self.config_path, "w") as f:
            yaml.dump(content, f)
        return self.config_path

    def test_load_default_config_missing_file(self):
        """Test loading when config file does not exist (should return defaults)"""
        # Ensure path points to non-existent file
        non_existent_path = os.path.join(self.test_dir.name, "non_existent.yaml")
        
        config = load_axiom_config(non_existent_path)
        
        self.assertIsInstance(config, AxiomConfig)
        self.assertIn("red", config.protection_zones)
        self.assertEqual(config.validators["blast_radius"]["threshold"], 10)

    def test_load_valid_config_from_file(self):
        """Test loading a valid configuration file"""
        config_data = {
            "protection_zones": {
                "red": [{"path": "domain/core", "max_files": 2}]
            },
            "validators": {
                "blast_radius": {"enabled": True, "threshold": 20, "strictness": "warning"}
            }
        }
        self.create_config_file(config_data)
        
        config = load_axiom_config(self.config_path)
        
        self.assertEqual(config.protection_zones["red"][0]["max_files"], 2)
        self.assertEqual(config.validators["blast_radius"]["threshold"], 20)
        self.assertEqual(config.validators["blast_radius"]["strictness"], "warning")

    def test_invalid_yaml_syntax(self):
        """Test syntax error in YAML file"""
        with open(self.config_path, "w") as f:
            f.write("protection_zones:\n  red: [ unclosed_list")
            
        with self.assertRaises(ConfigurationError):
            load_axiom_config(self.config_path)

    def test_invalid_schema_negative_max_files(self):
        """Test validation error for negative max_files"""
        config_data = {
            "protection_zones": {
                "red": [{"path": "domain/core", "max_files": -1}]
            }
        }
        self.create_config_file(config_data)
        
        with self.assertRaisesRegex(ConfigurationError, "must be a positive integer"):
            load_axiom_config(self.config_path)

    def test_invalid_schema_absolute_path(self):
        """Test validation error for absolute paths in zones"""
        config_data = {
            "protection_zones": {
                "red": [{"path": "/absolute/path", "max_files": 1}]
            }
        }
        self.create_config_file(config_data)
        
        with self.assertRaisesRegex(ConfigurationError, "Zone paths must be relative"):
            load_axiom_config(self.config_path)

    def test_invalid_strictness_value(self):
        """Test validation error for invalid strictness"""
        config_data = {
            "validators": {
                "blast_radius": {"enabled": True, "threshold": 10, "strictness": "invalid_mode"}
            }
        }
        self.create_config_file(config_data)
        
        with self.assertRaisesRegex(ConfigurationError, "strictness must be one of"):
            load_axiom_config(self.config_path)

    @patch.dict(os.environ, {"LOG_PATH": ".custom/logs/axiom.log", "MAX_FILES": "5"})
    def test_env_var_interpolation(self):
        """Test environment variable interpolation"""
        # Note: Integer fields in YAML loaded as string if using ${VAR} unless explicitly handled or coerced.
        # But for strings like paths it should work directly.
        # For integers, if written as string "${VAR}" in YAML, it loads as string. 
        # The loader currently does not auto-cast types after interpolation, so let's test string interpolation first.
        
        config_data = {
            "reporters": {
                "log_file": {"enabled": True, "path": "${LOG_PATH}"}
            }
        }
        self.create_config_file(config_data)
        
        config = load_axiom_config(self.config_path)
        self.assertEqual(config.reporters["log_file"]["path"], ".custom/logs/axiom.log")

    def test_partial_override(self):
        """Test that file overrides defaults (partial override is basically dict update)"""
        # Since we use config_data.update(file_data), keys present in file replace default keys.
        # Nested dicts might be wiped if not careful, but for this simple version we check top level keys.
        
        # Default has red, yellow, green zones. If we provide only 'red' in file:
        config_data = {
            "protection_zones": {
                "red": [{"path": "new/red", "max_files": 1}]
            }
        }
        self.create_config_file(config_data)
        config = load_axiom_config(self.config_path)
        
        # New red zone
        self.assertEqual(config.protection_zones["red"][0]["path"], "new/red")
        # With deep merge, 'yellow' zone from default persists because key is not present in file.
        self.assertIn("yellow", config.protection_zones)

if __name__ == '__main__':
    unittest.main()
