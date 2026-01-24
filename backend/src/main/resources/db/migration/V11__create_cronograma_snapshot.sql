-- Migration: Create cronograma_snapshot table
-- Description: Creates table to store immutable snapshots of frozen schedules (baseline)
-- Date: 2026-01-23

-- Create cronograma_snapshot table
CREATE TABLE IF NOT EXISTS cronograma_snapshot (
    snapshot_id UUID PRIMARY KEY,
    programa_obra_id UUID NOT NULL,
    presupuesto_id UUID NOT NULL,
    
    -- Snapshot data stored as JSONB
    fechas_snapshot JSONB NOT NULL,
    duraciones_snapshot JSONB NOT NULL,
    secuencia_snapshot JSONB NOT NULL,
    calendarios_snapshot JSONB NOT NULL,
    
    -- Snapshot metadata
    snapshot_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    snapshot_algorithm VARCHAR(50) NOT NULL,
    
    -- Foreign key constraints
    CONSTRAINT fk_cronograma_snapshot_programa_obra 
        FOREIGN KEY (programa_obra_id) 
        REFERENCES programa_obra(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_cronograma_snapshot_presupuesto 
        FOREIGN KEY (presupuesto_id) 
        REFERENCES presupuesto(id) 
        ON DELETE CASCADE,
    
    -- Constraints
    CONSTRAINT chk_snapshot_algorithm_not_empty 
        CHECK (snapshot_algorithm IS NOT NULL AND LENGTH(TRIM(snapshot_algorithm)) > 0)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_cronograma_snapshot_programa_obra 
    ON cronograma_snapshot(programa_obra_id);

CREATE INDEX IF NOT EXISTS idx_cronograma_snapshot_presupuesto 
    ON cronograma_snapshot(presupuesto_id);

CREATE INDEX IF NOT EXISTS idx_cronograma_snapshot_date 
    ON cronograma_snapshot(snapshot_date DESC);

-- Add comments for documentation
COMMENT ON TABLE cronograma_snapshot IS 'Snapshots inmutables del cronograma congelado (baseline temporal)';
COMMENT ON COLUMN cronograma_snapshot.snapshot_id IS 'ID único del snapshot';
COMMENT ON COLUMN cronograma_snapshot.programa_obra_id IS 'ID del programa de obra congelado';
COMMENT ON COLUMN cronograma_snapshot.presupuesto_id IS 'ID del presupuesto asociado';
COMMENT ON COLUMN cronograma_snapshot.fechas_snapshot IS 'Datos de fechas del cronograma en formato JSONB';
COMMENT ON COLUMN cronograma_snapshot.duraciones_snapshot IS 'Datos de duraciones del cronograma en formato JSONB';
COMMENT ON COLUMN cronograma_snapshot.secuencia_snapshot IS 'Datos de secuencia y dependencias en formato JSONB';
COMMENT ON COLUMN cronograma_snapshot.calendarios_snapshot IS 'Datos de calendarios y restricciones temporales en formato JSONB';
COMMENT ON COLUMN cronograma_snapshot.snapshot_date IS 'Fecha y hora en que se creó el snapshot';
COMMENT ON COLUMN cronograma_snapshot.snapshot_algorithm IS 'Versión del algoritmo usado para generar el snapshot';
