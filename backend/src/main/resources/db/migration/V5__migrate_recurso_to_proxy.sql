-- Migración de datos: Convertir recursos existentes a formato RecursoProxy
-- 
-- Esta migración:
-- 1. Copia todos los recursos de la tabla 'recurso' a 'recurso_proxy'
-- 2. Genera external_id como 'LEGACY_{uuid}' para recursos legacy
-- 3. Marca catalog_source como 'BUDGETPRO_LEGACY'
-- 4. Marca estado como 'OBSOLETO' (recursos legacy ya no se usan activamente)
-- 5. Preserva timestamps y created_by
-- 6. Es idempotente (puede ejecutarse múltiples veces sin duplicar datos)

-- Migrar recursos existentes a recurso_proxy
INSERT INTO recurso_proxy (
    id,
    external_id,
    catalog_source,
    nombre_snapshot,
    tipo_snapshot,
    unidad_snapshot,
    precio_snapshot,
    snapshot_date,
    estado,
    version,
    created_at,
    updated_at,
    created_by
)
SELECT 
    r.id,
    CONCAT('LEGACY_', r.id::text) AS external_id,
    'BUDGETPRO_LEGACY' AS catalog_source,
    r.nombre AS nombre_snapshot,
    r.tipo::text AS tipo_snapshot,
    COALESCE(r.unidad_base, r.unidad, 'UND') AS unidad_snapshot,
    COALESCE(r.costo_referencia, 0.00) AS precio_snapshot,
    COALESCE(r.created_at, CURRENT_TIMESTAMP) AS snapshot_date,
    'OBSOLETO' AS estado,
    0 AS version,
    COALESCE(r.created_at, CURRENT_TIMESTAMP) AS created_at,
    COALESCE(r.updated_at, CURRENT_TIMESTAMP) AS updated_at,
    COALESCE(r.created_by, '00000000-0000-0000-0000-000000000000'::uuid) AS created_by
FROM recurso r
WHERE NOT EXISTS (
    SELECT 1 
    FROM recurso_proxy rp 
    WHERE rp.id = r.id
)
AND r.nombre IS NOT NULL
AND r.tipo IS NOT NULL;

-- Verificación de migración
DO $$
DECLARE
    recurso_count INTEGER;
    proxy_count INTEGER;
    null_nombre_count INTEGER;
    null_tipo_count INTEGER;
BEGIN
    -- Contar recursos originales
    SELECT COUNT(*) INTO recurso_count FROM recurso;
    
    -- Contar proxies migrados
    SELECT COUNT(*) INTO proxy_count 
    FROM recurso_proxy 
    WHERE catalog_source = 'BUDGETPRO_LEGACY';
    
    -- Contar recursos con datos inválidos (no migrados)
    SELECT COUNT(*) INTO null_nombre_count 
    FROM recurso 
    WHERE nombre IS NULL;
    
    SELECT COUNT(*) INTO null_tipo_count 
    FROM recurso 
    WHERE tipo IS NULL;
    
    -- Log de resultados
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Resultados de migración:';
    RAISE NOTICE '  - Recursos totales: %', recurso_count;
    RAISE NOTICE '  - Proxies migrados: %', proxy_count;
    RAISE NOTICE '  - Recursos con nombre NULL: %', null_nombre_count;
    RAISE NOTICE '  - Recursos con tipo NULL: %', null_tipo_count;
    RAISE NOTICE '========================================';
    
    -- Validación: verificar que todos los recursos válidos fueron migrados
    IF recurso_count - null_nombre_count - null_tipo_count != proxy_count THEN
        RAISE WARNING 'Advertencia: Algunos recursos no fueron migrados. Verificar datos NULL.';
    ELSE
        RAISE NOTICE 'Migración exitosa: Todos los recursos válidos fueron migrados.';
    END IF;
END $$;

-- Crear índice adicional para búsquedas por catalog_source legacy
CREATE INDEX IF NOT EXISTS idx_recurso_proxy_legacy 
    ON recurso_proxy(catalog_source) 
    WHERE catalog_source = 'BUDGETPRO_LEGACY';
