# PRODUCCIÓN (RPC) MODULE CANONICAL NOTEBOOK

## 1. Propósito del Módulo
El módulo de Producción (Reportes de Producción de Campo - RPC) es el componente responsable de registrar el avance físico "real" ejecutado en obra. A diferencia de las Estimaciones (que son financieras), los RPC son técnicos y sirven como fuente de verdad para el control de avance y la generación posterior de estimaciones.

## 2. Invariantes y Reglas de Negocio

### REGLA-001: Inmutabilidad de Reportes Aprobados/Rechazados

**Status:** ✅ Verified
**Type:** Dominio
**Severity:** CRITICAL

**Description:**
Un reporte aprobado o rechazado es inmutable; no se puede editar ni eliminar.

**Implementation:**
- **Entity/Class:** `ProduccionValidator`
- **Method:** `validarEditable`
- **Validation:** Estado != APROBADO && Estado != RECHAZADO

**Code Evidence:**
```java
if (reporte.getEstado() == EstadoReporteProduccion.APROBADO
        || reporte.getEstado() == EstadoReporteProduccion.RECHAZADO) {
    throw new BusinessRuleException(
            "Un reporte aprobado es inmutable. Debe crear una Nota de Crédito o un Reporte Deductivo para corregir."
    );
}
```

### REGLA-002: Fecha de Reporte No Futura

**Status:** ✅ Verified
**Type:** Temporal
**Severity:** HIGH

**Description:**
La fecha del reporte es obligatoria y no puede ser futura.

**Implementation:**
- **Entity/Class:** `ProduccionValidator`
- **Method:** `validarFechaNoFutura`
- **Validation:** fecha != null && !fecha.isAfter(now)

**Code Evidence:**
```java
if (fechaReporte == null) {
    throw new BusinessRuleException("La fecha del reporte es obligatoria.");
}
if (fechaReporte.isAfter(LocalDate.now())) {
    throw new BusinessRuleException("La fecha del reporte no puede ser futura.");
}
```

### REGLA-003: Proyecto en Ejecución

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** CRITICAL

**Description:**
Solo se puede reportar avance en proyectos en estado ACTIVO.

**Implementation:**
- **Entity/Class:** `ProduccionValidator`
- **Method:** `validarProyectoEnEjecucion`
- **Validation:** Proyecto.Estado == ACTIVO

**Code Evidence:**
```java
if (proyecto.getEstado() != EstadoProyecto.ACTIVO) {
    throw new BusinessRuleException("No se puede reportar avance en un proyecto que no está en ACTIVO.");
}
```

### REGLA-004: Control de Exceso de Metrado

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** HIGH

**Description:**
La cantidad reportada es obligatoria y el reporte no puede exceder el metrado vigente de la partida.

**Implementation:**
- **Entity/Class:** `ProduccionValidator`
- **Method:** `validarNoExcesoMetrado`
- **Validation:** (AcumuladoAprobado + CantidadNueva) <= MetradoVigente

**Code Evidence:**
```java
if (avanceTotal.compareTo(metradoVigente) > 0) {
    throw new BusinessRuleException(
            "La cantidad reportada excede el saldo disponible de la partida. Requiere Orden de Cambio.");
}
```

### REGLA-005: Detalle Mínimo Requerido

**Status:** ✅ Verified
**Type:** Dominio
**Severity:** MEDIUM

**Description:**
Un reporte de producción debe contener al menos un detalle.

**Implementation:**
- **Entity/Class:** `ProduccionServiceImpl`
- **Method:** `validarDetalles`
- **Validation:** detalles != null && !isEmpty

**Code Evidence:**
```java
if (detalles == null || detalles.isEmpty()) {
    throw new BusinessRuleException("El reporte debe contener al menos un detalle.");
}
```

### REGLA-006: Aprobación en PENDIENTE

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
Solo se puede aprobar un reporte en estado PENDIENTE.

**Implementation:**
- **Entity/Class:** `ProduccionServiceImpl`
- **Method:** `aprobarReporte`
- **Validation:** Estado == PENDIENTE

**Code Evidence:**
```java
if (reporte.getEstado() != EstadoReporteProduccion.PENDIENTE) {
    throw new BusinessRuleException("Solo se puede aprobar un reporte en estado PENDIENTE.");
}
```

### REGLA-007: Rechazo en PENDIENTE con Motivo

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
Solo se puede rechazar un reporte en estado PENDIENTE y el motivo es obligatorio.

**Implementation:**
- **Entity/Class:** `ProduccionServiceImpl`
- **Method:** `rechazarReporte`
- **Validation:** Estado == PENDIENTE && Motivo != null

**Code Evidence:**
```java
if (reporte.getEstado() != EstadoReporteProduccion.PENDIENTE) {
    throw new BusinessRuleException("Solo se puede rechazar un reporte en estado PENDIENTE.");
}
if (motivo == null || motivo.isBlank()) {
    throw new BusinessRuleException("El motivo de rechazo es obligatorio.");
}
```

### REGLA-008: Estado Inicial PENDIENTE

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
Si el estado del reporte es nulo al crear, se asigna PENDIENTE por defecto.

**Implementation:**
- **Entity/Class:** `ProduccionServiceImpl`
- **Method:** `crearReporte`
- **Validation:** Defaults to PENDIENTE

**Code Evidence:**
```java
if (reporte.getEstado() == null) {
    reporte.setEstado(EstadoReporteProduccion.PENDIENTE);
}
```

### REGLA-009: Reporte No Nulo

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
El objeto reporte no puede ser nulo para procesarlo.

**Implementation:**
- **Entity/Class:** `ProduccionServiceImpl`
- **Method:** `crearReporte`
- **Validation:** reporte != null

**Code Evidence:**
```java
if (reporte == null) {
    throw new BusinessRuleException("El reporte no puede ser nulo.");
}
```

### REGLA-058: Estados Válidos de Reporte

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
El estado de reporte de producción debe estar restringido al conjunto: {PENDIENTE, APROBADO, RECHAZADO}.

**Implementation:**
- **Entity/Class:** `EstadoReporteProduccion` (Enum) / `ReporteProduccionEntity`
- **Validation:** Enum constraint / JPA @Enumerated

**Code Evidence:**
```java
public enum EstadoReporteProduccion {
    PENDIENTE, APROBADO, RECHAZADO
}

@Enumerated(EnumType.STRING)
private EstadoReporteProduccion estado;
```

### REGLA-059: Cantidad Reportada No Negativa

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** MEDIUM

**Description:**
La cantidad reportada en un detalle RPC debe ser mayor o igual a cero. (Nota: `DetalleRPCRequest` exige `@Positive`, es decir > 0).

**Implementation:**
- **Entity/Class:** `DetalleRPCRequest`
- **Validation:** @Positive annotation

**Code Evidence:**
```java
@Positive(message = "La cantidad reportada debe ser positiva")
BigDecimal cantidadReportada
```

### REGLA-076: Campos Obligatorios Reporte

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En ReporteProduccion: fecha_reporte, responsable_id y estado son obligatorios.

**Implementation:**
- **Entity/Class:** `ReporteProduccionEntity`
- **Validation:** @NotNull annotations

**Code Evidence:**
```java
@NotNull @Column(name = "fecha_reporte", nullable = false)
private LocalDate fechaReporte;

@NotNull @Column(name = "responsable_id", nullable = false)
private UUID responsableId;

@NotNull @Enumerated(EnumType.STRING)
private EstadoReporteProduccion estado;
```

### REGLA-077: Campos Obligatorios Detalle

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En DetalleRPC: reporte_id, partida_id y cantidad_reportada son obligatorios.

**Implementation:**
- **Entity/Class:** `DetalleRPCEntity`
- **Validation:** @NotNull annotations

**Code Evidence:**
```java
@NotNull @JoinColumn(name = "reporte_id", nullable = false)
private ReporteProduccionEntity reporteProduccion;

@NotNull @JoinColumn(name = "partida_id", nullable = false)
private PartidaEntity partida;

@NotNull @Column(name = "cantidad_reportada", nullable = false)
private BigDecimal cantidadReportada;
```

### REGLA-081: Validación Request Creación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En RPC request: fechaReporte y responsableId obligatorios; debe incluir al menos un detalle.

**Implementation:**
- **Entity/Class:** `CrearReporteProduccionRequest`
- **Validation:** @NotNull, @NotEmpty

**Code Evidence:**
```java
@NotNull(message = "La fecha del reporte es obligatoria")
LocalDate fechaReporte,
@NotNull(message = "El responsableId es obligatorio")
UUID responsableId,
@NotEmpty(message = "Debe incluir al menos un detalle")
List<@Valid DetalleRPCRequest> detalles
```

### REGLA-082: Validación Request Detalle

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En detalle RPC request: partidaId y cantidad reportada obligatorias; cantidad positiva.

**Implementation:**
- **Entity/Class:** `DetalleRPCRequest`
- **Validation:** @NotNull, @Positive

**Code Evidence:**
```java
@NotNull(message = "La partidaId es obligatoria")
UUID partidaId,
@NotNull(message = "La cantidad reportada es obligatoria")
@Positive(message = "La cantidad reportada debe ser positiva")
BigDecimal cantidadReportada
```

### REGLA-083: Validación Request Aprobación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
En aprobación RPC request: aprobadorId es obligatorio.

**Implementation:**
- **Entity/Class:** `AprobarReporteRequest`
- **Validation:** @NotNull

**Code Evidence:**
```java
@NotNull(message = "El aprobadorId es obligatorio")
UUID aprobadorId
```

### REGLA-084: Validación Request Rechazo

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
En rechazo RPC request: aprobadorId y motivo son obligatorios.

**Implementation:**
- **Entity/Class:** `RechazarReporteRequest` (DTO/produccion)
- **Validation:** @NotBlank, @NotNull

**Code Evidence:**
```java
@NotNull(message = "El aprobadorId es obligatorio")
UUID aprobadorId,
@NotBlank(message = "El motivo de rechazo es obligatorio")
String motivo
```

### REGLA-139: Legacy Request Validación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
En RPC legacy request: fechaReporte obligatoria y al menos un item.

**Implementation:**
- **Entity/Class:** `infrastructure.rest.dto.produccion.CrearReporteRequest`
- **Validation:** @NotNull, @NotEmpty

**Code Evidence:**
```java
@NotNull(message = "La fecha del reporte es obligatoria")
LocalDate fechaReporte,
@NotEmpty(message = "Debe incluir al menos un item")
List<@Valid DetalleItemRequest> items
```

### REGLA-140: Legacy Item Validación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
En item RPC legacy: partidaId y cantidad obligatorias; cantidad positiva.

**Implementation:**
- **Entity/Class:** `infrastructure.rest.dto.produccion.DetalleItemRequest`
- **Validation:** @NotNull, @Positive

**Code Evidence:**
```java
@NotNull(message = "La partidaId es obligatoria")
UUID partidaId,
@NotNull(message = "La cantidad es obligatoria")
@Positive(message = "La cantidad debe ser positiva")
BigDecimal cantidad
```

### REGLA-141: Legacy Rechazo Validación

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** LOW

**Description:**
En rechazo RPC legacy: motivo obligatorio.

**Implementation:**
- **Entity/Class:** `infrastructure.rest.dto.produccion.RechazarReporteRequest`
- **Validation:** @NotBlank

**Code Evidence:**
```java
@NotBlank(message = "El motivo es obligatorio")
String motivo
```
