-- Migración Flyway V6: Crear tablas inventario_item y movimiento_inventario (Kardex)
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear enum para tipo de movimiento de inventario
CREATE TYPE movimiento_inventario_tipo AS ENUM ('ENTRADA_COMPRA', 'SALIDA_CONSUMO', 'AJUSTE');

-- Crear tabla inventario_item
CREATE TABLE inventario_item (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    cantidad_fisica NUMERIC(19,6) NOT NULL DEFAULT 0,
    costo_promedio NUMERIC(19,4) NOT NULL DEFAULT 0,
    ubicacion VARCHAR(200),
    ultima_actualizacion TIMESTAMP NOT NULL DEFAULT now(),
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_inventario_item_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT fk_inventario_item_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id),
    CONSTRAINT uq_inventario_proyecto_recurso
        UNIQUE (proyecto_id, recurso_id)
);

CREATE INDEX idx_inventario_proyecto ON inventario_item(proyecto_id);
CREATE INDEX idx_inventario_recurso ON inventario_item(recurso_id);

-- Crear tabla movimiento_inventario (Kardex)
CREATE TABLE movimiento_inventario (
    id UUID PRIMARY KEY,
    inventario_item_id UUID NOT NULL,
    tipo movimiento_inventario_tipo NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL,
    costo_unitario NUMERIC(19,4) NOT NULL,
    costo_total NUMERIC(19,4) NOT NULL,
    compra_detalle_id UUID, -- Opcional: para trazabilidad de compras
    referencia VARCHAR(500) NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT now(),
    version INT NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    
    CONSTRAINT fk_movimiento_inventario_item
        FOREIGN KEY (inventario_item_id) REFERENCES inventario_item(id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_inventario_compra_detalle
        FOREIGN KEY (compra_detalle_id) REFERENCES compra_detalle(id) ON DELETE SET NULL
);

CREATE INDEX idx_movimiento_inventario_item ON movimiento_inventario(inventario_item_id);
CREATE INDEX idx_movimiento_inventario_tipo ON movimiento_inventario(tipo);
CREATE INDEX idx_movimiento_inventario_fecha ON movimiento_inventario(fecha_hora);
CREATE INDEX idx_movimiento_inventario_compra ON movimiento_inventario(compra_detalle_id);
