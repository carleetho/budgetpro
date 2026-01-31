#!/usr/bin/env python3
import os
import sys
import subprocess
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(message)s') # Simple format for install script
logger = logging.getLogger("AxiomInstallHook")

def check_cursorrules_exists():
    """Checks if .cursorrules file exists in project root."""
    return os.path.exists(".cursorrules")

def prompt_generate_cursorrules():
    """Prompts user to generate .cursorrules."""
    print("\n🤖 AI Assistant Integration")
    print("--------------------------------------------------")
    print("AXIOM can generate a '.cursorrules' file to teach your AI assistant (Cursor, Windsurf, etc.)")
    print("about your project's architectural rules, protection zones, and strict boundaries.")
    
    response = input("\nDo you want to generate '.cursorrules' now? [Y/n] ").strip().lower()
    return response in ('', 'y', 'yes')

def run_sync_script():
    """Executes the sync_cursorrules.py script."""
    try:
        # Determine path to sync script relative to this file
        base_dir = os.path.dirname(os.path.abspath(__file__))
        sync_script = os.path.join(base_dir, "sync_cursorrules.py")
        
        # Run sync script with --force since we already prompted/checked
        subprocess.run([sys.executable, sync_script, "--force"], check=True)
        return True
    except subprocess.CalledProcessError:
        logger.error("❌ Failed to generate .cursorrules.")
        return False
    except Exception as e:
        logger.error(f"❌ Error executing sync script: {e}")
        return False

def validate_cursorrules_sync():
    """
    Checks if existing .cursorrules matches config.
    This effectively runs sync script without force to check for changes relative to config.
    Wait, simple way is just to let the user know they can update it.
    """
    # For simplicity in install hook, we won't do deep diffing logic here,
    # as installing usually implies setting up or refreshing environment.
    pass

def main():
    try:
        if not check_cursorrules_exists():
            if prompt_generate_cursorrules():
                logger.info("Generating .cursorrules...")
                if run_sync_script():
                    logger.info("✅ .cursorrules generated successfully.")
            else:
                logger.info("ℹ️ Skipping .cursorrules generation. You can run 'python3 tools/axiom/sync_cursorrules.py' later.")
        else:
            # File exists
            logger.info("ℹ️ .cursorrules already exists.")
            # Optional: Start interactive update check?
            # For now, just inform user.
            
    except KeyboardInterrupt:
        print("\nOperation cancelled.")
        sys.exit(1)
    except Exception as e:
        logger.error(f"Error in install hook: {e}")
        # Build should normally continue even if this optional step fails
        sys.exit(0) 

if __name__ == "__main__":
    main()
