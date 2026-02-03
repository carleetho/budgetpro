import unittest
import os
import sys
import shutil
import tempfile
import subprocess

class TestSyncWorkflow(unittest.TestCase):
    
    def setUp(self):
        # Paths
        self.root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..", ".."))
        self.fixtures_dir = os.path.join(self.root_dir, "tools", "axiom", "tests", "fixtures")
        self.script_path = os.path.join(self.root_dir, "tools", "axiom", "sync_cursorrules.py")
        self.schema_path = os.path.join(self.root_dir, "tools", "axiom", "schema", "axiom-config.schema.json")
        
        # Temp dir for output
        self.test_dir = tempfile.mkdtemp()
        self.output_path = os.path.join(self.test_dir, ".cursorrules")
        
        self.valid_config = os.path.join(self.fixtures_dir, "valid_config.yaml")
        self.invalid_config = os.path.join(self.fixtures_dir, "invalid_config.yaml")

    def tearDown(self):
        shutil.rmtree(self.test_dir)

    def run_sync(self, config_path, force=False, additional_args=None):
        cmd = [sys.executable, self.script_path]
        cmd.extend(["--config-path", config_path])
        cmd.extend(["--schema-path", self.schema_path])
        cmd.extend(["--output-path", self.output_path])
        
        if force:
            cmd.append("--force")
            
        if additional_args:
            cmd.extend(additional_args)
            
        return subprocess.run(cmd, capture_output=True, text=True)

    def test_full_generation_valid(self):
        """Test generating .cursorrules from a valid config."""
        result = self.run_sync(self.valid_config, force=True)
        
        self.assertEqual(result.returncode, 0, f"Script failed: {result.stderr}")
        self.assertTrue(os.path.exists(self.output_path))
        
        with open(self.output_path, 'r') as f:
            content = f.read()
            self.assertIn("# Test Bot", content)
            self.assertIn("Priority 1", content)

    def test_validation_failure(self):
        """Test that invalid config fails validation."""
        result = self.run_sync(self.invalid_config, force=True)
        
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Config validation failed", result.stderr)
        self.assertFalse(os.path.exists(self.output_path))

    def test_force_flag_prevents_interaction(self):
        """Test that --force enables non-interactive mode."""
        # First run to create file
        self.run_sync(self.valid_config, force=True)
        
        # Modify file slightly to trigger diff
        with open(self.output_path, 'a') as f:
            f.write("\n# Modified")
            
        # Second run with force, should overwrite without hanging on input
        result = self.run_sync(self.valid_config, force=True)
        self.assertEqual(result.returncode, 0)
        
        with open(self.output_path, 'r') as f:
            content = f.read()
            self.assertNotIn("# Modified", content)

    def test_missing_config_file(self):
        """Test error when config path is wrong."""
        result = self.run_sync("/non/existent/path.yaml", force=True)
        self.assertEqual(result.returncode, 3) 
        self.assertIn("Configuration file not found", result.stderr)

if __name__ == '__main__':
    unittest.main()
