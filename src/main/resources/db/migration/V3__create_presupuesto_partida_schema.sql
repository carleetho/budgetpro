-- Migración Flyway V3: Crear tablas presupuesto y partida
-- Basado en el ERD Físico Definitivo (_docs/context/08_erd_fisico_definitivo_sql.md)

-- Crear tabla proyecto (requerida para FK de presupuesto)
-- NOTA: Esta tabla es un "proyecto pasivo del sistema" usado solo para integridad referencial
CREATE TABLE IF NOT EXISTS proyecto (
    id UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proyecto_estado ON proyecto(estado);

-- Crear tabla presupuesto
-- Según ERD definitivo: id UUID, proyecto_id UUID, version INT, es_contractual BOOLEAN, created_at, updated_at
CREATE TABLE presupuesto (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    version INT NOT NULL,
    es_contractual BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_presupuesto_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)
);

CREATE INDEX idx_presupuesto_proyecto ON presupuesto(proyecto_id);

-- Crear tabla partida
-- Según ERD definitivo: id UUID, presupuesto_id UUID, codigo VARCHAR(50), descripcion TEXT, created_at, updated_at
CREATE TABLE partida (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_partida_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id)
);

CREATE INDEX idx_partida_presupuesto ON partida(presupuesto_id);
