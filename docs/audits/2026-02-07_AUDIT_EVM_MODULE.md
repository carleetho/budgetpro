# Audit Report: EVM Module

**Date:** 2026-02-07
**Module:** EVM (Earned Value Management)
**Canonical Spec:** `docs/canonical/modules/EVM_MODULE_CANONICAL.md`
**Auditor:** AXIOM AI Assistant

## 1. Executive Summary

The EVM module is in a **Functional (50%)** state, focused on snapshot metrics and physical progress registration. The core "Earned Value" logic is implemented in `EVMSnapshot`, but the critical "Metrado Cap" governance is currently soft (warning-only in `ControlAvanceService`).

**Key Findings:**

- **Soft Governance:** The "Metrado Cap" (Rule E-01) is implemented as a `System.out.println` warning in `ControlAvanceService`, not a blocking exception. This is a significant deviation from strict governance.
- **Rich Metrics:** The `EVMSnapshot` entity contains sophisticated logic for CPI, SPI, EAC, and ETC calculations, exceeding the "Basic" status claim in the canonical spec.
- **Valuation Flow:** `Valuacion` entity exists with state transitions (BORRADOR -> APROBADA), supporting the financial cycle.

## 2. Rule Verification Status

### 2.1. Documented in Canonical (Verification)

| ID   | Rule Description                                      | Status     | Code Evidences (or Gap)                                                             |
| :--- | :---------------------------------------------------- | :--------- | :---------------------------------------------------------------------------------- |
| E-01 | **Metrado Cap:** Progress cannot exceed Budget.       | 🟡 Soft    | `ControlAvanceService` calculates cumulative but only prints a WARNING if exceeded. |
| E-02 | **Date Constraint:** No future progress.              | 🟡 Partial | `AvanceFisico` does not explicitly check `fecha <= now`.                            |
| E-03 | **Active Project:** Only active projects.             | 🔴 Missing | `ControlAvanceService` does not check Project status.                               |
| E-04 | **Period Consistency:** Align with reporting periods. | 🔴 Missing | No logic found enforcing period alignment in `Valuacion`.                           |

### 2.2. Discovered Rules (Code Inspection)

These rules are implemented in Java but missing from the Canonical Spec.

| ID (New) | Rule Description                                                    | Type       | Source Class   |
| :------- | :------------------------------------------------------------------ | :--------- | :------------- |
| **E-05** | **EV Cap:** Earned Value cannot exceed Budget at Completion (BAC).  | Invariant  | `EVMSnapshot`  |
| **E-06** | **Positive Progress:** Executed metrado cannot be negative.         | Domain     | `AvanceFisico` |
| **E-07** | **Valuation Immutability:** Approved Valuations cannot be modified. | Governance | `Valuacion`    |
| **E-08** | **Code Normalization:** Valuation code is trim/uppercase.           | Technical  | `Valuacion`    |
| **E-09** | **Snapshot Integrity:** PV, EV, AC, BAC must be non-null.           | Domain     | `EVMSnapshot`  |

## 3. Context & Completeness Analysis

| Dimension          | Status     | Notes                                                                              |
| :----------------- | :--------- | :--------------------------------------------------------------------------------- |
| **Domain Model**   | 🟡 Good    | `EVMSnapshot` is rich. `Valuacion` is solid. `AvanceFisico` is basic.              |
| **Data Contracts** | 🔴 Low     | No JSON schemas found for EVM metrics in code.                                     |
| **Business Logic** | 🟡 Partial | The "Soft Cap" on metrado is a governance risk. Needs to be configurable to Block. |

## 4. Recommendations

1.  **Harden E-01:** Upgrade `ControlAvanceService` to throw `MetradoExcedidoException` instead of printing a warning, or make it configurable (Strict Mode).
2.  **Implement E-02 & E-03:** Add date and status checks in `ControlAvanceService.registrarAvance`.
3.  **Document E-05:** This is a critical financial invariant that prevents "over-earning" in metrics.

---

**Traceability:**

- 0 Strictly Verified Rules (All partially implemented or missing)
- 4 Partial/Soft Rules
- 5 Newly Discovered Rules (E-05..E-09)
