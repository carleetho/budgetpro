import unittest
from tools.axiom.override_detector import detect_overrides
from tools.axiom.config_loader import AxiomConfig

class TestOverrideDetector(unittest.TestCase):
    
    def test_no_overrides(self):
        result = detect_overrides("Clean up code")
        self.assertFalse(result.bypass_blast_radius)
        self.assertEqual(len(result.bypass_zones), 0)

    def test_override_estimacion(self):
        result = detect_overrides("Fix bug in calculation OVERRIDE_ESTIMACION")
        self.assertIn("domain/estimacion", result.bypass_zones)
        self.assertFalse(result.bypass_blast_radius)

    def test_override_presupuesto(self):
        result = detect_overrides("Update budget logic OVERRIDE_PRESUPUESTO")
        self.assertIn("domain/presupuesto", result.bypass_zones)
        self.assertFalse(result.bypass_blast_radius)

    def test_override_domain_core(self):
        result = detect_overrides("Refactor entities OVERRIDE_DOMAIN_CORE")
        self.assertIn("domain/valueobjects", result.bypass_zones)
        self.assertIn("domain/entities", result.bypass_zones)

    def test_bigbang_approved(self):
        result = detect_overrides("Huge refactor BIGBANG_APPROVED")
        self.assertTrue(result.bypass_blast_radius)

    def test_case_insensitivity(self):
        result = detect_overrides("override_estimacion legacy fix")
        self.assertIn("domain/estimacion", result.bypass_zones)

    def test_multiple_keywords(self):
        result = detect_overrides("Major update OVERRIDE_ESTIMACION and BIGBANG_APPROVED")
        self.assertIn("domain/estimacion", result.bypass_zones)
        self.assertTrue(result.bypass_blast_radius)

    def test_empty_message(self):
        result = detect_overrides("")
        self.assertFalse(result.bypass_blast_radius)
        self.assertEqual(len(result.bypass_zones), 0)

    def test_none_message(self):
        result = detect_overrides(None)
        self.assertFalse(result.bypass_blast_radius)
        self.assertEqual(len(result.bypass_zones), 0)

if __name__ == '__main__':
    unittest.main()
