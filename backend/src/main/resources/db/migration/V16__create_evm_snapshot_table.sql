-- V16__create_evm_snapshot_table.sql
-- Create table for EVM (Earned Value Management) snapshots.

CREATE TABLE evm_snapshot (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    fecha_corte TIMESTAMP NOT NULL,
    fecha_calculo TIMESTAMP NOT NULL,
    
    -- Métricas Base
    pv DECIMAL(19, 4) NOT NULL,
    ev DECIMAL(19, 4) NOT NULL,
    ac DECIMAL(19, 4) NOT NULL,
    bac DECIMAL(19, 4) NOT NULL,
    
    -- Métricas de Variación
    cv DECIMAL(19, 4) NOT NULL,
    sv DECIMAL(19, 4) NOT NULL,
    
    -- Índices de Desempeño
    cpi DECIMAL(19, 4) NOT NULL,
    spi DECIMAL(19, 4) NOT NULL,
    
    -- Proyecciones
    eac DECIMAL(19, 4) NOT NULL,
    etc DECIMAL(19, 4) NOT NULL,
    vac DECIMAL(19, 4) NOT NULL,
    
    interpretacion TEXT,
    
    -- Metadata (AuditEntity)
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_evm_snapshot_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyectos(id),
    CONSTRAINT uk_evm_snapshot_proyecto_fecha UNIQUE (proyecto_id, fecha_corte)
);

-- Indexes for performance
CREATE INDEX idx_evm_snapshot_proyecto ON evm_snapshot(proyecto_id);
CREATE INDEX idx_evm_snapshot_fecha_corte ON evm_snapshot(fecha_corte);
