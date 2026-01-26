-- Migration: Add freeze fields to programa_obra table
-- Description: Adds columns to support schedule freeze mechanism (baseline establishment)
-- Date: 2026-01-23

-- Add freeze state columns to programa_obra table
ALTER TABLE programa_obra
    ADD COLUMN IF NOT EXISTS congelado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS congelado_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS congelado_by UUID,
    ADD COLUMN IF NOT EXISTS snapshot_algorithm VARCHAR(50);

-- Add comment to columns for documentation
COMMENT ON COLUMN programa_obra.congelado IS 'Indica si el cronograma ha sido congelado (baseline establecido)';
COMMENT ON COLUMN programa_obra.congelado_at IS 'Timestamp de cuando se congeló el cronograma';
COMMENT ON COLUMN programa_obra.congelado_by IS 'ID del usuario que congeló el cronograma';
COMMENT ON COLUMN programa_obra.snapshot_algorithm IS 'Versión del algoritmo usado para generar el snapshot del cronograma';

-- Create index for faster queries on frozen schedules
CREATE INDEX IF NOT EXISTS idx_programa_obra_congelado ON programa_obra(congelado) WHERE congelado = TRUE;

-- Data migration: Set default values for existing records
UPDATE programa_obra
SET congelado = FALSE
WHERE congelado IS NULL;
