-- Estimaciones (faltante en set Flyway del repo)

CREATE TABLE IF NOT EXISTS estimacion (
  id UUID PRIMARY KEY,
  proyecto_id UUID NOT NULL,
  numero_estimacion INTEGER NOT NULL,
  fecha_corte DATE NOT NULL,
  periodo_inicio DATE NOT NULL,
  periodo_fin DATE NOT NULL,
  monto_bruto NUMERIC(19,4) NOT NULL,
  amortizacion_anticipo NUMERIC(19,4) NOT NULL,
  retencion_fondo_garantia NUMERIC(19,4) NOT NULL,
  monto_neto_pagar NUMERIC(19,4) NOT NULL,
  evidencia_url VARCHAR(1000),
  estado VARCHAR(20) NOT NULL,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_estimacion_numero UNIQUE (proyecto_id, numero_estimacion),
  CONSTRAINT chk_estimacion_periodo CHECK (periodo_fin >= periodo_inicio),
  CONSTRAINT chk_estimacion_montos_nonneg CHECK (
    monto_bruto >= 0 AND amortizacion_anticipo >= 0 AND retencion_fondo_garantia >= 0 AND monto_neto_pagar >= 0
  )
);

CREATE INDEX IF NOT EXISTS idx_estimacion_proyecto ON estimacion (proyecto_id);
CREATE INDEX IF NOT EXISTS idx_estimacion_numero ON estimacion (proyecto_id, numero_estimacion);

CREATE TABLE IF NOT EXISTS detalle_estimacion (
  id UUID PRIMARY KEY,
  estimacion_id UUID NOT NULL REFERENCES estimacion(id) ON DELETE CASCADE,
  partida_id UUID NOT NULL,
  cantidad_avance NUMERIC(19,4) NOT NULL,
  precio_unitario NUMERIC(19,4) NOT NULL,
  importe NUMERIC(19,4) NOT NULL,
  acumulado_anterior NUMERIC(19,4) NOT NULL,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_detalle_estimacion_partida UNIQUE (estimacion_id, partida_id),
  CONSTRAINT chk_detalle_estimacion_nonneg CHECK (
    cantidad_avance >= 0 AND precio_unitario >= 0 AND importe >= 0 AND acumulado_anterior >= 0
  )
);

CREATE INDEX IF NOT EXISTS idx_detalle_estimacion_estimacion ON detalle_estimacion (estimacion_id);
CREATE INDEX IF NOT EXISTS idx_detalle_estimacion_partida ON detalle_estimacion (partida_id);

