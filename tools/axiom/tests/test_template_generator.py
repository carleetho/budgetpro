import unittest
import os
import sys

# Add tools/axiom to path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", "..")))

from tools.axiom.lib.template_generator import TemplateGenerator

class TestTemplateGenerator(unittest.TestCase):
    
    def setUp(self):
        # Determine path to real template for integration-style testing
        # Or mock environment. Using real path for simplicity as file exists.
        base_dir = os.path.dirname(os.path.abspath(__file__))
        template_dir = os.path.join(base_dir, "..", "templates")
        self.generator = TemplateGenerator(template_dir=template_dir)
        
        self.sample_config = {
            "system": {
                "role": "Test Architect",
                "priorities": ["Priority 1", "Priority 2"]
            },
            "axioms": {
                "protection_zones": {
                    "red": [{"path": "core/", "max_files": 1}],
                    "green": [{"path": "app/", "max_files": 10}]
                },
                "prohibitions": [
                    {"rule": "No Bugs", "reason": "Bad", "severity": "BLOCKING"}
                ],
                "hexagonal_boundaries": {
                    "permitted": ["A -> B"],
                    "forbidden": ["B -> A"]
                },
                "override_keywords": [
                    {"keyword": "SKIP", "context": "Testing"}
                ]
            },
            "history": {
                "lessons_learned": ["Lesson 1"],
                "incidents": ["Incident 1"]
            }
        }

    def test_render_output_structure(self):
        output = self.generator.render(self.sample_config)
        self.assertIn("# Test Architect", output)
        self.assertIn("Priority 1", output)
        self.assertIn("RED ZONE", output)
        self.assertIn("No Bugs", output)
        self.assertIn("SKIP", output)
        self.assertIn("Operation Protocol", output)
        
    def test_word_count_validation(self):
        short_text = "This is a short text."
        self.assertTrue(self.generator.validate_word_count(short_text, limit=100))
        
        long_text = "word " * 101
        self.assertFalse(self.generator.validate_word_count(long_text, limit=100))

if __name__ == '__main__':
    unittest.main()
