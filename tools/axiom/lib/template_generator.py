import os
import logging
import jinja2

class TemplateGenerator:
    """
    Generates markdown files from Jinja2 templates using configuration data.
    Enforces word count limits to ensure compatibility with AI Context windows.
    """
    
    def __init__(self, template_path):
        """
        Initialize the generator with a specific template.
        
        Args:
            template_path (str): Absolute path to the Jinja2 template file
        """
        self.logger = logging.getLogger(__name__)
        
        if not os.path.exists(template_path):
            raise FileNotFoundError(f"Template not found at {template_path}")
            
        self.template_path = template_path
        self.template_dir = os.path.dirname(template_path)
        self.template_file = os.path.basename(template_path)
        
        # Configure Jinja2 environment
        self.env = jinja2.Environment(
            loader=jinja2.FileSystemLoader(self.template_dir),
            trim_blocks=True,
            lstrip_blocks=True,
            autoescape=False  # Markdown generation, typically safe from config
        )
        
        # Register custom filters if needed (e.g., formatting)
        # self.env.filters['format_date'] = ...

    def render(self, config_dict):
        """
        Render the template with the provided configuration.
        
        Args:
            config_dict (dict): Configuration data (e.g., loaded from yaml)
            
        Returns:
            str: Rendered markdown content
        """
        try:
            template = self.env.get_template(self.template_file)
            return template.render(**config_dict)
        except jinja2.TemplateError as e:
            self.logger.error(f"Template rendering failed: {e}")
            raise

    def validate_word_count(self, content, limit=2000):
        """
        Check if the content word count is within limits.
        
        Args:
            content (str): The rendered content
            limit (int): Word count limit (default 2000)
            
        Returns:
            bool: True if within limit, False otherwise
        """
        # Simple whitespace splitting as per requirements
        words = content.split()
        count = len(words)
        
        if count >= limit:
            self.logger.warning(f"Content length ({count} words) exceeds limit of {limit} words.")
            return False
        elif count > (limit * 0.9):
            self.logger.warning(f"Content length ({count} words) is approaching limit of {limit} words.")
            
        return True
