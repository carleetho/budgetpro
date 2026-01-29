import unittest
from unittest.mock import patch, mock_open
from tools.axiom.validators.lazy_code_validator import LazyCodeValidator
from tools.axiom.validators.base_validator import Violation

class TestLazyCodeValidator(unittest.TestCase):
    def setUp(self):
        self.validator = LazyCodeValidator({})

    def test_name(self):
        self.assertEqual(self.validator.name, "lazy_code_validator")

    def test_empty_method_detection(self):
        content = """
        public class Test {
            public void emptyMethod() { }
            
            public void commentedMethod() {
                // TODO: implement
            }
            
            public void realMethod() {
                System.out.println("hello");
            }
        }
        """
        violations = self.validator._check_empty_methods("Test.java", content)
        # Should detect emptyMethod and commentedMethod (if regex is broad enough)
        # My regex: EMPTY_METHOD_PATTERN = r"(?:public|private|protected|static|\s) +[\w<>\[\], ]+\s+\w+\s*\([^)]*\)\s*\{\s*(?://.*|/\*[\s\S]*?\*/|\s)*\}"
        
        self.assertEqual(len(violations), 2)
        self.assertIn("Método vacío detectado", violations[0].message)
        self.assertEqual(violations[0].line_number, 2) 
        self.assertEqual(violations[1].line_number, 4)

    def test_persistence_lazy_return_detection(self):
        content = """
        public class UserRepo {
            public User find() {
                return null;
            }
            public Optional<User> findOpt() {
                return Optional.empty();
            }
            public User ok() {
                return new User();
            }
        }
        """
        # Persistence detection should only run for files in /infrastructure/persistence/
        # but the method _check_persistence_lazy_patterns just takes content.
        violations = self.validator._check_persistence_lazy_patterns("infrastructure/persistence/UserRepo.java", content)
        
        self.assertEqual(len(violations), 2)
        self.assertIn("Retorno null", violations[0].message)
        self.assertIn("Retorno empty", violations[1].message)
        self.assertEqual(violations[0].line_number, 4)
        self.assertEqual(violations[1].line_number, 7)

    def test_critical_todo_detection(self):
        content = """
        /**
         * Business logic
         * // TODO: refactor this header
         */
        public class Service {
            // TODO: implement logic
            public void execute() {
                /* TODO: subtask */
            }
        }
        """
        violations = self.validator._check_critical_todos("domain/Service.java", content)
        
        self.assertEqual(len(violations), 3)
        self.assertIn("TODO en módulo crítico", violations[0].message)
        self.assertEqual(violations[0].line_number, 4) 
        self.assertEqual(violations[1].line_number, 7)
        self.assertEqual(violations[2].line_number, 9)

    @patch('os.path.exists')
    @patch('builtins.open', new_callable=mock_open)
    def test_validate_filtering(self, mock_file, mock_exists):
        mock_exists.return_value = True
        
        # Scenario: 
        # 1. domain file with TODO -> BLOCK
        # 2. persistence file with return null -> BLOCK
        # 3. application file with return null -> ALLOW
        
        files = [
            "/path/to/domain/Logic.java",
            "/path/to/infrastructure/persistence/Repo.java",
            "/path/to/application/Service.java"
        ]
        
        contents = {
            "/path/to/domain/Logic.java": "// TODO: fix",
            "/path/to/infrastructure/persistence/Repo.java": "return null;",
            "/path/to/application/Service.java": "return null;"
        }
        
        def side_effect(path, *args, **kwargs):
            m = mock_open(read_data=contents.get(path, "")).return_value
            return m
            
        mock_file.side_effect = side_effect
        
        result = self.validator.validate(files)
        
        # Logic.java -> 1 violation (TODO) + 1 (empty detection if we don't mock it well, but let's assume content is just that line)
        # Repo.java -> 1 violation (null return)
        # Service.java -> 0 violations (persistence rule doesn't apply)
        
        # Total violations should be 2 (TODO in domain, null in persistence)
        # Note: if my TODO regex also triggers empty methods because of lack of surrounding context in mock, it might be more.
        # But here logic is separate.
        
        viol_paths = [v.file_path for v in result.violations]
        self.assertIn("/path/to/domain/Logic.java", viol_paths)
        self.assertIn("/path/to/infrastructure/persistence/Repo.java", viol_paths)
        self.assertNotIn("/path/to/application/Service.java", viol_paths)

if __name__ == "__main__":
    unittest.main()
