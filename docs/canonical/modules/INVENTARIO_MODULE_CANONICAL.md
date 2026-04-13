# INVENTARIO_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: Stock por proyecto, costo promedio, movimientos de almacén (entrada/salida)  
> **Status**: Functional (70%)  
> **Owner**: Logistica Team  
> **Last Updated**: 2026-04-12  
> **Authors**: Antigravity (sync código `main`), BudgetPro

**Dominio inventario:** `com.budgetpro.domain.logistica.inventario` (`GestionInventarioService`, etc.) · **Aplicación consulta:** `com.budgetpro.application.inventario` · **Almacén REST:** `com.budgetpro.application.almacen` + `AlmacenController` → `/api/v1/almacen`.

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State         | Deliverables                         |
| ----------- | --------- | -------------------- | ------------------------------------ |
| **Current** | Now       | 70% (Stock + almacén + transferencias REST) | Consulta por proyecto, movimientos almacén, transferencias HTTP, PMP |
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
| REGLA-117 | **Toda compra de bienes físicos genera entrada a inventario; inventario sin compra es ilegal.** | ✅ Implemented |
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
| UC-I02 | Register Ingress (Purchase)   | P0       | ✅               |
| UC-I03 | Register Egress (Consumption) | P1       | 🟡 **Parcial:** `POST /api/v1/almacen/movimientos` con `tipoMovimiento` **SALIDA** e imputación (`partidaId` cuando aplica; ver REGLA-049). No hay flujo REST separado “consumo obra” además de almacén. |
| UC-I04 | Warehouse Transfer            | P2       | ✅ `TransferenciaController` — `POST /api/v1/transferencias/entre-bodegas` y `POST /api/v1/transferencias/entre-proyectos` (orquesta dominio `TransferenciaService` / casos de uso asociados). |

## 7. Domain / application

- **`GestionInventarioService`** y repositorios de dominio: PMP, validaciones de stock, integración con compras/recepciones.
- **`ConsultarInventarioUseCase` / `ConsultarInventarioUseCaseImpl`:** lectura agregada por proyecto → REST.
- **`RegistrarMovimientoAlmacenUseCase`:** registro de movimientos vía `AlmacenController`.
- **`ConsultarMovimientosAlmacenUseCase`:** listado de movimientos por `almacenId` (filtro opcional `recursoId`).

## 8. REST Endpoints

| Method | Path | Description | Status |
| ------ | ---- | ----------- | ------ |
| GET | `/api/v1/proyectos/{proyectoId}/inventario` | Listar ítems de inventario del proyecto (`ConsultarInventarioUseCase`) | ✅ |
| POST | `/api/v1/almacen/movimientos` | Registrar movimiento de almacén (entrada/salida; body `RegistrarMovimientoAlmacenRequest`) | ✅ |
| GET | `/api/v1/almacen/movimientos` | Listar movimientos (`almacenId` obligatorio; `recursoId` opcional) | ✅ |
| POST | `/api/v1/transferencias/entre-bodegas` | Transferencia entre almacenes/bodegas (`TransferenciaEntreBodegasRequest`) | ✅ |
| POST | `/api/v1/transferencias/entre-proyectos` | Transferencia entre proyectos (`TransferenciaEntreProyectosRequest`) | ✅ |

**Estudio de gaps (Ola 1):** [INVENTARIO_GAP_STUDY.md](../radiography/gaps/INVENTARIO_GAP_STUDY.md) — **O-12** en [CODE_DOC_REVIEW_LOG.md](../radiography/CODE_DOC_REVIEW_LOG.md).

## 9. Observability

- **Metrics**: `inventory.value.total`
- **Logs**: Adjustments (Theft/Loss tracking).

## 10. Integration Points

- **Consumes**: `Compras` (Ingress)
- **Exposes**: `StockAvailability` to `Produccion`

## 11. Technical Debt & Risks

- [ ] **Locking**: High concurrency on same item (e.g., Cement) needs Row Locking. (Medium)
- [ ] **Detalle stock por almacén**: refinamiento de consultas agregadas vs UI avanzada.

## 12. Detailed Rule Specifications

### REGLA-049: Salida Movement Validation

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
El movimiento de almacén de tipo SALIDA requiere partidaId.

**Implementation:**
- **Entity:** `MovimientoAlmacenEntity`
- **Method:** `isPartidaValidaParaSalida`

**Code Evidence:**
```java
if (tipo == TipoMovimiento.SALIDA && partidaId == null) {
    throw new IllegalArgumentException("Salida de almacén requiere partida imputada");
}
```

### REGLA-050: Default Movement Type

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
Si tipo es nulo en movimiento de almacén, se asigna tipoMovimiento.

**Implementation:**
- **Entity:** `MovimientoAlmacenEntity`
- **Method:** `@PrePersist`

**Code Evidence:**
```java
if (this.tipo == null) {
    this.tipo = this.tipoMovimiento; // Fallback legacy compatibility
}
```

### REGLA-064: Movement Financial Integrity

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
En movimiento_almacen: cantidad > 0, precio_unitario >= 0, importe_total >= 0.

**Implementation:**
- **Database:** `V14__create_almacen_inventarios_schema.sql`
- **Constraint:** CHECK constraints

**Code Evidence:**
```sql
CHECK (cantidad > 0),
CHECK (precio_unitario >= 0),
CHECK (importe_total >= 0)
```

### REGLA-065: Stock Financial Integrity

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
En stock_movimiento: saldos y valores no pueden ser negativos.

**Implementation:**
- **Database:** `V14__create_almacen_inventarios_schema.sql`
- **Constraint:** CHECK constraints

**Code Evidence:**
```sql
CHECK (saldo_cantidad >= 0),
CHECK (saldo_valor >= 0)
```

### REGLA-085: Movement Request Validation

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Para registrar movimiento: campos obligatorios y valores positivos.

**Implementation:**
- **DTO:** `RegistrarMovimientoAlmacenRequest`
- **Annotations:** `@NotNull`, `@DecimalMin`

**Code Evidence:**
```java
@NotNull UUID almacenId,
@NotNull UUID recursoId,
@DecimalMin("0.01") BigDecimal cantidad
```

### REGLA-117: Purchase-Inventory Integration

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
Toda compra de bienes físicos genera entrada a inventario; inventario sin compra es ilegal (para ingresos).

**Implementation:**
- **Process:** Compras Module -> Inventarios Integration
- **Verification:** Business Process Rule

**Code Evidence:**
```java
// Automatic trigger from Purchase Approval to Inventory Ingress
```

### REGLA-136: Unique Warehouse Code

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** MEDIUM

**Description:**
En almacén, el código es único por proyecto.

**Implementation:**
- **Database:** `V14__create_almacen_inventarios_schema.sql`
- **Constraint:** UNIQUE(proyecto_id, codigo)

**Code Evidence:**
```sql
CONSTRAINT uq_almacen_codigo UNIQUE (proyecto_id, codigo)
```

### REGLA-137: Unique Stock Entry

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** HIGH

**Description:**
En stock_actual, la combinación (almacen_id, recurso_id) es única.

**Implementation:**
- **Database:** `V14__create_almacen_inventarios_schema.sql`
- **Constraint:** UNIQUE INDEX

**Code Evidence:**
```sql
CREATE UNIQUE INDEX idx_stock_actual_almacen_recurso ON stock_actual(almacen_id, recurso_id)
```

### REGLA-154: Inventory-Budget Integrity

**Status:** 🟡 Policy
**Type:** Gobierno
**Severity:** HIGH

**Description:**
Inventario sin Partida es ilegal (para salidas).

**Implementation:**
- **Logic:** Validation of Egress vs Budget Items
- **See also:** REGLA-049

**Code Evidence:**
```java
// Cross-check: Output must be linked to a Leaf Partida
```

