import os
from jinja2 import Environment, FileSystemLoader
import logging

logger = logging.getLogger("AxiomTemplateGenerator")

class TemplateGenerator:
    """
    Generates .cursorrules files from axiom.config.yaml data using Jinja2 templates.
    """
    
    def __init__(self, template_dir=None):
        if template_dir is None:
            # Default to ../templates relative to this file
            base_dir = os.path.dirname(os.path.abspath(__file__))
            template_dir = os.path.join(base_dir, "..", "templates")
            
        self.env = Environment(loader=FileSystemLoader(template_dir))
        self.template_name = "cursorrules.template.md"
        
    def render(self, config_data):
        """
        Renders the template with the provided configuration data.
        
        Args:
            config_data (dict): The loaded axiom.config.yaml data.
            
        Returns:
            str: The rendered markdown content.
        """
        try:
            template = self.env.get_template(self.template_name)
            output = template.render(**config_data)
            return output
        except Exception as e:
            logger.error(f"Failed to render template: {e}")
            raise

    def validate_word_count(self, content, limit=2000):
        """
        Validates that the content word count is within limits.
        
        Args:
            content (str): The rendered content.
            limit (int): Max word count (approx token proxy).
            
        Returns:
            bool: True if safe, False if exceeds limit.
        """
        # Simple whitespace splitting approximation
        words = content.split()
        count = len(words)
        
        if count > limit:
            logger.warning(f"Content exceeds word limit! ({count} > {limit})")
            return False
        return True
