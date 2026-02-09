# Audit Report: Module Specs Radiography

**Date:** 2026-02-07
**Auditor:** Antigravity
**Subject:** `docs/canonical/radiography/MODULE_SPECS_CURRENT.md`
**Reference:** `docs/audits/INVENTARIO_REGLAS_EXISTENTES_FASE1.md`

## 1. Executive Summary

`MODULE_SPECS_CURRENT.md` is adequate as a high-level maturity scorecard but **fails as a specification document**. It does not reference the detailed, module-specific constraints (REGLA-105 to REGLA-161) that define the _behavior_ of these modules. It treats modules as black boxes without documenting their **Inter-Module Behavioral Contracts**.

## 2. Completeness Assessment

| Metric               | Status         | Notes                                                                                                  |
| :------------------- | :------------- | :----------------------------------------------------------------------------------------------------- |
| **Scope definition** | ✅ Complete    | Correctly identifies modules and responsibilities.                                                     |
| **Maturity scoring** | ✅ Accurate    | Aligns with findings (RRHH is skeletal, Presupuesto is advanced).                                      |
| **Behavioral Spec**  | 🔴 **Missing** | The "Rules of Engagement" between modules (e.g., "Project must be ACTIVE for X to happen") are absent. |

## 3. Findings (The Logic Gap)

The inventory contains a massive block of "Governance Rules" (REGLA-105 through REGLA-161) derived from `*_SPECS.md` files. These define the **Lifecycle Coupling** of modules:

- **Project Lifecycle**: Rules 105-109 dictate exactly when other modules can operate (e.g., "No purchases in Draft"). This is the **Core Orchestration Logic** of the ERP and is missing from the Radiography summary.
- **Budget Lifecycle**: Rules 110-114 define the rigid "Frozen" contract that constrains Compras and Inventario.
- **Inventory rules**: Rules 118-121 define the strict dependency on Purchase and Budget.

## 4. Recommendations

1. **Lifecycle Matrix**: Create a matrix in `MODULE_SPECS_CURRENT.md` showing which modules are "Unlocked" by which Project/Budget states (derived from REGLA-105, 106, 110).
2. **Reference Specifics**: Instead of just "Functionality", list the **Key Invariants** per module (e.g., "Estimacion: Sequential Check", "Partida: Hierarchy Check").
3. **Cross-Link**: Explicitly link to the existing `docs/modules/*_SPECS.md` files which appear to be the source of truth for Rules 105-161.
