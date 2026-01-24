# Estrategia de Transacciones - Principio de Baseline

## Resumen Ejecutivo

Este documento describe la estrategia de transacciones implementada para garantizar la atomicidad del **Principio de Baseline**, donde la aprobación del presupuesto y el congelamiento del cronograma deben ser operaciones atómicas.

## Principio de Baseline

**Baseline = Presupuesto (CONGELADO) + Cronograma (CONGELADO)**

Cuando un presupuesto se aprueba, el cronograma asociado también debe congelarse simultáneamente. Esta operación debe ser **atómica**: o ambas operaciones se completan exitosamente, o ninguna se persiste.

## Arquitectura de Transacciones

### Capa de Aplicación (Use Case)

**Clase:** `AprobarPresupuestoUseCaseImpl`

```java
@Transactional(
    rollbackFor = Exception.class,
    propagation = Propagation.REQUIRED
)
public void aprobar(UUID presupuestoId)
```

**Responsabilidades:**
- Inicia la transacción que engloba todo el proceso
- Configura rollback para todas las excepciones (`rollbackFor = Exception.class`)
- Usa propagación `REQUIRED` (por defecto) para iniciar o participar en una transacción existente

**Flujo:**
1. Valida prerrequisitos (partidas con APU, cálculo posible)
2. Delega a `PresupuestoService.aprobar()` que orquesta el baseline
3. Cualquier excepción no capturada provoca rollback automático

### Capa de Dominio (Servicios)

#### PresupuestoService

**Clase:** `PresupuestoService`

**Configuración:** NO tiene `@Transactional`

**Razón:** Participa en la transacción iniciada por el use case mediante propagación `REQUIRED` (por defecto).

**Responsabilidades:**
- Orquesta la aprobación del presupuesto
- Llama a `CronogramaService.congelarPorPresupuesto()` dentro de la misma transacción
- Persiste el presupuesto aprobado

**Flujo:**
1. Valida que existe ProgramaObra
2. Aprueba el presupuesto (genera hashes de integridad)
3. Llama a `CronogramaService.congelarPorPresupuesto()` (misma transacción)
4. Persiste el presupuesto aprobado

#### CronogramaService

**Clase:** `CronogramaService`

**Configuración:** NO tiene `@Transactional`

**Razón:** Participa en la transacción iniciada por el use case a través de `PresupuestoService`.

**Responsabilidades:**
- Congela el ProgramaObra
- Genera el snapshot del cronograma
- Persiste ambos (ProgramaObra congelado + snapshot)

**Flujo:**
1. Valida que existe ProgramaObra con fechas válidas
2. Congela el ProgramaObra
3. Genera el snapshot con datos JSONB
4. Persiste ProgramaObra y snapshot

## Garantías de Atomicidad

### Escenarios de Rollback

La transacción hace rollback automático en los siguientes casos:

1. **Falla en validación de prerrequisitos:**
   - Partidas sin APU → `PresupuestoNoPuedeAprobarseException` → rollback

2. **Falla en aprobación del presupuesto:**
   - Presupuesto no encontrado → `IllegalStateException` → rollback
   - Presupuesto ya aprobado → `IllegalStateException` → rollback

3. **Falla en congelamiento del cronograma:**
   - No existe ProgramaObra → `PresupuestoSinCronogramaException` → rollback
   - ProgramaObra sin fechas → `IllegalStateException` → rollback
   - ProgramaObra ya congelado → `IllegalStateException` → rollback

4. **Falla en persistencia:**
   - Error al guardar presupuesto → rollback completo
   - Error al guardar ProgramaObra → rollback completo
   - Error al guardar snapshot → rollback completo

### Estados Parciales Prohibidos

La estrategia garantiza que **nunca** se produzcan estos estados inconsistentes:

- ❌ Presupuesto aprobado sin cronograma congelado
- ❌ Cronograma congelado sin presupuesto aprobado
- ❌ Presupuesto aprobado sin snapshot generado
- ❌ Snapshot generado sin presupuesto aprobado

## Propagación de Transacciones

```
┌─────────────────────────────────────────────────────────┐
│ Use Case (@Transactional REQUIRED)                      │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ PresupuestoService.aprobar()                        │ │
│ │ (participa en transacción del use case)            │ │
│ │ ┌─────────────────────────────────────────────────┐ │ │
│ │ │ CronogramaService.congelarPorPresupuesto()     │ │ │
│ │ │ (participa en la misma transacción)            │ │ │
│ │ └─────────────────────────────────────────────────┘ │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

**Todas las operaciones se ejecutan en la misma transacción.**

## Configuración de Rollback

### `rollbackFor = Exception.class`

Esta configuración garantiza que **cualquier excepción** (checked o unchecked) provoca rollback:

- `RuntimeException` → rollback ✅
- `Exception` → rollback ✅
- `IllegalStateException` → rollback ✅
- `PresupuestoSinCronogramaException` → rollback ✅
- `PresupuestoNoPuedeAprobarseException` → rollback ✅

### Excepciones de Aplicación

Las excepciones de aplicación (`PresupuestoNoEncontradoException`, `PresupuestoNoPuedeAprobarseException`) extienden `RuntimeException`, por lo que automáticamente provocan rollback.

## Tests de Integración

### Verificación de Atomicidad

Los tests de integración verifican explícitamente:

1. **Rollback cuando falla congelamiento:**
   - `TransaccionalidadBaselineIntegrationTest.aprobarPresupuesto_debeHacerRollbackSiFallaCongelamientoCronograma()`

2. **Rollback cuando no existe presupuesto:**
   - `TransaccionalidadBaselineIntegrationTest.aprobarPresupuesto_debeHacerRollbackSiPresupuestoNoExiste()`

3. **Rollback cuando no existe cronograma:**
   - `TransaccionalidadBaselineIntegrationTest.aprobarPresupuesto_debeHacerRollbackSiNoExisteCronograma()`

4. **Commit exitoso cuando todo es válido:**
   - `TransaccionalidadBaselineIntegrationTest.aprobarPresupuesto_debeSerAtomicoCuandoTodoEsValido()`

### Base de Datos de Prueba

Los tests usan `AbstractIntegrationTest` que:
- Configura un contenedor PostgreSQL con Testcontainers
- Ejecuta migraciones Flyway automáticamente
- Aísla cada test en su propia transacción (`@Transactional`)

## Mejores Prácticas

### ✅ Hacer

1. **Mantener `@Transactional` solo en la capa de aplicación:**
   - Use cases manejan la transaccionalidad
   - Servicios de dominio participan en la transacción

2. **Usar `rollbackFor = Exception.class`:**
   - Garantiza rollback para todas las excepciones
   - Evita estados parciales

3. **Documentar la estrategia:**
   - Comentarios en código explican el flujo transaccional
   - Tests verifican explícitamente la atomicidad

4. **Validar prerrequisitos antes de modificar estado:**
   - Validaciones tempranas evitan trabajo innecesario
   - Fallos rápidos mejoran el rendimiento

### ❌ Evitar

1. **NO usar `@Transactional` en servicios de dominio:**
   - Rompe el principio de responsabilidad única
   - Dificulta el control de transacciones

2. **NO capturar excepciones sin re-lanzarlas:**
   - Si se captura una excepción, debe re-lanzarse o manejarse explícitamente
   - Las excepciones no capturadas provocan rollback automático

3. **NO hacer operaciones fuera de la transacción:**
   - Todas las operaciones de persistencia deben estar en la misma transacción
   - Evitar llamadas a servicios externos dentro de la transacción

## Referencias

- `AprobarPresupuestoUseCaseImpl.java` - Implementación del use case
- `PresupuestoService.java` - Servicio de dominio para presupuesto
- `CronogramaService.java` - Servicio de dominio para cronograma
- `BaselinePrincipleIntegrationTest.java` - Tests del principio de baseline
- `TransaccionalidadBaselineIntegrationTest.java` - Tests de atomicidad

## Conclusión

La estrategia de transacciones implementada garantiza que el **Principio de Baseline** se mantenga atómicamente, evitando estados parciales y asegurando la consistencia de datos entre presupuesto y cronograma.
