-- Migración Flyway V4: Crear tablas apu y apu_insumo
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear tabla apu
CREATE TABLE apu (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL UNIQUE,
    rendimiento NUMERIC(19,6), -- Opcional, cantidad de unidades que se pueden producir por día
    unidad VARCHAR(20), -- Copia de la unidad de la partida
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_apu_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE
);

CREATE INDEX idx_apu_partida ON apu(partida_id);

-- Crear tabla apu_insumo
CREATE TABLE apu_insumo (
    id UUID PRIMARY KEY,
    apu_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL, -- Cantidad técnica por unidad de partida
    precio_unitario NUMERIC(19,4) NOT NULL, -- Snapshot del precio del recurso al momento de agregar
    subtotal NUMERIC(19,4) NOT NULL, -- Calculado: cantidad * precio_unitario
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_apu_insumo_apu
        FOREIGN KEY (apu_id) REFERENCES apu(id) ON DELETE CASCADE,
    CONSTRAINT fk_apu_insumo_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id)
);

CREATE INDEX idx_apu_insumo_apu ON apu_insumo(apu_id);
CREATE INDEX idx_apu_insumo_recurso ON apu_insumo(recurso_id);
