# ALERTAS_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: Análisis paramétrico de presupuesto (alertas de ingeniería / costos)  
> **Status**: Functional (90%)  
> **Owner**: Intelligence Team  
> **Last Updated**: 2026-04-12  
> **Authors**: Antigravity (sync código `main`)

**Dominio:** `com.budgetpro.domain.finanzas.alertas` · **Motor:** `AnalizadorParametricoService` · **Aplicación:** `AnalizarPresupuestoUseCase` / `AnalizarPresupuestoUseCaseImpl` · **REST:** `AnalisisController` → `/api/v1/analisis`.

## 1. Module Overview
Monitors engineering and financial parameters to detect anomalies proactively.

## 2. Invariants (Business Rules)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-026 | **Zero Cost Equipment Critical Alert** | ✅ Implemented |
| REGLA-027 | **Steel/Concrete Ratio (80-150 kg/m3)** | ✅ Implemented |
| REGLA-028 | **Aggregate Size vs Member Width** | ✅ Implemented |
| REGLA-116 | **Budget Overrun triggers Alert (Non-blocking)** | ✅ Implemented |

## 3. Data Contracts
- `AlertaParametrica`: Value Object containing level, message, and context.

## 4. Detailed Rule Specifications

### REGLA-026: Equipment Cost Zero

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
Si un recurso EQUIPO tiene costo_horario = 0, se genera ALERTA CRÍTICA.

**Implementation:**
- **Service:** `AnalizadorParametricoService.analizarMaquinaria`
- **Action:** Create Critical Alert

**Code Evidence:**
```java
if (costoHorario.compareTo(BigDecimal.ZERO) == 0) {
    alertas.add(new AlertaParametrica(..., NivelAlerta.CRITICA, ...));
}
```

### REGLA-027: Steel Ratio Anomaly

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** WARNING

**Description:**
El ratio Kg Acero / m3 Concreto debe estar entre 80 y 150; fuera de rango genera WARNING.

**Implementation:**
- **Service:** `AnalizadorParametricoService.analizarAceroConcreto`
- **Validation:** Range Check

**Code Evidence:**
```java
if (ratio < 80 || ratio > 150) {
    return NivelAlerta.WARNING;
}
```

### REGLA-028: Aggregate Size Constraint

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** WARNING

**Description:**
El tamaño del agregado no debe exceder 1/5 del ancho del elemento.

**Implementation:**
- **Service:** `AnalizadorParametricoService.analizarTamanoAgregado`
- **Validation:** `size > width / 5`

**Code Evidence:**
```java
if (agregadoSize > elementoAncho.divide(BigDecimal.valueOf(5))) {
    return NivelAlerta.WARNING;
}
```

### REGLA-116: Soft Budget Block

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
Superar el tope del APU no bloquea la compra; se emite alerta.

**Implementation:**
- **Process:** Purchase Logic
- **Action:** Log Warning / Emit Alert Event

**Code Evidence:**
```java
// Non-blocking validation logic in ComprasService
```

---

## Apéndice A — REST API (sync 2026-04-08)

| Method | Path | Descripción |
| --- | --- | --- |
| GET | `/api/v1/analisis/alertas/{presupuestoId}` | Ejecuta el análisis y devuelve `AnalisisPresupuestoResponse` (alertas paramétricas agregadas). |

**Deuda / alcance:** no hay otros verbos en `AnalisisController`; histórico o re-ejecución explícita dependen de capa de persistencia (`AnalisisPresupuestoRepository`) y del caso de uso, no de rutas adicionales documentadas aquí.

**Estudio de gaps (Ola 1b):** [ALERTAS_GAP_STUDY.md](../radiography/gaps/ALERTAS_GAP_STUDY.md).
