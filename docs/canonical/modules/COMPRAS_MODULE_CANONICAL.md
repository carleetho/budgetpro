# COMPRAS Module - Canonical Specification

> **Status**: Functional (60%)
> **Owner**: Logistica Team
> **Last Updated**: 2026-02-15

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State          | Deliverables                                     |
| ----------- | --------- | --------------------- | ------------------------------------------------ |
| **Current** | Now       | 60% (Purchase Orders) | Direct Purchase, Stock Ingress, Purchase Orders, Provider Mgmt |
| **Next**    | +1 Month  | 80%                   | Comparativo de Precios, Approval Workflow        |
| **Target**  | +3 Months | 100%                  | Integración completa, Reportes avanzados         |

## 2. Invariants (Business Rules)

| ID   | Rule                                                                                                              | Status         |
| ---- | ----------------------------------------------------------------------------------------------------------------- | -------------- |
| L-01 | **Budget Check**: A purchase cannot be authorized if it exceeds the available budget balance (Saldo por Ejercer). | ✅ Implemented |
| L-02 | **Independent Prices**: Purchase prices are independent of APU Snapshot reference prices.                         | ✅ Implemented |
| L-03 | **Stock Update**: Every purchase of "Material" must increase physical inventory.                                  | ✅ Implemented |
| L-04 | **Provider Valid**: Must purchase from active providers only.                                                     | ✅ Implemented |


### 2.2 Extended Rule Inventory (Phase 1 Alignment)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-031 | **El proyectoId, la fecha, el proveedor y los detalles son obligatorios en una compra.** | ✅ Implemented |
| REGLA-032 | **El subtotal de un detalle de compra es cantidad * precio unitario; cantidad y precio unitario no pueden ser negativos.** | ✅ Implemented |
| REGLA-033 | **Para procesar una compra, las partidas deben existir; se descuenta de billetera, se aprueba la compra y se registra entrada a inventario.** | ✅ Implemented |
| REGLA-092 | **Para registrar compra: proyectoId, fecha, proveedor y detalles obligatorios.** | ✅ Implemented |
| REGLA-093 | **En detalle de compra request: recursoId, partidaId, cantidad y precioUnitario obligatorios; cantidad y precioUnitario no negativos.** | ✅ Implemented |
| REGLA-102 | **Ningún proceso operativo puede existir fuera del presupuesto (compras, inventarios, mano de obra, avances físicos, pagos).** | 🟡 Implemented |
| REGLA-105 | **Un Proyecto en BORRADOR no puede ejecutar compras, inventarios, mano de obra ni avances físicos.** | 🟡 Implemented |
| REGLA-115 | **Compra sin clasificación es ilegal; compra sin Partida es válida.** | 🟡 Implemented |
| REGLA-116 | **Superar el tope del APU no bloquea la compra; se emite alerta y se exige motivo clasificado.** | 🟡 Implemented |
| REGLA-117 | **Toda compra de bienes físicos genera entrada a inventario; inventario sin compra es ilegal.** | 🟡 Implemented |
| REGLA-119 | **Salida sin Partida es ilegal; entrada sin compra es ilegal.** | 🟡 Implemented |
| REGLA-150 | **Ningún módulo operativo puede ejecutar acciones si el Proyecto no está en estado ACTIVO.** | 🟡 Implemented |
| REGLA-153 | **Toda compra debe vincularse a una Partida válida del Presupuesto CONGELADO.** | 🟡 Implemented |
| REGLA-157 | **El exceso de consumo debe registrarse como Excepción de consumo o Insumo asociado a Orden de Cambio.** | 🟡 Implemented |

## 3. Domain Events

| Event Name                | Trigger             | Content (Payload)   | Status |
| ------------------------- | ------------------- | ------------------- | ------ |
| `CompraRegistradaEvent`   | Purchase saved      | `compraId`, `items` | ✅     |
| `OrdenCompraEnviadaEvent` | PO sent to provider | `ordenId`, `proyectoId`, `montoTotal`, `detalles` | ✅     |
| `OrdenCompraRecibidaEvent` | PO received | `ordenId`, `proyectoId`, `montoTotal`, `detalles` | ✅     |

## 4. State Constraints

```mermaid
graph TD
    BORRADOR --> SOLICITADA
    SOLICITADA --> APROBADA
    APROBADA --> ENVIADA
    ENVIADA --> RECIBIDA
    SOLICITADA --> BORRADOR
```

- **Constraint**: Inventory only increases on `RECIBIDA`.
- **State Machine**: BORRADOR → SOLICITADA → APROBADA → ENVIADA → RECIBIDA
- **Rejection**: SOLICITADA → BORRADOR (rechazo de aprobación)

## 5. Data Contracts

### Entity: Compra

- `id`: UUID
- `proveedor`: String
- `total`: BigDecimal

### JSON Schema (Evolution)

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "OrdenCompra",
  "properties": {
    "condiciones_pago": {
      "type": "string",
      "description": "Status: 🔴 Missing"
    }
  }
}
```

## 6. Use Cases

| ID     | Use Case                  | Priority | Status |
| ------ | ------------------------- | -------- | ------ |
| UC-L01 | Register Direct Purchase  | P0       | ✅     |
| UC-L02 | Check Budget Availability | P0       | ✅     |
| UC-L03 | Generate Purchase Order   | P1       | ✅     |
| UC-L04 | Receive Goods (Partial)   | P1       | ✅     |

## 7. Domain Services

- **Service**: `CompraService`
- **Responsibility**: Validates budget caps and coordinates inventory updates.

## 8. REST Endpoints

| Method | Path                                | Description                          | Status |
| ------ | ----------------------------------- | ------------------------------------ | ------ |
| POST   | `/api/v1/compras`                   | Register direct purchase             | ✅     |
| POST   | `/api/v1/ordenes-compra`            | Create purchase order                | ✅     |
| GET    | `/api/v1/ordenes-compra`            | List purchase orders (with filters)  | ✅     |
| GET    | `/api/v1/ordenes-compra/{id}`      | Get purchase order by ID             | ✅     |
| PUT    | `/api/v1/ordenes-compra/{id}`       | Update purchase order (BORRADOR)     | ✅     |
| DELETE | `/api/v1/ordenes-compra/{id}`      | Delete purchase order (BORRADOR)     | ✅     |
| POST   | `/api/v1/ordenes-compra/{id}/solicitar` | Request approval (BORRADOR → SOLICITADA) | ✅     |
| POST   | `/api/v1/ordenes-compra/{id}/aprobar`   | Approve order (SOLICITADA → APROBADA) | ✅     |
| POST   | `/api/v1/ordenes-compra/{id}/enviar`    | Send to provider (APROBADA → ENVIADA) | ✅     |
| POST   | `/api/v1/ordenes-compra/{id}/confirmar-recepcion` | Confirm receipt (ENVIADA → RECIBIDA) | ✅     |

## 9. Observability

- **Metrics**: `purchase.total.amount`
- **Logs**: Budget overrride attempts.

## 10. Integration Points

- **Consumes**: `Presupuesto` (Limits), `Inventario` (Request creation)
- **Exposes**: `Expense` to `Billetera`, `Stock` to `Inventario`

## 11. Technical Debt & Risks

- [x] **Free Text Providers**: ✅ Resolved - `Proveedor` entity implemented. Migration guide available at `docs/migration/PROVIDER_MIGRATION_GUIDE.md`
- [ ] **Pagination**: List endpoints don't support pagination yet (Medium)
- [ ] **Provider API**: REST endpoints for Provider CRUD not yet implemented (Medium)

## 12. API Documentation

- **OpenAPI Specification**: `backend/src/main/resources/api-docs/orden-compra-api.yaml`
- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html`
- **Migration Guide**: `docs/migration/PROVIDER_MIGRATION_GUIDE.md`

## 13. Implementation Details

### 13.1. Purchase Order State Machine

**States**: BORRADOR → SOLICITADA → APROBADA → ENVIADA → RECIBIDA

**Transitions**:
- `solicitar()`: BORRADOR → SOLICITADA (validates L-01, L-04, REGLA-153)
- `aprobar()`: SOLICITADA → APROBADA
- `rechazar()`: SOLICITADA → BORRADOR
- `enviar()`: APROBADA → ENVIADA (publishes `OrdenCompraEnviadaEvent`)
- `confirmarRecepcion()`: ENVIADA → RECIBIDA (updates inventory, publishes `OrdenCompraRecibidaEvent`)

### 13.2. Provider Entity

**Status**: ✅ Implemented

**Features**:
- RUC uniqueness validation
- State management (ACTIVO, INACTIVO, BLOQUEADO)
- Audit trail (createdBy, updatedBy, createdAt, updatedAt)
- Referential integrity with `orden_compra`

**Migration**: See `docs/migration/PROVIDER_MIGRATION_GUIDE.md` for migration from free-text providers.
