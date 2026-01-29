import unittest
import sys
import os
import yaml

# Add tools/axiom/lib to path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../lib')))

from template_generator import TemplateGenerator

class TestTemplateGenerator(unittest.TestCase):
    
    def setUp(self):
        self.base_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
        self.template_path = os.path.join(self.base_dir, 'templates/cursorrules.template.md')
        self.generator = TemplateGenerator(self.template_path)
        
        # Load the actual example config (since we created it in Task 1)
        config_path = os.path.join(self.base_dir, 'axiom.config.yaml')
        if os.path.exists(config_path):
            with open(config_path, 'r') as f:
                self.config = yaml.safe_load(f)
        else:
            # Fallback mock config for independent testing
            self.config = {
                'system': {
                    'role': 'Test Architect',
                    'priorities': ['P1', 'P2']
                },
                'axioms': {
                    'protection_zones': [
                        {'name': 'Zone A', 'level': 'RED', 'paths': ['a/*'], 'description': 'Desc A'}
                    ],
                    'prohibitions': ['Do not do X'],
                    'hexagonal_boundaries': {
                        'permitted': [{'from': 'A', 'to': 'B'}],
                        'forbidden': [{'from': 'B', 'to': 'A'}]
                    },
                    'override_keywords': {'TEST_OVERRIDE': 'For testing'},
                    'historical_context': {
                        'baseline_violations': ['V1'],
                        'lessons_learned': ['L1']
                    }
                }
            }

    def test_rendering(self):
        """Test that template renders and includes key sections."""
        rendered = self.generator.render(self.config)
        
        # Check System
        self.assertIn(self.config['system']['role'], rendered)
        
        # Check Axioms
        if 'prohibitions' in self.config['axioms']:
            self.assertIn(self.config['axioms']['prohibitions'][0], rendered)
            
        # Check Zones
        if 'protection_zones' in self.config['axioms']:
            self.assertIn(self.config['axioms']['protection_zones'][0]['name'], rendered)

    def test_word_count_validation(self):
        """Test word count validation logic."""
        # Short content
        self.assertTrue(self.generator.validate_word_count("Word " * 100))
        
        # Long content
        self.assertFalse(self.generator.validate_word_count("Word " * 2001))
        
    def test_real_config_render(self):
        """Test rendering with the actual config file on disk."""
        rendered = self.generator.render(self.config)
        print(f"\nRendered size: {len(rendered.split())} words")
        self.assertTrue(self.generator.validate_word_count(rendered))
        
        # Verifying specific Spanish content from Task 1 requirements
        self.assertIn("NO BORRES CÓDIGO FUNCIONAL", rendered)
        self.assertIn("NIVEL: RED", rendered)

if __name__ == '__main__':
    unittest.main()
