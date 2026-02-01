# INVENTARIO Module - Canonical Specification

> **Status**: Functional (50%)
> **Owner**: Logistica Team
> **Last Updated**: 2026-01-31

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State         | Deliverables                         |
| ----------- | --------- | -------------------- | ------------------------------------ |
| **Current** | Now       | 50% (Stock Tracking) | Item tracking, Average Cost          |
| **Next**    | +1 Month  | 75%                  | Movements (Salida a Obra), Transfers |
| **Target**  | +3 Months | 90%                  | Physical Audit, Waste Analysis       |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                           | Status             |
| ---- | ------------------------------------------------------------------------------ | ------------------ |
| I-01 | **No Negative Stock**: Stock cannot become negative.                           | ✅ Implemented     |
| I-02 | **Avg Cost Update**: Weighted Average Cost (PMP) must update on every ingress. | ✅ Implemented     |
| I-03 | **FIFO**: (Future) Support FIFO valuation? Currently Weighted Avg.             | 🟡 Policy Decision |

## 3. Domain Events

| Event Name            | Trigger     | Content (Payload) | Status |
| --------------------- | ----------- | ----------------- | ------ |
| `InventarioBajoAlert` | Stock < Min | `itemId`          | 🔴     |

## 4. State Constraints

- Items are Entities. Movements are Immutable Events history.

## 5. Data Contracts

### Entity: InventarioItem

- `id`: UUID
- `cantidad`: BigDecimal
- `costoPromedio`: BigDecimal

### JSON Schema (Evolution)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "MovimientoInventario",
  "properties": {
    "tipo": {
      "type": "string",
      "enum": ["INGRESO", "SALIDA", "AJUSTE", "TRANSFERENCIA"],
      "description": "Status: 🟡 Only INGRESO/SALIDA supported"
    }
  }
}
```

## 6. Use Cases

| ID     | Use Case                      | Priority | Status           |
| ------ | ----------------------------- | -------- | ---------------- |
| UC-I01 | View Stock                    | P0       | ✅               |
| UC-I02 | Register Ingress (Purchase)   | P0       | ✅ (via Compras) |
| UC-I03 | Register Egress (Consumption) | P1       | 🔴               |
| UC-I04 | Warehouse Transfer            | P2       | 🔴               |

## 7. Domain Services

- **Service**: `InventarioService`
- **Responsibility**: Calculates PMP and authorizes egress.

## 8. REST Endpoints

| Method | Path                                | Description   | Status |
| ------ | ----------------------------------- | ------------- | ------ |
| GET    | `/api/v1/proyectos/{id}/inventario` | List items    | ✅     |
| POST   | `/api/v1/almacen/movimientos`       | Manual adjust | ✅     |

## 9. Observability

- **Metrics**: `inventory.value.total`
- **Logs**: Adjustments (Theft/Loss tracking).

## 10. Integration Points

- **Consumes**: `Compras` (Ingress)
- **Exposes**: `StockAvailability` to `Produccion`

## 11. Technical Debt & Risks

- [ ] **Locking**: High concurrency on same item (e.g., Cement) needs Row Locking. (Medium)
