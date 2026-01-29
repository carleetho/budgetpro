import unittest
import os
import tempfile
import yaml
from unittest.mock import patch
from tools.axiom.config_loader import load_axiom_config, reset_config_cache, ConfigurationError

class TestIntegrationConfig(unittest.TestCase):
    
    def setUp(self):
        reset_config_cache()
        self.test_dir = tempfile.TemporaryDirectory()
        self.config_path = os.path.join(self.test_dir.name, "axiom.config.yaml")

    def tearDown(self):
        self.test_dir.cleanup()
        reset_config_cache()

    def create_config(self, data):
        with open(self.config_path, "w") as f:
            yaml.dump(data, f)
        return self.config_path

    def test_full_workflow_valid_config(self):
        """Test loading a complete valid configuration file"""
        config_data = {
            "protection_zones": {
                "red": [{"path": "domain/core", "max_files": 1}],
                "green": [{"path": "tests", "max_files": 20}]
            },
            "validators": {
                "blast_radius": {"enabled": True, "threshold": 15, "strictness": "hybrid"}
            },
            "reporters": {
                "console": {"enabled": True},
                "log_file": {"enabled": False}
            }
        }
        self.create_config(config_data)
        
        config = load_axiom_config(self.config_path)
        
        self.assertEqual(config.protection_zones["red"][0]["max_files"], 1)
        self.assertEqual(config.protection_zones["green"][0]["max_files"], 20)
        self.assertEqual(config.validators["blast_radius"]["strictness"], "hybrid")
        self.assertTrue(config.reporters["console"]["enabled"])
        self.assertFalse(config.reporters["log_file"]["enabled"])

    def test_missing_file_workflow(self):
        """Test workflow when configuration file is missing"""
        config_path = os.path.join(self.test_dir.name, "does_not_exist.yaml")
        config = load_axiom_config(config_path)
        
        # Should have defaults
        self.assertIn("red", config.protection_zones)
        self.assertTrue(config.reporters["console"]["enabled"])

    def test_partial_config_with_defaults(self):
        """Test partial configuration merging with defaults"""
        # File only overrides blast radius threshold
        config_data = {
            "validators": {
                "blast_radius": {"enabled": True, "threshold": 50, "strictness": "blocking"}
            }
        }
        self.create_config(config_data)
        
        config = load_axiom_config(self.config_path)
        
        # Overridden value
        self.assertEqual(config.validators["blast_radius"]["threshold"], 50)
        # Default value preserved (protection zones)
        self.assertIn("red", config.protection_zones)

    def test_data_integrity_overlap_error(self):
        """Test error when protection zones overlap"""
        config_data = {
            "protection_zones": {
                "red": [{"path": "domain", "max_files": 1}],
                "yellow": [{"path": "domain/submodule", "max_files": 5}]
            },
            "reporters": {"console": {"enabled": True}}
        }
        self.create_config(config_data)
        
        with self.assertRaisesRegex(ConfigurationError, "Overlapping protection zones"):
            load_axiom_config(self.config_path)

    def test_data_integrity_no_reporters_error(self):
        """Test error when no reporters are enabled"""
        config_data = {
            "reporters": {
                "console": {"enabled": False},
                "log_file": {"enabled": False},
                "metrics": {"enabled": False}
            }
        }
        self.create_config(config_data)
        
        with self.assertRaisesRegex(ConfigurationError, "At least one reporter must be enabled"):
            load_axiom_config(self.config_path)

    @patch.dict(os.environ, {"AXIOM_ENV": "production", "LOG_LEVEL": "INFO"})
    def test_env_var_interpolation_workflow(self):
        """Test environment variable interpolation in integration scenario"""
        config_data = {
            "reporters": {
                "log_file": {"enabled": True, "path": "/var/log/${AXIOM_ENV}/axiom.log"}
            }
        }
        self.create_config(config_data)
        
        config = load_axiom_config(self.config_path)
        self.assertEqual(config.reporters["log_file"]["path"], "/var/log/production/axiom.log")

    def test_config_reload(self):
        """Test reloading configuration after file change"""
        # 1. Initial Config
        self.create_config({"validators": {"blast_radius": {"enabled": True, "threshold": 10}}})
        config1 = load_axiom_config(self.config_path)
        self.assertEqual(config1.validators["blast_radius"]["threshold"], 10)
        
        # 2. Update File
        reset_config_cache()
        self.create_config({"validators": {"blast_radius": {"enabled": True, "threshold": 99}}})
        
        # 3. Reload
        config2 = load_axiom_config(self.config_path)
        self.assertEqual(config2.validators["blast_radius"]["threshold"], 99)

if __name__ == '__main__':
    unittest.main()
