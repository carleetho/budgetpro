CREATE TABLE apu_snapshot (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL REFERENCES partida(id),
    external_apu_id VARCHAR(255) NOT NULL,
    catalog_source VARCHAR(50) NOT NULL,
    rendimiento_original DECIMAL(19,4) NOT NULL,
    rendimiento_vigente DECIMAL(19,4) NOT NULL,
    rendimiento_modificado BOOLEAN DEFAULT FALSE,
    rendimiento_modificado_por UUID,
    rendimiento_modificado_en TIMESTAMP,
    unidad_snapshot VARCHAR(50) NOT NULL,
    snapshot_date TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX idx_apu_snapshot_partida ON apu_snapshot(partida_id);
CREATE INDEX idx_apu_snapshot_date ON apu_snapshot(snapshot_date);
CREATE INDEX idx_apu_snapshot_modificado ON apu_snapshot(rendimiento_modificado) WHERE rendimiento_modificado = true;
