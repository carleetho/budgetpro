import os
import sys
import logging
import subprocess

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger("axiom-install-hook")

def check_cursorrules_exists(project_root="."):
    """
    Check if .cursorrules exists in the project root.
    """
    path = os.path.join(project_root, ".cursorrules")
    return os.path.exists(path)

def prompt_generate_cursorrules():
    """
    Prompt the user to generate .cursorrules.
    Returns True if user confirms, False otherwise.
    """
    while True:
        response = input("\n[AXIOM] Generate .cursorrules for AI assistant integration? (y/n): ").strip().lower()
        if response in ['y', 'yes']:
            return True
        if response in ['n', 'no']:
            return False
            
def run_sync_script(project_root="."):
    """
    Run the sync_cursorrules.py script.
    """
    script_path = os.path.join(project_root, "tools/axiom/sync_cursorrules.py")
    if not os.path.exists(script_path):
        logger.error(f"Sync script not found at {script_path}")
        return False
        
    logger.info("Running sync_cursorrules.py...")
    try:
        # We run with --force to skip the overwrite confirmation since we just asked the user
        result = subprocess.run(
            [sys.executable, script_path, "--force"],
            check=True,
            cwd=project_root,
            capture_output=True,
            text=True
        )
        logger.info(result.stdout)
        return True
    except subprocess.CalledProcessError as e:
        logger.error(f"Failed to generate .cursorrules: {e.stderr}")
        return False

def validate_cursorrules_sync(project_root="."):
    """
    Check if .cursorrules is in sync (basic check).
    In a real implementation, this might calculate hashes. 
    For now, we just check existence and warn if it might be outdated based on file mtime.
    """
    cursorrules_path = os.path.join(project_root, ".cursorrules")
    config_path = os.path.join(project_root, "tools/axiom/axiom.config.yaml")
    
    if not os.path.exists(cursorrules_path) or not os.path.exists(config_path):
        return
        
    # Simple check: if config is newer than cursorrules, warn user
    if os.path.getmtime(config_path) > os.path.getmtime(cursorrules_path):
        logger.warning("WARNING: axiom.config.yaml is newer than .cursorrules. Run 'python3 tools/axiom/sync_cursorrules.py' to update.")

def execute_hook(project_root="."):
    """
    Main entry point for the hook.
    """
    if not check_cursorrules_exists(project_root):
        if prompt_generate_cursorrules():
            if run_sync_script(project_root):
                logger.info("[AXIOM] .cursorrules generated successfully.")
            else:
                logger.warning("[AXIOM] Failed to generate .cursorrules. Please run it manually later.")
        else:
            logger.warning("[AXIOM] Skipping .cursorrules generation. AI assistant guidance will be limited.")
    else:
        validate_cursorrules_sync(project_root)

if __name__ == "__main__":
    execute_hook()
