import unittest
from unittest.mock import patch, mock_open
from tools.axiom.validators.security_validator import SecurityValidator
from tools.axiom.validators.base_validator import Violation

class TestSecurityValidator(unittest.TestCase):
    def test_name(self):
        validator = SecurityValidator({})
        self.assertEqual(validator.name, "security_validator")

    def test_validate_returns_result(self):
        validator = SecurityValidator({})
        result = validator.validate(["test.py"])
        self.assertEqual(result.validator_name, "security_validator")
        # .gitignore should exist in the real project, so this might vary based on env
        # In a real unit test, we'd mock os.path.exists
        
    @patch('os.path.exists')
    @patch('builtins.open', new_callable=mock_open, read_data=".env\n*.log\nnode_modules\ntarget\n")
    def test_check_gitignore_missing_gemini(self, mock_file, mock_exists):
        mock_exists.return_value = True
        validator = SecurityValidator({"checks": {"gitignore": True}})
        violations = validator._check_gitignore()
        
        self.assertEqual(len(violations), 1)
        self.assertIn(".gemini", violations[0].message)

    @patch('os.path.exists')
    def test_check_gitignore_missing_file(self, mock_exists):
        mock_exists.return_value = False
        validator = SecurityValidator({"checks": {"gitignore": True}})
        violations = validator._check_gitignore()
        
        self.assertEqual(len(violations), 1)
        self.assertIn("missing", violations[0].message.lower())

    @patch('os.path.exists')
    @patch('builtins.open', new_callable=mock_open, read_data="API_KEY = 'AKIA1234567890ABCDEF'\n")
    def test_check_credentials_leaked_aws_key(self, mock_file, mock_exists):
        mock_exists.return_value = True
        validator = SecurityValidator({"checks": {"credentials": True}})
        violations = validator._check_credentials(["src/config.py"])
        
        self.assertEqual(len(violations), 1)
        self.assertIn("AWS Access Key", violations[0].message)
        self.assertEqual(violations[0].file_path, "src/config.py")
        self.assertEqual(violations[0].line_number, 1)

    @patch('os.path.exists')
    @patch('builtins.open', new_callable=mock_open, read_data="password = 'safe'\nsample_token = 'xoxb-1234567890-abcdef123456'\n")
    def test_check_credentials_leaked_slack_token(self, mock_file, mock_exists):
        mock_exists.return_value = True
        validator = SecurityValidator({"checks": {"credentials": True}})
        violations = validator._check_credentials(["deploy.sh"])
        
        # password='safe' shouldn't match (too short or not the right pattern)
        # slack token should match
        self.assertEqual(len(violations), 1)
        self.assertIn("Slack Token", violations[0].message)
        self.assertEqual(violations[0].line_number, 2)

    def test_check_compilation_not_java(self):
        validator = SecurityValidator({"checks": {"file_integrity": True}})
        violations = validator._check_compilation(["README.md"])
        self.assertEqual(len(violations), 0)

    @patch('os.path.exists')
    @patch('subprocess.run')
    def test_check_compilation_success(self, mock_run, mock_exists):
        mock_exists.return_value = True
        mock_run.return_value.returncode = 0
        
        validator = SecurityValidator({"checks": {"file_integrity": True}})
        violations = validator._check_compilation(["backend/src/Main.java"])
        
        self.assertEqual(len(violations), 0)
        mock_run.assert_called_once()

    @patch('os.path.exists')
    @patch('subprocess.run')
    def test_check_compilation_failure(self, mock_run, mock_exists):
        mock_exists.return_value = True
        mock_run.return_value.returncode = 1
        mock_run.return_value.stderr = "Compilation error"
        
        validator = SecurityValidator({"checks": {"file_integrity": True}})
        violations = validator._check_compilation(["backend/src/Main.java"])
        
        self.assertEqual(len(violations), 1)
        self.assertIn("COMPILATION ERROR", violations[0].message)

    def test_strictness_standard(self):
        validator = SecurityValidator({"strictness": "standard"})
        # HIGH should be warning
        self.assertEqual(validator._resolve_severity(SecurityValidator.SEC_HIGH), "warning")
        # CRITICAL should be blocking
        self.assertEqual(validator._resolve_severity(SecurityValidator.SEC_CRITICAL), "blocking")

    def test_strictness_strict(self):
        validator = SecurityValidator({"strictness": "strict"})
        # HIGH should be blocking
        self.assertEqual(validator._resolve_severity(SecurityValidator.SEC_HIGH), "blocking")

    def test_strictness_permissive(self):
        validator = SecurityValidator({"strictness": "permissive"})
        # HIGH should be warning
        self.assertEqual(validator._resolve_severity(SecurityValidator.SEC_HIGH), "warning")
        # CRITICAL should still be blocking (safety first)
        self.assertEqual(validator._resolve_severity(SecurityValidator.SEC_CRITICAL), "blocking")

if __name__ == "__main__":
    unittest.main()
