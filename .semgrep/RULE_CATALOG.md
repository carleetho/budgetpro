# Semgrep Rule Catalog

This document provides a comprehensive catalog of all Semgrep rules in the BudgetPro project.

**Auto-generated** - Do not edit manually. Run `.semgrep/scripts/generate-catalog.py` to update.

---

**Last Updated**: 2026-01-31 05:10:09 UTC

**Total Rules**: 21
**Total Rule Files**: 19


## ARCHITECTURE

| Rule ID | Severity | Description | File |
|---------|----------|-------------|------|
| `01-domain-layer-isolation` | 🔴 ERROR | Domain layer isolation violation. | [01-domain-layer-isolation.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/architecture/domain/01-domain-layer-isolation.yaml) |
| `02-transactional-boundary` | 🔴 ERROR | Transactional boundary violation. | [02-transactional-boundary.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/architecture/domain/02-transactional-boundary.yaml) |
| `03-dto-validation-boundary` | 🔴 ERROR | DTO validation boundary violation. | [03-dto-validation-boundary.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/architecture/infrastructure/03-dto-validation-boundary.yaml) |

## DOMAIN

| Rule ID | Severity | Description | File |
|---------|----------|-------------|------|
| `01-estimacion-state-machine` | 🔴 ERROR | Invalid Estimacion state transition. | [01-estimacion-state-machine.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/01-estimacion-state-machine.yaml) |
| `02-presupuesto-hash-immutability` | 🔴 ERROR | The 'integrityHashApproval' field is immutable after generation. | [02-presupuesto-hash-immutability.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/02-presupuesto-hash-immutability.yaml) |
| `03-bigdecimal-precision-standard` | 🔴 ERROR | Financial calculation precision violation. | [03-bigdecimal-precision-standard.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/03-bigdecimal-precision-standard.yaml) |
| `budgetpro.domain.immutability.entity-final-fields` | ⚠️  WARNING | INMUTABILIDAD: Campos sin 'final' en entidad de dominio. | [04-entity-final-fields.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/04-entity-final-fields.yaml) |
| `budgetpro.domain.immutability.entity-final-fields.critical` | 🔴 ERROR | INMUTABILIDAD: Campos sin 'final' en entidad de dominio crítico. | [04-entity-final-fields.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/04-entity-final-fields.yaml) |
| `budgetpro.domain.immutability.snapshot-markers` | ⚠️  WARNING | INMUTABILIDAD: Snapshot sin marcador de inmutabilidad. | [05-snapshot-immutability.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/05-snapshot-immutability.yaml) |
| `budgetpro.domain.immutability.snapshot-no-setters` | 🔴 ERROR | INMUTABILIDAD: Snapshot con setters detectado. | [05-snapshot-immutability.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/05-snapshot-immutability.yaml) |
| `budgetpro.domain.immutability.valueobject-no-setters` | 🔴 ERROR | INMUTABILIDAD: Value Object con setters. | [06-valueobject-no-setters.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/domain/06-valueobject-no-setters.yaml) |

## PERFORMANCE

| Rule ID | Severity | Description | File |
|---------|----------|-------------|------|
| `budgetpro.performance.inefficient-bigdecimal-operations` | ℹ️  INFO | Inefficient BigDecimal scaling detected.  | [02-inefficient-bigdecimal-operations.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/performance/02-inefficient-bigdecimal-operations.yaml) |
| `budgetpro.performance.n-plus-one-query` | ⚠️  WARNING | Potential N+1 query or inefficient batch operation detected. | [01-n-plus-one-query.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/performance/01-n-plus-one-query.yaml) |

## QUALITY

| Rule ID | Severity | Description | File |
|---------|----------|-------------|------|
| `budgetpro.quality.exception-handling-standards` | ⚠️  WARNING | Generic or empty exception handling detected. | [02-exception-handling-standards.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/quality/02-exception-handling-standards.yaml) |
| `budgetpro.quality.logging-standards` | 🔴 ERROR | Potential sensitive data logging detected. | [03-logging-standards.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/quality/03-logging-standards.yaml) |
| `budgetpro.quality.null-safety-patterns` | ℹ️  INFO | Missing null-safety check for public method argument '$ARG'. | [01-null-safety-patterns.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/quality/01-null-safety-patterns.yaml) |

## SECURITY

| Rule ID | Severity | Description | File |
|---------|----------|-------------|------|
| `01-hardcoded-secrets` | 🔴 ERROR | Potential hardcoded secret found in string literal. | [01-hardcoded-secrets.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/security/01-hardcoded-secrets.yaml) |
| `02-jwt-weak-secret` | 🔴 ERROR | JWT secret is too weak (length < 32 characters). | [02-jwt-weak-secret.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/security/02-jwt-weak-secret.yaml) |
| `03-missing-input-validation` | ⚠️  WARNING | Missing @Valid annotation on @RequestBody parameter in $METHOD. | [03-missing-input-validation.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/security/03-missing-input-validation.yaml) |
| `04-cors-misconfiguration` | 🔴 ERROR | CORS configuration uses a wildcard "*". | [04-cors-misconfiguration.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/security/04-cors-misconfiguration.yaml) |
| `05-weak-cryptographic-algorithm` | 🔴 ERROR | Weak cryptographic algorithm "$ALG" detected. | [05-weak-cryptographic-algorithm.yaml](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/rules/security/05-weak-cryptographic-algorithm.yaml) |

---

## Summary by Severity

- 🔴 **ERROR**: 14 rules (blocking)
- ⚠️  **WARNING**: 5 rules (non-blocking)
- ℹ️  **INFO**: 2 rules (informational)

## Summary by Category

- **ARCHITECTURE**: 3 rules
- **DOMAIN**: 8 rules
- **PERFORMANCE**: 2 rules
- **QUALITY**: 3 rules
- **SECURITY**: 5 rules

## Enforcement Context

All rules are executed in the following contexts:

### Local Development
- **Config**: `.semgrep/config/local.yaml`
- **Enforcement**: All findings are warnings (non-blocking)
- **Purpose**: Early feedback without interrupting workflow

### Pull Request
- **Config**: `.semgrep/config/pr.yaml`
- **Enforcement**: ERROR blocks merge, WARNING allows merge
- **Purpose**: Quality gate before integration

### Main Branch
- **Config**: `.semgrep/config/main.yaml`
- **Enforcement**: ERROR blocks push, WARNING logged
- **Purpose**: Protection of main branch + metrics collection

---

## Documentation

- [Semgrep Developer Guide](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/docs/semgrep-guide.md)
- [Immutability Validator](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/.semgrep/docs/immutability-validator.md)
- [Exception Guidelines](file:///home/wazoox/Desktop/budgetpro-backend/.semgrep/.semgrep/docs/exception-guidelines.md)

---

**Generated by**: `.semgrep/scripts/generate-catalog.py`
