# BILLETERA_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: Caja del proyecto, movimientos INGRESO/EGRESO, saldo no negativo  
> **Status**: Functional (70%)  
> **Owner**: Finanzas Team  
> **Last Updated**: 2026-04-12  
> **Authors**: Antigravity (sync código `main`), BudgetPro

**Dominio:** `com.budgetpro.domain.finanzas.model` (`Billetera`, `MovimientoCaja`) · **Aplicación:** `com.budgetpro.application.finanzas.billetera` · **REST:** `BilleteraController` + **`BilleteraQueryController`** → `/api/v1/billeteras`.

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State    | Deliverables                                     |
| ----------- | --------- | --------------- | ------------------------------------------------ |
| **Current** | Now       | 70% (Cash Flow + consulta) | Ingress/Egress, consulta saldo/movimientos, Balance Check |
| **Next**    | +1 Month  | 75%             | Multi-account Support, Bank Reconciliation       |
| **Target**  | +3 Months | 90%             | Cash Flow Forecasting (Flujo de Caja Proyectado) |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                                   | Status         |
| ---- | -------------------------------------------------------------------------------------- | -------------- |
| B-01 | **Non-Negative**: Wallet balance cannot be negative (Debit check).                     | ✅ Implemented |
| B-02 | **Audit Trail**: Every movement must have a source reference (EstimationId, CompraId). | ✅ Implemented |
| B-03 | **Currency Mix**: Cannot mix currencies in same wallet account.                        | ✅ Implemented |
| B-04 | **Evidence Lock**: Max 3 pending movements awaiting evidence before blocking new ops.  | ✅ Implemented |
| B-05 | **Crypto Integrity**: Budget hash validation before egress to prevent tampering.       | ✅ Implemented |


### 2.2 Extended Rule Inventory (Phase 1 Alignment)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-033 | **Para procesar una compra, las partidas deben existir; se descuenta de billetera, se aprueba la compra y se registra entrada a inventario.** | ✅ Implemented |
| REGLA-042 | **En MovimientoCaja: monto > 0, referencia no vacía, billeteraId y tipo no nulos.** | ✅ Implemented |

## 3. Domain Events

| Event Name               | Trigger      | Content (Payload)      | Status |
| ------------------------ | ------------ | ---------------------- | ------ |
| `SaldoInsuficienteEvent` | Failed debit | `proyectoId`, `amount` | 🟡     |

## 4. State Constraints

- Wallet is State-less (Aggregate of Movements). Use snapshots for performance.

## 5. Data Contracts

### Entity: Billetera

- `id`: UUID
- `moneda`: String (ISO-4217, 3 characters) - Wallet currency constraint
- `saldo`: BigDecimal

### Entity: MovimientoCaja

- `id`: UUID
- `billeteraId`: UUID
- `monto`: BigDecimal
- `moneda`: String (ISO-4217, 3 characters) - Movement currency
- `tipo`: Enum (INGRESO, EGRESO)
- `referencia`: String
- `evidenciaUrl`: String (URL)

### JSON Schema (Evolution)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Movimiento",
  "properties": {
    "tipo_cambio": { "type": "number", "description": "Status: 🔴 Missing" }
  }
}
```

## 6. Use Cases

| ID     | Use Case                 | Priority | Status |
| ------ | ------------------------ | -------- | ------ |
| UC-B01 | Register Ingress (Cobro) | P0       | ✅ `RegistrarMovimientoCajaUseCase` + REST (`INGRESO`) |
| UC-B02 | Register Egress (Pago)   | P0       | 🟡 REST acepta `EGRESO`, pero `RegistrarMovimientoCajaUseCaseImpl` llama a `Billetera.egresar(..., null, false)` — **deuda** documentada en código (falta `presupuestoId` / política explícita en API). |
| UC-B03 | Check Balance            | P0       | ✅ `GET /api/v1/billeteras/{billeteraId}/saldo` y `GET .../movimientos` (`BilleteraQueryController`; DTOs `BilleteraSaldoResponse`, `MovimientoCajaResponse`). |
| UC-B04 | Multi-currency Transfer  | P2       | 🔴     |

## 7. Domain / application

- **Agregado `Billetera`:** ingresar / egresar con invariantes (saldo ≥ 0, tope de movimientos sin evidencia, etc.).
- **`RegistrarMovimientoCajaUseCaseImpl`:** único caso de uso expuesto vía REST para caja genérica.
- **Integración indirecta:** `AprobarEstimacionUseCaseImpl` y flujos de **compra** cargan `Billetera` por `proyectoId` sin pasar por `BilleteraController`.

## 8. REST Endpoints

| Method | Path | Description | Status |
| ------ | ---- | ----------- | ------ |
| POST | `/api/v1/billeteras/{billeteraId}/movimientos` | Registrar movimiento (`RegistrarMovimientoRequest`: monto; moneda PEN/USD/EUR; tipo INGRESO o EGRESO — **tipo case-insensitive** en binding REST; referencia obligatoria; evidencia opcional) | ✅ |
| GET | `/api/v1/billeteras/{billeteraId}/saldo` | Consultar saldo agregado por moneda | ✅ |
| GET | `/api/v1/billeteras/{billeteraId}/movimientos` | Listar movimientos de la billetera | ✅ |

**Nota:** no hay `GET /api/v1/proyectos/{id}/billetera`; el cliente usa `billeteraId` conocido o flujos que lo resuelven por proyecto fuera de este controlador.

## 9. Observability

- **Metrics**: `wallet.balance.current`
- **Logs**: All financial transactions.

## 10. Integration Points

- **Consumes**: `Estimacion` (Ingress), `Compra` (Egress), `Planilla` (Egress)
- **Exposes**: `LiquidityStatus` to `Dashboard`

## 11. Technical Debt & Risks

- [ ] **Concurrency**: Balance updates need Optimistic Locking to prevent Race Conditions. (Critical)
- [ ] **Consulta por proyecto**: resolver `billeteraId` desde `proyectoId` en un endpoint dedicado (opcional UX).
- [ ] **EGRESO vía API genérica:** alinear request/use case con reglas de `Billetera.egresar` (presupuesto / integridad) o documentar perfil “solo ingreso manual”.
