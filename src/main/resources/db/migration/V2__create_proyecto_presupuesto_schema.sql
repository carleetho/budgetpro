-- Migración Flyway V2: Crear tablas proyecto y presupuesto
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear tipo enumerado para estado_proyecto
CREATE TYPE estado_proyecto AS ENUM (
    'BORRADOR',
    'ACTIVO',
    'SUSPENDIDO',
    'CERRADO'
);

-- Crear tipo enumerado para estado_presupuesto
CREATE TYPE estado_presupuesto AS ENUM (
    'EN_EDICION',
    'APROBADO'
);

-- Crear tabla proyecto
CREATE TABLE proyecto (
    id UUID PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    ubicacion VARCHAR(500),
    estado estado_proyecto NOT NULL DEFAULT 'BORRADOR',
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT uq_proyecto_nombre UNIQUE (nombre)
);

CREATE INDEX idx_proyecto_estado ON proyecto(estado);

-- Crear tabla presupuesto
CREATE TABLE presupuesto (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    estado estado_presupuesto NOT NULL DEFAULT 'EN_EDICION',
    es_contractual BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_presupuesto_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE CASCADE
);

CREATE INDEX idx_presupuesto_proyecto ON presupuesto(proyecto_id);
CREATE INDEX idx_presupuesto_estado ON presupuesto(estado);
