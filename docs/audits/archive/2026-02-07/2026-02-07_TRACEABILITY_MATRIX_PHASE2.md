# Traceability Matrix: Phase 2 Audits

**Date:** 2026-02-07
**Scope:** RRHH, Billetera, EVM, Cronograma, Cross-Cutting
**Status:** Audit Completed

## 1. RRHH Module Rules

| Rule ID  | Description                        | Source    | Status      | Technical Trace (Class/Method)                 |
| :------- | :--------------------------------- | :-------- | :---------- | :--------------------------------------------- |
| **R-01** | Civil Construction labor caps      | Canonical | 🟡 Partial  | `ConfiguracionLaboralExtendida` (Factors only) |
| **R-02** | No attendance for inactive workers | Canonical | 🔴 Missing  | `AsistenciaRegistro` (No check)                |
| **R-03** | Double Booking prevention          | Canonical | ✅ Verified | `AsistenciaRegistro.detectOverlap()`           |
| **R-04** | Config Integrity (Non-negative)    | Code      | ✅ Verified | `ConfiguracionLaboralExtendida`                |
| **R-05** | Social Security Cap (0-100)        | Code      | ✅ Verified | `ConfiguracionLaboralExtendida`                |
| **R-06** | Employee Reference Integrity       | Code      | ✅ Verified | `Empleado`                                     |
| **R-07** | History Continuity check           | Code      | ✅ Verified | `Empleado.actualizarCondicionesLaborales`      |
| **R-08** | Overnight Shift Logic              | Code      | ✅ Verified | `AsistenciaRegistro.esOvernight`               |
| **R-09** | FSR Calculation Logic              | Code      | ✅ Verified | `CalculadorFSR`                                |
| **R-10** | Config Closure StartDate Check     | Code      | ✅ Verified | `ConfiguracionLaboralExtendida.cerrar`         |

## 2. Billetera Module Rules

| Rule ID  | Description                   | Source    | Status      | Technical Trace (Class/Method)                   |
| :------- | :---------------------------- | :-------- | :---------- | :----------------------------------------------- |
| **B-01** | Non-Negative Balance          | Canonical | ✅ Verified | `Billetera.egresar`                              |
| **B-02** | Audit Trail Immutability      | Canonical | ✅ Verified | `Billetera` (Append-only list)                   |
| **B-03** | Currency Mix Prevention       | Canonical | ✅ Verified | `Billetera.ingresar/egresar`                     |
| **B-04** | Evidence Lock (Max 3 Pending) | Code      | ✅ Verified | `Billetera.contarMovimientosPendientesEvidencia` |
| **B-05** | Crypto Integrity Check (Hash) | Code      | ✅ Verified | `Billetera` (Integrity Exception)                |
| **B-06** | Amount Positivity (>0)        | Code      | ✅ Verified | `MovimientoCaja`                                 |
| **B-07** | Currency ISO Format           | Code      | ✅ Verified | `MovimientoCaja`                                 |
| **B-08** | Reference Obligation          | Code      | ✅ Verified | `MovimientoCaja`                                 |

## 3. EVM Module Rules

| Rule ID  | Description                      | Source    | Status      | Technical Trace (Class/Method)          |
| :------- | :------------------------------- | :-------- | :---------- | :-------------------------------------- |
| **E-01** | Metrado Cap (Progress <= Budget) | Canonical | 🟡 Soft     | `ControlAvanceService` (Warning only)   |
| **E-02** | Date Constraint (No Future)      | Canonical | 🟡 Partial  | `AvanceFisico` (Missing explicit check) |
| **E-03** | Active Project Constraint        | Canonical | 🔴 Missing  | `ControlAvanceService`                  |
| **E-04** | Period Consistency               | Canonical | 🔴 Missing  | `Valuacion`                             |
| **E-05** | EV Cap (EV <= BAC)               | Code      | ✅ Verified | `EVMSnapshot.validarInvariantes`        |
| **E-06** | Positive Progress                | Code      | ✅ Verified | `AvanceFisico`                          |
| **E-07** | Valuation Immutability           | Code      | ✅ Verified | `Valuacion.aprobar`                     |
| **E-08** | Valuation Code Normalization     | Code      | ✅ Verified | `Valuacion`                             |
| **E-09** | Snapshot Integrity (NotNull)     | Code      | ✅ Verified | `EVMSnapshot`                           |

## 4. Cronograma Module Rules

| Rule ID  | Description                     | Source    | Status      | Technical Trace (Class/Method)                    |
| :------- | :------------------------------ | :-------- | :---------- | :------------------------------------------------ |
| **C-01** | Program Frozen (Baseline)       | Canonical | ✅ Verified | `ProgramaObra.actualizarFechas`                   |
| **C-02** | Dependency Integrity (Cycles)   | Canonical | 🟡 Partial  | `CalculoCronograma` (Basic Check)                 |
| **C-03** | One Activity Per Leaf           | Canonical | ✅ Verified | `ActividadProgramada` (Mandatory PartidaId)       |
| **C-04** | Working Days Calculation        | Canonical | 🔴 Missing  | `CalculoCronogramaService` (Uses ChronoUnit.DAYS) |
| **C-05** | Date Consistency (End >= Start) | Code      | ✅ Verified | `ProgramaObra`, `ActividadProgramada`             |
| **C-06** | Self-Dependency Prevention      | Code      | ✅ Verified | `ActividadProgramada`                             |
| **C-07** | Financing Duration Logic        | Code      | ✅ Verified | `CalculoCronogramaService`                        |
| **C-08** | Freeze Metadata Auditing        | Code      | ✅ Verified | `ProgramaObra`                                    |

## 5. Cross-Cutting Module Rules

| Rule ID  | Description                     | Source    | Status      | Technical Trace (Class/Method) |
| :------- | :------------------------------ | :-------- | :---------- | :----------------------------- |
| **X-01** | Hexagonal Purity                | Canonical | ✅ Verified | `shared.port` Architecture     |
| **X-02** | Fail-Fast Validation            | Canonical | ✅ Verified | All Domain Constructors        |
| **X-03** | Auditability (User ID)          | Canonical | 🟡 Partial  | Ad-hoc implementations         |
| **X-04** | JWT Secret Strength (32+ chars) | Code      | ✅ Verified | `JwtService`                   |
| **X-05** | Stateless Auth Policy           | Code      | ✅ Verified | `SecurityConfig`               |
| **X-06** | CORS Whitelist                  | Code      | ✅ Verified | `SecurityConfig`               |
| **X-07** | Public Endpoint Whitelist       | Code      | ✅ Verified | `SecurityConfig`               |

## Summary

- **Verified Rules:** 28
- **Partial/Soft Rules:** 6
- **Missing Rules:** 5
- **Total Rules Tracked:** 39
