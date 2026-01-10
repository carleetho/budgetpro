-- Migración Flyway V1: Crear tipos enumerados y tabla recurso
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear tipos enumerados para recurso_tipo
-- NOTA: El dominio define MATERIAL, MANO_OBRA, SUBCONTRATO, ACTIVO
-- Pero el ERD menciona EQUIPO y SERVICIO. Usamos los valores del dominio como fuente de verdad.
CREATE TYPE recurso_tipo AS ENUM (
    'MATERIAL',
    'MANO_OBRA',
    'SUBCONTRATO',
    'ACTIVO'
);

-- Crear tipo enumerado para recurso_estado
CREATE TYPE recurso_estado AS ENUM (
    'ACTIVO',
    'EN_REVISION',
    'DEPRECADO'
);

-- Crear tabla recurso
CREATE TABLE recurso (
    id UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    nombre_normalizado TEXT NOT NULL,
    tipo recurso_tipo NOT NULL,
    unidad_base VARCHAR(20) NOT NULL,
    atributos JSONB NOT NULL DEFAULT '{}',
    estado recurso_estado NOT NULL DEFAULT 'ACTIVO',

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by UUID NOT NULL,

    CONSTRAINT uq_recurso_nombre UNIQUE (nombre_normalizado)
);

-- Crear índices para mejorar rendimiento de consultas
CREATE INDEX idx_recurso_tipo ON recurso(tipo);
CREATE INDEX idx_recurso_estado ON recurso(estado);
