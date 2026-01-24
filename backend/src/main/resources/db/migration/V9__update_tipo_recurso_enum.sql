-- Migración V9: Actualizar TipoRecurso enum y migrar datos legacy
-- 
-- Esta migración:
-- 1. Actualiza registros existentes con EQUIPO a EQUIPO_MAQUINA
-- 2. Permite usar los nuevos valores EQUIPO_MAQUINA y EQUIPO_HERRAMIENTA

-- Migrar registros existentes: EQUIPO -> EQUIPO_MAQUINA
-- (aplicar a todas las tablas que usan tipo_recurso)

-- Tabla recurso_proxy
UPDATE recurso_proxy
SET tipo_snapshot = 'EQUIPO_MAQUINA'
WHERE tipo_snapshot = 'EQUIPO';

-- Tabla recurso (si existe y tiene tipo_recurso)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'recurso' AND column_name = 'tipo_recurso') THEN
        UPDATE recurso
        SET tipo_recurso = 'EQUIPO_MAQUINA'
        WHERE tipo_recurso = 'EQUIPO';
    END IF;
END $$;

-- Tabla apu_insumo_snapshot (nueva columna agregada en V7)
UPDATE apu_insumo_snapshot
SET tipo_recurso = 'EQUIPO_MAQUINA'
WHERE tipo_recurso = 'EQUIPO';

-- Verificación de migración
DO $$
DECLARE
    recurso_proxy_count INTEGER;
    apu_insumo_count INTEGER;
    recurso_count INTEGER;
BEGIN
    -- Contar registros migrados en recurso_proxy
    SELECT COUNT(*) INTO recurso_proxy_count
    FROM recurso_proxy
    WHERE tipo_snapshot = 'EQUIPO_MAQUINA';
    
    -- Contar registros migrados en apu_insumo_snapshot
    SELECT COUNT(*) INTO apu_insumo_count
    FROM apu_insumo_snapshot
    WHERE tipo_recurso = 'EQUIPO_MAQUINA';
    
    -- Contar registros legacy que aún usan EQUIPO (no debería haber ninguno)
    SELECT COUNT(*) INTO recurso_count
    FROM recurso_proxy
    WHERE tipo_snapshot = 'EQUIPO';
    
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Resultados de migración TipoRecurso:';
    RAISE NOTICE '  - RecursoProxy migrados a EQUIPO_MAQUINA: %', recurso_proxy_count;
    RAISE NOTICE '  - APUInsumoSnapshot migrados a EQUIPO_MAQUINA: %', apu_insumo_count;
    RAISE NOTICE '  - Registros legacy con EQUIPO (debería ser 0): %', recurso_count;
    RAISE NOTICE '========================================';
    
    IF recurso_count > 0 THEN
        RAISE WARNING 'Advertencia: % registros aún usan EQUIPO. Verificar migración.', recurso_count;
    ELSE
        RAISE NOTICE 'Migración exitosa: Todos los registros EQUIPO fueron migrados a EQUIPO_MAQUINA.';
    END IF;
END $$;

-- Comentario sobre backward compatibility
COMMENT ON COLUMN recurso_proxy.tipo_snapshot IS 'Tipo de recurso: MATERIAL, MANO_OBRA, EQUIPO_MAQUINA, EQUIPO_HERRAMIENTA, SUBCONTRATO. EQUIPO está deprecated pero se acepta para backward compatibility.';
COMMENT ON COLUMN apu_insumo_snapshot.tipo_recurso IS 'Tipo de recurso: MATERIAL, MANO_OBRA, EQUIPO_MAQUINA, EQUIPO_HERRAMIENTA, SUBCONTRATO. EQUIPO está deprecated pero se acepta para backward compatibility.';
