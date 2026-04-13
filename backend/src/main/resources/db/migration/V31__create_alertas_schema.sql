-- Alertas paramétricas (faltante en set Flyway del repo)

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'nivel_alerta') THEN
    CREATE TYPE nivel_alerta AS ENUM ('INFO', 'WARNING', 'CRITICA');
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_alerta_parametrica') THEN
    CREATE TYPE tipo_alerta_parametrica AS ENUM ('MAQUINARIA_COSTO_HORARIO', 'ACERO_RATIO_CONCRETO', 'CONCRETO_TAMANO_AGREGADO');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS analisis_presupuesto (
  id UUID PRIMARY KEY,
  presupuesto_id UUID NOT NULL REFERENCES presupuesto(id),
  fecha_analisis TIMESTAMP NOT NULL,
  total_alertas INTEGER NOT NULL,
  alertas_criticas INTEGER NOT NULL,
  alertas_warning INTEGER NOT NULL,
  alertas_info INTEGER NOT NULL,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_analisis_presupuesto_presupuesto ON analisis_presupuesto (presupuesto_id);
CREATE INDEX IF NOT EXISTS idx_analisis_presupuesto_fecha ON analisis_presupuesto (fecha_analisis);

CREATE TABLE IF NOT EXISTS alerta_parametrica (
  id UUID PRIMARY KEY,
  analisis_id UUID NOT NULL REFERENCES analisis_presupuesto(id) ON DELETE CASCADE,
  tipo_alerta tipo_alerta_parametrica NOT NULL,
  nivel nivel_alerta NOT NULL,
  partida_id UUID,
  recurso_id UUID,
  mensaje TEXT NOT NULL,
  valor_detectado NUMERIC(19,6),
  valor_esperado_min NUMERIC(19,6),
  valor_esperado_max NUMERIC(19,6),
  sugerencia TEXT,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alerta_analisis ON alerta_parametrica (analisis_id);
CREATE INDEX IF NOT EXISTS idx_alerta_tipo ON alerta_parametrica (tipo_alerta);
CREATE INDEX IF NOT EXISTS idx_alerta_nivel ON alerta_parametrica (nivel);
CREATE INDEX IF NOT EXISTS idx_alerta_partida ON alerta_parametrica (partida_id);
CREATE INDEX IF NOT EXISTS idx_alerta_recurso ON alerta_parametrica (recurso_id);

