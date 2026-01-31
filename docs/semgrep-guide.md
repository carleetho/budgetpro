# Semgrep Developer Guide

This guide explains how to use Semgrep for static analysis in the BudgetPro project.

## Overview

Semgrep is integrated into BudgetPro's development workflow to enforce:

- **Security**: Prevent vulnerabilities and bad practices
- **Domain Integrity**: Enforce immutability and business logic constraints
- **Architecture**: Maintain hexagonal architecture boundaries
- **Performance**: Identify potential bottlenecks
- **Quality**: Ensure code quality and consistency

**Current Status**:

- 32 rules across 5 categories
- Auto-discovery from `.semgrep/rules/`
- Context-aware enforcement (local, PR, main)
- Integrated with AXIOM and GitHub Actions

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

- **ERROR** (🔴 Blocking): Security vulnerabilities, critical logic errors, or architecture violations. **BLOCKS PR merge** in CI/CD.
- **WARNING** (⚠️ Non-blocking): Significant issues that should be addressed. Shows in PR comments but **allows merge**.
- **INFO** (ℹ️ Informational): Code quality suggestions or minor improvements. Hidden by default in local scans.

### Enforcement by Context

| Context   | ERROR            | WARNING      | INFO   |
| --------- | ---------------- | ------------ | ------ |
| **Local** | Warning          | Info         | Hidden |
| **PR**    | **Blocks merge** | Allows merge | Logged |
| **Main**  | **Blocks push**  | Logged       | Logged |

### Current Distribution

- 🔴 **ERROR**: 21 rules (security, domain critical, architecture)
- ⚠️ **WARNING**: 8 rules (domain non-critical, performance, quality)
- ℹ️ **INFO**: 3 rules (quality suggestions)

See [Rule Catalog](file:///.semgrep/RULE_CATALOG.md) for complete list.

## Handling False Positives

If a rule triggers a false positive or you have a valid exception, you can suppress it using a `nosemgrep` annotation:

```java
// nosemgrep: budgetpro.security.hardcoded-secrets
// Justificación: Public API key documented at https://docs.provider.com/keys
// This is a PUBLIC key intentionally committed to version control.
String publicKey = "pk_live_abc123";
```

**CRITICAL**: All exceptions MUST include justification. See [Exception Guidelines](file:///.semgrep/docs/exception-guidelines.md) for:

- When exceptions are valid
- Required justification format
- Review process
- Audit procedures

**Invalid Exception** (will be rejected in code review):

```java
// nosemgrep: budgetpro.domain.immutability.entity-final-fields
// Justificación: Es más fácil así  ❌ REJECTED
private String campo;
```

## Auto-fix

Some rules support auto-fixing. You can apply fixes to the codebase:

```bash
semgrep scan --config .semgrep/rules/ --autofix
```

_Review all changes before committing._
