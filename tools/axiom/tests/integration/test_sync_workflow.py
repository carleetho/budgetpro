import os
import sys
import shutil
import subprocess
import pytest

# Paths
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../../..'))
TOOLS_DIR = os.path.join(PROJECT_ROOT, 'tools/axiom')
SYNC_SCRIPT = os.path.join(TOOLS_DIR, 'sync_cursorrules.py')
SCHEMA_PATH = os.path.join(TOOLS_DIR, 'schema/axiom-config.schema.json')
TEMPLATE_PATH = os.path.join(TOOLS_DIR, 'templates/cursorrules.template.md')
FIXTURES_DIR = os.path.join(TOOLS_DIR, 'tests/fixtures')

@pytest.fixture
def clean_env(tmp_path):
    """
    Sets up a temporary environment with necessary files copied.
    """
    # Create temp workspace
    workspace = tmp_path / "workspace"
    workspace.mkdir()
    
    # Copy fixtures
    shutil.copy(os.path.join(FIXTURES_DIR, 'valid_config.yaml'), workspace / 'axiom.config.yaml')
    shutil.copy(os.path.join(FIXTURES_DIR, 'invalid_config_missing_field.yaml'), workspace / 'invalid_config.yaml')
    shutil.copy(os.path.join(FIXTURES_DIR, 'invalid_config_bad_yaml.yaml'), workspace / 'bad_yaml.yaml')
    shutil.copy(os.path.join(FIXTURES_DIR, 'existing_cursorrules.md'), workspace / '.cursorrules')
    
    # We need the real schema and template for the script to work
    # The script accepts paths, so we can point to the real ones or copy them
    # Let's copy them to be safe and isolated
    fixtures_schema_dir = workspace / "schema"
    fixtures_schema_dir.mkdir()
    shutil.copy(SCHEMA_PATH, fixtures_schema_dir / 'axiom-config.schema.json')
    
    fixtures_template_dir = workspace / "templates"
    fixtures_template_dir.mkdir()
    shutil.copy(TEMPLATE_PATH, fixtures_template_dir / 'cursorrules.template.md')
    
    return workspace

def run_sync(workspace, config_name='axiom.config.yaml', args=None, input_str=None):
    """
    Helper to run the sync script as a subprocess.
    """
    if args is None:
        args = []
        
    cmd = [
        sys.executable,
        SYNC_SCRIPT,
        '--config-path', str(workspace / config_name),
        '--schema-path', str(workspace / 'schema/axiom-config.schema.json'),
        '--template-path', str(workspace / 'templates/cursorrules.template.md'),
        '--output-path', str(workspace / '.cursorrules')
    ] + args
    
    # Set PYTHONPATH to include project root so imports work
    env = os.environ.copy()
    env['PYTHONPATH'] = PROJECT_ROOT + os.pathsep + env.get('PYTHONPATH', '')

    result = subprocess.run(
        cmd,
        input=input_str,
        capture_output=True,
        text=True,
        cwd=PROJECT_ROOT,  # Run from project root but pointing to temp files
        env=env
    )
    return result

class TestSyncWorkflow:

    def test_sync_new_cursorrules(self, clean_env):
        """Test generating .cursorrules from scratch with --force."""
        workspace = clean_env
        output_file = workspace / '.cursorrules'
        if output_file.exists():
            output_file.unlink() # Ensure it doesn't exist
            
        result = run_sync(workspace, args=['--force'])
        
        assert result.returncode == 0
        assert "Successfully synced" in result.stderr # Logging goes to stderr by default
        assert output_file.exists()
        
        content = output_file.read_text(encoding='utf-8')
        assert "# 1. ROL Y PERSONALIDAD" in content
        assert "Test Architect" in content

    def test_sync_no_changes(self, clean_env):
        """Test when config matches existing file."""
        workspace = clean_env
        
        # First run to generate it
        run_sync(workspace, args=['--force'])
        
        # Second run
        result = run_sync(workspace, args=['--force'])
        
        assert result.returncode == 0
        assert "No changes needed" in result.stderr

    def test_sync_with_force_flag(self, clean_env):
        """Test overwriting existing file with --force."""
        workspace = clean_env
        output_file = workspace / '.cursorrules'
        
        # Modify existing file to be different
        output_file.write_text("DIFFERENT CONTENT", encoding='utf-8')
        
        result = run_sync(workspace, args=['--force'])
        
        assert result.returncode == 0
        assert "Successfully synced" in result.stderr
        
        content = output_file.read_text(encoding='utf-8')
        assert "Test Architect" in content
        assert "DIFFERENT CONTENT" not in content

    def test_sync_user_confirms(self, clean_env):
        """Test overwriting with interactive confirmation (input='y')."""
        workspace = clean_env
        output_file = workspace / '.cursorrules'
        
        # Existing file is 'existing_cursorrules.md' copied in fixture
        
        result = run_sync(workspace, input_str="y\n")
        
        assert result.returncode == 0
        assert "Successfully synced" in result.stderr
        
        content = output_file.read_text(encoding='utf-8')
        assert "Test Architect" in content

    def test_sync_user_declines(self, clean_env):
        """Test declining overwrite (input='n')."""
        workspace = clean_env
        output_file = workspace / '.cursorrules'
        original_content = output_file.read_text(encoding='utf-8')
        
        result = run_sync(workspace, input_str="n\n")
        
        assert result.returncode == 0
        assert "Operation cancelled" in result.stderr
        
        new_content = output_file.read_text(encoding='utf-8')
        assert new_content == original_content

    def test_missing_config_file(self, clean_env):
        """Test error when config file is missing."""
        workspace = clean_env
        
        result = run_sync(workspace, config_name='non_existent.yaml')
        
        assert result.returncode == 1
        assert "Configuration file not found" in result.stderr

    def test_invalid_yaml_syntax(self, clean_env):
        """Test error when YAML is invalid."""
        workspace = clean_env
        
        result = run_sync(workspace, config_name='bad_yaml.yaml')
        
        assert result.returncode == 1
        assert "Failed to parse YAML" in result.stderr

    def test_schema_validation_failure(self, clean_env):
        """Test error when schema validation fails."""
        workspace = clean_env
        
        result = run_sync(workspace, config_name='invalid_config.yaml')
        
        assert result.returncode == 1
        assert "Configuration validation failed" in result.stderr

    def test_file_encoding_and_line_endings(self, clean_env):
        """Test that output file uses UTF-8 and LF."""
        workspace = clean_env
        output_file = workspace / '.cursorrules'
        if output_file.exists():
            output_file.unlink()
            
        run_sync(workspace, args=['--force'])
        
        # Read as bytes to check line endings directly
        content_bytes = output_file.read_bytes()
        
        # Check for LF (\n) and no CRLF (\r\n)
        # Note: This checks strictly for Unix line endings
        assert b'\r\n' not in content_bytes
        
        # Check UTF-8 validity
        content_str = content_bytes.decode('utf-8') # Should not raise error
        assert "Test Architect" in content_str
