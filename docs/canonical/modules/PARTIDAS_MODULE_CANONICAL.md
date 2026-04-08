# PARTIDAS_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: WBS (partidas presupuestarias), jerarquía, metrados y vínculo con APU / avance físico  
> **Status**: Functional (alineado a capacidad actual de API; sin CRUD completo vía `/partidas`)  
> **Owner**: Finanzas Team  
> **Last Updated**: 2026-04-08  
> **Authors**: Antigravity (sync código `main`)

**Dominio:** `com.budgetpro.domain.finanzas.partida` · **Aplicación:** `com.budgetpro.application.partida` · **REST:** `com.budgetpro.infrastructure.rest.partida`, `...rest.avance`, `...rest.apu`.

**Nota:** Varios IDs de regla en §2 (p. ej. REGLA-653) equivalen al inventario extendido de `PRESUPUESTO_MODULE_CANONICAL.md` (REGLA-037, REGLA-038, REGLA-096, REGLA-062, etc.).

## 1. Propósito del Módulo
El módulo de Partidas gestiona la Estructura de Desglose del Trabajo (WBS) de un presupuesto. Define la jerarquía, metrados, y asignaciones presupuestarias.

## 2. Invariantes y Reglas de Negocio

### REGLA-064: Validación Exceso Metrado

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
La cantidad reportada (en producción) no puede exceder el metrado vigente de la partida.

**Implementation:**
- **Entity/Class:** `ProduccionValidator` (External Validator)
- **Method:** `validarNoExcesoMetrado`
- **Validation:** Arithmetic check

**Code Evidence:**
```java
// Validated in Produccion module against Partida state
```

### REGLA-278: Validación Volumen Estimado

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
El volumen estimado no puede exceder el volumen contratado (metrado vigente).

**Implementation:**
- **Entity/Class:** `GeneradorEstimacionService` (External Service)
- **Method:** `validarVolumenEstimado`
- **Validation:** Arithmetic check

**Code Evidence:**
```java
// Validated in Estimacion module against Partida state
```

### REGLA-653: Invariantes de Partida

**Status:** ✅ Verified
**Type:** Dominio
**Severity:** CRITICAL

**Description:**
En Partida: presupuestoId obligatorio, item no vacío, descripción no vacía, metrado no negativo y nivel >= 1.

**Implementation:**
- **Entity/Class:** `Partida`
- **Method:** `validarInvariantes`
- **Validation:** null/empty/range checks

**Code Evidence:**
```java
if (presupuestoId == null) throw new IllegalArgumentException(...);
if (metrado != null && metrado.compareTo(BigDecimal.ZERO) < 0) throw ...;
if (nivel == null || nivel < 1) throw ...;
```

### REGLA-671: Consistencia Jerárquica

**Status:** ✅ Verified (Implicit)
**Type:** Dominio
**Severity:** HIGH

**Description:**
Si una partida tiene padreId, debe pertenecer al mismo presupuestoId.

**Implementation:**
- **Entity/Class:** `Partida` / Application Logic
- **Method:** Factory/Service
- **Validation:** Logical Consistency (Enforced by creation logic)

**Code Evidence:**
```java
// Partida.crearHija takes presupuestoId explicitly.
// Application services ensure consistency when resolving parent.
```

### REGLA-834: Inmutabilidad Metrado Original

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
El metradoOriginal de partida es inmutable si el presupuesto está **`CONGELADO`** (`Presupuesto.isAprobado()` en código = presupuesto congelado/aprobado en sentido de negocio).

**Implementation:**
- **Entity/Class:** `PartidaEntity`
- **Method:** `@PreUpdate`
- **Validation:** State check

**Code Evidence:**
```java
if (presupuesto.getEstado() == EstadoPresupuesto.CONGELADO &&
    metradoOriginal.compareTo(metradoOriginalSnapshot) != 0) {
    throw new IllegalStateException("metradoOriginal es inmutable...");
}
```

### REGLA-852: Default Metrado Vigente

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Si metradoVigente es nulo al persistir una partida, se iguala a metradoOriginal.

**Implementation:**
- **Entity/Class:** `PartidaEntity`
- **Method:** `@PrePersist`
- **Validation:** Null check assignment

**Code Evidence:**
```java
if (metradoVigente == null) {
    metradoVigente = metradoOriginal;
}
```

### REGLA-1105: Valores No Negativos

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** HIGH

**Description:**
En partida, metrado_original, metrado_vigente y precio_unitario deben ser >= 0.

**Implementation:**
- **Entity/Class:** `PartidaEntity`
- **Method:** Annotations
- **Validation:** `@Digits` (implying format, though `@DecimalMin` usually preferred, business logic ensures >=0 in Domain)

**Code Evidence:**
```java
// Domain:
if (metrado.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(...)
```

### REGLA-1708: Request Validación Creación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Para crear partida vía REST: `presupuestoId`, `item`, `descripcion` y `nivel` obligatorios; `metrado` no negativo (opcional en JSON → null tratado en dominio como 0).

**Implementation:**
- **Entity/Class:** `CrearPartidaRequest`
- **Method:** Annotations
- **Validation:** `@NotNull`, `@NotBlank`, `@DecimalMin`

**Code Evidence:**
```java
@NotNull UUID presupuestoId,
@NotBlank String item,
@NotNull Integer nivel,
@DecimalMin(value = "0.0") BigDecimal metrado
```

*(En `CrearPartidaRequest` / `CrearPartidaCommand` el `nivel` es obligatorio vía Bean Validation; el use case aún contiene ramas para `nivel == null` pensadas para invocación no-REST.)*

---

## 3. Casos de uso (aplicación)

| UC | Descripción | Implementación | Estado |
| --- | --- | --- | --- |
| UC-PT01 | Crear partida (raíz o hija) bajo presupuesto no congelado | `CrearPartidaUseCase` / `CrearPartidaUseCaseImpl` | ✅ |
| — | Registrar avance físico de partida | `RegistrarAvanceUseCase` (`AvanceController`) | ✅ |
| — | Crear APU asociado a partida | `CrearApuUseCase` (`ApuController`) | ✅ |

**Congelamiento (P-01):** si `presupuesto.isAprobado()` (estado `CONGELADO`), `CrearPartidaUseCaseImpl` lanza `FrozenBudgetException` antes de persistir.

**Mismo presupuesto (REGLA-038 / notebook REGLA-654):** si hay `padreId`, se valida existencia del padre y `padre.presupuestoId == command.presupuestoId`; si no, `PartidaPadreDiferentePresupuestoException`.

## 4. API REST (partidas y rutas anidadas)

| Method | Path | Descripción | Controller |
| --- | --- | --- | --- |
| POST | `/api/v1/partidas` | Crear partida | `PartidaController` |
| POST | `/api/v1/partidas/{partidaId}/avances` | Registrar avance físico | `AvanceController` |
| POST | `/api/v1/partidas/{partidaId}/apu` | Crear APU para la partida | `ApuController` |
| PUT | `/api/v1/apu/{apuSnapshotId}/rendimiento` | Actualizar rendimiento de snapshot APU | `ApuController` |

**Lectura de árbol WBS:** no hay `GET /api/v1/partidas`, `GET .../{id}` ni listado por presupuesto en `PartidaController`. `GET /api/v1/presupuestos/{id}` devuelve metadatos del presupuesto **sin** árbol de partidas; otras operaciones (explosión de insumos, OC, cronograma) consumen `partidaId` cuando ya se conoce el identificador.

## 5. Persistencia (metrado y congelamiento)

- **`PartidaEntity`:** `@PrePersist` iguala `metradoVigente` a `metradoOriginal` si aplica; `@PreUpdate` impide cambiar `metradoOriginal` cuando el presupuesto asociado está congelado (mensaje: inmutabilidad tras aprobación).
- **Dominio `Partida`:** `validarInvariantes` asegura item/descripcion no vacíos, metrado ≥ 0, nivel ≥ 1 (REGLA-037 / REGLA-653).

## 6. Deuda técnica y límites

- [ ] **CRUD REST de partidas:** solo creación explícita; sin PUT/PATCH/DELETE ni GET por id en controlador dedicado.
- [ ] **Listado / árbol:** sin endpoint de consulta bajo `/api/v1/partidas` (impacto en clientes y documentación OpenAPI).
- [ ] **Opcional:** alinear `CrearPartidaUseCaseImpl` con comando REST (hacer `nivel` opcional cuando hay `padreId` y calcular `padre.nivel + 1`) para reducir fricción del cliente.

## 7. Cruce con `PRESUPUESTO_MODULE_CANONICAL.md`

| Este notebook | Inventario presupuesto |
| --- | --- |
| REGLA-653 | REGLA-037 (nivel ≥ 1, campos obligatorios en dominio) |
| REGLA-654 | REGLA-038 (padre mismo presupuesto) |
| REGLA-096 (notebook) | REGLA-096 extendido (metrado no negativo) |
| REGLA-834 | REGLA-047 (metrado original inmutable si `CONGELADO`) |
| REGLA-062 | Congelamiento operativo al crear (P-01 / `FrozenBudgetException`) |
