-- Migration: Add fecha_inicio column to proyecto table (REQ-64, Invariant E-04)
-- Description: Start date for period consistency validation. Required when frecuencia_control is set.
-- Nullable: projects without period enforcement need no start date.

ALTER TABLE proyecto
    ADD COLUMN fecha_inicio TIMESTAMP;
