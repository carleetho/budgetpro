-- Migración Flyway V2: Crear tabla billetera
-- Basado estrictamente en el ERD Físico Definitivo (_docs/context/08_erd_fisico_definitivo_sql.md)

-- Crear tabla proyecto (requerida para FK de billetera)
-- NOTA: Esta tabla es un "proyecto pasivo del sistema" usado solo para integridad referencial
CREATE TABLE IF NOT EXISTS proyecto (
    id UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proyecto_estado ON proyecto(estado);

-- Crear tabla billetera
-- ERD Físico Definitivo: id UUID, proyecto_id UUID UNIQUE, saldo_actual NUMERIC(19,4), version INT, created_at, updated_at
CREATE TABLE billetera (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL UNIQUE,
    saldo_actual NUMERIC(19,4) NOT NULL DEFAULT 0,
    version INT NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_billetera_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)
);

-- NOTA: El ERD definitivo no especifica índices adicionales ni constraints CHECK.
-- La validación de saldo no negativo se realiza a nivel dominio.
