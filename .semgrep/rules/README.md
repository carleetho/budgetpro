# Semgrep Custom Rules Index

This directory contains custom Semgrep rules for the BudgetPro project, organized by category.

## Directory Structure

- `security/`: Rules for identifying security vulnerabilities and bad practices.
- `domain/`: Business logic constraints and domain-specific safety checks.
  - **Immutability Validator**: Enforces immutability in domain entities, snapshots, and value objects. See [IMMUTABILITY_VALIDATOR.md](domain/IMMUTABILITY_VALIDATOR.md) for comprehensive documentation.
- `architecture/`: Enforcement of hexagonal architecture boundaries and dependency rules.
- `performance/`: Rules to identify potential performance bottlenecks or inefficient patterns.
- `quality/`: General code quality, naming conventions, and style enforcement.

## Contribution Guidelines

1. **Rule ID**: Follow the pattern `budgetpro.<category>.<subcategory>.<rule-id>`.
2. **Severity**:
   - `CRITICAL`: Immediate blocking. Safety or security breach.
   - `HIGH`: Blocking in PR. Serious violations of architecture or logic.
   - `MEDIUM`: Warning. Significant but non-blocking code quality issues.
   - `LOW`: Info/Warning. Minor improvements or stylistic suggestions.
3. **Documentation**: Every rule must have a `message` and `metadata` section explaining the "Why" and "How to fix".
4. **Testing**: Every rule MUST have a corresponding test file in `.semgrep/tests/<category>/`.
