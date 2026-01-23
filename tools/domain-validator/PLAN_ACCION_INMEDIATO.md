# Plan de Acción Inmediato - Análisis del Report.json

**Fecha**: 2026-01-23  
**Reporte**: `report.json`  
**Estado**: 🔴 CRITICAL_VIOLATIONS (25 críticas, 16 advertencias)

---

## 🎯 Resumen Ejecutivo

### Situación Actual
- **Total Violaciones**: 41 (25 críticas + 16 advertencias)
- **Módulos COMPLETE**: 4 (catalogo, billetera, alertas, evm)
- **Módulos IN_PROGRESS**: 5 (proyecto, presupuesto, tiempo, compras, estimacion)
- **Módulos NOT_STARTED**: 3 (inventarios, rrhh, cambios)

### Hallazgos Clave

#### ✅ Falsos Positivos Identificados (4 violaciones)
1. **Billetera en proyecto**: Existe pero no se detecta
2. **EstadoPresupuesto**: Enum existe pero no se detecta
3. **NaturalezaGasto**: Enum existe pero no se detecta
4. **APUSnapshot en presupuesto**: Existe en catalogo, verificar regla

#### ⚠️ Problemas Reales (21 violaciones críticas)
- **15 DATA_DEPENDENCY**: Se resolverán cuando módulos base estén COMPLETE
- **1 STATE_DEPENDENCY**: Cambios requiere Presupuesto CONGELADO
- **5 BUSINESS_LOGIC**: Referencias y relaciones no detectadas

---

## 🚀 Acciones Inmediatas (Sprint Actual)

### Prioridad 1: Corregir Falsos Positivos (1-2 días)

#### ✅ Tarea 1.1: Corregir Detección de Billetera
**Archivo**: `CodebaseAnalyzer.java`  
**Acción**: Agregar "billetera" a keywords de módulo "proyecto"  
**Impacto**: -1 violación crítica

```java
moduleKeywords.put("proyecto", List.of("proyecto", "billetera"));
```

#### ✅ Tarea 1.2: Corregir Detección de EstadoPresupuesto
**Archivo**: `StateMachineDetector.java`  
**Problema**: No detecta enums en `com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto`  
**Acción**: Mejorar búsqueda de enums en paquetes model  
**Impacto**: -1 violación crítica

#### ✅ Tarea 1.3: Corregir Detección de NaturalezaGasto
**Archivo**: `StateMachineDetector.java`  
**Problema**: No detecta enum en módulo compras  
**Acción**: Mejorar detección de enums y mapeo a módulos  
**Impacto**: -1 violación crítica

#### ⚠️ Tarea 1.4: Analizar APUSnapshot en Presupuesto
**Problema**: APUSnapshot existe en catalogo pero se requiere en presupuesto  
**Acción**: 
1. Revisar `canonical-roadmap.json` - ¿es regla válida?
2. Si es válida: Verificar si presupuesto debe tener referencia a APUSnapshot
3. Si es falsa: Ajustar roadmap o detección
**Impacto**: -1 violación crítica (si es falso positivo)

#### ✅ Tarea 1.5: Corregir Detección de Relación ActividadProgramada -> Partida
**Archivo**: `ValidationRuleExecutor.java`  
**Estado**: ✅ Relación existe (campo `partidaId` en ActividadProgramada)  
**Problema**: No se detecta relación por UUID  
**Acción**: Mejorar detección de relaciones por campos UUID  
**Impacto**: -1 violación crítica

**Resultado Esperado Fase 1**: Reducir de 25 a 20-21 violaciones críticas

---

### Prioridad 2: Completar Baseline Foundation (1-2 semanas)

#### Tarea 2.1: Completar Módulo Proyecto
**Estado Actual**: IN_PROGRESS (3 entidades, 0 servicios, 3 endpoints)  
**Violaciones**: 1 crítica (Billetera - se resuelve en Fase 1)  
**Acciones**:
1. ✅ Resolver detección de Billetera (Fase 1.1)
2. Verificar que todas las reglas de validación se cumplen
3. Ejecutar validación y verificar estado COMPLETE

**Criterio de Éxito**: Estado COMPLETE, 0 violaciones críticas

#### Tarea 2.2: Completar Módulo Presupuesto
**Estado Actual**: IN_PROGRESS (15 entidades, 7 servicios, 5 endpoints)  
**Violaciones**: 3 críticas
- APUSnapshot (Fase 1.4)
- EstadoPresupuesto (Fase 1.2)
- Dependencia de proyecto (se resuelve cuando proyecto esté COMPLETE)

**Acciones**:
1. Resolver falsos positivos (Fase 1)
2. Verificar que proyecto esté COMPLETE
3. Validar que todas las entidades requeridas existen
4. Verificar métodos `aprobar()` y `congelar()` en PresupuestoService

**Criterio de Éxito**: Estado COMPLETE, 0 violaciones críticas

#### Tarea 2.3: Completar Módulo Tiempo
**Estado Actual**: IN_PROGRESS (8 entidades, 3 servicios, 0 endpoints)  
**Violaciones**: 2 críticas
- Relación ActividadProgramada -> Partida (Fase 1.5)
- Dependencia de presupuesto (se resuelve cuando presupuesto esté COMPLETE)

**Acciones**:
1. Resolver detección de relación (Fase 1.5)
2. Verificar que presupuesto esté COMPLETE
3. Validar acoplamiento temporal con Presupuesto

**Criterio de Éxito**: Estado COMPLETE, 0 violaciones críticas

**Resultado Esperado Fase 2**: Reducir de 20-21 a ≤5 violaciones críticas

---

## 📊 Análisis de Violaciones por Tipo

### BUSINESS_LOGIC (9 críticas, 15 advertencias)
- **Falsos Positivos**: 4 (Billetera, EstadoPresupuesto, NaturalezaGasto, APUSnapshot)
- **Relaciones no detectadas**: 1 (ActividadProgramada -> Partida)
- **Referencias no detectadas**: 4 (Compra, Estimacion, Inventario)

### DATA_DEPENDENCY (15 críticas)
**Todas se resolverán cuando módulos base estén COMPLETE**:
- presupuesto requiere proyecto COMPLETE
- tiempo requiere presupuesto COMPLETE
- compras requiere presupuesto y proyecto COMPLETE
- estimacion requiere presupuesto y proyecto COMPLETE
- evm requiere presupuesto, tiempo, compras, estimacion COMPLETE
- alertas requiere compras, inventarios, rrhh, estimacion COMPLETE
- billetera requiere proyecto COMPLETE

### STATE_DEPENDENCY (1 crítica)
- cambios requiere Presupuesto en estado CONGELADO

### TEMPORAL_DEPENDENCY (1 advertencia)
- presupuesto y tiempo: Freeze acoplado (WARNING, no crítico)

---

## 🛠️ Herramientas Disponibles

### Scripts de Análisis

1. **analyze-report.sh**: Análisis detallado del reporte
   ```bash
   ./analyze-report.sh report.json
   ```

2. **validate.sh**: Ejecutar validación
   ```bash
   ./validate.sh --format json --output report.json
   ```

### Comandos Útiles

```bash
# Análisis completo
cd tools/domain-validator
./analyze-report.sh report.json

# Validación con reporte JSON
./validate.sh --format json --output report.json

# Validación estricta
./validate.sh --strict
```

---

## ✅ Checklist de Seguimiento

### Fase 1: Falsos Positivos (1-2 días)
- [ ] Tarea 1.1: Corregir detección Billetera
- [ ] Tarea 1.2: Corregir detección EstadoPresupuesto
- [ ] Tarea 1.3: Corregir detección NaturalezaGasto
- [ ] Tarea 1.4: Analizar APUSnapshot
- [ ] Tarea 1.5: Corregir detección relación ActividadProgramada -> Partida
- [ ] Validación: Verificar reducción a ≤20 violaciones críticas

### Fase 2: Baseline Foundation (1-2 semanas)
- [ ] Tarea 2.1: Completar módulo proyecto
- [ ] Tarea 2.2: Completar módulo presupuesto
- [ ] Tarea 2.3: Completar módulo tiempo
- [ ] Validación: Verificar reducción a ≤5 violaciones críticas

---

## 📈 Métricas de Progreso

| Métrica | Actual | Meta Fase 1 | Meta Fase 2 | Meta Final |
|---------|--------|-------------|-------------|------------|
| Violaciones Críticas | 25 | 20 | 5 | 0 |
| Violaciones WARNING | 16 | 16 | 10 | 0-5 |
| Módulos COMPLETE | 4 | 4 | 7 | 12 |
| Exit Code | 1 | 1 | 2 | 0 |

---

**Próxima Revisión**: Al completar Fase 1  
**Responsable**: Equipo de Desarrollo  
**Última Actualización**: 2026-01-23
