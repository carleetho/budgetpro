# Plan Estratégico de Validación - BudgetPro Domain Validator

**Fecha de Análisis**: 2026-01-23  
**Reporte Analizado**: `report.json`  
**Estado General**: 🔴 **CRITICAL_VIOLATIONS** (25 críticas, 16 advertencias)  
**Versión Roadmap**: 1.0.0  
**Última Actualización**: 2026-01-23 (Análisis detallado con script)

---

## 📊 Resumen Ejecutivo

### Estado Actual del Sistema

| Categoría | Cantidad | Estado |
|-----------|----------|--------|
| **Módulos Completos** | 4 | ✅ catalogo, billetera, alertas, evm |
| **Módulos en Progreso** | 5 | 🟡 proyecto, presupuesto, tiempo, compras, estimacion |
| **Módulos No Iniciados** | 3 | ⚪ inventarios, rrhh, cambios |
| **Violaciones Críticas** | 25 | 🔴 Bloquean desarrollo |
| **Advertencias** | 16 | ⚠️ Requieren atención |
| **Total Violaciones** | 41 | - |

### Problemas Críticos Identificados

#### 🔧 Falsos Positivos (4 violaciones - Corregir Detección)
1. **Billetera en proyecto**: Billetera está COMPLETE pero no se detecta en módulo proyecto
   - ✅ **SOLUCIÓN CONOCIDA**: Actualizar `CodebaseAnalyzer.filterByModule()` para incluir "billetera" en keywords de "proyecto"
2. **APUSnapshot en presupuesto**: APUSnapshot existe en módulo catalogo (COMPLETE) pero no se detecta en presupuesto
   - ⚠️ **ANÁLISIS NECESARIO**: Verificar si es regla válida del roadmap o error de detección
3. **EstadoPresupuesto no detectado**: Enum existe en `com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto`
   - ✅ **SOLUCIÓN CONOCIDA**: Mejorar detección de enums/state machines en `StateMachineDetector`
4. **NaturalezaGasto no detectado**: Enum existe en `com.budgetpro.domain.logistica.compra.model.NaturalezaGasto`
   - ✅ **SOLUCIÓN CONOCIDA**: Mejorar detección de enums en módulo compras

#### ⚠️ Problemas Reales (21 violaciones críticas)
1. **Dependencias de Estado**: Módulos requieren estados específicos (ej: Presupuesto CONGELADO para cambios)
2. **Relaciones Faltantes**: ActividadProgramada -> Partida existe (partidaId en ActividadProgramada) pero no se detecta
   - ✅ **VERIFICADO**: ActividadProgramada tiene campo `partidaId` (UUID), relación 1:1 según comentarios
   - ⚠️ **SOLUCIÓN**: Mejorar detección de relaciones en `ValidationRuleExecutor`
3. **Referencias Faltantes**: Varias referencias no detectadas (Compra.presupuesto_id, Estimacion.presupuesto_id, etc.)
4. **Dependencias de Módulos**: 15 violaciones de DATA_DEPENDENCY que se resolverán cuando módulos base estén COMPLETE
5. **Acoplamiento Temporal**: Freeze acoplado entre Presupuesto y Tiempo (WARNING, no crítico pero importante)

---

## 🎯 Objetivos Estratégicos

### Objetivo 1: Completar Baseline Foundation (Prioridad CRÍTICA)
**Meta**: Llevar módulos de baseline (proyecto, presupuesto, tiempo) a estado COMPLETE

### Objetivo 2: Resolver Falsos Positivos (Prioridad ALTA)
**Meta**: Corregir detección de entidades que existen pero no se mapean correctamente

### Objetivo 3: Implementar Módulos de Ejecución (Prioridad MEDIA)
**Meta**: Completar compras, inventarios, rrhh siguiendo dependencias

### Objetivo 4: Validar Acoplamiento Temporal (Prioridad MEDIA)
**Meta**: Verificar que freeze acoplado funciona correctamente

---

## 📋 Plan de Acción por Fases

### 🔴 FASE 1: Resolución de Falsos Positivos (Sprint Inmediato)

**Duración Estimada**: 1-2 días  
**Impacto**: Reduce ~4-5 violaciones críticas (falsos positivos)

#### Tarea 1.1: Corregir Detección de Billetera en Módulo Proyecto
- **Problema**: Billetera está COMPLETE pero validador no la detecta en módulo proyecto
- **Causa Raíz**: Mapeo de módulos a palabras clave no incluye "billetera" para módulo "proyecto"
- **Solución**: Actualizar `CodebaseAnalyzer.filterByModule()` para incluir "billetera" en keywords de "proyecto"
- **Archivos a Modificar**:
  - `tools/domain-validator/src/main/java/com/budgetpro/validator/analyzer/CodebaseAnalyzer.java`
- **Criterio de Éxito**: Violación crítica de Billetera en proyecto desaparece

#### Tarea 1.2: Verificar Detección de APUSnapshot
- **Problema**: APUSnapshot no detectado en módulo presupuesto
- **Estado Actual**: APUSnapshot existe en módulo catalogo (COMPLETE)
- **Análisis Necesario**: Verificar si es un problema de detección o de regla de validación
- **Acción**: Revisar `canonical-roadmap.json` para entender por qué presupuesto requiere APUSnapshot
- **Criterio de Éxito**: Confirmar si es falso positivo o regla válida

#### Tarea 1.3: Corregir Detección de EstadoPresupuesto
- **Problema**: State machine EstadoPresupuesto no detectado
- **Estado Actual**: ✅ Enum existe en `com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto`
- **Causa Raíz**: `StateMachineDetector` no detecta enums en paquetes `model`
- **Solución**: Mejorar `StateMachineDetector` para buscar enums en paquetes model
- **Archivos a Modificar**:
  - `tools/domain-validator/src/main/java/com/budgetpro/validator/analyzer/StateMachineDetector.java`
- **Criterio de Éxito**: Violación crítica de EstadoPresupuesto desaparece

#### Tarea 1.4: Corregir Detección de NaturalezaGasto
- **Problema**: Enum NaturalezaGasto no detectado en módulo compras
- **Estado Actual**: ✅ Enum existe en `com.budgetpro.domain.logistica.compra.model.NaturalezaGasto`
- **Causa Raíz**: `StateMachineDetector` no detecta enums o no se mapea a módulo compras
- **Solución**: Verificar y mejorar detección de enums en módulo compras
- **Archivos a Modificar**:
  - `tools/domain-validator/src/main/java/com/budgetpro/validator/analyzer/StateMachineDetector.java`
- **Criterio de Éxito**: Violación crítica de NaturalezaGasto desaparece

#### Tarea 1.5: Corregir Detección de Relación ActividadProgramada -> Partida
- **Problema**: Relación no detectada aunque existe en código
- **Estado Actual**: ✅ ActividadProgramada tiene campo `partidaId` (UUID) - relación 1:1
- **Causa Raíz**: `ValidationRuleExecutor` no detecta relaciones por campos UUID
- **Solución**: Mejorar detección de relaciones en `ValidationRuleExecutor.relationship_exists`
- **Archivos a Modificar**:
  - `tools/domain-validator/src/main/java/com/budgetpro/validator/engine/ValidationRuleExecutor.java`
- **Criterio de Éxito**: Violación crítica de relación desaparece

---

### 🟡 FASE 2: Completar Baseline Foundation (Sprint 1-2)

**Duración Estimada**: 1-2 semanas  
**Impacto**: Resuelve dependencias críticas para todos los módulos

#### Tarea 2.1: Completar Módulo Proyecto
**Estado Actual**: IN_PROGRESS (3 entidades, 0 servicios, 3 endpoints)

**Acciones Requeridas**:
1. ✅ Verificar que Billetera se detecta correctamente (Fase 1)
2. ⬜ Implementar servicios faltantes si son requeridos
3. ⬜ Completar validaciones de reglas de negocio
4. ⬜ Ejecutar validación y verificar estado COMPLETE

**Criterio de Éxito**: 
- Estado: COMPLETE
- Sin violaciones críticas
- Todas las entidades requeridas detectadas

#### Tarea 2.2: Completar Módulo Presupuesto
**Estado Actual**: IN_PROGRESS (15 entidades, 7 servicios, 5 endpoints)

**Violaciones Críticas a Resolver**:
1. ⬜ **APUSnapshot no detectado**: Verificar si es falso positivo (Fase 1.2)
2. ⬜ **EstadoPresupuesto no detectado**: Implementar o corregir detección (Fase 1.3)
3. ⬜ **Dependencia de proyecto**: Resolver cuando proyecto esté COMPLETE (Tarea 2.1)

**Acciones Requeridas**:
1. Resolver falsos positivos identificados en Fase 1
2. Verificar que todas las entidades requeridas existen
3. Implementar EstadoPresupuesto si no existe
4. Verificar que PresupuestoService tiene método `aprobar()` y `congelar()`
5. Validar acoplamiento temporal con Tiempo (ver Tarea 2.3)

**Criterio de Éxito**:
- Estado: COMPLETE
- Sin violaciones críticas
- EstadoPresupuesto implementado y detectado
- Métodos de freeze funcionando

#### Tarea 2.3: Completar Módulo Tiempo
**Estado Actual**: IN_PROGRESS (8 entidades, 3 servicios, 0 endpoints)

**Violaciones Críticas a Resolver**:
1. ✅ **Relación ActividadProgramada -> Partida**: ✅ VERIFICADO - Existe campo `partidaId` en ActividadProgramada (Fase 1.5)
2. ⬜ **Dependencia de presupuesto**: Resolver cuando presupuesto esté COMPLETE (Tarea 2.2)

**Acciones Requeridas**:
1. ✅ Relación ActividadProgramada -> Partida verificada (campo `partidaId` existe)
2. Corregir detección de relación (Fase 1.5)
3. Completar cuando presupuesto esté COMPLETE
3. Verificar acoplamiento temporal con Presupuesto
   - Confirmar que `PresupuestoService.aprobar()` llama a `CronogramaService.congelarPorPresupuesto()`
4. Implementar endpoints REST si son requeridos

**Criterio de Éxito**:
- Estado: COMPLETE
- Sin violaciones críticas
- Relación ActividadProgramada -> Partida verificada/implementada
- Acoplamiento temporal funcionando

---

### 🟢 FASE 3: Módulos de Ejecución (Sprint 3-4)

**Duración Estimada**: 2-3 semanas  
**Dependencias**: Fase 2 completada

#### Tarea 3.1: Completar Módulo Compras
**Estado Actual**: IN_PROGRESS (6 entidades, 2 servicios, 0 endpoints)

**Violaciones Críticas a Resolver**:
1. ✅ **Enum NaturalezaGasto**: ✅ VERIFICADO - Existe pero no se detecta (Fase 1.4)
   - Valores actuales: DIRECTO_PARTIDA, GENERAL_OBRA, ADMINISTRATIVO
   - ⚠️ **NOTA**: Valores del código difieren ligeramente del roadmap (sin prefijo GASTO_)
2. ⬜ **Referencia Compra.presupuesto_id -> Presupuesto**: Verificar si existe en código
3. ⬜ **Dependencias**: Resolver cuando presupuesto y proyecto estén COMPLETE

**Acciones Requeridas**:
1. ✅ Corregir detección de NaturalezaGasto (Fase 1.4)
2. Verificar referencia a Presupuesto en Compra (buscar campo presupuestoId)
3. Verificar si valores del enum coinciden con roadmap o ajustar roadmap
3. Implementar endpoints REST si son requeridos

**Criterio de Éxito**:
- Estado: COMPLETE
- NaturalezaGasto implementado y detectado
- Referencias a Presupuesto verificadas

#### Tarea 3.2: Implementar Módulo Inventarios
**Estado Actual**: NOT_STARTED (5 entidades detectadas, 1 servicio, 0 endpoints)

**Violaciones Críticas a Resolver**:
1. ⬜ **Referencia Inventario.compra_id -> Compra**: Implementar cuando compras esté COMPLETE
2. ⬜ **Dependencia de compras**: Resolver cuando compras esté COMPLETE

**Acciones Requeridas**:
1. Completar implementación de entidades (ya existen parcialmente)
2. Implementar servicios faltantes
3. Implementar endpoints REST
4. Verificar referencias a Compra

**Criterio de Éxito**:
- Estado: COMPLETE
- Todas las entidades implementadas
- Referencias a Compra verificadas

#### Tarea 3.3: Implementar Módulo RRHH
**Estado Actual**: NOT_STARTED (0 entidades, 0 servicios, 0 endpoints)

**Violaciones Críticas a Resolver**:
1. ⬜ **Dependencia de tiempo**: Resolver cuando tiempo esté COMPLETE

**Acciones Requeridas**:
1. Implementar entidades del módulo RRHH
2. Implementar servicios
3. Implementar endpoints REST
4. Verificar integración con módulo tiempo

**Criterio de Éxito**:
- Estado: COMPLETE
- Módulo completamente implementado
- Integración con tiempo verificada

---

### 🔵 FASE 4: Módulos de Análisis y Soporte (Sprint 5+)

**Duración Estimada**: 2-3 semanas  
**Dependencias**: Fases 2 y 3 completadas

#### Tarea 4.1: Completar Módulo Estimación
**Estado Actual**: IN_PROGRESS (7 entidades, 1 servicio, 2 endpoints)

**Violaciones Críticas a Resolver**:
1. ⬜ **Referencia Estimacion.presupuesto_id -> Presupuesto**: Verificar
2. ⬜ **Referencia Estimacion.billetera_id -> Billetera**: Verificar
3. ⬜ **Dependencias**: Resolver cuando presupuesto y proyecto estén COMPLETE

**Acciones Requeridas**:
1. Verificar referencias a Presupuesto y Billetera
2. Completar servicios faltantes
3. Validar integración completa

**Criterio de Éxito**:
- Estado: COMPLETE
- Todas las referencias verificadas

#### Tarea 4.2: Verificar Módulo EVM
**Estado Actual**: COMPLETE (pero con dependencias faltantes)

**Violaciones Críticas a Resolver**:
1. ⬜ **Dependencias**: Resolver cuando presupuesto, tiempo, compras, estimacion estén COMPLETE

**Acciones Requeridas**:
1. Verificar que todas las dependencias están completas
2. Validar integración con módulos dependientes

**Criterio de Éxito**:
- Sin violaciones de dependencias
- Integración completa verificada

#### Tarea 4.3: Implementar Módulo Cambios
**Estado Actual**: NOT_STARTED (6 entidades detectadas, 1 servicio, 0 endpoints)

**Violaciones Críticas a Resolver**:
1. ⬜ **State Dependency**: Requiere Presupuesto en estado CONGELADO
2. ⬜ **Dependencia de presupuesto**: Resolver cuando presupuesto esté COMPLETE y CONGELADO

**Acciones Requeridas**:
1. Completar implementación de entidades
2. Implementar servicios
3. Verificar que Presupuesto puede alcanzar estado CONGELADO
4. Implementar validación de estado antes de usar módulo cambios

**Criterio de Éxito**:
- Estado: COMPLETE
- Validación de estado CONGELADO funcionando

---

## 🔍 Análisis Detallado de Violaciones

### Violaciones por Tipo

#### 1. BUSINESS_LOGIC (13 violaciones críticas)

| Módulo | Violación | Estado Actual | Acción Requerida |
|--------|-----------|---------------|------------------|
| proyecto | Billetera no detectada | Billetera COMPLETE | Corregir detección |
| presupuesto | APUSnapshot no detectado | APUSnapshot en catalogo | Verificar regla |
| presupuesto | EstadoPresupuesto no detectado | ? | Implementar o corregir |
| tiempo | ActividadProgramada -> Partida | ? | Verificar relación |
| compras | NaturalezaGasto no detectado | ? | Implementar enum |
| compras | Compra.presupuesto_id -> Presupuesto | ? | Verificar referencia |
| inventarios | Inventario.compra_id -> Compra | ? | Verificar referencia |
| estimacion | Estimacion.presupuesto_id -> Presupuesto | ? | Verificar referencia |
| estimacion | Estimacion.billetera_id -> Billetera | ? | Verificar referencia |

#### 2. DATA_DEPENDENCY (12 violaciones críticas)

Todas estas se resolverán cuando los módulos dependientes estén COMPLETE:
- presupuesto requiere proyecto COMPLETE
- tiempo requiere presupuesto COMPLETE
- compras requiere presupuesto y proyecto COMPLETE
- estimacion requiere presupuesto y proyecto COMPLETE
- evm requiere presupuesto, tiempo, compras, estimacion COMPLETE
- alertas requiere compras, inventarios, rrhh, estimacion COMPLETE
- billetera requiere proyecto COMPLETE

#### 3. STATE_DEPENDENCY (1 violación crítica)

- cambios requiere Presupuesto en estado CONGELADO

#### 4. TEMPORAL_DEPENDENCY (1 advertencia)

- presupuesto y tiempo: Freeze acoplado (WARNING, no crítico)

---

## 📈 Métricas de Progreso

### Indicadores Clave (KPIs)

| Métrica | Valor Actual | Meta Fase 1 | Meta Fase 2 | Meta Final |
|---------|--------------|-------------|-------------|------------|
| Violaciones Críticas | 25 | 20 | 5 | 0 |
| Violaciones WARNING | 16 | 16 | 10 | 0-5 |
| Módulos COMPLETE | 4 | 4 | 7 | 12 |
| Módulos IN_PROGRESS | 5 | 5 | 0 | 0 |
| Módulos NOT_STARTED | 3 | 3 | 0 | 0 |
| Exit Code | 1 (CRITICAL) | 1 | 2 (WARNINGS) | 0 (PASSED) |

### Criterios de Éxito por Fase

**Fase 1 (Falsos Positivos)**:
- ✅ Reducir violaciones críticas de 28 a ≤23
- ✅ Billetera detectada en módulo proyecto
- ✅ EstadoPresupuesto identificado o implementado

**Fase 2 (Baseline)**:
- ✅ proyecto: COMPLETE
- ✅ presupuesto: COMPLETE
- ✅ tiempo: COMPLETE
- ✅ Reducir violaciones críticas a ≤5
- ✅ Exit code: 2 (WARNINGS) o mejor

**Fase 3 (Ejecución)**:
- ✅ compras: COMPLETE
- ✅ inventarios: COMPLETE
- ✅ rrhh: COMPLETE
- ✅ Reducir violaciones críticas a 0

**Fase 4 (Análisis)**:
- ✅ estimacion: COMPLETE
- ✅ cambios: COMPLETE
- ✅ evm: Sin violaciones de dependencias
- ✅ Exit code: 0 (PASSED)

---

## 🛠️ Herramientas y Scripts

### Scripts de Automatización

1. **validate.sh**: Ejecutar validación con opciones
   ```bash
   ./validate.sh --format json --output report.json
   ```

2. **Análisis de Reporte**: Script para analizar report.json
   ```bash
   # Por crear
   ./analyze-report.sh report.json
   ```

3. **Generación de Roadmap**: Visualizar dependencias
   ```bash
   java -jar target/domain-validator-1.0.0-SNAPSHOT.jar generate-roadmap
   ```

### Comandos Útiles

```bash
# Validación básica
./validate.sh

# Validación estricta
./validate.sh --strict

# Generar reporte JSON
./validate.sh --format json --output report.json

# Verificar módulo específico
java -jar target/domain-validator-1.0.0-SNAPSHOT.jar check-module presupuesto
```

---

## 🎯 Priorización de Tareas

### Matriz de Prioridad vs Impacto

| Tarea | Prioridad | Impacto | Esfuerzo | Orden |
|-------|-----------|---------|----------|-------|
| Corregir detección Billetera | 🔴 ALTA | ALTO | BAJO | 1 |
| Verificar EstadoPresupuesto | 🔴 ALTA | ALTO | MEDIO | 2 |
| Completar módulo proyecto | 🔴 ALTA | ALTO | MEDIO | 3 |
| Completar módulo presupuesto | 🔴 ALTA | ALTO | ALTO | 4 |
| Completar módulo tiempo | 🔴 ALTA | ALTO | MEDIO | 5 |
| Implementar NaturalezaGasto | 🟡 MEDIA | MEDIO | BAJO | 6 |
| Verificar relaciones faltantes | 🟡 MEDIA | MEDIO | MEDIO | 7 |
| Completar módulos ejecución | 🟢 BAJA | ALTO | ALTO | 8+ |

---

## 📝 Notas de Implementación

### Problemas Conocidos

1. **Falso Positivo Billetera**: Billetera está COMPLETE pero no se detecta en módulo proyecto
   - **Causa**: Mapeo de keywords en CodebaseAnalyzer
   - **Solución**: Agregar "billetera" a keywords de "proyecto"

2. **APUSnapshot en Presupuesto**: APUSnapshot existe en catalogo pero se requiere en presupuesto
   - **Análisis Necesario**: Verificar si es regla válida o error de roadmap
   - **Acción**: Revisar especificaciones del dominio

3. **Relación ActividadProgramada -> Partida**: No detectada
   - **Análisis Necesario**: Verificar si existe en código o necesita implementarse
   - **Acción**: Buscar referencias en ActividadProgramada

### Decisiones Pendientes

1. ¿EstadoPresupuesto existe o necesita implementarse?
2. ¿NaturalezaGasto existe o necesita implementarse?
3. ¿Relación ActividadProgramada -> Partida es directa o indirecta?
4. ¿APUSnapshot debe estar en módulo presupuesto o solo en catalogo?

---

## 🔄 Proceso de Validación Continua

### Checkpoints por Sprint

**Al inicio de cada sprint**:
1. Ejecutar validación: `./validate.sh --format json --output report-sprint-X.json`
2. Analizar nuevas violaciones
3. Actualizar este plan estratégico
4. Priorizar tareas del sprint

**Al final de cada sprint**:
1. Ejecutar validación final
2. Comparar con reporte inicial
3. Medir progreso (KPIs)
4. Documentar lecciones aprendidas

### Integración CI/CD

```yaml
# Ejemplo de integración en CI/CD
- name: Validate Domain Roadmap
  run: |
    cd tools/domain-validator
    ./validate.sh --strict --format json --output validation-report.json
  continue-on-error: false
```

---

## 📚 Referencias

- **Roadmap Canónico**: `docs/context/ROADMAP_CANONICO.md`
- **Especificaciones de Módulos**: `docs/modules/*_SPECS.md`
- **Domain Model**: `docs/DOMAIN_MODEL.md`
- **Validator README**: `tools/domain-validator/README.md`

---

## ✅ Checklist de Seguimiento

### Fase 1: Falsos Positivos
- [ ] Tarea 1.1: Corregir detección Billetera
- [ ] Tarea 1.2: Verificar APUSnapshot
- [ ] Tarea 1.3: Corregir detección EstadoPresupuesto
- [ ] Tarea 1.4: Corregir detección NaturalezaGasto
- [ ] Tarea 1.5: Corregir detección relación ActividadProgramada -> Partida
- [ ] Validación: Reducir violaciones críticas de 25 a ≤20

### Fase 2: Baseline Foundation
- [ ] Tarea 2.1: Completar módulo proyecto
- [ ] Tarea 2.2: Completar módulo presupuesto
- [ ] Tarea 2.3: Completar módulo tiempo
- [ ] Validación: Reducir violaciones críticas a ≤5

### Fase 3: Módulos de Ejecución
- [ ] Tarea 3.1: Completar módulo compras
- [ ] Tarea 3.2: Implementar módulo inventarios
- [ ] Tarea 3.3: Implementar módulo rrhh
- [ ] Validación: Reducir violaciones críticas a 0

### Fase 4: Módulos de Análisis
- [ ] Tarea 4.1: Completar módulo estimacion
- [ ] Tarea 4.2: Verificar módulo evm
- [ ] Tarea 4.3: Implementar módulo cambios
- [ ] Validación: Exit code 0 (PASSED)

---

**Última Actualización**: 2026-01-23  
**Próxima Revisión**: Al completar Fase 1
