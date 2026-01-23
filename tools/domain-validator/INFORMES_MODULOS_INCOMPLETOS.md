# Informes Detallados de Módulos Incompletos

**Fecha**: 2026-01-23  
**Basado en**: `report.json` y `canonical-roadmap.json`  
**Objetivo**: Detallar qué falta en cada módulo para alcanzar estado COMPLETE

---

## 📋 Resumen Ejecutivo

### Módulos IN_PROGRESS (5)
1. **presupuesto** - 15 entidades, 7 servicios, 5 endpoints
2. **tiempo** - 8 entidades, 3 servicios, 0 endpoints
3. **compras** - 6 entidades, 2 servicios, 0 endpoints
4. **estimacion** - 7 entidades, 1 servicio, 2 endpoints

### Módulos NOT_STARTED (3)
1. **inventarios** - 5 entidades, 1 servicio, 0 endpoints
2. **rrhh** - 0 entidades, 0 servicios, 0 endpoints
3. **cambios** - 6 entidades, 1 servicio, 0 endpoints

---

## 1. MÓDULO: PRESUPUESTO

### Estado Actual
- **Status**: IN_PROGRESS
- **Entidades detectadas**: 15
- **Servicios detectados**: 7
- **Endpoints detectados**: 5

### Entidades Existentes ✅
1. `com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto`
2. `com.budgetpro.domain.finanzas.presupuesto.model.IntegrityAuditEntry`
3. `com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId`
4. `com.budgetpro.domain.finanzas.partida.model.PartidaId`
5. `com.budgetpro.domain.finanzas.partida.model.Partida`
6. `com.budgetpro.domain.finanzas.alertas.model.AnalisisPresupuesto`
7. `com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida`
8. `com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId`
9. `com.budgetpro.domain.finanzas.control.service.DatosControlPartida`
10. `com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartidaId`
11. `com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartida`
12. `com.budgetpro.domain.logistica.compra.service.PartidaData`

### Servicios Existentes ✅
1. `com.budgetpro.domain.finanzas.alertas.service.DatosPartida`
2. `com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashServiceImpl`
3. `com.budgetpro.domain.logistica.compra.service.PartidaData`
4. `com.budgetpro.domain.finanzas.presupuesto.service.PresupuestoService`
5. `com.budgetpro.domain.finanzas.control.service.DatosControlPartida`
6. `com.budgetpro.domain.finanzas.presupuesto.service.CalculoPresupuestoService`
7. `com.budgetpro.domain.finanzas.presupuesto.service.IntegrityAuditLog`

### Endpoints Existentes ✅
1. `POST /api/v1/presupuestos` - Crear presupuesto
2. `POST /api/v1/presupuestos/{presupuestoId}/aprobar` - Aprobar presupuesto
3. `GET /api/v1/presupuestos/{presupuestoId}` - Consultar presupuesto
4. `GET /api/v1/presupuestos/{presupuestoId}/control-costos` - Control de costos
5. `GET /api/v1/presupuestos/{presupuestoId}/explosion-insumos` - Explosión de insumos

### Requisitos del Roadmap

#### ✅ Cumplidos
- ✅ Entidad `Presupuesto` existe
- ✅ Entidad `Partida` existe
- ✅ Servicio `PresupuestoService` con métodos `aprobar()` y `congelar()`
- ✅ Enum `EstadoPresupuesto` con estados: BORRADOR, CONGELADO, INVALIDADO

#### ⚠️ Pendientes / Falsos Positivos

1. **APUSnapshot en presupuesto** (FALSO POSITIVO)
   - **Estado**: Existe en módulo `catalogo` como `com.budgetpro.domain.catalogo.model.APUSnapshot`
   - **Problema**: El validador no detecta que APUSnapshot existe en catalogo
   - **Acción**: Verificar si presupuesto debe tener referencia directa a APUSnapshot o si la relación es indirecta
   - **Impacto**: -1 violación crítica (si se corrige detección)

2. **EstadoPresupuesto** (FALSO POSITIVO)
   - **Estado**: Enum existe en `com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto`
   - **Problema**: El validador no detecta el enum correctamente
   - **Acción**: Mejorar detección de enums en `StateMachineDetector.java`
   - **Impacto**: -1 violación crítica

3. **Dependencia de proyecto** (SE RESUELVE AUTOMÁTICAMENTE)
   - **Estado**: Proyecto está COMPLETE según report.json
   - **Problema**: Violación DATA_DEPENDENCY se resolverá cuando se valide correctamente
   - **Acción**: Verificar que proyecto esté marcado como COMPLETE en validación
   - **Impacto**: -1 violación crítica

### Método `congelar()` en PresupuestoService

**Estado**: ✅ Existe como método `aprobar()` que internamente congela el presupuesto

**Verificación**:
```java
// En PresupuestoService.java línea 94
public CronogramaSnapshot aprobar(PresupuestoId presupuestoId, UUID approvedBy)
```

El método `aprobar()` realiza:
1. Valida estado BORRADOR
2. Genera hashes de integridad
3. Cambia estado a CONGELADO
4. Congela cronograma asociado

**Conclusión**: El método `congelar()` está implementado como parte de `aprobar()`. Si el roadmap requiere un método separado `congelar()`, debe agregarse.

### Acciones Requeridas

1. **Corregir detección de EstadoPresupuesto** (Prioridad ALTA)
   - Archivo: `StateMachineDetector.java`
   - Mejorar búsqueda de enums en paquetes `model`

2. **Verificar/Corregir detección de APUSnapshot** (Prioridad MEDIA)
   - Verificar si presupuesto debe tener referencia directa
   - Si no es necesaria, ajustar regla en roadmap

3. **Verificar método `congelar()`** (Prioridad BAJA)
   - Si roadmap requiere método separado, agregar:
   ```java
   public void congelar(PresupuestoId presupuestoId, UUID approvedBy) {
       aprobar(presupuestoId, approvedBy);
   }
   ```

### Criterio de Éxito
- ✅ Todas las entidades requeridas existen
- ✅ Todos los servicios requeridos existen
- ✅ Métodos `aprobar()` y `congelar()` existen
- ✅ Enum `EstadoPresupuesto` detectado correctamente
- ✅ Dependencia de proyecto resuelta
- ⚠️ APUSnapshot: Verificar si es requerido o falso positivo

---

## 2. MÓDULO: TIEMPO

### Estado Actual
- **Status**: IN_PROGRESS
- **Entidades detectadas**: 8
- **Servicios detectados**: 3
- **Endpoints detectados**: 0 ⚠️

### Entidades Existentes ✅
1. `com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId`
2. `com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra`
3. `com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramadaId`
4. `com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot`
5. `com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId`
6. `com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada`
7. `com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository`
8. `com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository`

### Servicios Existentes ✅
1. `com.budgetpro.domain.finanzas.cronograma.service.SnapshotGeneratorService`
2. `com.budgetpro.domain.finanzas.cronograma.service.CalculoCronogramaService`
3. `com.budgetpro.domain.finanzas.cronograma.service.CronogramaService`

### Endpoints Existentes ✅ (pero no detectados)
1. `POST /api/v1/proyectos/{proyectoId}/cronograma/actividades` - Programar actividad
2. `GET /api/v1/proyectos/{proyectoId}/cronograma` - Consultar cronograma

**Nota**: Los endpoints existen pero están en `CronogramaController`, no en un controlador específico de "tiempo". El validador puede no detectarlos por el nombre del módulo.

### Requisitos del Roadmap

#### ✅ Cumplidos
- ✅ Entidad `ActividadProgramada` existe
- ✅ Entidad `ProgramaObra` existe
- ✅ Relación ActividadProgramada -> Partida existe (campo `partidaId`)

#### ⚠️ Pendientes / Falsos Positivos

1. **Relación ActividadProgramada -> Partida** (FALSO POSITIVO)
   - **Estado**: Existe campo `partidaId` en `ActividadProgramada` (línea 27)
   - **Problema**: El validador no detecta relaciones por UUID
   - **Acción**: Mejorar detección de relaciones por campos UUID en `ValidationRuleExecutor.java`
   - **Impacto**: -1 violación crítica

2. **Dependencia de presupuesto** (SE RESUELVE AUTOMÁTICAMENTE)
   - **Estado**: Presupuesto está IN_PROGRESS
   - **Problema**: Violación DATA_DEPENDENCY se resolverá cuando presupuesto esté COMPLETE
   - **Acción**: Completar módulo presupuesto primero
   - **Impacto**: -1 violación crítica

3. **Endpoints no detectados** (FALSO POSITIVO)
   - **Estado**: Endpoints existen en `CronogramaController`
   - **Problema**: El validador puede no asociarlos con módulo "tiempo"
   - **Acción**: Verificar mapeo de controladores a módulos en `CodebaseAnalyzer.java`
   - **Impacto**: Puede afectar estado COMPLETE

### Verificación de Relación ActividadProgramada -> Partida

**Código verificado**:
```java
// En ActividadProgramada.java línea 27
private final UUID partidaId;
```

**Conclusión**: La relación existe. Es un falso positivo del validador.

### Acciones Requeridas

1. **Corregir detección de relación ActividadProgramada -> Partida** (Prioridad ALTA)
   - Archivo: `ValidationRuleExecutor.java`
   - Mejorar detección de relaciones por campos UUID

2. **Verificar detección de endpoints** (Prioridad MEDIA)
   - Verificar si `CronogramaController` se asocia con módulo "tiempo"
   - Ajustar keywords de módulo si es necesario

3. **Completar módulo presupuesto** (Prioridad ALTA)
   - Resolver dependencia DATA_DEPENDENCY

### Criterio de Éxito
- ✅ Todas las entidades requeridas existen
- ✅ Todos los servicios requeridos existen
- ✅ Relación ActividadProgramada -> Partida detectada correctamente
- ✅ Endpoints detectados correctamente
- ✅ Dependencia de presupuesto resuelta

---

## 3. MÓDULO: COMPRAS

### Estado Actual
- **Status**: IN_PROGRESS
- **Entidades detectadas**: 6
- **Servicios detectados**: 2
- **Endpoints detectados**: 0 ⚠️

### Entidades Existentes ✅
1. `com.budgetpro.domain.logistica.compra.model.CompraDetalleId`
2. `com.budgetpro.domain.logistica.compra.model.CompraId`
3. `com.budgetpro.domain.logistica.compra.model.CompraDetalle`
4. `com.budgetpro.domain.logistica.compra.model.Compra`
5. `com.budgetpro.domain.logistica.compra.service.PartidaData`
6. `com.budgetpro.domain.logistica.compra.port.out.CompraRepository`

### Servicios Existentes ✅
1. `com.budgetpro.domain.logistica.compra.service.ProcesarCompraService`
2. `com.budgetpro.domain.logistica.compra.service.PartidaData`

### Endpoints Existentes ✅ (pero no detectados)
1. `POST /api/v1/compras` - Registrar compra

**Nota**: El endpoint existe pero puede no estar siendo detectado correctamente.

### Requisitos del Roadmap

#### ✅ Cumplidos
- ✅ Entidad `Compra` existe
- ✅ Enum `NaturalezaGasto` existe con valores: DIRECTO_PARTIDA, GENERAL_OBRA, ADMINISTRATIVO

#### ⚠️ Pendientes / Falsos Positivos

1. **NaturalezaGasto** (FALSO POSITIVO)
   - **Estado**: Enum existe en `com.budgetpro.domain.logistica.compra.model.NaturalezaGasto`
   - **Problema**: El validador no detecta el enum correctamente
   - **Acción**: Mejorar detección de enums en `StateMachineDetector.java`
   - **Impacto**: -1 violación crítica

2. **Referencia Compra -> Presupuesto** (⚠️ FALTA)
   - **Requisito**: Campo `presupuesto_id` en Compra
   - **Estado**: ❌ NO existe. Compra solo tiene `proyectoId` (línea 28)
   - **Acción**: Agregar campo `presupuestoId` en entidad `Compra`
   - **Impacto**: -1 violación crítica (BUSINESS_LOGIC)

3. **Dependencias de presupuesto y proyecto** (SE RESUELVEN AUTOMÁTICAMENTE)
   - **Estado**: Ambos módulos deben estar COMPLETE
   - **Problema**: Violaciones DATA_DEPENDENCY se resolverán cuando dependencias estén COMPLETE
   - **Acción**: Completar módulos presupuesto y proyecto primero
   - **Impacto**: -2 violaciones críticas

### Verificación de Referencia Compra -> Presupuesto

**Necesita verificación**: Revisar entidad `Compra` para confirmar campo `presupuesto_id`

### Acciones Requeridas

1. **Corregir detección de NaturalezaGasto** (Prioridad ALTA)
   - Archivo: `StateMachineDetector.java`
   - Mejorar detección de enums

2. **Agregar referencia Compra -> Presupuesto** (Prioridad ALTA)
   - Agregar campo `presupuestoId` en entidad `Compra`
   - Actualizar factory methods y constructores
   - Actualizar mappers y entidades JPA

3. **Verificar detección de endpoint** (Prioridad MEDIA)
   - Verificar si endpoint POST /api/v1/compras se detecta correctamente

4. **Completar módulos dependientes** (Prioridad ALTA)
   - Completar presupuesto y proyecto

### Criterio de Éxito
- ✅ Entidad `Compra` existe
- ✅ Enum `NaturalezaGasto` detectado correctamente
- ⚠️ Referencia Compra -> Presupuesto: **AGREGAR campo `presupuestoId`**
- ✅ Endpoint detectado correctamente
- ✅ Dependencias de presupuesto y proyecto resueltas

---

## 4. MÓDULO: ESTIMACION

### Estado Actual
- **Status**: IN_PROGRESS
- **Entidades detectadas**: 7
- **Servicios detectados**: 1
- **Endpoints detectados**: 2

### Entidades Existentes ✅
1. `com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacion`
2. `com.budgetpro.domain.finanzas.estimacion.model.Estimacion`
3. `com.budgetpro.domain.finanzas.estimacion.model.DetalleEstimacionId`
4. `com.budgetpro.domain.finanzas.estimacion.model.EstimacionId`
5. `com.budgetpro.domain.finanzas.estimacion.port.out.EstimacionRepository`
6. `com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajusteId`
7. `com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajuste`

### Servicios Existentes ✅
1. `com.budgetpro.domain.finanzas.estimacion.service.GeneradorEstimacionService`

### Endpoints Existentes ✅
1. `POST /api/v1/proyectos/{proyectoId}/estimaciones` - Generar estimación
2. `PUT /api/v1/proyectos/estimaciones/{estimacionId}/aprobar` - Aprobar estimación

### Requisitos del Roadmap

#### ✅ Cumplidos
- ✅ Entidad `Estimacion` existe
- ✅ Endpoints existen

#### ⚠️ Pendientes / Falsos Positivos

1. **Referencia Estimacion -> Presupuesto** (⚠️ FALTA)
   - **Requisito**: Campo `presupuesto_id` en Estimacion
   - **Estado**: ❌ NO existe. Estimacion solo tiene `proyectoId` (línea 30)
   - **Acción**: Agregar campo `presupuestoId` en entidad `Estimacion`
   - **Impacto**: -1 violación crítica (BUSINESS_LOGIC)

2. **Referencia Estimacion -> Billetera** (⚠️ FALTA)
   - **Requisito**: Campo `billetera_id` en Estimacion
   - **Estado**: ❌ NO existe. Estimacion solo tiene `proyectoId`
   - **Acción**: Agregar campo `billeteraId` en entidad `Estimacion`
   - **Impacto**: -1 violación crítica (BUSINESS_LOGIC)

3. **Dependencias de presupuesto y proyecto** (SE RESUELVEN AUTOMÁTICAMENTE)
   - **Estado**: Ambos módulos deben estar COMPLETE
   - **Problema**: Violaciones DATA_DEPENDENCY se resolverán cuando dependencias estén COMPLETE
   - **Acción**: Completar módulos presupuesto y proyecto primero
   - **Impacto**: -2 violaciones críticas

### Acciones Requeridas

1. **Agregar referencia Estimacion -> Presupuesto** (Prioridad ALTA)
   - Agregar campo `presupuestoId` en entidad `Estimacion`
   - Actualizar factory methods y constructores
   - Actualizar mappers y entidades JPA

2. **Agregar referencia Estimacion -> Billetera** (Prioridad ALTA)
   - Agregar campo `billeteraId` en entidad `Estimacion`
   - Actualizar factory methods y constructores
   - Actualizar mappers y entidades JPA

3. **Completar módulos dependientes** (Prioridad ALTA)
   - Completar presupuesto y proyecto

### Criterio de Éxito
- ✅ Entidad `Estimacion` existe
- ⚠️ Referencia Estimacion -> Presupuesto: **AGREGAR campo `presupuestoId`**
- ⚠️ Referencia Estimacion -> Billetera: **AGREGAR campo `billeteraId`**
- ✅ Endpoints existen
- ✅ Dependencias de presupuesto y proyecto resueltas

---

## 5. MÓDULO: INVENTARIOS

### Estado Actual
- **Status**: NOT_STARTED
- **Entidades detectadas**: 5
- **Servicios detectados**: 1
- **Endpoints detectados**: 0 ⚠️

### Entidades Existentes ✅
1. `com.budgetpro.domain.logistica.inventario.model.MovimientoInventario`
2. `com.budgetpro.domain.logistica.inventario.model.MovimientoInventarioId`
3. `com.budgetpro.domain.logistica.inventario.model.InventarioId`
4. `com.budgetpro.domain.logistica.inventario.model.InventarioItem`
5. `com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository`

### Servicios Existentes ✅
1. `com.budgetpro.domain.logistica.inventario.service.GestionInventarioService`

### Endpoints Existentes ✅ (pero no detectados)
1. `GET /api/v1/proyectos/{proyectoId}/inventario` - Consultar inventario

**Nota**: El endpoint existe pero el módulo está marcado como NOT_STARTED. Puede ser un problema de detección.

### Requisitos del Roadmap

#### ✅ Cumplidos
- ✅ Entidades del módulo existen (InventarioItem, MovimientoInventario)

#### ⚠️ Pendientes

1. **Referencia Inventario -> Compra** (✅ EXISTE INDIRECTAMENTE)
   - **Requisito**: Campo `compra_id` en Inventario
   - **Estado**: ✅ Existe indirectamente. `MovimientoInventario` tiene `compraDetalleId` (línea 21)
   - **Acción**: Verificar si el validador acepta relación indirecta o requiere campo directo
   - **Impacto**: Puede ser violación si validador requiere campo directo

2. **Endpoints faltantes** (VERIFICAR)
   - **Estado**: Solo existe GET para consultar
   - **Acción**: Verificar si se requieren más endpoints (POST, PUT, DELETE)
   - **Impacto**: Puede afectar estado COMPLETE

3. **Dependencias de compras y presupuesto** (SE RESUELVEN AUTOMÁTICAMENTE)
   - **Estado**: Ambos módulos deben estar COMPLETE
   - **Problema**: Violaciones DATA_DEPENDENCY se resolverán cuando dependencias estén COMPLETE
   - **Acción**: Completar módulos compras y presupuesto primero
   - **Impacto**: -2 violaciones críticas

4. **Estado del módulo** (VERIFICAR)
   - **Estado**: Marcado como NOT_STARTED pero tiene entidades y servicios
   - **Problema**: Puede ser problema de detección
   - **Acción**: Verificar por qué se marca como NOT_STARTED
   - **Impacto**: Afecta estado general

### Acciones Requeridas

1. **Verificar referencia Inventario -> Compra** (Prioridad MEDIA)
   - La relación existe indirectamente en `MovimientoInventario.compraDetalleId`
   - Verificar si el validador acepta esta relación indirecta
   - Si no, considerar agregar campo directo en `InventarioItem`

2. **Verificar estado del módulo** (Prioridad ALTA)
   - Revisar por qué se marca como NOT_STARTED
   - Ajustar detección si es necesario

3. **Evaluar endpoints adicionales** (Prioridad MEDIA)
   - Determinar si se requieren endpoints para crear/actualizar inventario
   - Implementar si es necesario

4. **Completar módulos dependientes** (Prioridad ALTA)
   - Completar compras y presupuesto

### Criterio de Éxito
- ✅ Entidades del módulo existen
- ✅ Referencia Inventario -> Compra existe (indirectamente en MovimientoInventario)
- ✅ Endpoints necesarios existen
- ⚠️ Módulo marcado como IN_PROGRESS o COMPLETE (actualmente NOT_STARTED)
- ✅ Dependencias de compras y presupuesto resueltas

---

## 6. MÓDULO: RRHH

### Estado Actual
- **Status**: NOT_STARTED
- **Entidades detectadas**: 0 ⚠️
- **Servicios detectados**: 0 ⚠️
- **Endpoints detectados**: 0 ⚠️

### Entidades Existentes
**NINGUNA DETECTADA**

### Servicios Existentes
**NINGUNO DETECTADO**

### Endpoints Existentes
**NINGUNO DETECTADO**

### Requisitos del Roadmap

#### ⚠️ Pendientes (TODO)

1. **Entidades del módulo** (PRIORIDAD CRÍTICA)
   - **Requisito**: Entidades en `com.budgetpro.domain.rrhh.model`
   - **Estado**: No existen
   - **Acción**: Crear entidades del módulo RRHH
   - **Impacto**: Módulo completamente faltante

2. **Dependencias de tiempo y presupuesto** (SE RESUELVEN AUTOMÁTICAMENTE)
   - **Estado**: Ambos módulos deben estar COMPLETE
   - **Problema**: Violaciones DATA_DEPENDENCY se resolverán cuando dependencias estén COMPLETE
   - **Acción**: Completar módulos tiempo y presupuesto primero
   - **Impacto**: -2 violaciones críticas

### Entidades Sugeridas (Basado en dominio)

El módulo RRHH debería incluir:

1. **Empleado** / **Trabajador**
   - ID, nombre, cargo, especialidad
   - Relación con proyecto

2. **AsignacionTrabajo** / **AsignacionRecursoHumano**
   - Relación con ActividadProgramada
   - Relación con Empleado
   - Horas asignadas, fechas

3. **RegistroTiempo** / **RegistroHoras**
   - Relación con ActividadProgramada
   - Relación con Empleado
   - Horas trabajadas, fecha

### Servicios Sugeridos

1. **GestionRRHHService**
   - Asignar trabajadores a actividades
   - Registrar horas trabajadas
   - Consultar asignaciones

### Endpoints Sugeridos

1. `POST /api/v1/proyectos/{proyectoId}/rrhh/asignaciones` - Asignar trabajador
2. `POST /api/v1/proyectos/{proyectoId}/rrhh/registros-tiempo` - Registrar horas
3. `GET /api/v1/proyectos/{proyectoId}/rrhh/asignaciones` - Consultar asignaciones
4. `GET /api/v1/proyectos/{proyectoId}/rrhh/registros-tiempo` - Consultar registros

### Acciones Requeridas

1. **Crear estructura del módulo** (Prioridad CRÍTICA)
   - Crear paquete `com.budgetpro.domain.rrhh.model`
   - Crear entidades base

2. **Implementar entidades** (Prioridad CRÍTICA)
   - Empleado/Trabajador
   - AsignacionTrabajo
   - RegistroTiempo

3. **Implementar servicios** (Prioridad ALTA)
   - GestionRRHHService

4. **Implementar endpoints** (Prioridad ALTA)
   - Controller REST para operaciones RRHH

5. **Completar módulos dependientes** (Prioridad ALTA)
   - Completar tiempo y presupuesto

### Criterio de Éxito
- ✅ Entidades del módulo existen
- ✅ Servicios del módulo existen
- ✅ Endpoints del módulo existen
- ✅ Dependencias de tiempo y presupuesto resueltas

---

## 7. MÓDULO: CAMBIOS

### Estado Actual
- **Status**: NOT_STARTED
- **Entidades detectadas**: 6
- **Servicios detectados**: 1
- **Endpoints detectados**: 0 ⚠️

### Entidades Existentes ✅
1. `com.budgetpro.domain.finanzas.reajuste.model.IndicePrecios`
2. `com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartidaId`
3. `com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajusteId`
4. `com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajuste`
5. `com.budgetpro.domain.finanzas.reajuste.model.DetalleReajustePartida`
6. `com.budgetpro.domain.finanzas.reajuste.model.IndicePreciosId`

### Servicios Existentes ✅
1. `com.budgetpro.domain.finanzas.reajuste.service.CalculadorReajusteService`

### Endpoints Existentes ✅ (pero no detectados)
1. `POST /api/v1/reajustes/calcular` - Calcular reajuste

**Nota**: El endpoint existe pero el módulo está marcado como NOT_STARTED. Puede ser un problema de detección o el módulo "cambios" es diferente de "reajuste".

### Requisitos del Roadmap

#### ✅ Cumplidos
- ✅ Entidades del módulo existen (en paquete `reajuste`)

#### ⚠️ Pendientes

1. **Estado del módulo** (VERIFICAR)
   - **Estado**: Marcado como NOT_STARTED pero tiene entidades y servicios
   - **Problema**: Puede ser problema de detección o mapeo incorrecto
   - **Acción**: Verificar si "cambios" y "reajuste" son el mismo módulo
   - **Impacto**: Afecta estado general

2. **Dependencia de presupuesto CONGELADO** (CRÍTICA)
   - **Requisito**: Presupuesto debe estar en estado CONGELADO
   - **Estado**: Violación STATE_DEPENDENCY
   - **Problema**: Requiere que presupuesto esté COMPLETE y en estado CONGELADO
   - **Acción**: Completar presupuesto y verificar estado CONGELADO
   - **Impacto**: -1 violación crítica

3. **Endpoints faltantes** (VERIFICAR)
   - **Estado**: Solo existe POST para calcular
   - **Acción**: Verificar si se requieren más endpoints
   - **Impacto**: Puede afectar estado COMPLETE

### Verificación: Módulo "cambios" vs "reajuste"

**Hipótesis**: El módulo "cambios" puede ser el mismo que "reajuste" o puede ser un módulo separado que aún no existe.

**Acción**: Verificar en roadmap si "cambios" y "reajuste" son el mismo concepto.

### Acciones Requeridas

1. **Verificar mapeo módulo "cambios"** (Prioridad ALTA)
   - Verificar si "cambios" = "reajuste"
   - Ajustar detección si es necesario

2. **Completar presupuesto y verificar estado CONGELADO** (Prioridad CRÍTICA)
   - Completar módulo presupuesto
   - Asegurar que presupuesto puede alcanzar estado CONGELADO
   - Resolver violación STATE_DEPENDENCY

3. **Evaluar endpoints adicionales** (Prioridad MEDIA)
   - Determinar si se requieren más endpoints
   - Implementar si es necesario

### Criterio de Éxito
- ✅ Entidades del módulo existen
- ✅ Servicios del módulo existen
- ✅ Endpoints necesarios existen
- ✅ Módulo marcado como IN_PROGRESS o COMPLETE
- ✅ Dependencia de presupuesto CONGELADO resuelta

---

## 🎯 Acciones Concretas Requeridas

### Referencias Faltantes (CRÍTICO)

1. **Compra -> Presupuesto**
   - **Archivo**: `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/Compra.java`
   - **Acción**: Agregar campo `private UUID presupuestoId;`
   - **Línea**: Después de línea 28 (`private final UUID proyectoId;`)

2. **Estimacion -> Presupuesto**
   - **Archivo**: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
   - **Acción**: Agregar campo `private UUID presupuestoId;`
   - **Línea**: Después de línea 30 (`private final UUID proyectoId;`)

3. **Estimacion -> Billetera**
   - **Archivo**: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
   - **Acción**: Agregar campo `private UUID billeteraId;`
   - **Línea**: Después de `presupuestoId`

### Correcciones en Validador (ALTA PRIORIDAD)

1. **EstadoPresupuesto** (Falso Positivo)
   - **Archivo**: `tools/domain-validator/src/main/java/.../StateMachineDetector.java`
   - **Problema**: No detecta enum en `com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto`
   - **Acción**: Mejorar búsqueda de enums en paquetes `model`

2. **NaturalezaGasto** (Falso Positivo)
   - **Archivo**: `tools/domain-validator/src/main/java/.../StateMachineDetector.java`
   - **Problema**: No detecta enum en `com.budgetpro.domain.logistica.compra.model.NaturalezaGasto`
   - **Acción**: Mejorar detección de enums y mapeo a módulos

3. **ActividadProgramada -> Partida** (Falso Positivo)
   - **Archivo**: `tools/domain-validator/src/main/java/.../ValidationRuleExecutor.java`
   - **Problema**: No detecta relación por campo UUID `partidaId`
   - **Acción**: Mejorar detección de relaciones por campos UUID

4. **APUSnapshot en Presupuesto** (Verificar)
   - **Archivo**: `tools/domain-validator/src/main/resources/canonical-roadmap.json`
   - **Problema**: APUSnapshot existe en catalogo pero se requiere en presupuesto
   - **Acción**: Verificar si es regla válida o ajustar roadmap

### Módulo RRHH (CRÍTICO - Crear desde cero)

**Estructura a crear**:
```
backend/src/main/java/com/budgetpro/domain/rrhh/
├── model/
│   ├── Empleado.java
│   ├── EmpleadoId.java
│   ├── AsignacionTrabajo.java
│   ├── AsignacionTrabajoId.java
│   ├── RegistroTiempo.java
│   └── RegistroTiempoId.java
├── service/
│   └── GestionRRHHService.java
└── port/
    └── out/
        └── EmpleadoRepository.java
```

**Endpoints a crear**:
- `POST /api/v1/proyectos/{proyectoId}/rrhh/asignaciones`
- `POST /api/v1/proyectos/{proyectoId}/rrhh/registros-tiempo`
- `GET /api/v1/proyectos/{proyectoId}/rrhh/asignaciones`
- `GET /api/v1/proyectos/{proyectoId}/rrhh/registros-tiempo`

### Módulo Cambios (Verificar Mapeo)

- **Verificar**: ¿"cambios" = "reajuste"?
- **Archivo**: `tools/domain-validator/src/main/resources/canonical-roadmap.json`
- **Acción**: Si son diferentes, crear módulo "cambios" o ajustar mapeo

---

## 📊 Resumen de Acciones por Prioridad

### Prioridad CRÍTICA
1. **RRHH**: Crear módulo completo (entidades, servicios, endpoints)
2. **Cambios**: Resolver dependencia de presupuesto CONGELADO
3. **Presupuesto**: Corregir detección de EstadoPresupuesto
4. **Tiempo**: Corregir detección de relación ActividadProgramada -> Partida
5. **Compras**: Corregir detección de NaturalezaGasto

### Prioridad ALTA
1. **Presupuesto**: Verificar método `congelar()` separado
2. **Presupuesto**: Verificar/Corregir detección de APUSnapshot
3. **Tiempo**: Verificar detección de endpoints
4. **Compras**: Verificar referencia Compra -> Presupuesto
5. **Estimacion**: Verificar referencias a Presupuesto y Billetera
6. **Inventarios**: Verificar referencia Inventario -> Compra
7. **Inventarios**: Verificar estado del módulo (NOT_STARTED vs IN_PROGRESS)
8. **Cambios**: Verificar mapeo módulo "cambios" vs "reajuste"

### Prioridad MEDIA
1. **Tiempo**: Verificar mapeo de controladores a módulos
2. **Compras**: Verificar detección de endpoint
3. **Inventarios**: Evaluar endpoints adicionales
4. **Cambios**: Evaluar endpoints adicionales

---

## 🔄 Orden de Ejecución Recomendado

### Fase 1: Corregir Falsos Positivos (1-2 días)
1. Corregir detección de EstadoPresupuesto
2. Corregir detección de NaturalezaGasto
3. Corregir detección de relación ActividadProgramada -> Partida
4. Verificar/Corregir detección de APUSnapshot

### Fase 2: Completar Baseline Foundation (1-2 semanas)
1. Completar módulo **presupuesto**
2. Completar módulo **tiempo**
3. Verificar referencias faltantes en **compras** y **estimacion**

### Fase 3: Completar Módulos de Ejecución (2-3 semanas)
1. Completar módulo **compras**
2. Completar módulo **estimacion**
3. Completar módulo **inventarios**

### Fase 4: Completar Módulos Restantes (2-3 semanas)
1. Crear módulo **rrhh** desde cero
2. Completar módulo **cambios** (verificar mapeo)

---

**Última Actualización**: 2026-01-23  
**Próxima Revisión**: Al completar Fase 1
