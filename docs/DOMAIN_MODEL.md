# Modelo de Dominio - BudgetPro

## Resumen

Este documento describe el modelo de dominio de BudgetPro, incluyendo agregados, entidades, value objects y sus relaciones. El modelo sigue principios de Domain-Driven Design (DDD) y arquitectura hexagonal.

## Agregados Principales

### 1. Proyecto

**Agregado Root:** `Proyecto`  
**Ubicación:** `com.budgetpro.domain.proyecto.model.Proyecto`

Representa un proyecto de ingeniería civil con su ciclo de vida completo.

**Entidades:**
- `Proyecto` (Root)
- `Billetera` (Entity dentro del agregado)

**Relaciones:**
- 1:N con `Presupuesto`
- 1:N con `Compra`
- 1:1 con `Billetera`
- 1:1 con `ProgramaObra`

### 2. Presupuesto

**Agregado Root:** `Presupuesto`  
**Ubicación:** `com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto`

Representa un presupuesto contractual con estructura WBS (Work Breakdown Structure).

**Entidades:**
- `Presupuesto` (Root)
- `Partida` (Entity)
- `AnalisisSobrecosto` (Entity)

**Relaciones:**
- N:1 con `Proyecto`
- 1:N con `Partida`
- 1:1 con `AnalisisSobrecosto`

**Estados:**
- `BORRADOR`: En edición
- `CONGELADO`: Aprobado y listo para uso

### 3. Partida

**Agregado Root:** `Partida`  
**Ubicación:** `com.budgetpro.domain.finanzas.partida.model.Partida`

Representa una partida presupuestaria en estructura jerárquica (WBS).

**Relaciones:**
- N:1 con `Presupuesto`
- 0..1 con `Partida` (padre, auto-referencia)
- 1:1 con `APU` (legacy)
- 1:1 con `APUSnapshot` (nuevo)
- 1:N con `ConsumoPartida`
- 1:N con `AvanceFisico`

**Estructura WBS:**
```
Presupuesto
├── Partida 01 (nivel 1)
│   ├── Partida 01.01 (nivel 2)
│   │   └── Partida 01.01.01 (nivel 3)
│   └── Partida 01.02 (nivel 2)
└── Partida 02 (nivel 1)
```

### 4. APU (Legacy)

**Agregado Root:** `APU`  
**Ubicación:** `com.budgetpro.domain.finanzas.apu.model.APU`

**Nota:** Esta entidad es legacy. Los nuevos presupuestos usan `APUSnapshot`.

**Entidades:**
- `APU` (Root)
- `ApuInsumo` (Entity)

**Relaciones:**
- 1:1 con `Partida`
- 1:N con `ApuInsumo`
- N:1 con `Recurso` (a través de `ApuInsumo`)

### 5. APUSnapshot

**Agregado Root:** `APUSnapshot`  
**Ubicación:** `com.budgetpro.domain.catalogo.model.APUSnapshot`

Representa un snapshot inmutable de APU desde catálogo externo.

**Entidades:**
- `APUSnapshot` (Root)
- `APUInsumoSnapshot` (Entity)

**Relaciones:**
- 1:1 con `Partida`
- 1:N con `APUInsumoSnapshot`

**Características:**
- Inmutable después de aprobación del presupuesto
- Captura datos de catálogo externo en `snapshotDate`
- Permite modificación de `rendimientoVigente` (Opción C)

### 6. Recurso

**Agregado Root:** `Recurso`  
**Ubicación:** `com.budgetpro.domain.recurso.model.Recurso`

Representa un recurso del catálogo local (Material, Mano de Obra, Equipo).

**Relaciones:**
- 1:1 con `RecursoProxy` (opcional)
- 1:N con `ApuInsumo` (legacy)
- 1:N con `InventarioItem`

### 7. RecursoProxy

**Agregado Root:** `RecursoProxy`  
**Ubicación:** `com.budgetpro.domain.catalogo.model.RecursoProxy`

Representa un proxy a un recurso en catálogo externo.

**Relaciones:**
- 0..1 con `Recurso` (opcional, para recursos locales)

**Características:**
- Almacena snapshot de datos del catálogo externo
- `external_id` y `catalog_source` identifican el recurso externo
- Permite búsqueda y validación sin depender del catálogo en tiempo de ejecución

### 8. Compra

**Agregado Root:** `Compra`  
**Ubicación:** `com.budgetpro.domain.logistica.compra.model.Compra`

Representa una compra de recursos para el proyecto.

**Entidades:**
- `Compra` (Root)
- `CompraDetalle` (Entity)

**Relaciones:**
- N:1 con `Proyecto`
- 1:N con `CompraDetalle`
- 1:1 con `ConsumoPartida` (a través de `CompraDetalle`)

**Nota:** `CompraDetalle` usa `recurso_external_id` (string) en lugar de FK a `Recurso`.

### 9. Estimación

**Agregado Root:** `Estimacion`  
**Ubicación:** `com.budgetpro.domain.finanzas.estimacion.model.Estimacion`

Representa una estimación de avance de obra para cobro.

**Entidades:**
- `Estimacion` (Root)
- `DetalleEstimacion` (Entity)

**Relaciones:**
- N:1 con `Proyecto`
- 1:N con `DetalleEstimacion`
- N:1 con `Partida` (a través de `DetalleEstimacion`)

**Estados:**
- `BORRADOR`: En edición
- `APROBADA`: Aprobada, genera ingreso en billetera
- `PAGADA`: Ya fue pagada

### 10. ProgramaObra

**Agregado Root:** `ProgramaObra`  
**Ubicación:** `com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra`

Representa el programa de obra (cronograma) de un proyecto.

**Entidades:**
- `ProgramaObra` (Root)
- `ActividadProgramada` (Entity)

**Relaciones:**
- 1:1 con `Proyecto`
- 1:N con `ActividadProgramada`
- 1:1 con `CronogramaSnapshot` (cuando está congelado)

**Estados:**
- `NO_CONGELADO`: En edición, fechas pueden modificarse
- `CONGELADO`: Baseline establecido, fechas inmutables

**Mecanismo de Freeze:**
- Campo `congelado` (Boolean) indica estado de congelamiento
- Metadata: `congeladoAt`, `congeladoBy`, `snapshotAlgorithm`
- Método `congelar(UUID approvedBy)`: Congela el cronograma
- Guards: `actualizarFechas()` y `actualizarFechaFinDesdeActividades()` 
  lanzan `CronogramaCongeladoException` si está congelado

**Acoplamiento Temporal:**
- Cuando `Presupuesto` se aprueba (CONGELADO), `ProgramaObra` 
  se congela automáticamente
- Ambas operaciones son atómicas (mismo contexto transaccional)
- Si el cronograma no puede congelarse, el presupuesto NO se aprueba

### 11. ActividadProgramada

**Agregado Root:** `ActividadProgramada`  
**Ubicación:** `com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada`

Representa una actividad programada en el cronograma.

**Relaciones:**
- N:1 con `ProgramaObra`
- 1:1 con `Partida` (a través de `partidaId`)

**Características:**
- Fechas: `fechaInicio`, `fechaFin`
- Duración: `duracionDias` (calculada)
- Dependencias: `predecesoras` (lista de UUIDs de actividades predecesoras)
- Tipo de dependencia: Fin-Inicio (simple)

### 12. CronogramaSnapshot

**Agregado Root:** `CronogramaSnapshot`  
**Ubicación:** `com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot`

Representa un snapshot inmutable del baseline temporal del cronograma.

**Relaciones:**
- 1:1 con `ProgramaObra` (un snapshot por cronograma congelado)
- Many-to-one con `Presupuesto` (múltiples snapshots si hay re-baseline)

**Características:**
- **Inmutable:** Todos los campos son final, no hay setters
- **Almacenamiento JSONB:** Datos temporales complejos almacenados como JSON
  - `fechasJson`: Fechas del programa y actividades
  - `duracionesJson`: Duraciones del programa y actividades
  - `secuenciaJson`: Secuencia y dependencias entre actividades
  - `calendariosJson`: Calendarios y restricciones temporales
- **Metadata:**
  - `snapshotDate`: Fecha y hora de creación del snapshot
  - `snapshotAlgorithm`: Versión del algoritmo ("TEMPORAL-SNAPSHOT-v1")

**Propósito:**
- Preservar el baseline temporal inmutable cuando se congela el cronograma
- Permitir comparación entre baseline y estado actual
- Soportar análisis de desviaciones temporales

## Diagrama de Relaciones

```mermaid
erDiagram
    PROYECTO ||--o{ PRESUPUESTO : "tiene"
    PROYECTO ||--o{ COMPRA : "realiza"
    PROYECTO ||--|| BILLETERA : "tiene"
    PROYECTO ||--|| PROGRAMA_OBRA : "tiene"
    
    PRESUPUESTO ||--o{ PARTIDA : "contiene"
    PRESUPUESTO ||--|| ANALISIS_SOBRECOSTO : "tiene"
    
    PARTIDA ||--o| PARTIDA : "padre (WBS)"
    PARTIDA ||--|| APU : "tiene_legacy"
    PARTIDA ||--|| APU_SNAPSHOT : "tiene_snapshot"
    
    APU ||--o{ APU_INSUMO : "contiene"
    APU_INSUMO }o--|| RECURSO : "usa"
    
    APU_SNAPSHOT ||--o{ APU_INSUMO_SNAPSHOT : "contiene"
    
    RECURSO ||--|| RECURSO_PROXY : "tiene_proxy"
    
    COMPRA ||--o{ COMPRA_DETALLE : "tiene"
    COMPRA_DETALLE }o--|| PARTIDA : "imputa"
    COMPRA_DETALLE ||--|| CONSUMO_PARTIDA : "genera"
    
    PROGRAMA_OBRA ||--o{ ACTIVIDAD_PROGRAMADA : "contiene"
    PROGRAMA_OBRA ||--|| CRONOGRAMA_SNAPSHOT : "tiene_baseline"
    ACTIVIDAD_PROGRAMADA }o--|| PARTIDA : "asociada"
    PRESUPUESTO ||--o{ CRONOGRAMA_SNAPSHOT : "tiene_baseline"
    
    PROYECTO ||--o{ ESTIMACION : "genera"
    ESTIMACION ||--o{ DETALLE_ESTIMACION : "tiene"
    DETALLE_ESTIMACION }o--|| PARTIDA : "estima"
```

## Value Objects

### Identificadores

Todos los agregados usan Value Objects para sus IDs:

- `ProyectoId` - `com.budgetpro.domain.proyecto.model.ProyectoId`
- `PresupuestoId` - `com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId`
- `PartidaId` - `com.budgetpro.domain.finanzas.partida.model.PartidaId`
- `APUSnapshotId` - `com.budgetpro.domain.catalogo.model.APUSnapshotId`
- `RecursoProxyId` - `com.budgetpro.domain.catalogo.model.RecursoProxyId`
- `CompraId` - `com.budgetpro.domain.logistica.compra.model.CompraId`
- `EstimacionId` - `com.budgetpro.domain.finanzas.estimacion.model.EstimacionId`

### Otros Value Objects

- `EstadoPresupuesto` - Enum de estados del presupuesto
- `EstadoProyecto` - Enum de estados del proyecto
- `EstadoEstimacion` - Enum de estados de estimación
- `TipoRecurso` - Enum (MATERIAL, MANO_OBRA, EQUIPO)
- `TipoConsumo` - Enum de tipos de consumo presupuestal

## Reglas de Negocio

### Inmutabilidad de Presupuestos

Una vez que un presupuesto está `CONGELADO` (aprobado):

- ❌ No se pueden agregar/quitar partidas
- ❌ No se pueden modificar snapshots (excepto `rendimientoVigente`)
- ❌ No se pueden cambiar precios de snapshots
- ✅ Se pueden registrar compras
- ✅ Se pueden registrar avances físicos
- ✅ Se pueden generar estimaciones

### Integridad Financiera

- **VD-02:** Los presupuestos aprobados no cambian por fluctuaciones de precios externos
- Los snapshots capturan precios al momento de creación
- Las compras tienen precios independientes de los snapshots

### WBS (Work Breakdown Structure)

- Las partidas forman una estructura jerárquica
- Cada partida tiene un `item` único (código WBS)
- El `nivel` indica la profundidad en la jerarquía
- Las partidas hoja deben tener APU o APUSnapshot

## Migración Legacy → Snapshot

### Estado Actual

El sistema soporta dos modelos en paralelo:

1. **Legacy:** `APU` + `APU_INSUMO` con FK a `RECURSO`
2. **Nuevo:** `APU_SNAPSHOT` + `APU_INSUMO_SNAPSHOT` con `recurso_external_id` (string)

### Estrategia de Migración

1. **Fase 1 (Completada):** Migración de datos legacy a snapshots
   - Ver: `docs/migration/V6_MIGRATION_GUIDE.md`

2. **Fase 2 (Futuro):** Deprecar tablas legacy
   - Los nuevos presupuestos solo usan snapshots
   - Los presupuestos legacy mantienen sus datos originales

## Referencias de Código

### Agregados

- `com.budgetpro.domain.proyecto.model.Proyecto`
- `com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto`
- `com.budgetpro.domain.finanzas.partida.model.Partida`
- `com.budgetpro.domain.catalogo.model.APUSnapshot`
- `com.budgetpro.domain.recurso.model.Recurso`
- `com.budgetpro.domain.catalogo.model.RecursoProxy`
- `com.budgetpro.domain.logistica.compra.model.Compra`
- `com.budgetpro.domain.finanzas.estimacion.model.Estimacion`

### Servicios de Dominio

- `com.budgetpro.domain.catalogo.service.SnapshotService`
- `com.budgetpro.domain.finanzas.presupuesto.service.PresupuestoService`

### Repositorios

- `com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository`
- `com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository`
- `com.budgetpro.domain.catalogo.port.ApuSnapshotRepository`
- `com.budgetpro.domain.catalogo.port.RecursoProxyRepository`

## Diagrama de Clases Simplificado

```mermaid
classDiagram
    class Proyecto {
        +ProyectoId id
        +String nombre
        +EstadoProyecto estado
    }
    
    class Presupuesto {
        +PresupuestoId id
        +UUID proyectoId
        +EstadoPresupuesto estado
        +aprobar()
    }
    
    class Partida {
        +PartidaId id
        +UUID presupuestoId
        +String item
        +BigDecimal metrado
        +getSaldoDisponible()
    }
    
    class APUSnapshot {
        +APUSnapshotId id
        +UUID partidaId
        +String externalApuId
        +BigDecimal rendimientoOriginal
        +BigDecimal rendimientoVigente
        +actualizarRendimiento()
        +calcularCostoTotal()
    }
    
    class RecursoProxy {
        +RecursoProxyId id
        +String externalId
        +String catalogSource
        +String nombreSnapshot
    }
    
    Proyecto "1" --> "*" Presupuesto
    Presupuesto "1" --> "*" Partida
    Partida "1" --> "0..1" APUSnapshot
    APUSnapshot "1" --> "*" APUInsumoSnapshot
```

## Notas de Diseño

### Separación Legacy/Nuevo

- Los modelos legacy (`APU`, `APU_INSUMO`) se mantienen para compatibilidad
- Los nuevos presupuestos usan exclusivamente snapshots
- La migración permite transición gradual

### Inmutabilidad

- Los snapshots son inmutables después de aprobación
- Excepción: `rendimientoVigente` puede modificarse (Opción C)
- Los precios de compra son independientes de snapshots

### Referencias Externas

- `APUInsumoSnapshot` usa `recurso_external_id` (string) en lugar de FK
- `CompraDetalle` usa `recurso_external_id` y `recurso_nombre` (snapshot)
- Esto permite independencia del catálogo en tiempo de ejecución
