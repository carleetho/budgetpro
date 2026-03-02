-- V17__add_evm_time_series.sql
-- Create table for EVM time series data (materialized table for event infrastructure).
-- This table stores periodic EVM metrics for projects, enabling time-series analysis.

-- Part A: DDL - Create table with 14 columns
CREATE TABLE evm_time_series (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proyecto_id UUID NOT NULL,
    fecha_corte DATE NOT NULL,
    periodo INTEGER NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- Métricas Base (from evm_snapshot)
    pv DECIMAL(19, 4) NOT NULL,
    ev DECIMAL(19, 4) NOT NULL,
    ac DECIMAL(19, 4) NOT NULL,
    bac DECIMAL(19, 4) NOT NULL,
    bac_ajustado DECIMAL(19, 4) NOT NULL,
    
    -- Índices de Desempeño (from evm_snapshot)
    cpi DECIMAL(19, 4) NOT NULL,
    spi DECIMAL(19, 4) NOT NULL,
    
    -- Metadata (AuditEntity)
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID NOT NULL,
    
    CONSTRAINT fk_evm_time_series_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT uk_evm_time_series_proyecto_fecha UNIQUE (proyecto_id, fecha_corte)
);

-- Indexes for performance
CREATE INDEX idx_evm_time_series_proyecto ON evm_time_series(proyecto_id);
CREATE INDEX idx_evm_time_series_proyecto_fecha ON evm_time_series(proyecto_id, fecha_corte);
CREATE INDEX idx_evm_time_series_fecha_corte ON evm_time_series(fecha_corte);

-- Part B: Backfill DML - Seed table with baseline row per existing project
-- Cold-start logic: For each project that has data in evm_snapshot, insert one baseline row
-- using the MAX(fecha_corte) snapshot per project. This ensures REQ-62/63 have historical
-- data from day one.
INSERT INTO evm_time_series (
    id,
    proyecto_id,
    fecha_corte,
    periodo,
    moneda,
    pv,
    ev,
    ac,
    bac,
    bac_ajustado,
    cpi,
    spi,
    created_at,
    updated_at,
    created_by
)
SELECT
    gen_random_uuid() AS id,
    s.proyecto_id,
    CAST(s.fecha_corte AS DATE) AS fecha_corte,  -- Cast TIMESTAMP to DATE
    1 AS periodo,  -- Baseline period
    'USD' AS moneda,  -- Default currency
    s.pv,
    s.ev,
    s.ac,
    s.bac,
    s.bac AS bac_ajustado,  -- Baseline assumption: no approved CO yet, so bac_ajustado = bac
    s.cpi,
    s.spi,
    NOW() AS created_at,
    NOW() AS updated_at,
    '00000000-0000-0000-0000-000000000001'::UUID AS created_by  -- com.budgetpro.shared.SystemActorIds.EVENT_INFRA_SYSTEM_USER_UUID_TEXT
FROM (
    -- Get the latest snapshot per project (MAX fecha_corte)
    SELECT DISTINCT ON (proyecto_id)
        proyecto_id,
        fecha_corte,
        pv,
        ev,
        ac,
        bac,
        cpi,
        spi
    FROM evm_snapshot
    ORDER BY proyecto_id, fecha_corte DESC
) s
WHERE NOT EXISTS (
    -- Idempotency guard: only insert if no row exists for this proyecto_id + fecha_corte
    SELECT 1
    FROM evm_time_series ets
    WHERE ets.proyecto_id = s.proyecto_id
      AND ets.fecha_corte = CAST(s.fecha_corte AS DATE)
);
