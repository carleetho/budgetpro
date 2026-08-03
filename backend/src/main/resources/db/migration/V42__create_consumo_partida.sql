-- Consumo presupuestal por partida (Plan vs Real / control de costos)
-- Alineado con ConsumoPartidaEntity (JPA).

CREATE TABLE IF NOT EXISTS consumo_partida (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL,
    compra_detalle_id UUID,
    monto NUMERIC(19, 4) NOT NULL,
    fecha DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_consumo_partida_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id),
    CONSTRAINT fk_consumo_partida_compra_detalle
        FOREIGN KEY (compra_detalle_id) REFERENCES compra_detalle(id),
    CONSTRAINT chk_consumo_partida_tipo
        CHECK (tipo IN ('COMPRA', 'PLANILLA', 'OTROS')),
    CONSTRAINT chk_consumo_partida_monto_no_negativo
        CHECK (monto >= 0)
);

CREATE INDEX IF NOT EXISTS idx_consumo_partida_partida
    ON consumo_partida (partida_id);
CREATE INDEX IF NOT EXISTS idx_consumo_partida_compra_detalle
    ON consumo_partida (compra_detalle_id);
CREATE INDEX IF NOT EXISTS idx_consumo_partida_fecha
    ON consumo_partida (fecha);
CREATE INDEX IF NOT EXISTS idx_consumo_partida_tipo
    ON consumo_partida (tipo);
