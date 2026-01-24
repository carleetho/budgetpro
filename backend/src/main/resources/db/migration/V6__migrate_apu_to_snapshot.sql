-- Migración de datos: Convertir APU y APU_INSUMO existentes a formato Snapshot
-- 
-- Esta migración:
-- 1. Copia todos los APUs de la tabla 'apu' a 'apu_snapshot'
-- 2. Copia todos los insumos de 'apu_insumo' a 'apu_insumo_snapshot'
-- 3. Genera external_apu_id como 'LEGACY_APU_{uuid}' para APUs legacy
-- 4. Marca catalog_source como 'BUDGETPRO_LEGACY'
-- 5. Inicializa rendimiento_original y rendimiento_vigente con el mismo valor
-- 6. Marca rendimiento_modificado = false (ningún APU legacy ha sido modificado)
-- 7. Convierte recurso_id FK a recurso_external_id string
-- 8. Preserva todas las relaciones y datos
-- 9. Es idempotente (puede ejecutarse múltiples veces sin duplicar datos)

-- Paso 1: Migrar APUs existentes a apu_snapshot
INSERT INTO apu_snapshot (
    id,
    partida_id,
    external_apu_id,
    catalog_source,
    rendimiento_original,
    rendimiento_vigente,
    rendimiento_modificado,
    rendimiento_modificado_por,
    rendimiento_modificado_en,
    unidad_snapshot,
    snapshot_date,
    version,
    created_at,
    updated_at,
    created_by
)
SELECT 
    a.id,
    a.partida_id::uuid,
    CONCAT('LEGACY_APU_', a.id::text) AS external_apu_id,
    'BUDGETPRO_LEGACY' AS catalog_source,
    COALESCE(a.rendimiento, 1.0) AS rendimiento_original,
    COALESCE(a.rendimiento, 1.0) AS rendimiento_vigente,
    false AS rendimiento_modificado,
    NULL AS rendimiento_modificado_por,
    NULL AS rendimiento_modificado_en,
    COALESCE(a.unidad, 'UND') AS unidad_snapshot,
    COALESCE(a.created_at, CURRENT_TIMESTAMP) AS snapshot_date,
    COALESCE(a.version, 0)::BIGINT AS version,
    COALESCE(a.created_at, CURRENT_TIMESTAMP) AS created_at,
    COALESCE(a.updated_at, CURRENT_TIMESTAMP) AS updated_at,
    '00000000-0000-0000-0000-000000000000'::uuid AS created_by
FROM apu a
WHERE NOT EXISTS (
    SELECT 1 
    FROM apu_snapshot aps 
    WHERE aps.id = a.id
)
AND a.partida_id IS NOT NULL;

-- Paso 2: Migrar APU_INSUMO a apu_insumo_snapshot
-- Nota: Solo migramos insumos cuyo APU padre ya fue migrado
INSERT INTO apu_insumo_snapshot (
    id,
    apu_snapshot_id,
    recurso_external_id,
    recurso_nombre,
    cantidad,
    precio_unitario,
    subtotal,
    created_at,
    updated_at,
    created_by
)
SELECT 
    ai.id,
    ai.apu_id AS apu_snapshot_id,
    CONCAT('LEGACY_', ai.recurso_id::text) AS recurso_external_id,
    COALESCE(r.nombre, rp.nombre_snapshot, 'RECURSO DESCONOCIDO') AS recurso_nombre,
    ai.cantidad,
    ai.precio_unitario,
    ai.subtotal,
    COALESCE(ai.created_at, CURRENT_TIMESTAMP) AS created_at,
    COALESCE(ai.updated_at, CURRENT_TIMESTAMP) AS updated_at,
    '00000000-0000-0000-0000-000000000000'::uuid AS created_by
FROM apu_insumo ai
LEFT JOIN recurso r ON ai.recurso_id = r.id
LEFT JOIN recurso_proxy rp ON rp.id = ai.recurso_id
WHERE EXISTS (
    SELECT 1 
    FROM apu_snapshot aps 
    WHERE aps.id = ai.apu_id
)
AND NOT EXISTS (
    SELECT 1 
    FROM apu_insumo_snapshot ais 
    WHERE ais.id = ai.id
)
AND ai.cantidad IS NOT NULL
AND ai.precio_unitario IS NOT NULL
AND ai.subtotal IS NOT NULL;

-- Verificación de migración
DO $$
DECLARE
    apu_count INTEGER;
    snapshot_count INTEGER;
    insumo_count INTEGER;
    insumo_snapshot_count INTEGER;
    orphaned_insumos INTEGER;
    invalid_insumos INTEGER;
BEGIN
    -- Contar APUs originales
    SELECT COUNT(*) INTO apu_count FROM apu WHERE partida_id IS NOT NULL;
    
    -- Contar snapshots migrados
    SELECT COUNT(*) INTO snapshot_count 
    FROM apu_snapshot 
    WHERE catalog_source = 'BUDGETPRO_LEGACY';
    
    -- Contar insumos originales
    SELECT COUNT(*) INTO insumo_count 
    FROM apu_insumo ai
    WHERE EXISTS (SELECT 1 FROM apu_snapshot aps WHERE aps.id = ai.apu_id);
    
    -- Contar insumos snapshots migrados
    SELECT COUNT(*) INTO insumo_snapshot_count 
    FROM apu_insumo_snapshot ais
    WHERE EXISTS (
        SELECT 1 FROM apu_snapshot aps 
        WHERE aps.id = ais.apu_snapshot_id 
        AND aps.catalog_source = 'BUDGETPRO_LEGACY'
    );
    
    -- Contar insumos huérfanos (APU no migrado)
    SELECT COUNT(*) INTO orphaned_insumos
    FROM apu_insumo ai
    WHERE NOT EXISTS (
        SELECT 1 FROM apu_snapshot aps WHERE aps.id = ai.apu_id
    );
    
    -- Contar insumos con datos inválidos
    SELECT COUNT(*) INTO invalid_insumos
    FROM apu_insumo
    WHERE cantidad IS NULL OR precio_unitario IS NULL OR subtotal IS NULL;
    
    -- Log de resultados
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Resultados de migración APU:';
    RAISE NOTICE '  - APUs totales válidos: %', apu_count;
    RAISE NOTICE '  - APU snapshots migrados: %', snapshot_count;
    RAISE NOTICE '  - Insumos totales válidos: %', insumo_count;
    RAISE NOTICE '  - Insumo snapshots migrados: %', insumo_snapshot_count;
    RAISE NOTICE '  - Insumos huérfanos (APU no migrado): %', orphaned_insumos;
    RAISE NOTICE '  - Insumos con datos inválidos: %', invalid_insumos;
    RAISE NOTICE '========================================';
    
    -- Validación: verificar que todos los APUs válidos fueron migrados
    IF apu_count != snapshot_count THEN
        RAISE WARNING 'Advertencia: Algunos APUs no fueron migrados. Verificar partida_id NULL.';
    ELSE
        RAISE NOTICE 'Migración APU exitosa: Todos los APUs válidos fueron migrados.';
    END IF;
    
    -- Validación: verificar que todos los insumos válidos fueron migrados
    IF insumo_count != insumo_snapshot_count THEN
        RAISE WARNING 'Advertencia: Algunos insumos no fueron migrados. Verificar datos NULL o APU no migrado.';
    ELSE
        RAISE NOTICE 'Migración insumos exitosa: Todos los insumos válidos fueron migrados.';
    END IF;
    
    -- Advertencia sobre insumos huérfanos
    IF orphaned_insumos > 0 THEN
        RAISE WARNING 'Advertencia: % insumos no fueron migrados porque su APU padre no existe o no fue migrado.', orphaned_insumos;
    END IF;
END $$;

-- Verificación de integridad referencial
DO $$
DECLARE
    broken_relationships INTEGER;
BEGIN
    -- Verificar que todos los insumos tienen un APU snapshot válido
    SELECT COUNT(*) INTO broken_relationships
    FROM apu_insumo_snapshot ais
    WHERE NOT EXISTS (
        SELECT 1 FROM apu_snapshot aps WHERE aps.id = ais.apu_snapshot_id
    );
    
    IF broken_relationships > 0 THEN
        RAISE EXCEPTION 'Error de integridad referencial: % insumos sin APU snapshot válido', broken_relationships;
    ELSE
        RAISE NOTICE 'Integridad referencial verificada: Todos los insumos tienen APU snapshot válido.';
    END IF;
END $$;

-- Verificación de rendimiento
DO $$
DECLARE
    rendimiento_mismatch INTEGER;
BEGIN
    -- Verificar que rendimiento_original = rendimiento_vigente para todos los legacy
    SELECT COUNT(*) INTO rendimiento_mismatch
    FROM apu_snapshot
    WHERE catalog_source = 'BUDGETPRO_LEGACY'
    AND rendimiento_original != rendimiento_vigente;
    
    IF rendimiento_mismatch > 0 THEN
        RAISE WARNING 'Advertencia: % APUs legacy tienen rendimiento_original != rendimiento_vigente', rendimiento_mismatch;
    ELSE
        RAISE NOTICE 'Rendimiento verificado: Todos los APUs legacy tienen rendimiento_original = rendimiento_vigente.';
    END IF;
END $$;

-- Crear índices adicionales para búsquedas eficientes
CREATE INDEX IF NOT EXISTS idx_apu_snapshot_legacy 
    ON apu_snapshot(catalog_source) 
    WHERE catalog_source = 'BUDGETPRO_LEGACY';

CREATE INDEX IF NOT EXISTS idx_apu_insumo_snapshot_legacy_recurso 
    ON apu_insumo_snapshot(recurso_external_id) 
    WHERE recurso_external_id LIKE 'LEGACY_%';
