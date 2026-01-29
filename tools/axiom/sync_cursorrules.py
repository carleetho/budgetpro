#!/usr/bin/env python3
import argparse
import sys
import os
import logging
from lib.config_loader import ConfigLoader
from lib.template_generator import TemplateGenerator
from lib.file_manager import FileManager

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("axiom-sync")

def main():
    parser = argparse.ArgumentParser(description="Sync AXIOM configuration to .cursorrules")
    parser.add_argument('--config-path', default='tools/axiom/axiom.config.yaml', help='Path to axiom.config.yaml')
    parser.add_argument('--schema-path', default='tools/axiom/schema/axiom-config.schema.json', help='Path to schema')
    parser.add_argument('--template-path', default='tools/axiom/templates/cursorrules.template.md', help='Path to template')
    parser.add_argument('--output-path', default='.cursorrules', help='Output path for .cursorrules')
    parser.add_argument('--force', action='store_true', help='Skip confirmation prompt')
    
    args = parser.parse_args()
    
    # Initialize components
    config_loader = ConfigLoader()
    template_generator = TemplateGenerator(args.template_path)
    file_manager = FileManager()
    
    try:
        # 1. Load and Validate Configuration
        logger.info(f"Loading configuration from {args.config_path}")
        config = config_loader.load_config(args.config_path)
        
        logger.info(f"Validating configuration against {args.schema_path}")
        config_loader.validate_config(config, args.schema_path)
        
        # 2. Generate Content
        logger.info("Generating .cursorrules content...")
        new_content = template_generator.render(config)
        
        if not template_generator.validate_word_count(new_content):
            logger.warning("Generated content exceeds recommended word limits!")
            
        # 3. Check for Changes
        existing_content = file_manager.read_cursorrules(args.output_path)
        
        if file_manager.files_are_different(existing_content, new_content):
            if existing_content:
                logger.info(f"Existing .cursorrules found at {args.output_path}. Content has changed.")
            else:
                logger.info(f"Creating new .cursorrules at {args.output_path}")
                
            # 4. Prompt for Confirmation (if not forced)
            if not args.force and existing_content:
                response = input("Overwrite existing .cursorrules? [y/N]: ")
                if response.lower() not in ['y', 'yes']:
                    logger.info("Operation cancelled by user.")
                    sys.exit(0)
            
            # 5. Write File
            file_manager.write_cursorrules(args.output_path, new_content)
            logger.info("Successfully synced .cursorrules")
        else:
            logger.info(".cursorrules is already up to date. No changes needed.")
            
    except Exception as e:
        logger.error(f"Sync failed: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()
