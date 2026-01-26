-- Create Estimacion table
CREATE TABLE estimacion (
    estimacion_id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL,
    numero_estimacion BIGINT NOT NULL, -- Sequence or auto-increment handled via trigger/app logic? Usually explicit. Let's make it simple sequence or just a number managed by app.
    estado VARCHAR(50) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    retencion_porcentaje DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_aprobacion TIMESTAMP,
    aprobado_por UUID,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_estado CHECK (estado IN ('BORRADOR', 'APROBADA', 'FACTURADA', 'ANULADA')),
    CONSTRAINT check_fechas CHECK (fecha_inicio <= fecha_fin)
);

CREATE INDEX idx_estimacion_presupuesto ON estimacion(presupuesto_id);
CREATE INDEX idx_estimacion_estado ON estimacion(estado);
CREATE INDEX idx_estimacion_periodo ON estimacion(fecha_inicio, fecha_fin);

-- Create Estimacion Item table
CREATE TABLE estimacion_item (
    item_id UUID PRIMARY KEY,
    estimacion_id UUID NOT NULL,
    partida_id UUID NOT NULL,
    concepto TEXT NOT NULL,
    monto_contractual DECIMAL(19,4) NOT NULL,
    
    porcentaje_anterior DECIMAL(5,2) NOT NULL,
    monto_anterior DECIMAL(19,4) NOT NULL,
    
    porcentaje_actual DECIMAL(5,2) NOT NULL,
    monto_actual DECIMAL(19,4) NOT NULL,
    
    CONSTRAINT fk_item_estimacion FOREIGN KEY (estimacion_id) REFERENCES estimacion(estimacion_id) ON DELETE CASCADE,
    CONSTRAINT uq_estimacion_partida UNIQUE (estimacion_id, partida_id)
);

CREATE INDEX idx_item_estimacion ON estimacion_item(estimacion_id);
CREATE INDEX idx_item_partida ON estimacion_item(partida_id);

-- Create Avance Partida table (Historical)
CREATE TABLE avance_partida (
    avance_id UUID PRIMARY KEY,
    partida_id UUID NOT NULL,
    estimacion_id UUID NOT NULL,
    fecha_registro TIMESTAMP NOT NULL,
    porcentaje_avance DECIMAL(5,2) NOT NULL,
    monto_acumulado DECIMAL(19,4) NOT NULL,
    
    CONSTRAINT fk_avance_estimacion FOREIGN KEY (estimacion_id) REFERENCES estimacion(estimacion_id)
);

CREATE INDEX idx_avance_partida ON avance_partida(partida_id);
CREATE INDEX idx_avance_estimacion ON avance_partida(estimacion_id);

-- Create Estimacion Snapshot table
CREATE TABLE estimacion_snapshot (
    snapshot_id UUID PRIMARY KEY,
    estimacion_id UUID NOT NULL,
    
    items_snapshot JSONB NOT NULL,
    totales_snapshot JSONB NOT NULL,
    metadata_snapshot JSONB NOT NULL,
    
    snapshot_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    snapshot_algorithm VARCHAR(50) NOT NULL,
    
    CONSTRAINT fk_snapshot_estimacion FOREIGN KEY (estimacion_id) REFERENCES estimacion(estimacion_id)
);

CREATE INDEX idx_snapshot_estimacion ON estimacion_snapshot(estimacion_id);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_estimacion_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_estimacion_timestamp
BEFORE UPDATE ON estimacion
FOR EACH ROW
EXECUTE FUNCTION update_estimacion_timestamp();
