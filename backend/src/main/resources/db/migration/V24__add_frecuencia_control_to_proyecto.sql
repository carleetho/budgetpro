-- Migration: Add frecuencia_control column to proyecto table (REQ-64, Invariant E-04)
-- Description: Supports Period Consistency — frequency control for report cut dates (SEMANAL, QUINCENAL, MENSUAL)
-- Nullable by design: existing projects have no period enforcement until configured.
-- New projects default to NULL; application configures via configurarFrecuencia when needed.

ALTER TABLE proyecto
    ADD COLUMN frecuencia_control VARCHAR(20)
        CHECK (frecuencia_control IS NULL OR frecuencia_control IN ('SEMANAL', 'QUINCENAL', 'MENSUAL'));
