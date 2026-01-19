-- Script de Rollback para V6__migrate_apu_to_snapshot.sql
-- 
-- ADVERTENCIA: Este script elimina los snapshots legacy migrados.
-- Solo ejecutar si es necesario revertir la migración.
-- 
-- Este script:
-- 1. Elimina los insumos snapshots con catalog_source legacy
-- 2. Elimina los APU snapshots con catalog_source = 'BUDGETPRO_LEGACY'
-- 3. Elimina los índices adicionales creados
-- 
-- NOTA: Los datos originales en las tablas 'apu' y 'apu_insumo' NO se eliminan,
-- solo se eliminan los snapshots migrados.

-- Eliminar índices adicionales
DROP INDEX IF EXISTS idx_apu_snapshot_legacy;
DROP INDEX IF EXISTS idx_apu_insumo_snapshot_legacy_recurso;

-- Eliminar insumos snapshots legacy (debe hacerse primero por FK constraint)
DELETE FROM apu_insumo_snapshot 
WHERE EXISTS (
    SELECT 1 FROM apu_snapshot aps 
    WHERE aps.id = apu_insumo_snapshot.apu_snapshot_id 
    AND aps.catalog_source = 'BUDGETPRO_LEGACY'
);

-- Eliminar APU snapshots legacy
DELETE FROM apu_snapshot 
WHERE catalog_source = 'BUDGETPRO_LEGACY';

-- Verificación
DO $$
DECLARE
    remaining_apu_count INTEGER;
    remaining_insumo_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO remaining_apu_count 
    FROM apu_snapshot 
    WHERE catalog_source = 'BUDGETPRO_LEGACY';
    
    SELECT COUNT(*) INTO remaining_insumo_count
    FROM apu_insumo_snapshot ais
    WHERE EXISTS (
        SELECT 1 FROM apu_snapshot aps 
        WHERE aps.id = ais.apu_snapshot_id 
        AND aps.catalog_source = 'BUDGETPRO_LEGACY'
    );
    
    IF remaining_apu_count > 0 THEN
        RAISE WARNING 'Aún quedan % APU snapshots legacy después del rollback', remaining_apu_count;
    ELSE
        RAISE NOTICE 'Rollback APU completado: Todos los APU snapshots legacy fueron eliminados.';
    END IF;
    
    IF remaining_insumo_count > 0 THEN
        RAISE WARNING 'Aún quedan % insumo snapshots legacy después del rollback', remaining_insumo_count;
    ELSE
        RAISE NOTICE 'Rollback insumos completado: Todos los insumo snapshots legacy fueron eliminados.';
    END IF;
END $$;
