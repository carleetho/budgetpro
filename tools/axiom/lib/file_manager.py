import os
import logging

class FileManager:
    """
    Handles file I/O operations for reading and writing .cursorrules files.
    """
    
    def __init__(self):
        self.logger = logging.getLogger(__name__)

    def read_cursorrules(self, path):
        """
        Read existing .cursorrules file if present.
        
        Args:
            path (str): Path to .cursorrules file
            
        Returns:
            str: File content or None if not found
        """
        if not os.path.exists(path):
            return None
            
        try:
            with open(path, 'r', encoding='utf-8') as f:
                return f.read()
        except IOError as e:
            self.logger.error(f"Failed to read existing .cursorrules: {e}")
            raise

    def write_cursorrules(self, path, content):
        """
        Write content to .cursorrules file with UTF-8 encoding and LF line endings.
        
        Args:
            path (str): Path to output file
            content (str): Content to write
        """
        try:
            # Ensure directory exists
            os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
            
            with open(path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            self.logger.info(f"Successfully wrote to {path}")
        except IOError as e:
            self.logger.error(f"Failed to write .cursorrules: {e}")
            raise

    def files_are_different(self, content1, content2):
        """
        Compare two content strings for equality.
        Normalize line endings before comparing to avoid false positives.
        
        Args:
            content1 (str): First content string
            content2 (str): Second content string
            
        Returns:
            bool: True if different, False if identical
        """
        if content1 is None and content2 is None:
            return False
        if content1 is None or content2 is None:
            return True
            
        return content1.replace('\r\n', '\n').strip() != content2.replace('\r\n', '\n').strip()
