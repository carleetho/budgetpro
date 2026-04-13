# GAP_ANALISIS_EVM_CPI_SPI.md

> **Scope**: Análisis de Brechas EVM  
> **Last Updated**: 2026-04-07  
> **Authors**: Ing. CL  

**Fecha del análisis original**: 2026-02-15  
**Objetivo**: Identificar la discrepancia entre documentación y código real

---

## ✅ HALLAZGO: CPI/SPI ESTÁN IMPLEMENTADOS

### Evidencia del Código

#### 1. **Domain Model** (`EVMSnapshot.java`)
```java
// Líneas 62-63: Cálculo de CPI y SPI
this.cpi = calcularIndice(ev, ac);  // Cost Performance Index
this.spi = calcularIndice(ev, pv);  // Schedule Performance Index

// Líneas 199-204: Getters expuestos
public BigDecimal getCpi() { return cpi; }
public BigDecimal getSpi() { return spi; }

// Líneas 137-153: Interpretación automática
if (cpi.compareTo(BigDecimal.ONE) < 0) {
    sb.append("Proyecto sobre presupuesto (CPI < 1.0)");
}
if (spi.compareTo(BigDecimal.ONE) < 0) {
    sb.append("Proyecto retrasado (SPI < 1.0)");
}
```

#### 2. **API Endpoint** (`EVMController.java`)
```java
// Línea 29: GET /api/v1/evm/{proyectoId}
@GetMapping("/{proyectoId}")
public ResponseEntity<EVMSnapshotResponse> obtenerMetricas(...)

// Líneas 40-43: Response incluye CPI y SPI
snapshot.getCpi(), snapshot.getSpi(), snapshot.getEac(), ...
```

#### 3. **Database Schema** (`V16__create_evm_snapshot_table.sql`)
```sql
-- Columnas CPI y SPI en la tabla
cpi DECIMAL(19,4) NOT NULL,
spi DECIMAL(19,4) NOT NULL,
```

#### 4. **Response DTO** (`EVMSnapshotResponse.java`)
```java
// Constructor incluye CPI y SPI
BigDecimal cpi, BigDecimal spi, BigDecimal eac, ...
```

---

## ❌ DISCREPANCIA EN DOCUMENTACIÓN

### 1. **EVM_MODULE_CANONICAL.md** (Líneas 70-78)
```json
{
  "properties": {
    "cpi": { "type": "number", "description": "Status: 🔴 Missing" },
    "spi": { "type": "number", "description": "Status: 🔴 Missing" }
  }
}
```
**❌ INCORRECTO**: Dice que faltan, pero están implementados.

### 2. **EVM_MODULE_CANONICAL.md** (Línea 12)
```
**Next** | +1 Month | 75% | SPI/CPI Calculation, S-Curve Generation
```
**❌ DESACTUALIZADO**: CPI/SPI ya están implementados. Solo falta S-Curve.

### 3. **MODULE_SPECS_CURRENT.md** (Línea 12)
```
**2. EVM** | Functional (50%) | Missing advanced EVM metrics.
```
**❌ AMBIGUO**: No especifica qué métricas faltan. CPI/SPI están implementados.

### 4. **EVM_MODULE_CANONICAL.md** (Línea 103)
```
GET | `/api/v1/proyectos/{id}/evm` | Get standard EVM metrics | 🔴
```
**❌ RUTA INCORRECTA**: El endpoint real es `/api/v1/evm/{proyectoId}` (no `/proyectos/{id}/evm`)

---

## 🎯 GAP REAL IDENTIFICADO

### ✅ LO QUE SÍ ESTÁ IMPLEMENTADO

1. ✅ **CPI (Cost Performance Index)**: Calculado y expuesto
2. ✅ **SPI (Schedule Performance Index)**: Calculado y expuesto
3. ✅ **EAC (Estimate At Completion)**: Calculado (EAC = BAC / CPI)
4. ✅ **ETC (Estimate To Complete)**: Calculado (ETC = EAC - AC)
5. ✅ **VAC (Variance At Completion)**: Calculado (VAC = BAC - EAC)
6. ✅ **CV (Cost Variance)**: Calculado (CV = EV - AC)
7. ✅ **SV (Schedule Variance)**: Calculado (SV = EV - PV)
8. ✅ **Interpretación automática**: Texto descriptivo de CPI/SPI
9. ✅ **API Endpoint**: `GET /api/v1/evm/{proyectoId}` funcional

### ✅ Sincronización 2026-04-07 (código `main`)

1. **S-Curve Report Generation (UC-E04)** — ✅ **COMPLETADO** — Endpoint `GET /api/v1/evm/{proyectoId}/s-curve` (`ObtenerSCurveUseCase`, `EVMController`).

2. **Forecast Completion Date (UC-E05)** — ✅ **COMPLETADO** — Endpoint `GET /api/v1/evm/{proyectoId}/forecast`.

3. **ValuacionCerradaEvent / E-04** — ✅ **COMPLETADO** en línea con `EVM_MODULE_CANONICAL.md` (listener, CRON `EVMPeriodoCierreScheduler`, cierre manual).

### 🟡 Pendientes menores (roadmap EVM)

- Métrica `evm.progress.registered.count` (observabilidad).
- Agregación tipo dashboard entre módulos.

---

## 📊 MÉTRICAS IMPLEMENTADAS vs. DOCUMENTADAS

| Métrica | Implementada | Documentada | Estado |
|---------|--------------|-------------|--------|
| **CPI** | ✅ Sí | ❌ Dice "Missing" | **DESACTUALIZADO** |
| **SPI** | ✅ Sí | ❌ Dice "Missing" | **DESACTUALIZADO** |
| **EAC** | ✅ Sí | ✅ Documentado | ✅ Correcto |
| **ETC** | ✅ Sí | ✅ Documentado | ✅ Correcto |
| **VAC** | ✅ Sí | ✅ Documentado | ✅ Correcto |
| **CV** | ✅ Sí | ✅ Documentado | ✅ Correcto |
| **SV** | ✅ Sí | ✅ Documentado | ✅ Correcto |
| **S-Curve** | ✅ Sí | Antes 🔴 Missing en texto narrativo (corregido 2026-04-07) | ✅ Correcto |
| **Forecast Date** | ✅ Sí | Alineado con endpoints actuales | ✅ Correcto |

---

## 🔧 ACCIONES REQUERIDAS

### 1. **Actualizar Documentación** (Prioridad Alta)

#### `EVM_MODULE_CANONICAL.md`:
```markdown
# Cambiar de:
"cpi": { "type": "number", "description": "Status: 🔴 Missing" }
"spi": { "type": "number", "description": "Status: 🔴 Missing" }

# A:
"cpi": { "type": "number", "description": "Status: ✅ Implemented" }
"spi": { "type": "number", "description": "Status: ✅ Implemented" }
```

#### `EVM_MODULE_CANONICAL.md` - Roadmap:
```markdown
# Cambiar de:
**Next** | +1 Month | 75% | SPI/CPI Calculation, S-Curve Generation

# A:
**Current** | Now | 65% | SPI/CPI Calculation ✅, Basic Forecasting ✅
**Next** | +1 Month | 75% | S-Curve Generation, Advanced Forecasting
```

#### `EVM_MODULE_CANONICAL.md` - Endpoints:
```markdown
# Cambiar de:
GET | `/api/v1/proyectos/{id}/evm` | Get standard EVM metrics | 🔴

# A:
GET | `/api/v1/evm/{proyectoId}` | Get EVM snapshot with CPI/SPI | ✅
```

#### `MODULE_SPECS_CURRENT.md`:
```markdown
# Cambiar de:
**2. EVM** | Functional (50%) | Missing advanced EVM metrics.

# A:
**2. EVM** | Functional (65%) | CPI/SPI implemented. Missing S-Curve and advanced forecasting.
```

### 2. **Features EVM (UC-E04 / UC-E05 / E-04)**

1. ✅ **COMPLETADO** — S-Curve: `ObtenerSCurveUseCase`, `EVMController`, `EVMTimeSeriesRepository` / `evm_time_series`.

2. ✅ **COMPLETADO** — Forecast: `ObtenerForecastFechaUseCase`, `EVMController`.

3. ✅ **COMPLETADO** — Cierre de período y propagación de serie temporal (ver canónico EVM).

---

## ✅ CONCLUSIÓN (2026-04-07)

**CPI/SPI/EAC/ETC/VAC** están implementados y expuestos. **No había “gap de código” en S-Curve o forecast**: el problema era **reverse drift** (código en `main` adelantado respecto a partes del análisis histórico y narrativas).

**Acción aplicada**: sincronización documental + regla de API explícita: rango `startDate > endDate` en UC-E04 → `IllegalArgumentException` → HTTP 400 (`GlobalExceptionHandler`).

**Seguimiento**: mantener `docs/canonical` y este GAP alineados con `main` en cada PR que toque EVM (ver `SYNC_WORKFLOW.md`).

---

**Generado por**: Análisis de código vs. documentación  
**Fecha análisis original**: 2026-02-15  
**Última sincronización**: 2026-04-07
