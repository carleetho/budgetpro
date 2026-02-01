# DOMAIN_INVARIANTS_CURRENT.md - Current State Radiography

> **Scope**: Cross-cutting
> **Last Updated**: 2026-01-31
> **Authors**: Antigravity (derived from Codebase & Architecture Docs)

## 1. Overview

This notebook captures the currently implemented business invariants across the BudgetPro domain. It serves as the baseline for compliance checking.

## 2. Invariants by Module

### 2.1. Module: Presupuesto

| ID   | Invariant / Business Rule                                                                                   | Status | Implementation Reference                   |
| ---- | ----------------------------------------------------------------------------------------------------------- | ------ | ------------------------------------------ |
| P-01 | **No Modification Frozen**: A budget cannot be modified (add/remove items) once it is in `CONGELADO` state. | ✅     | `Presupuesto.java`, `PresupuestoValidator` |
| P-02 | **WBS Hierarchy**: Partidas must form a strict hierarchical tree structure (Parent-Child).                  | ✅     | `Partida.java` (padreId)                   |
| P-03 | **Leaf Node APU**: Only leaf partidas (lowest level) can have an associated APU or APUSnapshot.             | ✅     | `PartidaValidator`                         |
| P-04 | **Snapshot Immutability**: APUSnapshots are immutable upon creation, except for `rendimientoVigente`.       | ✅     | `APUSnapshot.java`                         |

### 2.2. Module: EVM (Earned Value Management)

| ID   | Invariant / Business Rule                                                                                                   | Status | Implementation Reference                   |
| ---- | --------------------------------------------------------------------------------------------------------------------------- | ------ | ------------------------------------------ |
| E-01 | **Metrado Cap**: Physical concrete progress (`metradoEjecutado`) cannot exceed the budgeted metrado without a Change Order. | ✅     | `AvanceFisico.java`, `ProduccionValidator` |
| E-02 | **Date Constraint**: Progress cannot be reported with a future date.                                                        | ✅     | `ProduccionValidator`                      |

### 2.3. Module: Cronograma

| ID   | Invariant / Business Rule                                                                    | Status | Implementation Reference      |
| ---- | -------------------------------------------------------------------------------------------- | ------ | ----------------------------- |
| C-01 | **Program Frozen**: When Budget is approved, the Program must be frozen (Baseline creation). | ✅     | `ProgramaObra.java`           |
| C-02 | **Dependency Integrity**: Start-to-Finish dependencies cannot create circular references.    | 🟡     | `CronogramaService` (Implied) |

### 2.4. Module: Compras

| ID   | Invariant / Business Rule                                                                                         | Status | Implementation Reference            |
| ---- | ----------------------------------------------------------------------------------------------------------------- | ------ | ----------------------------------- |
| L-01 | **Budget Check**: A purchase cannot be authorized if it exceeds the available budget balance (Saldo por Ejercer). | ✅     | `ProcesarCompraDirectaService.java` |
| L-02 | **Independent Prices**: Purchase prices are independent of APU Snapshot reference prices.                         | ✅     | `Compra.java`                       |

### 2.5. Module: Estimacion

| ID    | Invariant / Business Rule                                                                                      | Status | Implementation Reference        |
| ----- | -------------------------------------------------------------------------------------------------------------- | ------ | ------------------------------- |
| ES-01 | **Sequential Approval**: Estimations must be approved in sequential order number.                              | ✅     | `EstimacionValidator`           |
| ES-02 | **Wallet Impact**: Approval of an estimation automatically triggers an ingress movement in the Project Wallet. | ✅     | `AprobarEstimacionUseCase.java` |

### 2.6. Module: Billetera

| ID   | Invariant / Business Rule                                          | Status | Implementation Reference |
| ---- | ------------------------------------------------------------------ | ------ | ------------------------ |
| B-01 | **Non-Negative**: Wallet balance cannot be negative (Debit check). | ✅     | `Billetera.java`         |

## 3. Risks & Technical Debt

- **Dependency Cycle**: Potential cycle in Dependency checking within Cronograma.
- **Legacy APUs**: Coexistence of Legacy APU and APUSnapshot creates complex validation logic in Partida.
