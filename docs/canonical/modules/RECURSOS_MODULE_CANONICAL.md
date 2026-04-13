# RECURSOS_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: Catálogo maestro de insumos (Materiales, MO, Equipo, Subcontrato)  
> **Status**: Functional (70%)  
> **Last Updated**: 2026-04-12  
> **Authors**: Antigravity (sync código `main`), BudgetPro

**Dominio:** `com.budgetpro.domain.finanzas.recurso` · **`TipoRecurso`:** `com.budgetpro.domain.shared.model.TipoRecurso` · **Aplicación:** `com.budgetpro.application.recurso` · **REST:** `RecursoController` → `/api/v1/recursos`.

**Nota de IDs:** el **REGLA-037** de §2 de este archivo aplica al agregado **Recurso** (nombre, tipo, unidad). El **REGLA-037** de `PRESUPUESTO_MODULE_CANONICAL.md` es la regla de **Partida** (WBS); son reglas distintas con el mismo número en inventarios extendidos.

## 1. Propósito del Módulo
El módulo de Recursos gestiona los insumos económicos (Materiales, Mano de Obra, Equipos, Subcontratos) utilizados en los presupuestos. Actúa como un catálogo maestro de precios y definiciones.

## 2. Invariantes y Reglas de Negocio

### REGLA-037: Invariantes de Recurso

**Status:** ✅ Verified
**Type:** Dominio
**Severity:** CRITICAL

**Description:**
En Recurso: nombre no vacío, tipo no nulo y unidadBase no vacía.

**Implementation:**
- **Entity/Class:** `Recurso`
- **Method:** `validarInvariantes`
- **Validation:** null/empty checks

**Code Evidence:**
```java
if (nombre == null || nombre.isBlank()) {
    throw new IllegalArgumentException("El nombre del recurso no puede estar vacío");
}
if (tipo == null) {
    throw new IllegalArgumentException("El tipo del recurso no puede ser nulo");
}
```

### REGLA-039: Creación Provisional

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** MEDIUM

**Description:**
Un recurso provisional se crea con estado EN_REVISION.

**Implementation:**
- **Entity/Class:** `Recurso`
- **Method:** `crearProvisional`
- **Validation:** Fixed state assignment

**Code Evidence:**
```java
public static Recurso crearProvisional(...) {
    return new Recurso(..., EstadoRecurso.EN_REVISION);
}
```

### REGLA-112: Tipos y Estados Válidos

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
En recurso, el tipo debe estar en {MATERIAL, MANO_OBRA, EQUIPO, SUBCONTRATO}; el estado en {ACTIVO, EN_REVISION, DEPRECADO}; costo_referencia >= 0.

**Implementation:**
- **Entity/Class:** `RecursoEntity` / `Recurso`
- **Method:** Enums, Annotations
- **Validation:** `@Enumerated`, `@Digits`

**Code Evidence:**
```java
@Enumerated(EnumType.STRING)
private TipoRecurso tipo;
@Enumerated(EnumType.STRING)
private EstadoRecurso estado;
@Digits(integer = 15, fraction = 4)
private BigDecimal costoReferencia;
```

### REGLA-116: Normalización de Nombre

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
El nombre del recurso se normaliza con trim, uppercase y espacios simples.

**Implementation:**
- **Entity/Class:** `Recurso` / `CrearRecursoUseCaseImpl`
- **Method:** `normalizarNombre`
- **Validation:** String manipulation

**Code Evidence:**
```java
return nombre.trim().toUpperCase().replaceAll("\\s+", " ");
```

### REGLA-1759: Request Validación Creación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Para crear recurso: nombre, tipo y unidadBase obligatorios en Request.

**Implementation:**
- **Entity/Class:** `CrearRecursoRequest`
- **Method:** Annotations
- **Validation:** `@NotBlank`

**Code Evidence:**
```java
@NotBlank(message = "El nombre del recurso es obligatorio")
String nombre,
@NotBlank(message = "El tipo del recurso es obligatorio")
String tipo
```

### REGLA-2242: Nombre Único Normalizado

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
No se puede crear un recurso con el mismo nombre normalizado si ya existe.

**Implementation:**
- **Entity/Class:** `RecursoEntity` / `CrearRecursoUseCaseImpl`
- **Method:** `@UniqueConstraint` / `existsByNombre`
- **Validation:** Database Constraint + Application Check

**Code Evidence:**
```java
@UniqueConstraint(name = "uq_recurso_nombre", columnNames = "nombre_normalizado")
// ...
if (recursoRepository.existsByNombre(nombreNormalizado)) {
    throw new RecursoDuplicadoException(nombreNormalizado);
}
```

### REGLA-2509: Inicialización Atributos

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
En DTO de recurso, atributos se inicializan como mapa vacío si es nulo.

**Implementation:**
- **Entity/Class:** `CrearRecursoRequest`
- **Method:** Constructor
- **Validation:** Null check assignment

**Code Evidence:**
```java
if (atributos == null) {
    atributos = new HashMap<>();
}
```

---

## Apéndice A — API REST y casos de uso (sync código `main`, 2026-04-12)

### REST

| Method | Path | Descripción | Status |
| --- | --- | --- | --- |
| POST | `/api/v1/recursos` | Alta de recurso (`CrearRecursoRequest` → `CrearRecursoCommand` vía `toCommand()`) — **201** + `Location` | ✅ |
| GET | `/api/v1/recursos` | Listado (`RecursoRepository.findAll` → dominio) | ✅ |
| GET | `/api/v1/recursos/{id}` | Detalle por id (`ObtenerRecursoUseCase`) | ✅ |
| PUT | `/api/v1/recursos/{id}` | Actualización parcial (`ActualizarRecursoUseCase`, `ActualizarRecursoRequest`) | ✅ |

**Deuda:** sin `DELETE` REST explícito; sin paginación/filtros en `GET` listado.

### Casos de uso

| UC | Descripción | Implementación |
| --- | --- | --- |
| UC-R01 | Crear recurso (provisional o no, según `esProvisional`) | `CrearRecursoUseCase` / `CrearRecursoUseCaseImpl` |
| UC-R02 | Listar recursos | `RecursoRepository.findAll` (expuesto vía `RecursoController`) |
| UC-R03 | Obtener recurso por id | `ObtenerRecursoUseCase` / `ObtenerRecursoUseCaseImpl` |
| UC-R04 | Actualizar recurso | `ActualizarRecursoUseCase` / `ActualizarRecursoUseCaseImpl` |

### Errores / advice

- `RecursoControllerAdvice` centraliza respuestas de dominio (p. ej. `RecursoDuplicadoException`).

### Integración

- **APU:** `CrearApuUseCaseImpl` valida existencia de cada `recursoId` de los insumos.
- **Inventario / almacén:** movimientos referencian `recursoId` (ver `INVENTARIO_MODULE_CANONICAL.md`).
