# AXIOM Configuration System

The AXIOM configuration system provides a centralized, validating, and flexible way to control the behavior of the AXIOM development governance tool. It uses a YAML-based configuration file (`axiom.config.yaml`) located at the repository root.

## Overview

AXIOM (Automated eXecution of Integrity & Operational Metrics) protects codebase quality by enforcing architectural rules and "blast radius" limits on changes.

The configuration system allows you to:

- Define **Protection Zones** (Red, Yellow, Green) with specific file limits.
- Configure **Validators** (e.g., Blast Radius strictness).
- Setup **Reporters** (Console, Log files, Metrics).
- Manage **Overrides** for emergency fixes or authorized large changes.

## Directory Structure

```
tools/axiom/
├── axiom_sentinel.py      # Pipeline orchestrator
├── pre_commit_hook.py     # Git hook entry point
├── install_hook.sh        # Hook installation script
├── config_loader.py       # Configuration loading
├── override_detector.py   # Override detection
├── validators/            # Validator interfaces and implementations
│   ├── security_validator.py # Security best practices check
├── reporters/             # Reporting interfaces (Console, Logs, etc.)
├── fixers/                # Auto-fixer interfaces
└── docs/
    ├── CONFIGURATION.md   # Detailed configuration reference
    ├── INSTALLATION.md    # Installation and Hook setup
    ├── MIGRATION_SECURITY.md # Security migration guide
    └── EXAMPLES.md        # Usage examples and scenarios
```

## Orchestration Pipeline

AXIOM follows a sequential execution pipeline:

1. **Discovery**: Identifies staged files via git.
2. **Validation**: Runs architectural and integrity validators.
3. **Override**: Applies bypasses based on commit message keywords.
4. **Fixing**: Triggers auto-fixers (if enabled).
5. **Reporting**: Outputs results to console and logs.
6. **Decision**: Blocks or allows the commit based on severity.

## Installation

To install the pre-commit hook:

```bash
bash tools/axiom/install_hook.sh
```

See [INSTALLATION.md](docs/INSTALLATION.md) for detailed setup and usage.

## Quick Start

1. **Check for `axiom.config.yaml`**: Ensure the file exists at the root of your project.
2. **Review Default Limits**:
   - **Red Zones** (Core Domain): Max 1 file per commit.
   - **Yellow Zones** (Infrastructure): Max 3 files per commit.
   - **Green Zones** (Tests/Apps): Max 10 files per commit.
3. **Validate Configuration**:
   The configuration is validated automatically when loaded. Invalid configurations will raise an error preventing AXIOM from running (fail-safe).

## Documentation

- [Detailed Configuration Reference](docs/CONFIGURATION.md) - Complete guide to all available options.
- [Usage Examples](docs/EXAMPLES.md) - Common scenarios, Python API usage, and troubleshooting.
