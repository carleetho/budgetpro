-- Migration: Add recepcion tables and update compra schema
-- Version: V21
-- Description: Adds new purchase order states, cantidad_recibida column, and recepcion tables

-- ============================================================================
-- 1. UPDATE COMPRA TABLE: Add CHECK constraint for new estado values
-- ============================================================================
-- Add CHECK constraint to compra.estado to include new states
-- Note: If constraint already exists, we drop and recreate it
DO $$
BEGIN
    -- Drop existing constraint if it exists
    ALTER TABLE compra DROP CONSTRAINT IF EXISTS ck_compra_estado;
    
    -- Add new constraint with all valid states
    ALTER TABLE compra 
        ADD CONSTRAINT ck_compra_estado 
        CHECK (estado IN ('BORRADOR', 'APROBADA', 'ENVIADA', 'PARCIAL', 'RECIBIDA'));
END $$;

-- Add index on estado if it doesn't exist (for filtering queries)
CREATE INDEX IF NOT EXISTS idx_compra_estado ON compra(estado);

-- ============================================================================
-- 2. UPDATE COMPRA_DETALLE: Add cantidad_recibida column
-- ============================================================================
-- Add cantidad_recibida column with default value and constraints
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'compra_detalle'
          AND column_name = 'cantidad_recibida'
    ) THEN
        ALTER TABLE compra_detalle 
            ADD COLUMN cantidad_recibida DECIMAL(19,4) NOT NULL DEFAULT 0.0000;
        
        -- Add CHECK constraint to ensure cantidad_recibida >= 0
        ALTER TABLE compra_detalle
            ADD CONSTRAINT chk_compra_detalle_cantidad_recibida_no_negativa
            CHECK (cantidad_recibida >= 0);
    END IF;
END $$;

COMMENT ON COLUMN compra_detalle.cantidad_recibida IS 
    'Cantidad recibida acumulada para este detalle de compra. Se actualiza al registrar recepciones.';

-- ============================================================================
-- 3. CREATE RECEPCION TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS recepcion (
    id UUID PRIMARY KEY,
    compra_id UUID NOT NULL REFERENCES compra(id),
    fecha_recepcion DATE NOT NULL,
    guia_remision VARCHAR(100) NOT NULL,
    creado_por_usuario_id UUID NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Unique constraint: one guia_remision per compra (idempotency)
    CONSTRAINT uq_recepcion_compra_guia UNIQUE (compra_id, guia_remision)
);

-- Indexes for recepcion table
CREATE INDEX IF NOT EXISTS idx_recepcion_compra ON recepcion(compra_id);
CREATE INDEX IF NOT EXISTS idx_recepcion_fecha ON recepcion(fecha_recepcion);
CREATE INDEX IF NOT EXISTS idx_recepcion_guia ON recepcion(guia_remision);

-- Comments
COMMENT ON TABLE recepcion IS 
    'Tabla de recepciones de órdenes de compra con cumplimiento legal (guía de remisión) y trazabilidad de auditoría (REGLA-167)';
COMMENT ON COLUMN recepcion.id IS 'ID único de la recepción';
COMMENT ON COLUMN recepcion.compra_id IS 'ID de la compra asociada';
COMMENT ON COLUMN recepcion.fecha_recepcion IS 'Fecha en que se recibió la mercancía';
COMMENT ON COLUMN recepcion.guia_remision IS 'Número de guía de remisión (requisito legal) - único por compra';
COMMENT ON COLUMN recepcion.creado_por_usuario_id IS 'ID del usuario que crea la recepción (REGLA-167)';
COMMENT ON COLUMN recepcion.fecha_creacion IS 'Fecha y hora de creación del registro (REGLA-167)';
COMMENT ON COLUMN recepcion.version IS 'Versión para optimistic locking';

-- ============================================================================
-- 4. CREATE RECEPCION_DETALLE TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS recepcion_detalle (
    id UUID PRIMARY KEY,
    recepcion_id UUID NOT NULL REFERENCES recepcion(id) ON DELETE CASCADE,
    compra_detalle_id UUID NOT NULL REFERENCES compra_detalle(id),
    recurso_id UUID NOT NULL,
    almacen_id UUID NOT NULL REFERENCES almacen(id),
    cantidad_recibida DECIMAL(19,4) NOT NULL,
    precio_unitario DECIMAL(19,4) NOT NULL,
    
    -- CHECK constraints for data integrity
    CONSTRAINT chk_recepcion_detalle_cantidad_positiva 
        CHECK (cantidad_recibida > 0),
    CONSTRAINT chk_recepcion_detalle_precio_no_negativo 
        CHECK (precio_unitario >= 0)
);

-- Indexes for recepcion_detalle table
CREATE INDEX IF NOT EXISTS idx_recepcion_detalle_recepcion_id 
    ON recepcion_detalle(recepcion_id);
CREATE INDEX IF NOT EXISTS idx_recepcion_detalle_compra_detalle_id 
    ON recepcion_detalle(compra_detalle_id);
CREATE INDEX IF NOT EXISTS idx_recepcion_detalle_almacen_id 
    ON recepcion_detalle(almacen_id);
CREATE INDEX IF NOT EXISTS idx_recepcion_detalle_recurso_id 
    ON recepcion_detalle(recurso_id);

-- Comments
COMMENT ON TABLE recepcion_detalle IS 
    'Detalles de recepción de productos asociados a una recepción, con soporte para distribución multi-almacén';
COMMENT ON COLUMN recepcion_detalle.id IS 'ID único del detalle de recepción';
COMMENT ON COLUMN recepcion_detalle.recepcion_id IS 'ID de la recepción asociada';
COMMENT ON COLUMN recepcion_detalle.compra_detalle_id IS 'ID del detalle de compra original';
COMMENT ON COLUMN recepcion_detalle.recurso_id IS 'ID del recurso recibido';
COMMENT ON COLUMN recepcion_detalle.almacen_id IS 'ID del almacén donde se recibe (soporte multi-almacén)';
COMMENT ON COLUMN recepcion_detalle.cantidad_recibida IS 'Cantidad recibida en esta recepción (debe ser > 0)';
COMMENT ON COLUMN recepcion_detalle.precio_unitario IS 'Precio unitario al momento de la recepción (debe ser >= 0)';
