-- Crear enum para estado de valuación
CREATE TYPE valuacion_estado AS ENUM ('BORRADOR', 'APROBADA');

-- Crear tabla avance_fisico
CREATE TABLE avance_fisico (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL,
    fecha DATE NOT NULL,
    metrado_ejecutado NUMERIC(19,6) NOT NULL,
    observacion TEXT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_avance_fisico_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE,
    CONSTRAINT chk_avance_metrado_positivo
        CHECK (metrado_ejecutado >= 0)
);

CREATE INDEX idx_avance_partida ON avance_fisico(partida_id);
CREATE INDEX idx_avance_fecha ON avance_fisico(fecha);

-- Crear tabla valuacion
CREATE TABLE valuacion (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    fecha_corte DATE NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    estado valuacion_estado NOT NULL DEFAULT 'BORRADOR',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_valuacion_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT uq_valuacion_proyecto_codigo
        UNIQUE (proyecto_id, codigo)
);

CREATE INDEX idx_valuacion_proyecto ON valuacion(proyecto_id);
CREATE INDEX idx_valuacion_fecha ON valuacion(fecha_corte);
