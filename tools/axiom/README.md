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
├── config_loader.py       # Main entry point for loading configuration
├── override_detector.py   # Detection of commit message override keywords
├── axiom.config.yaml      # (Template) Default configuration file
└── docs/
    ├── CONFIGURATION.md   # Detailed configuration reference
    └── EXAMPLES.md        # Usage examples and scenarios
```

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
