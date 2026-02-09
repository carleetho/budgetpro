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


### 2.2 Extended Rule Inventory (Phase 1 Alignment)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-033 | **Para procesar una compra, las partidas deben existir; se descuenta de billetera, se aprueba la compra y se registra entrada a inventario.** | ✅ Implemented |
| REGLA-049 | **El movimiento de almacén de tipo SALIDA requiere partidaId.** | ✅ Implemented |
| REGLA-050 | **Si tipo es nulo en movimiento de almacén, se asigna tipoMovimiento.** | ✅ Implemented |
| REGLA-064 | **En movimiento_almacen: cantidad > 0, precio_unitario >= 0, importe_total >= 0.** | ✅ Implemented |
| REGLA-065 | **En stock_movimiento: cantidad_entrada, cantidad_salida, saldo_cantidad, saldo_valor y costo_promedio_ponderado no pueden ser negativos.** | ✅ Implemented |
| REGLA-085 | **Para registrar movimiento de almacén: almacenId, recursoId, tipoMovimiento, fechaMovimiento, cantidad y precioUnitario son obligatorios; cantidad > 0; precioUnitario >= 0.** | ✅ Implemented |
| REGLA-102 | **Ningún proceso operativo puede existir fuera del presupuesto (compras, inventarios, mano de obra, avances físicos, pagos).** | 🟡 Implemented |
| REGLA-105 | **Un Proyecto en BORRADOR no puede ejecutar compras, inventarios, mano de obra ni avances físicos.** | 🟡 Implemented |
| REGLA-117 | **Toda compra de bienes físicos genera entrada a inventario; inventario sin compra es ilegal.** | 🟡 Implemented |
| REGLA-118 | **Un movimiento de inventario solo puede existir si proyecto ACTIVO, presupuesto CONGELADO, compra válida y salida imputada a Partida.** | 🟡 Implemented |
| REGLA-119 | **Salida sin Partida es ilegal; entrada sin compra es ilegal.** | 🟡 Implemented |
| REGLA-120 | **La salida de inventario reduce saldo disponible del APU; exceso debe registrarse como Excepción formal.** | 🟡 Implemented |
| REGLA-121 | **Diferencias entre inventario físico y sistema deben registrarse como Excepción.** | 🟡 Implemented |
| REGLA-134 | **En movimiento_almacen, el tipo y tipo_movimiento deben estar en {ENTRADA, SALIDA, DEVOLUCION}.** | ✅ Implemented |
| REGLA-136 | **En almacén, el código es único por proyecto.** | ✅ Implemented |
| REGLA-137 | **En stock_actual, la combinación (almacen_id, recurso_id) es única.** | ✅ Implemented |
| REGLA-150 | **Ningún módulo operativo puede ejecutar acciones si el Proyecto no está en estado ACTIVO.** | 🟡 Implemented |
| REGLA-154 | **Inventario sin Partida es ilegal.** | 🟡 Implemented |

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
