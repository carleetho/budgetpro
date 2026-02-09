# Audit Report: RRHH Module

**Date:** 2026-02-07
**Module:** RRHH (Human Resources)
**Canonical Spec:** `docs/canonical/modules/RRHH_MODULE_CANONICAL.md`
**Auditor:** AXIOM AI Assistant

## 1. Executive Summary

The RRHH module is in a **Foundational** state (20% maturity claim in canonical), but code inspection reveals a more advanced domain model than documented. While the canonical spec lists only 3 rules (mostly missing), the codebase implements strict invariants for labor configuration, employee management, and attendance logic.

**Key Findings:**

- **Documentation Lag:** The canonical spec is severely outdated (missing ~10 implemented rules).
- **Hidden Complexity:** `ConfiguracionLaboralExtendida` handles complex regime factors (regional, risk, overnight) not mentioned in the high-level spec.
- **Critical Gap:** No explicit invariants found for "Civil Construction Regime" caps (Rule R-01 in canonical is marked "Config only" but no code enforces the caps logic found in industry standards).

## 2. Rule Verification Status

### 2.1. Documented in Canonical (Verification)

| ID   | Rule Description                                         | Status      | Code Evidences (or Gap)                                                                 |
| :--- | :------------------------------------------------------- | :---------- | :-------------------------------------------------------------------------------------- |
| R-01 | **Labor Regime:** Civil Construction caps apply.         | 🟡 Partial  | `ConfiguracionLaboralExtendida` exists but only defines factors, not the capping logic. |
| R-02 | **Attendance:** No attendance for inactive workers.      | 🔴 Missing  | `AsistenciaRegistro.registrar` does not check `Empleado.estado`.                        |
| R-03 | **Double Booking:** Worker cannot be effectively cloned. | ✅ Verified | `AsistenciaRegistro.detectOverlap` explicitly implements this check.                    |

### 2.2. Discovered Rules (Code Inspection)

These rules are implemented in Java but missing from the Canonical Spec.

| ID (New) | Rule Description                                                                         | Type      | Source Class                    |
| :------- | :--------------------------------------------------------------------------------------- | :-------- | :------------------------------ |
| **R-04** | **Config Integrity:** Labor days, holidays cannot be negative. Factors must be positive. | Invariant | `ConfiguracionLaboralExtendida` |
| **R-05** | **Social Security Cap:** SS % must be between 0 and 100.                                 | Financial | `ConfiguracionLaboralExtendida` |
| **R-06** | **Reference Integrity:** Employee must have valid ID, Name, Lastname, and Status.        | Invariant | `Empleado`                      |
| **R-07** | **History Continuity:** New labor condition date cannot be before current start date.    | Temporal  | `Empleado`                      |
| **R-08** | **Overnight Shift:** If exit time < entry time, it counts as overnight (next day).       | Logic     | `AsistenciaRegistro`            |
| **R-09** | **FSR Calculation:** FSR = (Total Paid / Total Worked) + SS Factor.                      | Financial | `CalculadorFSR`                 |
| **R-10** | **Config Closure:** Closing date cannot be before start date.                            | Temporal  | `ConfiguracionLaboralExtendida` |

## 3. Context & Completeness Analysis

| Dimension          | Status     | Notes                                                                       |
| :----------------- | :--------- | :-------------------------------------------------------------------------- |
| **Domain Model**   | 🟡 Good    | Entities `Empleado`, `HistorialLaboral`, `Asistencia` are well defined.     |
| **Data Contracts** | 🔴 Low     | Canonical JSON schemas are missing for `Asistencia` and `Planilla`.         |
| **Business Logic** | 🟡 Partial | FSR calculation is present, but true "Payroll" generation logic is missing. |

## 4. Recommendations

1.  **Update Canonical:** Promote rules R-04 through R-10 to the `RRHH_MODULE_CANONICAL.md`.
2.  **Fix R-02:** Add validation in `AsistenciaRegistro.registrar` to fail if `Empleado` is `INACTIVO` (requires fetching Empleado).
3.  **Formalize Regime:** Create a `RegimenLaboral` strategy to encapsulate R-01 logic explicitly.

---

**Traceability:**

- 1 Verified Rule (R-03)
- 1 Partial Rule (R-01)
- 1 Failed Rule (R-02)
- 7 Newly Discovered Rules (R-04..R-10)
