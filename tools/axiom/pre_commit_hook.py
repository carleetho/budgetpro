#!/usr/bin/env python3
"""
AXIOM Pre-commit Hook Entry Point

This script is intended to be invoked by the git pre-commit hook.
It initializes the AxiomSentinel orchestrator and executes the validation pipeline.

Usage:
    python3 pre_commit_hook.py [--dry-run]

Exit Codes:
    0 - Validation passed or dry-run enabled
    1 - Blocking violations found or execution error
"""

import sys
import logging
import traceback
import subprocess
from tools.axiom.axiom_sentinel import AxiomSentinel

def main():
    # 1. Parse simple CLI arguments
    dry_run = "--dry-run" in sys.argv
    
    # 2. Configure basic logging to stderr for git hook visibility
    # Note: AxiomSentinel also configures logging, but we ensure 
    # top-level errors are caught and logged properly here.
    logging.basicConfig(
        level=logging.INFO,
        format='%(levelname)s: %(message)s',
        stream=sys.stderr
    )
    logger = logging.getLogger("AXIOM-Hook")

    try:
        # 3. Instantiate and run orchestrator
        sentinel = AxiomSentinel(dry_run=dry_run)
        exit_code = sentinel.run()
        
        # 3.5. If validation passed, auto-stage the updated metrics file
        if exit_code == 0:
            try:
                subprocess.run(["git", "add", ".budgetpro/metrics.json"], check=False, capture_output=True)
                logger.info("Auto-staged updated metrics.json")
            except Exception as e:
                logger.warning(f"Failed to auto-stage metrics.json: {e}")
        
        # 4. Exit with orchestrator's decision
        sys.exit(exit_code)
        
    except Exception as e:
        # 5. Graceful handling of unhandled exceptions
        logger.error(f"AXIOM pipeline failed with an unexpected error: {e}")
        if dry_run:
            logger.info("DRY-RUN: Exception suppressed. Would have blocked commit.")
            sys.exit(0)
        
        # Log stack trace to stderr for debugging
        traceback.print_exc(file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
