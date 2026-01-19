# Guía de Migración V5: Recurso a RecursoProxy

## Resumen

Esta migración convierte todos los registros existentes de la tabla `recurso` al nuevo formato `recurso_proxy`, preservando todos los datos y marcando los recursos legacy apropiadamente.

## Objetivo

Migrar recursos existentes del sistema legacy a la nueva arquitectura de proxies, permitiendo:
- Integración con catálogos externos (CAPECO, SENCICO)
- Preservación de datos históricos
- Identificación clara de recursos legacy vs. externos

## Cambios Realizados

### Tabla `recurso_proxy`

Se insertan nuevos registros con:
- **id**: Mismo UUID del recurso original
- **external_id**: `LEGACY_{uuid}` (formato: `LEGACY_550e8400-e29b-41d4-a716-446655440000`)
- **catalog_source**: `BUDGETPRO_LEGACY`
- **nombre_snapshot**: Copia de `recurso.nombre`
- **tipo_snapshot**: Copia de `recurso.tipo` (convertido a texto)
- **unidad_snapshot**: `recurso.unidad_base` o `recurso.unidad` o `'UND'` por defecto
- **precio_snapshot**: `recurso.costo_referencia` o `0.00` si es NULL
- **snapshot_date**: `recurso.created_at` o `CURRENT_TIMESTAMP`
- **estado**: `OBSOLETO` (recursos legacy ya no se usan activamente)
- **version**: `0`
- **created_at, updated_at, created_by**: Preservados del recurso original

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
\i backend/src/main/resources/db/migration/V5__migrate_recurso_to_proxy.sql
```

## Verificación

Después de ejecutar la migración, verifica los resultados:

```sql
-- Contar recursos originales
SELECT COUNT(*) FROM recurso;

-- Contar proxies migrados
SELECT COUNT(*) FROM recurso_proxy WHERE catalog_source = 'BUDGETPRO_LEGACY';

-- Verificar que coinciden (excluyendo NULLs)
SELECT 
    (SELECT COUNT(*) FROM recurso WHERE nombre IS NOT NULL AND tipo IS NOT NULL) as recursos_validos,
    (SELECT COUNT(*) FROM recurso_proxy WHERE catalog_source = 'BUDGETPRO_LEGACY') as proxies_migrados;
```

## Características

### Idempotencia

La migración es **idempotente**: puede ejecutarse múltiples veces sin duplicar datos.

El script usa `NOT EXISTS` para verificar que un recurso no haya sido migrado previamente:

```sql
WHERE NOT EXISTS (
    SELECT 1 FROM recurso_proxy rp WHERE rp.id = recurso.id
)
```

### Manejo de NULLs

- Recursos con `nombre IS NULL` o `tipo IS NULL` **NO se migran** (filtrados en WHERE)
- `unidad_snapshot` usa `COALESCE` para manejar valores NULL
- `precio_snapshot` usa `COALESCE` con `0.00` como valor por defecto
- `created_by` usa UUID por defecto si es NULL

### Preservación de Datos

- Todos los timestamps se preservan (`created_at`, `updated_at`)
- El `created_by` se preserva del recurso original
- El ID se mantiene igual (mismo UUID)

## Rollback

Si necesitas revertir la migración, usa el script de rollback:

```sql
\i docs/migration/V5_ROLLBACK_migrate_recurso_to_proxy.sql
```

**ADVERTENCIA**: El rollback elimina los proxies legacy migrados, pero NO afecta la tabla `recurso` original.

## Impacto

### Tablas Afectadas

- ✅ `recurso_proxy`: Nuevos registros insertados
- ❌ `recurso`: **NO se modifica** (solo lectura)

### Aplicación

- Los recursos legacy ahora están disponibles como `RecursoProxy` con `catalog_source = 'BUDGETPRO_LEGACY'`
- El estado `OBSOLETO` indica que son recursos legacy
- El `external_id` permite identificarlos fácilmente: `LEGACY_{uuid}`

## Rendimiento

- **Tiempo estimado**: < 5 minutos para 10,000 registros
- **Índices**: Se crea índice adicional `idx_recurso_proxy_legacy` para búsquedas eficientes
- **Transacción**: La migración se ejecuta en una sola transacción

## Troubleshooting

### Error: "duplicate key value violates unique constraint"

**Causa**: El recurso ya fue migrado previamente.

**Solución**: La migración es idempotente, pero si hay un error de constraint, verifica:

```sql
SELECT r.id, rp.id 
FROM recurso r 
LEFT JOIN recurso_proxy rp ON r.id = rp.id 
WHERE rp.id IS NULL;
```

### Advertencia: "Algunos recursos no fueron migrados"

**Causa**: Recursos con `nombre IS NULL` o `tipo IS NULL`.

**Solución**: Revisa y corrige los datos:

```sql
SELECT id, nombre, tipo 
FROM recurso 
WHERE nombre IS NULL OR tipo IS NULL;
```

## Próximos Pasos

Después de esta migración:

1. ✅ Recursos migrados a proxies
2. ⏳ Migrar APUs (tarea separada)
3. ⏳ Actualizar referencias en CompraDetalle (ya completado en V4)
4. ⏳ Deprecar tabla `recurso` (futuro)

## Referencias

- Script de migración: `V5__migrate_recurso_to_proxy.sql`
- Script de rollback: `docs/migration/V5_ROLLBACK_migrate_recurso_to_proxy.sql`
- Tests: `RecursoMigrationTest.java`
