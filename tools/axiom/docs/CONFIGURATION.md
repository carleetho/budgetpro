# AXIOM Configuration Reference

This document provides a detailed reference for all configuration options available in `axiom.config.yaml`.

## File Format

The configuration file uses **YAML** syntax. The system uses a **Deep Merge** strategy:

- If a file is present, its values override the defaults.
- Sections missing from the file will fallback to system defaults.

## Configuration Sections

### 1. `protection_zones`

Defines specific limits for different parts of the codebase based on risk.

```yaml
protection_zones:
  red:
    - path: "domain/core"
      max_files: 1
  yellow:
    - path: "infrastructure"
      max_files: 3
  green:
    - path: "tests"
      max_files: 10
```

- **path**: Relative path to the directory (no leading `/`). Overlapping paths (e.g., `domain` vs `domain/core`) are **forbidden** and will cause a validation error.
- **max_files**: Maximum number of changed files allowed in a single commit/PR for this zone. Must be a positive integer.

### 2. `validators`

Configures the behavior of specific validation engines.

```yaml
validators:
  blast_radius:
    enabled: true
    threshold: 10
    strictness: "blocking"
```

- **enabled**: Enable/disable this validator.
- **threshold**: Global limit on total files changed (unless deeper zone limit applies).
- **strictness**:
  - `blocking`: Validation failure breaks the build.
  - `warning`: validation failure logs a warning but proceeds.
  - `hybrid`: `blocking` for Red zones, `warning` for others.

### 3. `reporters`

Configures where validation results are sent. **At least one reporter must be enabled.**

```yaml
reporters:
  console:
    enabled: true # Real-time terminal output with colors
  log_file:
    enabled: true # Persistent audit trail
    path: ".budgetpro/validation.log"
  metrics:
    enabled: true # historical data for trend analysis
    path: ".budgetpro/metrics.json"
```

- **Environment Variables**: Paths support interpolation, e.g., `${LOG_PATH}`.

### 4. `auto_fix`

Configures automatic remediation of issues.

```yaml
auto_fix:
  enabled: false
  safe_only: true
```

- **safe_only**: If true, only applies "safe" fixes (formatting, imports) and avoids logic changes.

### 5. `overrides`

Controls the manual bypass system.

```yaml
overrides:
  enabled_keywords: []
```

_(Note: Keywords are currently hardcoded in `override_detector.py` but enabled via structure here for future extensibility)_

## Environment Variables

You can use environment variables in string values (like paths) using `${VAR_NAME}` syntax.
Example:

```yaml
reporters:
  log_file:
    path: "${AXIOM_LOGS_DIR}/axiom.log"
```

## Validation Rules

The configuration loader enforces strict integrity:

1. **No Overlaps**: You cannot define a zone `domain` and `domain/core` simultaneously.
2. **Mandatory Reporting**: At least one reporter must be active.
3. **Type Safety**: `max_files` must be positive integers; `strictness` must be a valid enum.
