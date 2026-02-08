# Audit Report: Cronograma Module

**Date:** 2026-02-07
**Module:** Cronograma (Scheduling)
**Canonical Spec:** `docs/canonical/modules/CRONOGRAMA_MODULE_CANONICAL.md`
**Auditor:** AXIOM AI Assistant

## 1. Executive Summary

The Cronograma module is in a **Functional (60%)** state, supporting basic Gantt scheduling with strict "Freeze" capabilities for baselining. However, the scheduling engine is naive: it lacks calendar awareness (working days vs calendar days) and robust circular dependency detection.

**Key Findings:**

- **Robust Baseline:** The "Frozen" state is strictly enforced in `ProgramaObra`, preventing any modifications to baselined schedules (Rule C-01).
- **Naive Algorithm:** `CalculoCronogramaService` treats all days as working days (Rule C-04 Missing) and only validates direct 1-level dependencies, missing cycle detection.
- **Dependency Type Constraint:** Only "Finish-to-Start" (FS) dependencies are supported by the logic in `CalculoCronogramaService`.

## 2. Rule Verification Status

### 2.1. Documented in Canonical (Verification)

| ID   | Rule Description                                 | Status      | Code Evidences (or Gap)                                                                 |
| :--- | :----------------------------------------------- | :---------- | :-------------------------------------------------------------------------------------- |
| C-01 | **Program Frozen:** Baseline cannot be modified. | ✅ Verified | `ProgramaObra.actualizarFechas` throws `CronogramaCongeladoException`.                  |
| C-02 | **Dependency Integrity:** No circular refs.      | 🟡 Partial  | `CalculoCronogramaService` checks `start >= end` but lacks topological sort for cycles. |
| C-03 | **One Activity Per Leaf:** 1:1 with Partida.     | ✅ Verified | `ActividadProgramada` has mandatory `partidaId`.                                        |
| C-04 | **Working Days:** Skip weekends/holidays.        | 🔴 Missing  | `ChronoUnit.DAYS` is used directly; no Calendar logic found.                            |

### 2.2. Discovered Rules (Code Inspection)

These rules are implemented in Java but missing from the Canonical Spec.

| ID (New) | Rule Description                                                  | Type      | Source Class                |
| :------- | :---------------------------------------------------------------- | :-------- | :-------------------------- |
| **C-05** | **Temporal Consistency:** End Date cannot be before Start Date.   | Invariant | `ProgramaObra`, `Actividad` |
| **C-06** | **Self-Dependency:** Activity cannot depend on itself.            | Logic     | `ActividadProgramada`       |
| **C-07** | **Financing Duration:** Duration in months = ceil((days+29)/30).  | Financial | `CalculoCronogramaService`  |
| **C-08** | **Freeze Metadata:** Must record WHO and WHEN froze the schedule. | Auditing  | `ProgramaObra`              |

## 3. Context & Completeness Analysis

| Dimension          | Status  | Notes                                                                               |
| :----------------- | :------ | :---------------------------------------------------------------------------------- |
| **Domain Model**   | 🟡 Good | `ProgramaObra` and `Actividad` are solid.                                           |
| **Data Contracts** | 🔴 Low  | JSON schema for `Dependencia` is missing `lag_days` support in code.                |
| **Business Logic** | 🔴 Weak | The lack of a true CPM (Critical Path Method) engine with calendars is a major gap. |

## 4. Recommendations

1.  **Implement C-04:** Introduce a `CalendarioProyecto` value object to handle working days calculation.
2.  **Upgrade C-02:** Replace the simple loop check with a Kahn's Algorithm or DFS implementation to detect cycles in `CalculoCronogramaService`.
3.  **Harden C-01:** The Freeze Guard is excellent. Ensure it persists to the Database correctly (check Entity mapping).

---

**Traceability:**

- 2 Verified Rules (C-01, C-03)
- 1 Partial Rule (C-02)
- 1 Failed Rule (C-04)
- 4 Newly Discovered Rules (C-05..C-08)
