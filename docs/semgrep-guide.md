# Semgrep Developer Guide

This guide explains how to use Semgrep for static analysis in the BudgetPro project.

## Installation

To run Semgrep locally, you need Python and `pip` installed.

```bash
pip install semgrep
```

Verify the installation:

```bash
semgrep --version
```

## Quick Start

### Scan Everything

Run all custom rules using the local profile (non-blocking):

```bash
./.semgrep/scripts/scan-all.sh
```

Alternatively, using the CLI directly:

```bash
semgrep scan --config .semgrep/config/local.yaml .
```

### Scan Specific Category

You can scan specific categories (security, domain, architecture, performance, quality):

```bash
./.semgrep/scripts/scan-category.sh security
```

### Run Automated Tests for Rules

To verify that custom rules are working correctly:

```bash
semgrep --test .semgrep/rules/
```

## Understanding Severity Levels

- **ERROR** (Critical/High): Blocking issues. Security vulnerabilities or critical logic errors. Must be fixed before merging.
- **WARNING** (Medium): Significant issues or architecture violations. Should be addressed.
- **INFO** (Low): Code quality suggestions or minor performance improvements.

## Handling False Positives

If a rule triggers a false positive, you can suppress it using a comment on the same or previous line:

```java
// nosemgrep: budgetpro.security.hardcoded-secrets
String myNotSecret = "this-is-fine";
```

_Always provide a reason for suppression._

## Auto-fix

Some rules support auto-fixing. You can apply fixes to the codebase:

```bash
semgrep scan --config .semgrep/rules/ --autofix
```

_Review all changes before committing._
