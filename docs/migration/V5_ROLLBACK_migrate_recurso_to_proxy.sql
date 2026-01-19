-- Script de Rollback para V5__migrate_recurso_to_proxy.sql
-- 
-- ADVERTENCIA: Este script elimina los proxies legacy migrados.
-- Solo ejecutar si es necesario revertir la migración.
-- 
-- Este script:
-- 1. Elimina los proxies con catalog_source = 'BUDGETPRO_LEGACY'
-- 2. Elimina el índice adicional creado
-- 
-- NOTA: Los datos originales en la tabla 'recurso' NO se eliminan,
-- solo se eliminan los proxies migrados.

-- Eliminar índice adicional
DROP INDEX IF EXISTS idx_recurso_proxy_legacy;

-- Eliminar proxies legacy migrados
DELETE FROM recurso_proxy 
WHERE catalog_source = 'BUDGETPRO_LEGACY';

-- Verificación
DO $$
DECLARE
    remaining_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO remaining_count 
    FROM recurso_proxy 
    WHERE catalog_source = 'BUDGETPRO_LEGACY';
    
    IF remaining_count > 0 THEN
        RAISE WARNING 'Aún quedan % proxies legacy después del rollback', remaining_count;
    ELSE
        RAISE NOTICE 'Rollback completado: Todos los proxies legacy fueron eliminados.';
    END IF;
END $$;
