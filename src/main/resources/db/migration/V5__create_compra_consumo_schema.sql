-- Migración Flyway V5: Crear tablas compra, compra_detalle y consumo_partida
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear enum para estado de compra
CREATE TYPE compra_estado AS ENUM ('BORRADOR', 'APROBADA');

-- Crear enum para tipo de consumo
CREATE TYPE consumo_tipo AS ENUM ('COMPRA', 'PLANILLA', 'OTROS');

-- Crear tabla compra
CREATE TABLE compra (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    fecha DATE NOT NULL,
    proveedor VARCHAR(200) NOT NULL,
    estado compra_estado NOT NULL DEFAULT 'BORRADOR',
    total NUMERIC(19,4) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_compra_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)
);

CREATE INDEX idx_compra_proyecto ON compra(proyecto_id);
CREATE INDEX idx_compra_fecha ON compra(fecha);
CREATE INDEX idx_compra_estado ON compra(estado);

-- Crear tabla compra_detalle
CREATE TABLE compra_detalle (
    id UUID PRIMARY KEY,
    compra_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    partida_id UUID NOT NULL, -- CRÍTICO: Imputación presupuestal
    cantidad NUMERIC(19,6) NOT NULL,
    precio_unitario NUMERIC(19,4) NOT NULL,
    subtotal NUMERIC(19,4) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_compra_detalle_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    CONSTRAINT fk_compra_detalle_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id),
    CONSTRAINT fk_compra_detalle_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id)
);

CREATE INDEX idx_compra_detalle_compra ON compra_detalle(compra_id);
CREATE INDEX idx_compra_detalle_recurso ON compra_detalle(recurso_id);
CREATE INDEX idx_compra_detalle_partida ON compra_detalle(partida_id);

-- Crear tabla consumo_partida
CREATE TABLE consumo_partida (
    id UUID PRIMARY KEY,
    partida_id UUID NOT NULL,
    compra_detalle_id UUID, -- Opcional: relación 1:1 con CompraDetalle
    monto NUMERIC(19,4) NOT NULL,
    fecha DATE NOT NULL,
    tipo consumo_tipo NOT NULL,
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_consumo_partida_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id),
    CONSTRAINT fk_consumo_partida_compra_detalle
        FOREIGN KEY (compra_detalle_id) REFERENCES compra_detalle(id) ON DELETE SET NULL
);

CREATE INDEX idx_consumo_partida_partida ON consumo_partida(partida_id);
CREATE INDEX idx_consumo_partida_compra_detalle ON consumo_partida(compra_detalle_id);
CREATE INDEX idx_consumo_partida_fecha ON consumo_partida(fecha);
CREATE INDEX idx_consumo_partida_tipo ON consumo_partida(tipo);
