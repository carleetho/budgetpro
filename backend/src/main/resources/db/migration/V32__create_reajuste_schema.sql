-- Reajuste de costos (faltante en set Flyway del repo)

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_indice_precios') THEN
    CREATE TYPE tipo_indice_precios AS ENUM ('INPC', 'INPP', 'CUSTOM');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'estado_estimacion_reajuste') THEN
    CREATE TYPE estado_estimacion_reajuste AS ENUM ('BORRADOR', 'APROBADA', 'APLICADA');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS indice_precios (
  id UUID PRIMARY KEY,
  codigo VARCHAR(50) NOT NULL,
  nombre VARCHAR(200) NOT NULL,
  tipo tipo_indice_precios NOT NULL,
  fecha_base DATE NOT NULL,
  valor NUMERIC(19,6) NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_indice_precios_codigo_fecha UNIQUE (codigo, fecha_base)
);

CREATE INDEX IF NOT EXISTS idx_indice_precios_codigo ON indice_precios (codigo);
CREATE INDEX IF NOT EXISTS idx_indice_precios_fecha ON indice_precios (fecha_base);
CREATE INDEX IF NOT EXISTS idx_indice_precios_tipo ON indice_precios (tipo);
CREATE INDEX IF NOT EXISTS idx_indice_precios_activo ON indice_precios (activo);

CREATE TABLE IF NOT EXISTS estimacion_reajuste (
  id UUID PRIMARY KEY,
  proyecto_id UUID NOT NULL REFERENCES proyecto(id),
  presupuesto_id UUID NOT NULL REFERENCES presupuesto(id),
  numero_estimacion INTEGER NOT NULL,
  fecha_corte DATE NOT NULL,
  indice_base_codigo VARCHAR(50) NOT NULL,
  indice_base_fecha DATE NOT NULL,
  indice_actual_codigo VARCHAR(50) NOT NULL,
  indice_actual_fecha DATE NOT NULL,
  valor_indice_base NUMERIC(19,6) NOT NULL,
  valor_indice_actual NUMERIC(19,6) NOT NULL,
  monto_base NUMERIC(19,4) NOT NULL,
  monto_reajustado NUMERIC(19,4) NOT NULL,
  diferencial NUMERIC(19,4) NOT NULL,
  porcentaje_variacion NUMERIC(5,2) NOT NULL,
  estado estado_estimacion_reajuste NOT NULL,
  observaciones TEXT,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_estimacion_reajuste_numero UNIQUE (proyecto_id, numero_estimacion)
);

CREATE INDEX IF NOT EXISTS idx_estimacion_reajuste_proyecto ON estimacion_reajuste (proyecto_id);
CREATE INDEX IF NOT EXISTS idx_estimacion_reajuste_presupuesto ON estimacion_reajuste (presupuesto_id);
CREATE INDEX IF NOT EXISTS idx_estimacion_reajuste_fecha_corte ON estimacion_reajuste (fecha_corte);
CREATE INDEX IF NOT EXISTS idx_estimacion_reajuste_estado ON estimacion_reajuste (estado);

CREATE TABLE IF NOT EXISTS detalle_reajuste_partida (
  id UUID PRIMARY KEY,
  estimacion_reajuste_id UUID NOT NULL REFERENCES estimacion_reajuste(id) ON DELETE CASCADE,
  partida_id UUID NOT NULL,
  monto_base NUMERIC(19,4) NOT NULL,
  monto_reajustado NUMERIC(19,4) NOT NULL,
  diferencial NUMERIC(19,4) NOT NULL,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_detalle_reajuste_partida UNIQUE (estimacion_reajuste_id, partida_id)
);

CREATE INDEX IF NOT EXISTS idx_detalle_reajuste_estimacion ON detalle_reajuste_partida (estimacion_reajuste_id);
CREATE INDEX IF NOT EXISTS idx_detalle_reajuste_partida ON detalle_reajuste_partida (partida_id);

