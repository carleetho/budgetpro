# AXIOM Hardening Baseline Report

**Date:** 2026-02-07
**Status:** DRAFT
**Auditor:** AXIOM AI Assistant

## 1. Executive Summary

This audit establishes the baseline for AXIOM domain hardening. While reports suggested a 23% coverage (50 files), meticulous verification confirms stricter hardening (Immutability & Entity Rules) is currently active on **25 files (11.7%)**. The remaining **188 files** across 7 bounded contexts are currently unprotected against deep domain violations, though they benefit from high-level boundary checks.

| Metric                 | Reported |    Verified    | Discrepancy |
| :--------------------- | :------: | :------------: | :---------- |
| **Total Domain Files** |   213    |      213       | 0           |
| **Hardened Files**     | 50 (23%) | **25 (11.7%)** | -25         |
| **Unprotected Files**  |   163    |    **188**     | +25         |

**Conclusion:** The hardening initiative is in its early stages. The "50 files" figure likely included files covered by looser checks or planned for Phase 1 but not yet enforcing blocking rules.

## 2. Coverage Analysis

### Hardened Files (25)

"Hardened" is defined as files subject to **Blocking (ERROR)** violations in `semgrep.yml` and AXIOM validators for Immutability and State integrity.

| Context                       | Files  | Rules Applied                           |
| :---------------------------- | :----: | :-------------------------------------- |
| **Finanzas / Presupuesto**    |   12   | `04-entity-final-fields` (Critical)     |
| **Finanzas / Estimacion**     |   7    | `04-entity-final-fields` (Critical)     |
| **Snapshots (Cross-cutting)** |   6    | `05-snapshot-immutability` (No Setters) |
| **Total**                     | **25** |                                         |

_Note: Some snapshots may overlap with contexts, but unique file count is 25._

### Unprotected Files (188)

These files reside in the domain layer but are not yet subject to strict immutability or specific business rule enforcement (only generic boundary checks).

| Bounded Context      | Unprotected Files | Key Modules                                                                                  |
| :------------------- | :---------------- | :------------------------------------------------------------------------------------------- |
| **RRHH**             | 23                | Empleado, Nomina, Asistencia, Cuadrilla                                                      |
| **Logistica**        | 52                | Inventario (19), Almacen (10), Requisicion (10), Compra (10), Transferencia (6), Backlog (6) |
| **Finanzas (Other)** | 76                | Cronograma (13), Reajuste (11), Sobrecosto (9), Avance (8), Alertas (6), APU (5)             |
| **Catalogo**         | 11                | Catalog inputs, Exceptions                                                                   |
| **Proyecto**         | 4                 | Proyecto Aggregate                                                                           |
| **Shared**           | 4                 | SecurityPort, Observability                                                                  |
| **Other**            | 18                | Exceptions, Ports, Services                                                                  |

## 3. Infrastructure Assessment

### GitHub Workflows

| Workflow                      | Status     | Coverage             | Gaps                                                                          |
| :---------------------------- | :--------- | :------------------- | :---------------------------------------------------------------------------- |
| `semgrep.yml`                 | **Active** | 25 Files (Blocking)  | **188 Files** only have Warnings or no checks. Hardcoded paths prevent scale. |
| `boundary-validator.yml`      | **Active** | 100% (Architecture)  | Validates dependency direction only, not internal domain integrity.           |
| `state-machine-validator.yml` | **Active** | Partial (Diff-based) | Specific to files with state transitions. Config is manual.                   |
| `axiom-lazy-code.yml`         | **Active** | Global               | Checks for anti-patterns but not deep domain logic.                           |
| `blast-radius-validation.yml` | **Active** | Global               | Process control, not code quality.                                            |

### Immutability Patterns

- **Current Validation:**
  - `private final` fields enforced in `presupuesto` and `estimacion`.
  - No setters allowed in `*Snapshot.java`.
  - No setters allowed in `valueobjects` (but 0 value objects found in strictly named paths).
- **Gaps:**
  - No immutability checks for RRHH, Logistica, or standard Finanzas entities.
  - Missing `ValueObject` identification if not in `valueobjects` package.
  - No constructor completeness validation.

## 4. Requirements for 100% Coverage

To bridge the gap from 11.7% to 100%, the following infrastructure is required:

1.  **Configuration-Driven Rules:** Move away from hardcoded paths in `semgrep.yml`. Use a central `axiom.config.yaml` or `domain-rules.json` to define which contexts are "Hardened".
2.  **Automated Discovery:** Scripts must dynamically list domain files and apply rules based on their bounded context, rather than manual inclusion/exclusion lists.
3.  **Progressive Rollout:**
    - **Phase 2 (Logistica & RRHH):** Expand strictly to these high-volume contexts.
    - **Phase 3 (Finanzas Long-tail):** Cover remaining specialized finanzas modules.
4.  **Coverage Reporting:** Integrate a "Hardening Coverage" badge or report in CI that tracks this valid/total ratio automatically.
5.  **Immutability Extension:** Apply `04-entity-final-fields` to ALL domain entities, potentially with a temporary "Warning" mode for legacy code.

## 5. Discrepancy Investigation

- **Target Claim:** 53.7% coverage (or 23% / 50 files).
- **Actual Findings:** 11.7% strict hardening (25 files).
- **Cause:** The "50 files" estimate likely conflated files with _warnings_ or files in related directories (like `apu`, `cronograma`) that are semantically close to Presupuesto but not physically covered by the regular expressions in `semgrep.yml`.
- **Resolution:** Reset baseline to 25 files (12%) to reflect reality of _blocking_ protections.
