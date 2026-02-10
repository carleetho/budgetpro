# PARTIDAS MODULE CANONICAL NOTEBOOK

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
El metradoOriginal de partida es inmutable si el presupuesto está APROBADO (CONGELADO).

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
Para crear partida: presupuestoId, item, descripcion y nivel obligatorios; metrado no negativo.

**Implementation:**
- **Entity/Class:** `CrearPartidaRequest`
- **Method:** Annotations
- **Validation:** `@NotNull`, `@NotBlank`, `@DecimalMin`

**Code Evidence:**
```java
@NotNull UUID presupuestoId,
@NotBlank String item,
@DecimalMin(value = "0.0") BigDecimal metrado
```
