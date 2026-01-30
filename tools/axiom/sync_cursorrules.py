#!/usr/bin/env python3
import argparse
import sys
import os
import logging

# Add project root to path to ensure modules found
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..")))

from tools.axiom.lib.config_loader import ConfigLoader
from tools.axiom.lib.file_manager import FileManager
from tools.axiom.lib.template_generator import TemplateGenerator

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(levelname)s: %(message)s')
logger = logging.getLogger("AxiomSync")

def main():
    parser = argparse.ArgumentParser(description="Generate .cursorrules from AXIOM configuration.")
    parser.add_argument("--config-path", default="tools/axiom/axiom.config.yaml", help="Path to axiom.config.yaml")
    parser.add_argument("--schema-path", default="tools/axiom/schema/axiom-config.schema.json", help="Path to schema file")
    parser.add_argument("--output-path", default=".cursorrules", help="Path to output file")
    parser.add_argument("--force", action="store_true", help="Overwrite without confirmation")
    
    args = parser.parse_args()
    
    try:
        # 1. Load and Validate Config
        logger.info(f"Loading configuration from {args.config_path}...")
        config = ConfigLoader.load_config(args.config_path)
        
        logger.info(f"Validating configuration...")
        ConfigLoader.validate_config(config, args.schema_path)
        
        # 2. Generate Content
        logger.info("Generating .cursorrules content...")
        # Template generator assumes templates dir relative to itself, which is fine
        generator = TemplateGenerator()
        content = generator.render(config)
        
        if not generator.validate_word_count(content):
            logger.warning("Generated content exceeds recommended word count limit (2000 words).")
            
        # 3. Check for Changes
        if not FileManager.files_are_different(args.output_path, content):
            logger.info("No changes detected. .cursorrules is up to date.")
            sys.exit(0)
            
        # 4. Write with Confirmation
        if not args.force and os.path.exists(args.output_path):
            response = input(f"File {args.output_path} will be overwritten. Continue? [y/N]: ").strip().lower()
            if response != 'y':
                logger.info("Operation cancelled by user.")
                sys.exit(0)
        
        FileManager.write_file(args.output_path, content)
        logger.info(f"Successfully generated {args.output_path}")
        sys.exit(0)
        
    except FileNotFoundError as e:
        logger.error(str(e))
        sys.exit(3) # Special code for missing config
    except Exception as e:
        logger.error(f"Sync failed: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
