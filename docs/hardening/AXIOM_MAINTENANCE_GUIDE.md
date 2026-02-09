# AXIOM Maintenance Guide

**Version:** 1.0
**Last Updated:** 2026-02-09
**Status:** OFFICIAL

## Overview
This guide defines the procedures for maintaining AXIOM compliance in the BudgetPro backend. With the domain layer 100% hardened, strict adherence to these guidelines is mandatory to prevent regression.

## 1. Compliance Rules

### 1.1 Immutability
- **Entities:** Must use `private final` fields. No setters. Use `wither` pattern or `reconstruir` methods.
- **Value Objects:** Must be immutable. No setters.
- **Collections:** Must return `Collections.unmodifiableList/Set/Map` or `List.copyOf`.
- **Snapshots:** Must be immutable representations of state.

### 1.2 Hexagonal Architecture
- **Dependencies:** Domain -> Application -> Infrastructure.
- **Isolation:** Domain must not depend on external frameworks (Spring, Hibernate annotations restricted to approved set).

## 2. Validator Configuration

Configuration files are located in `.budgetpro/`:
- `axiom.config.yaml`: Main configuration.
- `domain-validator.yaml`: Domain hardening rules.
- `.semgrep/generated-domain-hardening.yml`: Auto-generated Semgrep rules.

### 2.1 Regeneration
If configuration changes, regenerate rules:
```bash
python3 tools/generate_domain_rules.py
```

## 3. Handling Violations

### 3.1 New Features
- Run `./axiom.sh --dry-run` before committing.
- Ensure new domain entities follow the template in `docs/hardening/AXIOM_HARDENING_PATTERNS.md`.

### 3.2 Exemptions
Exemptions are **restricted**.
- **Annotations:** `@Immutable` is preferred.
- **Supressions:** Use `// nosemgrep: rule-id` ONLY if:
    1. It is a Builder pattern.
    2. It is a legacy mutable entity approved by architecture (e.g., `Empleado`, `Presupuesto`).
    3. It is a JPA requirement that cannot be solved otherwise.

## 4. CI/CD Integration

The hardening is enforced in the build pipeline:
- **Semgrep Scan:** Blocks PRs with ERROR severity findings.
- **Build Breaker:** Maven build fails if strict mode violations occur.

## 5. Emergency Procedures

If a critical fix is blocked by AXIOM:
1. Declare `MODE_0` or `MODE_1` in `.cursorrules` (if applicable).
2. Use specific `nosemgrep` suppression with justification comment.
3. Schedule immediate tech debt cleanup ticket.
