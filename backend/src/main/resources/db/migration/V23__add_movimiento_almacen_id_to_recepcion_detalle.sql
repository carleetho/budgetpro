-- Migration: Add movimiento_almacen_id to recepcion_detalle
-- Version: V23
-- Description: Adds movimiento_almacen_id column to recepcion_detalle table to track
--              the MovimientoAlmacen created for each reception detail. This allows
--              the API to return the movimientoAlmacenId in the response.

-- ============================================================================
-- 1. ADD COLUMN movimiento_almacen_id TO recepcion_detalle
-- ============================================================================
ALTER TABLE recepcion_detalle 
ADD COLUMN movimiento_almacen_id UUID;

-- ============================================================================
-- 2. ADD FOREIGN KEY CONSTRAINT
-- ============================================================================
ALTER TABLE recepcion_detalle
ADD CONSTRAINT fk_recepcion_detalle_movimiento_almacen
FOREIGN KEY (movimiento_almacen_id) 
REFERENCES movimiento_almacen(id)
ON DELETE SET NULL;

-- ============================================================================
-- 3. ADD INDEX FOR PERFORMANCE
-- ============================================================================
CREATE INDEX idx_recepcion_detalle_movimiento_almacen_id 
ON recepcion_detalle(movimiento_almacen_id);

-- ============================================================================
-- 4. ADD COMMENT
-- ============================================================================
COMMENT ON COLUMN recepcion_detalle.movimiento_almacen_id IS 
    'ID del movimiento de almacén creado para este detalle de recepción. Referencia a movimiento_almacen(id).';
