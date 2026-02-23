-- Migration: Fix cantidad_recibida scale to match cantidad column
-- Version: V22
-- Description: Changes cantidad_recibida scale from 4 to 6 to match cantidad column precision
--              This prevents data loss from rounding when amounts with up to 6 decimal places are received.

-- ============================================================================
-- 1. UPDATE COMPRA_DETALLE: Change cantidad_recibida scale from 4 to 6
-- ============================================================================
-- Alter the column to change scale from 4 to 6
-- This requires recreating the column since PostgreSQL doesn't support direct scale changes
DO $$
BEGIN
    -- Check if column exists and has the wrong scale
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'compra_detalle'
          AND column_name = 'cantidad_recibida'
          AND numeric_scale = 4
    ) THEN
        -- Drop the CHECK constraint if it exists
        ALTER TABLE compra_detalle 
            DROP CONSTRAINT IF EXISTS chk_compra_detalle_cantidad_recibida_no_negativa;
        
        -- Alter the column to change scale from 4 to 6
        ALTER TABLE compra_detalle 
            ALTER COLUMN cantidad_recibida TYPE DECIMAL(19,6);
        
        -- Re-add the CHECK constraint
        ALTER TABLE compra_detalle
            ADD CONSTRAINT chk_compra_detalle_cantidad_recibida_no_negativa
            CHECK (cantidad_recibida >= 0);
    END IF;
END $$;

-- ============================================================================
-- 2. UPDATE RECEPCION_DETALLE: Change cantidad_recibida scale from 4 to 6
-- ============================================================================
-- Alter the column to change scale from 4 to 6
-- This ensures consistency since recepcion_detalle.cantidad_recibida represents
-- the same unit as compra_detalle.cantidad
DO $$
BEGIN
    -- Check if column exists and has the wrong scale
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'recepcion_detalle'
          AND column_name = 'cantidad_recibida'
          AND numeric_scale = 4
    ) THEN
        -- Drop the CHECK constraint if it exists
        ALTER TABLE recepcion_detalle 
            DROP CONSTRAINT IF EXISTS chk_recepcion_detalle_cantidad_positiva;
        
        -- Alter the column to change scale from 4 to 6
        ALTER TABLE recepcion_detalle 
            ALTER COLUMN cantidad_recibida TYPE DECIMAL(19,6);
        
        -- Re-add the CHECK constraint
        ALTER TABLE recepcion_detalle
            ADD CONSTRAINT chk_recepcion_detalle_cantidad_positiva
            CHECK (cantidad_recibida > 0);
    END IF;
END $$;

-- Update comments to reflect the correct precision
COMMENT ON COLUMN compra_detalle.cantidad_recibida IS 
    'Cantidad recibida acumulada para este detalle de compra. Se actualiza al registrar recepciones. Precision: DECIMAL(19,6) para coincidir con cantidad.';

COMMENT ON COLUMN recepcion_detalle.cantidad_recibida IS 
    'Cantidad recibida en esta recepción (debe ser > 0). Precision: DECIMAL(19,6) para coincidir con compra_detalle.cantidad.';
