-- Cronograma operativo (actividad_programada + dependencia_actividad) (faltante en set Flyway del repo)

CREATE TABLE IF NOT EXISTS actividad_programada (
  id UUID PRIMARY KEY,
  partida_id UUID NOT NULL UNIQUE REFERENCES partida(id),
  programa_obra_id UUID NOT NULL REFERENCES programa_obra(id),
  fecha_inicio DATE,
  fecha_fin DATE,
  duracion_dias INTEGER,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_actividad_fechas CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio),
  CONSTRAINT chk_actividad_duracion CHECK (duracion_dias IS NULL OR duracion_dias > 0)
);

CREATE INDEX IF NOT EXISTS idx_actividad_programada_programa ON actividad_programada (programa_obra_id);
CREATE INDEX IF NOT EXISTS idx_actividad_programada_partida ON actividad_programada (partida_id);

CREATE TABLE IF NOT EXISTS dependencia_actividad (
  id UUID PRIMARY KEY,
  actividad_id UUID NOT NULL REFERENCES actividad_programada(id) ON DELETE CASCADE,
  actividad_predecesora_id UUID NOT NULL,
  CONSTRAINT uq_dependencia_actividad UNIQUE (actividad_id, actividad_predecesora_id),
  CONSTRAINT chk_dependencia_no_self CHECK (actividad_id <> actividad_predecesora_id)
);

CREATE INDEX IF NOT EXISTS idx_dependencia_actividad ON dependencia_actividad (actividad_id);
CREATE INDEX IF NOT EXISTS idx_dependencia_predecesora ON dependencia_actividad (actividad_predecesora_id);

