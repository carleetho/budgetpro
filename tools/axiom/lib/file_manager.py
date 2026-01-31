import os
import logging

logger = logging.getLogger("AxiomFileManager")

class FileManager:
    """
    Handles file I/O operations for AXIOM tools.
    """
    
    @staticmethod
    def read_file(path):
        """
        Reads a text file safely.
        
        Args:
            path (str): File path.
            
        Returns:
            str: File content or None if not found.
        """
        if not os.path.exists(path):
            return None
            
        try:
            with open(path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            logger.error(f"Failed to read file {path}: {e}")
            raise

    @staticmethod
    def write_file(path, content):
        """
        Writes content to a file using UTF-8 and LF line endings.
        
        Args:
            path (str): File path.
            content (str): Content to write.
        """
        try:
            # Ensure directory exists
            os.makedirs(os.path.dirname(os.path.abspath(path)), exist_ok=True)
            
            with open(path, 'w', encoding='utf-8', newline='\n') as f:
                f.write(content)
            logger.info(f"Successfully wrote to {path}")
        except Exception as e:
            logger.error(f"Failed to write file {path}: {e}")
            raise

    @staticmethod
    def files_are_different(path, new_content):
        """
        Compares existing file content with new content.
        
        Args:
            path (str): Path to existing file.
            new_content (str): Content to compare against.
            
        Returns:
            bool: True if files are different or existing file doesn't exist.
        """
        existing_content = FileManager.read_file(path)
        if existing_content is None:
            return True
            
        # Normalize line endings for comparison if needed, 
        # but string equality checking is usually sufficient for text files 
        # if write_file enforces \n
        return existing_content.strip() != new_content.strip()
