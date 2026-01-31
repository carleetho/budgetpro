import os
import sys
import time
import logging
import subprocess
from typing import List, Dict, Any, Optional, Set
from dataclasses import dataclass, field

from tools.axiom.config_loader import load_axiom_config, AxiomConfig
from tools.axiom.override_detector import detect_overrides, OverrideResult
from tools.axiom.validators.base_validator import BaseValidator, Violation, ValidationResult
from tools.axiom.validators.security_validator import SecurityValidator
from tools.axiom.validators.lazy_code_validator import LazyCodeValidator
from tools.axiom.validators.naming_validator import NamingValidator
from tools.axiom.validators.boundary_validator import BoundaryValidator
from tools.axiom.validators.state_machine_validator import StateMachineValidator
from tools.axiom.validators.semgrep_validator import SemgrepValidator
from tools.axiom.validators.domain_validator import DomainValidator
from tools.axiom.validators.blast_radius_validator import BlastRadiusValidator
from tools.axiom.lib.blast_radius_adapter import BlastRadiusAdapter
from tools.axiom.validators.dependency_validator import DependencyValidator
from tools.axiom.reporters.console_reporter import ConsoleReporter
from tools.axiom.reporters.log_reporter import LogReporter
from tools.axiom.reporters.metrics_reporter import MetricsReporter
from tools.axiom.reporters.base_reporter import BaseReporter, ReportResult
from tools.axiom.fixers.base_fixer import BaseFixer, FixResult
from tools.axiom.fixers.simple_fixer import SimpleFixer

@dataclass
class AggregatedViolations:
    """Groups violations by severity and tracks metadata."""
    blocking: List[Violation] = field(default_factory=list)
    warning: List[Violation] = field(default_factory=list)
    info: List[Violation] = field(default_factory=list)
    by_validator: Dict[str, List[Violation]] = field(default_factory=dict)
    
    @property
    def total_count(self) -> int:
        return len(self.blocking) + len(self.warning) + len(self.info)

class AxiomSentinel:
    """
    The central orchestrator for the AXIOM validation pipeline.
    
    Coordinates file discovery, validator execution, violation aggregation,
    override application, and reporting.
    """

    def __init__(self, config_path: Optional[str] = None, dry_run: bool = False):
        """
        Initialize the orchestrator.
        
        Args:
            config_path: Optional path to axiom.config.yaml
            dry_run: If True, log decisions but always exit 0
        """
        self.config_path = config_path
        self.dry_run = dry_run
        self.config: Optional[AxiomConfig] = None
        self.validators: List[BaseValidator] = []
        self.reporters: List[BaseReporter] = []
        self.fixers: List[BaseFixer] = []
        
        # Setup logging
        self._setup_logging()

    def _setup_logging(self):
        """Configures structured logging for the orchestrator."""
        log_format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
        if self.dry_run:
            log_format = '[DRY-RUN] ' + log_format
            
        logging.basicConfig(
            level=logging.INFO,
            format=log_format,
            stream=sys.stderr
        )
        self.logger = logging.getLogger("AxiomSentinel")

    def _load_configuration(self) -> AxiomConfig:
        """Loads and validates the pipeline configuration."""
        self.logger.info("Loading AXIOM configuration...")
        try:
            self.config = load_axiom_config(self.config_path) if self.config_path else load_axiom_config()
            return self.config
        except Exception as e:
            self.logger.error(f"Failed to load configuration: {e}. Falling back to defaults.")
            from tools.axiom.config_loader import DEFAULT_CONFIG
            self.config = AxiomConfig.from_dict(DEFAULT_CONFIG)
            return self.config

    def _discover_staged_files(self) -> List[str]:
        """Discovers files staged in the git index."""
        self.logger.info("Discovering staged files...")
        try:
            result = subprocess.run(
                ["git", "diff", "--cached", "--name-only"],
                capture_output=True,
                text=True,
                check=True
            )
            files = [f.strip() for f in result.stdout.splitlines() if f.strip()]
            self.logger.info(f"Found {len(files)} staged files.")
            return files
        except subprocess.CalledProcessError as e:
            self.logger.error(f"Git execution failed: {e}")
            return []

    def _initialize_components(self):
        """
        Instantiates enabled validators, reporters, and fixers based on configuration.
        """
        if not self.config:
            return

        # 1. Initialize Validators
        val_config = self.config.validators
        
        # Security Validator
        sec_config = val_config.get("security_validator", {})
        if sec_config.get("enabled", True):
            from dataclasses import asdict
            full_config_dict = asdict(self.config)
            self.validators.append(SecurityValidator(sec_config, full_config_dict))
            self.logger.info("SecurityValidator initialized.")

        # Lazy Code Validator
        if self.config and self.config.validators.get('lazy_code', {}).get('enabled', False):
            lazy_config = self.config.validators['lazy_code']
            self.validators.append(LazyCodeValidator(lazy_config))
            self.logger.debug("LazyCodeValidator registered")

        # Blast Radius Validator
        if self.config and self.config.validators.get('blast_radius', {}).get('enabled', True):
            blast_config = self.config.validators['blast_radius']
            adapter = BlastRadiusAdapter(self.config)
            self.validators.append(BlastRadiusValidator(blast_config, adapter))
            self.logger.info("BlastRadiusValidator initialized.")
        
        # Dependency Validator
        dep_config = val_config.get("dependency_validator", {})
        if dep_config.get("enabled", True):
            self.validators.append(DependencyValidator(dep_config))
            self.logger.info("DependencyValidator initialized.")
        
        # Naming Validator
        naming_config = val_config.get("naming_validator", {})
        if naming_config.get("enabled", True):
            root_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            self.validators.append(NamingValidator(naming_config, root_dir))
            self.logger.info("NamingValidator initialized.")
        
        # Boundary Validator
        boundary_config = val_config.get("boundary_validator", {})
        if boundary_config.get("enabled", True):
            root_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            self.validators.append(BoundaryValidator(boundary_config, root_dir))
            self.logger.info("BoundaryValidator initialized.")

        # State Machine Validator
        sm_config = val_config.get("state_machine_validator", {})
        if sm_config.get("enabled", True):
            root_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            self.validators.append(StateMachineValidator(sm_config, root_dir))
            self.logger.info("StateMachineValidator initialized.")
        
        # Semgrep Validator
        semgrep_config = val_config.get("semgrep_validator", {})
        if semgrep_config.get("enabled", True):
            root_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            self.validators.append(SemgrepValidator(semgrep_config, root_dir))
            self.logger.info("SemgrepValidator initialized.")
        
        # Domain Validator
        domain_config = val_config.get("domain", {})
        if domain_config.get("enabled", True):
            root_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            self.validators.append(DomainValidator(domain_config, root_dir))
            self.logger.info("DomainValidator initialized.")
        
        # 2. Initialize Reporters
        if self.config and hasattr(self.config, 'reporters'):
            rep_config = self.config.reporters
            
            # Console Reporter
            if rep_config.get('console', {}).get('enabled', True):
                self.reporters.append(ConsoleReporter(rep_config.get('console', {})))
                self.logger.info("ConsoleReporter registered.")
                
            # Log File Reporter
            if rep_config.get('log_file', {}).get('enabled', True):
                self.reporters.append(LogReporter(rep_config.get('log_file', {})))
                self.logger.info("LogReporter registered.")
                
            # Metrics Reporter
            if rep_config.get('metrics', {}).get('enabled', True):
                self.reporters.append(MetricsReporter(rep_config.get('metrics', {})))
                self.logger.info("MetricsReporter registered.")
        
        # 3. Initialize Fixers
        if self.config and hasattr(self.config, 'auto_fix'):
            fix_config = self.config.auto_fix
            if fix_config.get('enabled', False):
                self.fixers.append(SimpleFixer(fix_config))
                self.logger.info("SimpleFixer initialized.")

    def _execute_validators(self, files: List[str]) -> List[ValidationResult]:
        """Executes all initialized validators sequentially."""
        results = []
        for validator in self.validators:
            self.logger.info(f"Running validator: {validator.name}...")
            start_time = time.time()
            try:
                result = validator.validate(files)
                duration = (time.time() - start_time) * 1000
                # Ensure result metadata is populated if validator missed it
                # result.execution_time_ms = duration # Assuming result is immutable/dataclass
                results.append(result)
                self.logger.info(f"Validator {validator.name} finished in {duration:.2f}ms with {len(result.violations)} violations.")
            except Exception as e:
                self.logger.error(f"Error executing validator {validator.name}: {e}")
                
        return results

    def _aggregate_violations(self, results: List[ValidationResult]) -> AggregatedViolations:
        """Groups all discovered violations by severity."""
        aggregated = AggregatedViolations()
        for result in results:
            aggregated.by_validator[result.validator_name] = result.violations
            for v in result.violations:
                if v.severity == "blocking":
                    aggregated.blocking.append(v)
                elif v.severity == "warning":
                    aggregated.warning.append(v)
                else:
                    aggregated.info.append(v)
        return aggregated

    def _apply_overrides(self, aggregated: AggregatedViolations) -> AggregatedViolations:
        """Applies override logic based on commit message keywords."""
        self.logger.info("Applying override detections...")
        try:
            # Get current/staged commit message
            # For pre-commit, the message might be in .git/COMMIT_EDITMSG
            # but standard detect_overrides takes a string. 
            # If we are in pre-commit, we usually don't have the message yet 
            # unless it's a 'prepare-commit-msg' hook.
            # However, requirements specify using 'git log -1' style discovery 
            # or commit message scanning.
            
            commit_msg = ""
            # Attempt to read from .git/COMMIT_EDITMSG if it exists
            git_dir_result = subprocess.run(["git", "rev-parse", "--git-dir"], capture_output=True, text=True)
            if git_dir_result.returncode == 0:
                msg_path = os.path.join(git_dir_result.stdout.strip(), "COMMIT_EDITMSG")
                if os.path.exists(msg_path):
                    with open(msg_path, "r") as f:
                        commit_msg = f.read()
            
            if not commit_msg:
                 # Fallback to last commit if no pending message (e.g. manual run)
                 result = subprocess.run(["git", "log", "-1", "--pretty=%B"], capture_output=True, text=True)
                 commit_msg = result.stdout
            
            override_result = detect_overrides(commit_msg, self.config)
            
            if override_result.bypass_blast_radius or override_result.bypass_zones:
                self.logger.info(f"Overrides detected: {override_result}")
                
                # Logic to downgrade or skip violations based on overrides
                # This is a simplified version: if blast radius is bypassed, 
                # we might clear blocking violations from 'blast_radius' validator.
                
                # Keep original lists but filter them
                original_blocking = aggregated.blocking
                aggregated.blocking = []
                
                for v in original_blocking:
                     # Check if violation's validator or zone is covered by overrides
                     # For now, a simple placeholder: if BIGBANG_APPROVED, move all to warning
                     if override_result.bypass_blast_radius:
                         v_mod = Violation(
                             file_path=v.file_path,
                             message=f"[OVERRIDDEN] {v.message}",
                             severity="warning",
                             validator_name=v.validator_name,
                             line_number=v.line_number
                         )
                         aggregated.warning.append(v_mod)
                     else:
                         aggregated.blocking.append(v)
                         
        except Exception as e:
            self.logger.error(f"Error applying overrides: {e}")
            
        return aggregated

    def _invoke_reporters(self, aggregated: AggregatedViolations, total_time_ms: float):
        """Sends aggregated results to all enabled reporters."""
        all_violations = aggregated.blocking + aggregated.warning + aggregated.info
        for reporter in self.reporters:
            self.logger.info(f"Invoking reporter: {reporter.name}...")
            try:
                reporter.report(all_violations, total_time_ms)
            except Exception as e:
                self.logger.error(f"Reporter {reporter.name} failed: {e}")

    def _execute_auto_fixer(self, aggregated: AggregatedViolations) -> AggregatedViolations:
        """Triggers auto-fix logic if enabled and applicable."""
        if not self.config or not self.config.auto_fix.get("enabled"):
            return aggregated
            
        if not self.fixers:
            return aggregated

        all_violations = aggregated.blocking + aggregated.warning + aggregated.info
        modified_files = set()
        
        for fixer in self.fixers:
            self.logger.info(f"Triggering auto-fixer: {fixer.name}...")
            try:
                fix_result = fixer.fix(all_violations)
                if fix_result.success and fix_result.fixed_files:
                    self.logger.info(f"Auto-fixer {fixer.name} success. Modified files: {fix_result.fixed_files}")
                    modified_files.update(fix_result.fixed_files)
                elif not fix_result.success:
                    self.logger.error(f"Auto-fixer {fixer.name} failed: {fix_result.error_message}")
            except Exception as e:
                self.logger.error(f"Unexpected error in auto-fixer {fixer.name}: {e}")
                
        if not modified_files:
            return aggregated

        # Re-validate modified files
        self.logger.info(f"Re-validating modified files: {list(modified_files)}")
        re_val_results = self._execute_validators(list(modified_files))
        re_val_aggregated = self._aggregate_violations(re_val_results)
        
        # Check if new blocking violations were introduced
        if re_val_aggregated.blocking:
            self.logger.error("New blocking violations detected after auto-fix. Rolling back changes.")
            for fixer in self.fixers:
                try:
                    fixer.rollback()
                except Exception as e:
                    self.logger.error(f"Failed to rollback fixer {fixer.name}: {e}")
            return aggregated

        # Re-validation passed, commit changes (cleanup backups)
        for fixer in self.fixers:
            try:
                fixer.commit()
            except Exception as e:
                self.logger.error(f"Failed to commit fixer {fixer.name}: {e}")
        
        # Finally, re-run full validation to provide a consistent final state
        all_files = self._discover_staged_files()
        final_val_results = self._execute_validators(all_files)
        return self._aggregate_violations(final_val_results)


    def _make_exit_decision(self, aggregated: AggregatedViolations) -> int:
        """Determines exit code based on remaining blocking violations."""
        if aggregated.blocking:
            self.logger.error(f"Commit blocked due to {len(aggregated.blocking)} blocking violations.")
            if self.dry_run:
                self.logger.info("DRY-RUN: Would have exited 1, but returning 0.")
                return 0
            return 1
        
        self.logger.info("No blocking violations found. Commit allowed.")
        return 0

    def run(self) -> int:
        """Executes the full AXIOM pipeline."""
        start_time = time.time()
        self.logger.info("AXIOM Sentinel starting...")
        
        # 1. Load configuration
        self._load_configuration()
        
        # 2. Discover staged files
        files = self._discover_staged_files()
        if not files:
            self.logger.info("No staged files to validate.")
            return 0
            
        # 3. Initialize components
        self._initialize_components()
        
        # 4. Execute validators
        val_results = self._execute_validators(files)
        
        # 5. Aggregate violations
        aggregated = self._aggregate_violations(val_results)
        
        # 6. Apply overrides
        aggregated = self._apply_overrides(aggregated)
        
        # 7. Execute auto-fixers
        aggregated = self._execute_auto_fixer(aggregated)
        
        # 8. Report results
        total_time_ms = (time.time() - start_time) * 1000
        self._invoke_reporters(aggregated, total_time_ms)
        
        # 9. Make decision
        exit_code = self._make_exit_decision(aggregated)
        
        self.logger.info(f"AXIOM Sentinel finished in {total_time_ms:.2f}ms.")
        return exit_code

if __name__ == "__main__":
    # Simple CLI support
    dry_run = "--dry-run" in sys.argv
    sentinel = AxiomSentinel(dry_run=dry_run)
    sys.exit(sentinel.run())
