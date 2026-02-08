# Audits Directory Guide

## 📁 Directory Structure

### `current/` - **USE THIS FOR DEVELOPMENT**

Active baseline reports reflecting system state post-Phase 0/1:

- `VERIFICATION_REPORT.md` - Critical gaps verification (P-01, ES-01, ES-02)
- `AXIOM_COVERAGE_BASELINE.md` - Domain hardening coverage metrics
- `DOCUMENTATION_COVERAGE_BASELINE.md` - Canonical notebook coverage

**Purpose:** Single source of truth for current system state
**Usage:** Reference these for all development decisions
**Updated:** After each phase completion

### `archive/` - **HISTORICAL REFERENCE ONLY**

Historical audits from pre-Phase 2 development (problems already resolved):

- `2026-01-12/` - Initial implementation audits
- `2026-01-18/` - Sprint audits and change reports
- `2026-02-07/` - Gap analysis and module audits

**Purpose:** Historical context and decision traceability
**Usage:** Reference only for understanding past decisions
**⚠️ WARNING:** Problems described here are already fixed - do not use for current state

### Root Files - **OPERATIONAL DOCUMENTATION**

Active operational files (not archived):

- `AXIOM_COVERAGE_DASHBOARD.md` - Real-time coverage dashboard
- `INVENTARIO_REGLAS_EXISTENTES_FASE1.md` - 161 verified business rules
- `LOGISTICA_VIOLATION_REPORT.md` - Active violation tracking
- Other active reports and inventories

## 🤖 AI Agent Guidelines

1. **Always use `current/` for system state** - These reflect post-Phase 0/1 reality
2. **Ignore `archive/` by default** - Excluded via `.cursorignore`
3. **Reference root operational files** - These are actively maintained
4. **Never assume problems from archive still exist** - Verify in `current/`

## 📊 Coverage Tracking

Current baselines (from `current/`):

- Critical Gaps: 3/3 verified resolved
- AXIOM Coverage: [See AXIOM_COVERAGE_BASELINE.md]
- Documentation Coverage: [See DOCUMENTATION_COVERAGE_BASELINE.md]
