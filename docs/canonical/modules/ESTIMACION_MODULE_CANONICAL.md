# ESTIMACION Module - Canonical Specification

> **Status**: Functional (60%)
> **Owner**: Finanzas Team
> **Last Updated**: 2026-01-31

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State          | Deliverables                          |
| ----------- | --------- | --------------------- | ------------------------------------- |
| **Current** | Now       | 60% (Sequential Flow) | Generate, Approve, Pay Flow           |
| **Next**    | +1 Month  | 75%                   | Deductions (Amortization, Guarantee)  |
| **Target**  | +3 Months | 90%                   | Integration with Electronic Invoicing |

## 2. Invariants (Business Rules)

| ID    | Rule                                                                                                                                                                   | Status            |
| ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------- |
| ES-01 | **Sequential Approval**: Estimations must be generated and approved in strict sequential order number (1, 2, 3...). N-1 state validation **IMPLEMENTED** (2026-02-07). | ✅ Fully Enforced |
| ES-02 | **Wallet Impact**: Approval of an estimation automatically triggers an ingress movement in the Project Wallet. Billetera integration **COMPLETED** (2026-02-07).       | ✅ Fully Enforced |
| ES-03 | **Non-Negative Payment**: Net payment amount cannot be negative after deductions.                                                                                      | ✅ Implemented    |
| ES-04 | **Advance Amortization**: Must amortize proportional% of the Advance Payment until fully repaid.                                                                       | 🟡 Partial        |

## 3. Domain Events

| Event Name                | Trigger              | Content (Payload)               | Status |
| ------------------------- | -------------------- | ------------------------------- | ------ |
| `EstimacionAprobadaEvent` | Approval action      | `estimacionId`, `montoNeto`     | ✅     |
| `EstimacionPagadaEvent`   | Payment confirmation | `estimacionId`, `transactionId` | 🔴     |

## 4. State Constraints

```mermaid
graph TD
    BORRADOR --> APROBADA
    APROBADA --> PAGADA
```

- **Constraint**: Cannot modify details once `APROBADA`.

## 5. Data Contracts

### Entity: Estimacion

- `id`: UUID
- `numerator`: Integer
- `montoBruto`: BigDecimal
- `montoNeto`: BigDecimal

### JSON Schema (Evolution)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Estimacion",
  "properties": {
    "amortizacion_anticipo": {
      "type": "number",
      "description": "Status: 🟡 Partial Logic"
    },
    "fondo_garantia": {
      "type": "number",
      "description": "Status: 🟡 Partial Logic"
    }
  }
}
```

## 6. Use Cases

| ID      | Use Case                  | Priority | Status |
| ------- | ------------------------- | -------- | ------ |
| UC-ES01 | Generate Estimacion       | P0       | ✅     |
| UC-ES02 | Approve Estimacion        | P0       | ✅     |
| UC-ES03 | Calculate Deductions      | P1       | 🟡     |
| UC-ES04 | Print Payment Certificate | P1       | 🔴     |

## 7. Domain Services

- **Service**: `EstimacionService`
- **Responsibility**: Calculates amounts based on current progress vs previous cumulative.
- **Methods**:
  - `generar(proyectoId)`: Creates next sequential estimation.

## 8. REST Endpoints

| Method | Path                                  | Description      | Status |
| ------ | ------------------------------------- | ---------------- | ------ |
| POST   | `/api/v1/proyectos/{id}/estimaciones` | Generate new     | ✅     |
| PUT    | `/api/v1/estimaciones/{id}/aprobar`   | Approve and Bill | ✅     |

## 9. Observability

- **Metrics**: `estimacion.value.avg`
- **Logs**: Approval signatures.

## 10. Integration Points

- **Consumes**: `AvanceFisico` (EVM) for "This Period" progress.
- **Exposes**: `Income` to `Billetera`.

## 11. Technical Debt & Risks

- [ ] **Rounding Errors**: Potential cent-differences in cumulative calculations. Needs standard RoundingMode. (Medium)
