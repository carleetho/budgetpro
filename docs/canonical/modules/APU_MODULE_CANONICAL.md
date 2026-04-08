# APU_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: Análisis de precio unitario por partida (insumos, rendimiento) y actualización de costos vía snapshot  
> **Status**: Functional (85%)  
> **Owner**: Cost Engineering Team  
> **Last Updated**: 2026-04-08  
> **Authors**: Antigravity (sync código `main`)

**Dominio:** `com.budgetpro.domain.finanzas.apu` · **Aplicación:** `com.budgetpro.application.apu` · **REST:** `ApuController` (base `/api/v1`). **Catálogo:** insumos referencian `Recurso` (`RecursoRepository`).

El **100%** histórico del notebook se reinterpreta como **núcleo de dominio + flujo principal**; la superficie HTTP y el catálogo siguen con deuda (sin listados, sin bulk).

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State            | Deliverables                      |
| ----------- | --------- | ----------------------- | --------------------------------- |
| **Current** | Now       | 85% (Core + 2 endpoints) | Crear APU por partida, ajuste rendimiento snapshot |
| **Next**    | +1 Month  | Optimization            | Bulk Updates, AI Suggestions      |

## 2. Invariants (Business Rules)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-035 | **PartidaId mandatory & Insumos list not null** | ✅ Implemented |
| REGLA-036 | **Subtotal = Qty * UnitPrice; Non-negative values** | ✅ Implemented |
| REGLA-094 | **Insumos list mandatory for creation** | ✅ Implemented |
| REGLA-095 | **Input validation (ResourceId, Qty, Price)** | ✅ Implemented |
| REGLA-120 | **Inventory Egress reduces APU balance** | 🟡 Policy |

## 3. Data Contracts

### Entity: APU
- `id`: UUID
- `partidaId`: UUID (Unique per Project Strategy)
- `rendimiento`: BigDecimal (e.g., m3/day)

En API de **actualización de rendimiento**, el path usa `{apuSnapshotId}` (identificador del snapshot persistido; ver `ActualizarRendimientoUseCase` / capa infra).

## 4. Casos de uso (aplicación)

| UC | Descripción | Implementación | Estado |
| --- | --- | --- | --- |
| — | Crear APU para una partida (insumos + rendimiento + unidad) | `CrearApuUseCase` / `CrearApuUseCaseImpl` | ✅ |
| — | Actualizar rendimiento de snapshot y recalcular costos afectados | `ActualizarRendimientoUseCase` / `ActualizarRendimientoUseCaseImpl` | ✅ |

## 5. REST API

| Method | Path | Descripción | Status |
| --- | --- | --- | --- |
| POST | `/api/v1/partidas/{partidaId}/apu` | Crear APU (`CrearApuRequest` → `CrearApuCommand`) | ✅ |
| PUT | `/api/v1/apu/{apuSnapshotId}/rendimiento` | Actualizar rendimiento (`ActualizarRendimientoRequest`: `nuevoRendimiento`, `usuarioId`) — **204** | ✅ |

**Nota:** Las rutas viven bajo prefijo `/api/v1` (no hay subcontrolador `/api/v1/apu` raíz para el POST de creación).

## 6. Integración

- **Partidas:** creación anidada en `partidaId`; validación de existencia en `CrearApuUseCaseImpl` (`PartidaRepository`).
- **Recursos:** cada insumo exige `recursoId` existente (`RecursoNoEncontradoException` si falta).
- **Presupuesto / snapshots:** alineado a flujo de congelación y explosión de insumos (ver `PRESUPUESTO_MODULE_CANONICAL.md`); detalle JPA fuera de este resumen.

## 7. Deuda técnica

- [ ] **Lectura:** sin `GET` de APU por partida ni listado en `ApuController`.
- [ ] **Bulk / optimización:** roadmap §1 “Next” (actualizaciones masivas, sugerencias) sin API dedicada.
- [ ] **REGLA-120:** política inventario ↔ APU sigue 🟡 en §2.

## 8. Detailed Rule Specifications

### REGLA-035: APU Integrity

**Status:** ✅ Verified
**Type:** Dominio
**Severity:** CRITICAL

**Description:**
En APU, el partidaId es obligatorio y la lista de insumos no puede ser nula.

**Implementation:**
- **Invariants:** `APU.validarInvariantes`
- **Validation:** Assertions

**Code Evidence:**
```java
if (partidaId == null) { throw new IllegalArgumentException("El partidaId no puede ser nulo"); }
if (insumos == null) { throw new IllegalArgumentException("La lista de insumos no puede ser nula"); }
```

### REGLA-036: Financial Consistency

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
En APU, el subtotal de insumo es cantidad * precio unitario; cantidad y precio unitario no pueden ser negativos.

**Implementation:**
- **Entity:** `ApuInsumo`
- **Method:** `calcularSubtotal`

**Code Evidence:**
```java
public BigDecimal calcularSubtotal() {
    return this.cantidad.multiply(this.precioUnitario);
}
```

### REGLA-094: Creation Request

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Para crear APU: lista de insumos obligatoria.

**Implementation:**
- **DTO:** `CrearApuRequest`
- **Validation:** `@NotNull`

**Code Evidence:**
```java
@NotNull
private List<ApuInsumoRequest> insumos;
```

### REGLA-095: Input Item Validation

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En insumo APU request: recursoId, cantidad y precioUnitario obligatorios; cantidad y precioUnitario no negativos.

**Implementation:**
- **DTO:** `ApuInsumoRequest`
- **Validation:** `@NotNull`, `@DecimalMin`

**Code Evidence:**
```java
@NotNull UUID recursoId,
@DecimalMin("0.0") BigDecimal cantidad,
@DecimalMin("0.0") BigDecimal precioUnitario
```
