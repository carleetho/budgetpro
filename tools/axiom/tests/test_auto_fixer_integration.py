import unittest
import os
import shutil
import tempfile
import yaml
import logging
from unittest.mock import patch, MagicMock
from tools.axiom.axiom_sentinel import AxiomSentinel
from tools.axiom.validators.base_validator import Violation, ValidationResult
from tools.axiom.config_loader import reset_config_cache

class TestAutoFixerIntegration(unittest.TestCase):
    def setUp(self):
        reset_config_cache()
        self.test_dir = tempfile.mkdtemp()
        self.old_cwd = os.getcwd()
        os.chdir(self.test_dir)
        
        # Initialize a basic git repo structure
        os.makedirs(".git")
        
        # Create a sample .gitignore
        self.gitignore_path = ".gitignore"
        with open(self.gitignore_path, "w") as f:
            f.write("node_modules\ntarget\n")
            
        # Create a basic axiom config
        self.config_path = "axiom.config.yaml"
        self.default_config = {
            "auto_fix": {
                "enabled": True,
                "safe_only": False
            },
            "validators": {
                "security_validator": {
                    "enabled": True,
                    "required_gitignore_entries": [".env"]
                }
            },
            "reporters": {
                "console": {"enabled": False},
                "log_file": {"enabled": False},
                "metrics": {"enabled": False}
            }
        }
        with open(self.config_path, "w") as f:
            yaml.dump(self.default_config, f)

        # Silence logging for tests
        logging.getLogger("AxiomSentinel").setLevel(logging.CRITICAL)

    def tearDown(self):
        os.chdir(self.old_cwd)
        # Attempt to cleanup, ignore errors
        try:
            shutil.rmtree(self.test_dir)
        except:
            pass

    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._discover_staged_files')
    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._apply_overrides')
    def test_e2e_auto_fix_success(self, mock_apply, mock_discover):
        abs_gitignore = os.path.abspath(self.gitignore_path)
        mock_discover.return_value = [abs_gitignore]
        mock_apply.side_effect = lambda x: x
        
        sentinel = AxiomSentinel(config_path=self.config_path)
        # Ensure we run in a clean state
        exit_code = sentinel.run()
        
        self.assertEqual(exit_code, 0)
        
        with open(abs_gitignore, "r") as f:
            content = f.read()
            self.assertIn(".env", content)
            self.assertIn("# Auto-added by AXIOM Sentinel", content)

    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._discover_staged_files')
    @patch('tools.axiom.axiom_sentinel.AxiomSentinel._apply_overrides')
    def test_auto_fix_disabled(self, mock_apply, mock_discover):
        mock_discover.return_value = [".gitignore"]
        mock_apply.side_effect = lambda x: x
        
        config = self.default_config.copy()
        config["auto_fix"]["enabled"] = False
        with open(self.config_path, "w") as f:
            yaml.dump(config, f)
            
        sentinel = AxiomSentinel(config_path=self.config_path)
        exit_code = sentinel.run()
        
        self.assertEqual(exit_code, 0)
        with open(self.gitignore_path, "r") as f:
            content = f.read()
            self.assertNotIn(".env", content)

if __name__ == "__main__":
    unittest.main()
