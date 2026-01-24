-- Migration: Add unidad column to compra_detalle
-- Version: V8
-- Description: Authority by PO - unit in which purchase arrives, for unit change detection vs catalog.

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'compra_detalle'
          AND column_name = 'unidad'
    ) THEN
        ALTER TABLE compra_detalle ADD COLUMN unidad VARCHAR(20);
    END IF;
END $$;

COMMENT ON COLUMN compra_detalle.unidad IS 
    'Unidad en que llega la compra (Authority by PO). Null = usar unidad del catálogo. Para detección de cambio de unidad.';
