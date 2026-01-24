CREATE TABLE apu_insumo_snapshot (
    id UUID PRIMARY KEY,
    apu_snapshot_id UUID NOT NULL REFERENCES apu_snapshot(id) ON DELETE CASCADE,
    recurso_external_id VARCHAR(255) NOT NULL,
    recurso_nombre VARCHAR(500) NOT NULL,
    cantidad DECIMAL(19,6) NOT NULL,
    precio_unitario DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX idx_apu_insumo_snapshot_apu ON apu_insumo_snapshot(apu_snapshot_id);
CREATE INDEX idx_apu_insumo_snapshot_recurso ON apu_insumo_snapshot(recurso_external_id);
