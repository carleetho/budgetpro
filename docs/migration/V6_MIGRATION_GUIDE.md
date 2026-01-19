# Guía de Migración V6: APU e Insumos a Snapshot

## Resumen

Esta migración convierte todos los registros existentes de las tablas `apu` y `apu_insumo` al nuevo formato snapshot, preservando todos los datos, relaciones y cálculos financieros.

## Objetivo

Migrar APUs e insumos existentes del sistema legacy a la nueva arquitectura de snapshots, permitiendo:
- Integración con catálogos externos de APUs
- Tracking de modificaciones de rendimiento con auditoría completa
- Preservación de datos históricos y relaciones
- Conversión de referencias de recursos a external_id

## Cambios Realizados

### Tabla `apu_snapshot`

Se insertan nuevos registros con:
- **id**: Mismo UUID del APU original
- **partida_id**: Preservado del APU original (FK a partida)
- **external_apu_id**: `LEGACY_APU_{uuid}` (formato: `LEGACY_APU_550e8400-e29b-41d4-a716-446655440000`)
- **catalog_source**: `BUDGETPRO_LEGACY`
- **rendimiento_original**: Copia de `apu.rendimiento` o `1.0` si es NULL
- **rendimiento_vigente**: Mismo valor que `rendimiento_original` (inicialmente igual)
- **rendimiento_modificado**: `false` (ningún APU legacy ha sido modificado)
- **rendimiento_modificado_por**: `NULL`
- **rendimiento_modificado_en**: `NULL`
- **unidad_snapshot**: Copia de `apu.unidad` o `'UND'` por defecto
- **snapshot_date**: `apu.created_at` o `CURRENT_TIMESTAMP`
- **version**: Convertido de `Integer` a `BIGINT`
- **created_at, updated_at, created_by**: Preservados o valores por defecto

### Tabla `apu_insumo_snapshot`

Se insertan nuevos registros con:
- **id**: Mismo UUID del insumo original
- **apu_snapshot_id**: FK al APU snapshot migrado
- **recurso_external_id**: `LEGACY_{recurso_uuid}` (convertido desde FK)
- **recurso_nombre**: Obtenido desde `recurso.nombre` o `recurso_proxy.nombre_snapshot` o `'RECURSO DESCONOCIDO'`
- **cantidad, precio_unitario, subtotal**: Preservados del insumo original
- **created_at, updated_at, created_by**: Preservados o valores por defecto

## Ejecución

### Automática (Flyway)

La migración se ejecuta automáticamente al iniciar la aplicación si Flyway está habilitado:

```bash
./mvnw spring-boot:run
```

### Manual

Si necesitas ejecutar la migración manualmente:

```sql
-- Conectar a la base de datos
psql -U budgetpro -d budgetpro_db

-- Ejecutar el script
\i backend/src/main/resources/db/migration/V6__migrate_apu_to_snapshot.sql
```

## Verificación

Después de ejecutar la migración, verifica los resultados:

```sql
-- Contar APUs originales
SELECT COUNT(*) FROM apu WHERE partida_id IS NOT NULL;

-- Contar APU snapshots migrados
SELECT COUNT(*) FROM apu_snapshot WHERE catalog_source = 'BUDGETPRO_LEGACY';

-- Contar insumos originales (solo de APUs migrados)
SELECT COUNT(*) FROM apu_insumo ai
WHERE EXISTS (SELECT 1 FROM apu_snapshot aps WHERE aps.id = ai.apu_id);

-- Contar insumos snapshots migrados
SELECT COUNT(*) FROM apu_insumo_snapshot ais
WHERE EXISTS (
    SELECT 1 FROM apu_snapshot aps 
    WHERE aps.id = ais.apu_snapshot_id 
    AND aps.catalog_source = 'BUDGETPRO_LEGACY'
);

-- Verificar integridad referencial
SELECT COUNT(*) FROM apu_insumo_snapshot ais
WHERE NOT EXISTS (
    SELECT 1 FROM apu_snapshot aps WHERE aps.id = ais.apu_snapshot_id
);
-- Debe retornar 0

-- Verificar rendimiento
SELECT COUNT(*) FROM apu_snapshot
WHERE catalog_source = 'BUDGETPRO_LEGACY'
AND rendimiento_original != rendimiento_vigente;
-- Debe retornar 0
```

## Características

### Idempotencia

La migración es **idempotente**: puede ejecutarse múltiples veces sin duplicar datos.

El script usa `NOT EXISTS` para verificar que un APU o insumo no haya sido migrado previamente:

```sql
WHERE NOT EXISTS (
    SELECT 1 FROM apu_snapshot aps WHERE aps.id = a.id
)
```

### Orden de Migración

1. **Primero**: Se migran los APUs (padres)
2. **Segundo**: Se migran los insumos (hijos), solo si su APU padre fue migrado

Esto garantiza la integridad referencial.

### Manejo de NULLs

- APUs con `partida_id IS NULL` **NO se migran** (filtrados en WHERE)
- Insumos con `cantidad IS NULL`, `precio_unitario IS NULL` o `subtotal IS NULL` **NO se migran**
- `rendimiento` usa `COALESCE` con `1.0` como valor por defecto
- `unidad` usa `COALESCE` con `'UND'` como valor por defecto
- `recurso_nombre` intenta obtener desde `recurso` o `recurso_proxy`, o usa `'RECURSO DESCONOCIDO'`

### Preservación de Datos

- Todos los timestamps se preservan (`created_at`, `updated_at`)
- El `created_by` se establece con UUID por defecto si no existe
- El ID se mantiene igual (mismo UUID)
- Las relaciones padre-hijo se preservan completamente

### Rendimiento

- **rendimiento_original** y **rendimiento_vigente** se inicializan con el mismo valor
- **rendimiento_modificado** = `false` para todos los APUs legacy
- Esto permite tracking futuro de modificaciones

## Rollback

Si necesitas revertir la migración, usa el script de rollback:

```sql
\i docs/migration/V6_ROLLBACK_migrate_apu_to_snapshot.sql
```

**ADVERTENCIA**: El rollback elimina los snapshots legacy migrados, pero NO afecta las tablas `apu` y `apu_insumo` originales.

**IMPORTANTE**: El rollback elimina primero los insumos (hijos) y luego los APUs (padres) para respetar las constraints de FK.

## Impacto

### Tablas Afectadas

- ✅ `apu_snapshot`: Nuevos registros insertados
- ✅ `apu_insumo_snapshot`: Nuevos registros insertados
- ❌ `apu`: **NO se modifica** (solo lectura)
- ❌ `apu_insumo`: **NO se modifica** (solo lectura)

### Aplicación

- Los APUs legacy ahora están disponibles como `APUSnapshot` con `catalog_source = 'BUDGETPRO_LEGACY'`
- El rendimiento está inicializado correctamente (original = vigente)
- Los insumos mantienen sus relaciones con los APUs
- Las referencias de recursos se convierten a `external_id` string

## Rendimiento

- **Tiempo estimado**: < 10 minutos para 5,000 APUs con 50,000 insumos
- **Índices**: Se crean índices adicionales para búsquedas eficientes
- **Transacción**: La migración se ejecuta en una sola transacción

## Troubleshooting

### Error: "duplicate key value violates unique constraint"

**Causa**: El APU o insumo ya fue migrado previamente.

**Solución**: La migración es idempotente, pero si hay un error de constraint, verifica:

```sql
-- APUs no migrados
SELECT a.id 
FROM apu a 
LEFT JOIN apu_snapshot aps ON a.id = aps.id 
WHERE aps.id IS NULL AND a.partida_id IS NOT NULL;

-- Insumos no migrados
SELECT ai.id 
FROM apu_insumo ai 
LEFT JOIN apu_insumo_snapshot ais ON ai.id = ais.id 
WHERE ais.id IS NULL 
AND EXISTS (SELECT 1 FROM apu_snapshot aps WHERE aps.id = ai.apu_id);
```

### Advertencia: "Algunos APUs no fueron migrados"

**Causa**: APUs con `partida_id IS NULL`.

**Solución**: Revisa y corrige los datos:

```sql
SELECT id, partida_id 
FROM apu 
WHERE partida_id IS NULL;
```

### Advertencia: "Algunos insumos no fueron migrados"

**Causa**: Insumos con datos NULL o cuyo APU padre no fue migrado.

**Solución**: Revisa los datos:

```sql
-- Insumos con datos NULL
SELECT id, cantidad, precio_unitario, subtotal 
FROM apu_insumo 
WHERE cantidad IS NULL OR precio_unitario IS NULL OR subtotal IS NULL;

-- Insumos huérfanos (APU no migrado)
SELECT ai.id, ai.apu_id
FROM apu_insumo ai
WHERE NOT EXISTS (
    SELECT 1 FROM apu_snapshot aps WHERE aps.id = ai.apu_id
);
```

### Error: "Error de integridad referencial"

**Causa**: Insumos sin APU snapshot válido.

**Solución**: Esto no debería ocurrir si la migración se ejecuta correctamente. Si ocurre, verifica:

```sql
SELECT ais.id, ais.apu_snapshot_id
FROM apu_insumo_snapshot ais
WHERE NOT EXISTS (
    SELECT 1 FROM apu_snapshot aps WHERE aps.id = ais.apu_snapshot_id
);
```

## Próximos Pasos

Después de esta migración:

1. ✅ APUs migrados a snapshots
2. ✅ Insumos migrados a snapshots
3. ✅ Referencias de recursos convertidas a external_id
4. ⏳ Deprecar tablas `apu` y `apu_insumo` (futuro)

## Referencias

- Script de migración: `V6__migrate_apu_to_snapshot.sql`
- Script de rollback: `docs/migration/V6_ROLLBACK_migrate_apu_to_snapshot.sql`
- Tests: `ApuMigrationTest.java`
