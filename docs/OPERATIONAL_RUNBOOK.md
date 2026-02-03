# Integrity Violation Response Runbook

**Fecha:** 2026-01-19  
**Versión:** 1.0  
**Propósito:** Procedimientos operacionales para responder a violaciones de integridad criptográfica

---

## Tabla de Contenidos

1. [Detección de Violaciones](#detección-de-violaciones)
2. [Respuesta Inmediata](#respuesta-inmediata)
3. [Investigación](#investigación)
4. [Métricas a Monitorear](#métricas-a-monitorear)
5. [Procedimientos de Recuperación](#procedimientos-de-recuperación)
6. [Escalación](#escalación)

---

## Detección de Violaciones

Las violaciones de integridad se detectan cuando:

1. **Aprobación de Compra Falla**
   - Excepción: `BudgetIntegrityViolationException`
   - Log: `CRITICAL: Budget integrity violation detected`
   - Métrica: `budget.integrity.violations.total` incrementa

2. **Egreso de Billetera Falla**
   - Excepción: `BudgetIntegrityViolationException`
   - Log: `CRITICAL: Budget integrity violation detected`
   - Métrica: `budget.integrity.violations.total` incrementa

3. **Monitoreo de Métricas**
   - Alert cuando `budget.integrity.violations.total > 0`
   - Revisar logs estructurados para contexto

### Indicadores

- **Log Level**: `ERROR` con acción `integrity_violation`
- **Métrica**: `budget.integrity.violations.total{violation_type="...", algorithm="SHA-256-v1"}`
- **Audit Table**: Entrada en `presupuesto_integrity_audit` con `event_type = 'HASH_VIOLATION'`

---

## Respuesta Inmediata

### 1. Bloqueo Automático

El sistema automáticamente:

- ✅ **Bloquea la transacción**: Rollback automático
- ✅ **Registra en audit log**: Entrada en `presupuesto_integrity_audit`
- ✅ **Genera log crítico**: Log estructurado con contexto completo
- ✅ **Incrementa métrica**: `budget.integrity.violations.total`

### 2. Acciones Manuales Inmediatas

1. **Verificar Alertas**

   ```bash
   # Consultar métricas de Prometheus
   curl http://localhost:8080/actuator/prometheus | grep budget.integrity.violations
   ```

2. **Revisar Logs Críticos**

   ```bash
   # Buscar violaciones en logs
   grep "CRITICAL: Budget integrity violation" /var/log/budgetpro/application.log
   ```

3. **Consultar Audit Log**
   ```sql
   SELECT
       id,
       presupuesto_id,
       event_type,
       hash_approval,
       hash_execution,
       validation_result,
       violation_details,
       validated_at
   FROM presupuesto_integrity_audit
   WHERE event_type = 'HASH_VIOLATION'
   ORDER BY validated_at DESC
   LIMIT 10;
   ```

---

## Investigación

### Paso 1: Identificar el Presupuesto Afectado

```sql
-- Obtener último evento de violación
SELECT
    presupuesto_id,
    violation_details,
    validated_at,
    validated_by
FROM presupuesto_integrity_audit
WHERE event_type = 'HASH_VIOLATION'
ORDER BY validated_at DESC
LIMIT 1;
```

### Paso 2: Comparar Hashes

```sql
-- Obtener hash esperado vs actual
SELECT
    hash_approval as expected_hash,
    hash_execution as actual_hash,
    violation_details
FROM presupuesto_integrity_audit
WHERE presupuesto_id = ?
  AND event_type = 'HASH_VIOLATION'
ORDER BY validated_at DESC
LIMIT 1;
```

### Paso 3: Revisar Cambios Recientes en Base de Datos

```sql
-- Verificar cambios en Partidas
SELECT
    id,
    item,
    descripcion,
    metrado_vigente,
    presupuesto_asignado,
    updated_at
FROM partida
WHERE presupuesto_id = ?
ORDER BY updated_at DESC;

-- Verificar cambios en APU Snapshots
SELECT
    apu.id,
    apu.partida_id,
    apu.rendimiento_vigente,
    apu.updated_at
FROM apu_snapshot apu
JOIN partida p ON apu.partida_id = p.id
WHERE p.presupuesto_id = ?
ORDER BY apu.updated_at DESC;
```

### Paso 4: Revisar Logs Estructurados

Buscar en logs el correlation ID del evento:

```json
{
  "component": "integrity-hash-service",
  "action": "integrity_violation",
  "presupuesto_id": "...",
  "expected_hash": "0123456789abcdef...",
  "actual_hash": "fedcba9876543210...",
  "violation_type": "Tampering detected: Approval hash mismatch",
  "correlation_id": "..."
}
```

### Paso 5: Verificar Acceso No Autorizado

1. Revisar logs de autenticación
2. Verificar cambios realizados por usuarios
3. Revisar permisos de usuarios con acceso al presupuesto

---

## Métricas a Monitorear

### Métricas Críticas

1. **`budget.integrity.violations.total`**
   - **Descripción**: Contador de violaciones de integridad detectadas
   - **Valor esperado**: 0 (cero violaciones)
   - **Alert threshold**: > 0 (cualquier violación es crítica)
   - **Tags**: `violation_type`, `algorithm`

2. **`budget.integrity.hash_validated.total{status="failure"}`**
   - **Descripción**: Contador de validaciones fallidas
   - **Valor esperado**: 0
   - **Alert threshold**: > 0
   - **Tags**: `status`, `algorithm`

### Métricas de Performance

3. **`budget.integrity.hash_calculation.duration`**
   - **Descripción**: Duración de cálculo de hash (p50, p95, p99)
   - **Valor esperado**: < 100ms (p95)
   - **Alert threshold**: > 500ms (p95)
   - **Tags**: `operation`, `algorithm`

4. **`budget.integrity.validation.duration`**
   - **Descripción**: Duración de validación de hash (p50, p95, p99)
   - **Valor esperado**: < 100ms (p95)
   - **Alert threshold**: > 500ms (p95)
   - **Tags**: `status`, `algorithm`

### Métricas de Operación

5. **`budget.integrity.hash_generated.total`**
   - **Descripción**: Contador de hashes generados
   - **Uso**: Monitorear volumen de aprobaciones
   - **Tags**: `operation`, `algorithm`

6. **`budget.integrity.partidas_count`**
   - **Descripción**: Gauge con cantidad de partidas procesadas
   - **Uso**: Monitorear tamaño de presupuestos
   - **Tags**: `operation`, `algorithm`

### Consultas Prometheus

```promql
# Violaciones totales (últimas 24 horas)
sum(increase(budget_integrity_violations_total[24h]))

# Tasa de validaciones fallidas
rate(budget_integrity_hash_validated_total{status="failure"}[5m])

# Percentil 95 de duración de cálculo
histogram_quantile(0.95, budget_integrity_hash_calculation_duration_seconds_bucket)

# Violaciones por tipo
sum by (violation_type) (budget_integrity_violations_total)
```

---

## Procedimientos de Recuperación

### Escenario 1: Tampering Detectado

**Síntoma**: Hash no coincide, cambios detectados en base de datos

**Acciones:**

1. **Inmediato**: Bloquear todas las transacciones del presupuesto afectado
2. **Investigación**: Identificar qué cambió y quién lo cambió
3. **Restauración**:
   - Si es cambio no autorizado: Restaurar desde backup
   - Si es cambio legítimo: Crear nuevo presupuesto (no modificar aprobado)
4. **Documentación**: Registrar incidente en sistema de tickets

### Escenario 2: Bug en Cálculo de Hash

**Síntoma**: Hash no coincide, pero no hay cambios en base de datos

**Acciones:**

1. **Verificación**: Confirmar que no hay cambios en datos
2. **Debug**: Revisar logs para identificar causa
3. **Fix**: Corregir bug en cálculo de hash
4. **Regeneración**: Regenerar hashes para presupuestos afectados (si es necesario)

### Escenario 3: Corrupción de Datos

**Síntoma**: Hash no coincide, datos inconsistentes

**Acciones:**

1. **Análisis**: Identificar alcance de corrupción
2. **Backup**: Verificar integridad de backups
3. **Restauración**: Restaurar desde último backup válido
4. **Validación**: Revalidar integridad después de restauración

---

## Escalación

### Nivel 1: Violación Detectada

- **Acción**: Bloqueo automático de transacción
- **Notificación**: Log crítico + métrica
- **Responsable**: Sistema automático

### Nivel 2: Múltiples Violaciones

- **Condición**: > 3 violaciones en 1 hora
- **Acción**: Notificar al equipo de desarrollo
- **Responsable**: On-call engineer

### Nivel 3: Violación Masiva

- **Condición**: > 10 violaciones o afecta múltiples presupuestos
- **Acción**: Escalar a CTO/Security team
- **Responsable**: Engineering manager

### Nivel 4: Brecha de Seguridad

- **Condición**: Evidencia de acceso no autorizado
- **Acción**: Activar procedimiento de respuesta a incidentes de seguridad
- **Responsable**: Security team + Legal

---

## Checklist de Respuesta

- [ ] Violación detectada y bloqueada automáticamente
- [ ] Log crítico revisado con correlation ID
- [ ] Audit log consultado para contexto
- [ ] Métricas verificadas en Prometheus
- [ ] Cambios en base de datos identificados
- [ ] Causa raíz determinada
- [ ] Acción correctiva implementada
- [ ] Incidente documentado
- [ ] Equipo notificado (si aplica)
- [ ] Post-mortem programado (si es crítico)

---

## Referencias

- [Integrity Implementation Guide](./INTEGRITY_IMPLEMENTATION.md)
- [Arquitectura Visual - Diagramas de Integridad](../ARQUITECTURA_VISUAL.md#11-arquitectura-de-integridad-criptográfica)
- [Business Manifesto - Integridad Criptográfica](../context/BUSINESS_MANIFESTO.md#9-principio-de-integridad-criptográfica-swiss-grade)

---

## Historial de Ejecuciones de Sesión (Logs)

### Sesión 2026-01-31: Estabilización y Restauración Masiva

**Objetivo**: Recuperar proyecto de estado MODE_0 (refactor fallido de Estimación).
**Acciones Ejecutadas**:

1. ✅ **Restauración Bulk**: Reemplazo de 202 archivos desde baseline `rescue/post-audit-base`.
2. ✅ **Remediación de Validadores**: Fix en `SecurityValidator.py` para rutas relativas de `mvnw`.
3. ✅ **Limpieza de Código Perezoso**: Eliminación de bloqueos en `EstimacionMapper` (reemplazo de `return null` por excepciones).
4. ✅ **Validación Final**: `./mvnw clean compile` resultando en `BUILD SUCCESS`.

**Métricas Finales**:

- Bloqueos resueltos: 6 (Axiom security + Lazy code).
- Advertencias remanentes: 1 (.gitignore desactualizado).
- Estatus Final: **MODE_2 (Estabilizado)**.
