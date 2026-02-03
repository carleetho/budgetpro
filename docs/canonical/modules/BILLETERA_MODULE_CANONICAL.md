# BILLETERA Module - Canonical Specification

> **Status**: Functional (50%)
> **Owner**: Finanzas Team
> **Last Updated**: 2026-01-31

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State    | Deliverables                                     |
| ----------- | --------- | --------------- | ------------------------------------------------ |
| **Current** | Now       | 50% (Cash Flow) | Ingress/Egress, Balance Check                    |
| **Next**    | +1 Month  | 75%             | Multi-account Support, Bank Reconciliation       |
| **Target**  | +3 Months | 90%             | Cash Flow Forecasting (Flujo de Caja Proyectado) |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                                   | Status         |
| ---- | -------------------------------------------------------------------------------------- | -------------- |
| B-01 | **Non-Negative**: Wallet balance cannot be negative (Debit check).                     | ✅ Implemented |
| B-02 | **Audit Trail**: Every movement must have a source reference (EstimationId, CompraId). | ✅ Implemented |
| B-03 | **Currency Mix**: Cannot mix currencies in same wallet account.                        | ✅ Implemented |

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
| UC-B01 | Register Ingress (Cobro) | P0       | ✅     |
| UC-B02 | Register Egress (Pago)   | P0       | ✅     |
| UC-B03 | Check Balance            | P0       | ✅     |
| UC-B04 | Multi-currency Transfer  | P2       | 🔴     |

## 7. Domain Services

- **Service**: `BilleteraService`
- **Responsibility**: Ledger management.

## 8. REST Endpoints

| Method | Path                               | Description           | Status |
| ------ | ---------------------------------- | --------------------- | ------ |
| GET    | `/api/v1/proyectos/{id}/billetera` | Get balance/movements | ✅     |

## 9. Observability

- **Metrics**: `wallet.balance.current`
- **Logs**: All financial transactions.

## 10. Integration Points

- **Consumes**: `Estimacion` (Ingress), `Compra` (Egress), `Planilla` (Egress)
- **Exposes**: `LiquidityStatus` to `Dashboard`

## 11. Technical Debt & Risks

- [ ] **Concurrency**: Balance updates need Optimistic Locking to prevent Race Conditions. (Critical)
