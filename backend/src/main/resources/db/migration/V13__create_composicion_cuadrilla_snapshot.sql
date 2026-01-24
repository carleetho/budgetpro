-- Migración V8: Crear tabla composicion_cuadrilla_snapshot
-- 
-- Esta migración crea la tabla para almacenar la composición de cuadrillas
-- que permite cálculo dinámico del costo día cuadrilla.

CREATE TABLE composicion_cuadrilla_snapshot (
    id UUID PRIMARY KEY,
    apu_insumo_snapshot_id UUID NOT NULL REFERENCES apu_insumo_snapshot(id) ON DELETE CASCADE,
    personal_external_id VARCHAR(255) NOT NULL,
    personal_nombre VARCHAR(500) NOT NULL,
    cantidad DECIMAL(19,6) NOT NULL,
    costo_dia DECIMAL(19,4) NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX idx_composicion_cuadrilla_insumo 
    ON composicion_cuadrilla_snapshot(apu_insumo_snapshot_id);

CREATE INDEX idx_composicion_cuadrilla_personal 
    ON composicion_cuadrilla_snapshot(personal_external_id);

-- Comentarios para documentación
COMMENT ON TABLE composicion_cuadrilla_snapshot IS 'Composición de cuadrillas para cálculo dinámico de costo de mano de obra';
COMMENT ON COLUMN composicion_cuadrilla_snapshot.cantidad IS 'Cantidad de personal (ej: 0.1 capataz, 2.0 peones)';
COMMENT ON COLUMN composicion_cuadrilla_snapshot.costo_dia IS 'Costo diario del tipo de personal';
COMMENT ON COLUMN composicion_cuadrilla_snapshot.moneda IS 'Moneda del costo (ISO 4217, ej: PEN, USD)';
