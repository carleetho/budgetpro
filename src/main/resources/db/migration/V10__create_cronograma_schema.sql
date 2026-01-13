-- Migración Flyway V10: Crear tablas para programa de obra y actividades programadas
-- Basado en metodología Suárez Salazar (Cap. 4 - Programación)

-- Crear tabla programa_obra (1:1 con proyecto)
CREATE TABLE programa_obra (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL UNIQUE,
    fecha_inicio DATE,
    fecha_fin_estimada DATE,
    duracion_total_dias INT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_programa_obra_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE CASCADE,
    CONSTRAINT chk_fecha_fin_mayor_inicio
        CHECK (fecha_fin_estimada IS NULL OR fecha_inicio IS NULL OR fecha_fin_estimada >= fecha_inicio),
    CONSTRAINT chk_duracion_positiva
        CHECK (duracion_total_dias IS NULL OR duracion_total_dias > 0)
);

CREATE INDEX idx_programa_obra_proyecto ON programa_obra(proyecto_id);

-- Crear tabla actividad_programada (1:1 con partida)
CREATE TABLE actividad_programada (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL UNIQUE,
    programa_obra_id UUID NOT NULL,
    fecha_inicio DATE,
    fecha_fin DATE,
    duracion_dias INT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_actividad_programada_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE,
    CONSTRAINT fk_actividad_programada_programa
        FOREIGN KEY (programa_obra_id) REFERENCES programa_obra(id) ON DELETE CASCADE,
    CONSTRAINT chk_actividad_fecha_fin_mayor_inicio
        CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT chk_actividad_duracion_positiva
        CHECK (duracion_dias IS NULL OR duracion_dias > 0)
);

CREATE INDEX idx_actividad_programada_programa ON actividad_programada(programa_obra_id);
CREATE INDEX idx_actividad_programada_partida ON actividad_programada(partida_id);

-- Crear tabla dependencia_actividad (dependencias Fin-Inicio)
CREATE TABLE dependencia_actividad (
    id UUID PRIMARY KEY,
    actividad_id UUID NOT NULL,
    actividad_predecesora_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_dependencia_actividad
        FOREIGN KEY (actividad_id) REFERENCES actividad_programada(id) ON DELETE CASCADE,
    CONSTRAINT chk_dependencia_no_autoreferencia
        CHECK (actividad_id != actividad_predecesora_id),
    CONSTRAINT uq_dependencia_actividad
        UNIQUE (actividad_id, actividad_predecesora_id)
);

CREATE INDEX idx_dependencia_actividad ON dependencia_actividad(actividad_id);
CREATE INDEX idx_dependencia_predecesora ON dependencia_actividad(actividad_predecesora_id);
