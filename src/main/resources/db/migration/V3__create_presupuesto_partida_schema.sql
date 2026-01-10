-- Migración Flyway V3: Crear tablas presupuesto y partida
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md) y requerimientos del dominio

-- Crear tabla proyecto (requerida para FK de presupuesto y partida)
-- NOTA: Esta tabla es un "proyecto pasivo del sistema" usado solo para integridad referencial
CREATE TABLE IF NOT EXISTS proyecto (
    id UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_proyecto_estado ON proyecto(estado);

-- Crear tipo enumerado para estado_partida
CREATE TYPE estado_partida AS ENUM (
    'BORRADOR',
    'APROBADA',
    'CERRADA'
);

-- Crear tabla presupuesto
CREATE TABLE presupuesto (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    total_asignado NUMERIC(19,4) NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_presupuesto_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)
);

CREATE INDEX idx_presupuesto_proyecto ON presupuesto(proyecto_id);

-- Crear tabla partida
CREATE TABLE partida (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL,
    proyecto_id UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    monto_presupuestado NUMERIC(19,4) NOT NULL DEFAULT 0,
    monto_reservado NUMERIC(19,4) NOT NULL DEFAULT 0,
    monto_ejecutado NUMERIC(19,4) NOT NULL DEFAULT 0,
    estado estado_partida NOT NULL DEFAULT 'BORRADOR',
    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_partida_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id),
    CONSTRAINT fk_partida_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT uq_partida_presupuesto_codigo
        UNIQUE (presupuesto_id, codigo),
    CONSTRAINT chk_partida_monto_presupuestado_no_negativo
        CHECK (monto_presupuestado >= 0),
    CONSTRAINT chk_partida_monto_reservado_no_negativo
        CHECK (monto_reservado >= 0),
    CONSTRAINT chk_partida_monto_ejecutado_no_negativo
        CHECK (monto_ejecutado >= 0),
    CONSTRAINT chk_partida_saldo_disponible_no_negativo
        CHECK (monto_presupuestado - (monto_reservado + monto_ejecutado) >= 0)
);

-- Crear índices para mejorar rendimiento de consultas
CREATE INDEX idx_partida_presupuesto ON partida(presupuesto_id);
CREATE INDEX idx_partida_proyecto ON partida(proyecto_id);
CREATE INDEX idx_partida_codigo ON partida(codigo);
CREATE INDEX idx_partida_estado ON partida(estado);
CREATE INDEX idx_partida_tipo ON partida(tipo);
