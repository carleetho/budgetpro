# Audit Report: Domain Invariants Radiography

**Date:** 2026-02-07
**Auditor:** Antigravity
**Subject:** `docs/canonical/radiography/DOMAIN_INVARIANTS_CURRENT.md`
**Reference:** `docs/audits/INVENTARIO_REGLAS_EXISTENTES_FASE1.md` (161 Rules)

## 1. Executive Summary

The current radiography document `DOMAIN_INVARIANTS_CURRENT.md` is **severely incomplete**, documenting only **15** out of **161** identified business rules (9.3% coverage). While it captures high-level critical invariants (like P-01 Frozen Budget), it misses the vast majority of granular validations, financial constraints, and governance rules that are already implemented and audited in the codebase.

## 2. Completeness Assessment

| Metric                    | Count                   |
| :------------------------ | :---------------------- |
| **Documented Invariants** | 15                      |
| **Actual Inventory**      | 161                     |
| **Missing Rules**         | **146**                 |
| **Coverage Score**        | **9.3%** (CRITICAL GAP) |

## 3. Critical Gaps by Category

The following categories of rules are largely absent from the radiography:

### 3.1. Financial Invariants (Missing ~40 rules)

- **Non-Negative Values**: Extensive `BigDecimal` checks across all modules (e.g., REGLA-014, REGLA-015, REGLA-032, REGLA-036).
- **Calculation Logic**: Formulas for `montoNeto` (REGLA-011), `subtotal` (REGLA-032), `costoReal` (REGLA-123).
- **Percentage Caps**: Safety limits on taxes/overhead (REGLA-023, REGLA-024).

### 3.2. Governance & Process Rules (Missing ~50 rules)

- **State Machines**: Detailed transitions for `Proyecto` (REGLA-108), `Presupuesto` (REGLA-111), `Recurso` (REGLA-063).
- **Inter-Module Dependencies**: "No Purchases without Budget" (REGLA-033), "No Inventory without Purchase" (REGLA-117).
- **Auditing**: Requirement for `created_by` (REGLA-073) and immutable history (REGLA-103).

### 3.3. Technical Constraints (Missing ~40 rules)

- **Input Validation**: `@NotNull`, `@NotBlank`, `@Positive` annotations on DTOs (REGLA-076 to REGLA-099).
- **Database Constraints**: `CHECK` constraints and `UNIQUE` indexes (REGLA-055 to REGLA-070).

## 4. Discrepancies & Conflicts

- **C-02 (Dependency Integrity)** is marked as `🟡 Partial` in Radiography, but REGLA-019 ("Activity cannot be predecessor of itself") and REGLA-020 ("Start-to-Finish validation") are **Explícita** (Explicit) in the code. The radiography understates the implementation maturity.
- **P-01 (No Modification Frozen)** is marked `✅`, which aligns with REGLA-001 and REGLA-047.
- **L-01 (Budget Check)** is marked `✅`, which aligns with REGLA-004 and REGLA-033.

## 5. Recommendations

1. **Mass Ingestion**: Update `DOMAIN_INVARIANTS_CURRENT.md` to import the structured list of 161 rules, categorized by Domain, Financial, Governance, and Technical types.
2. **Promote Technical Rules**: Elevate DB constraints (CHECKs) to domain invariant status in documentation, as they enforce business truth.
3. **Update Status**: Change C-02 status to ✅ based on REGLA-019/020 evidence.
4. **Link to Inventory**: Make `DOMAIN_INVARIANTS_CURRENT.md` a summary view that links to the detailed `INVENTARIO_REGLAS_EXISTENTES.md` for the full list.
