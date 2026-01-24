-- Tabla Agregado: INVENTARIO_ITEM
CREATE TABLE inventario_item (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    recurso_external_id VARCHAR(50) NOT NULL,
    bodega_id UUID NOT NULL,
    
    -- Campos Snapshot (Inmutables)
    nombre VARCHAR(255) NOT NULL,
    clasificacion VARCHAR(50) NOT NULL,
    unidad_base VARCHAR(20) NOT NULL,
    
    -- Estado Físico y Costo
    cantidad_fisica DECIMAL(19, 4) NOT NULL DEFAULT 0,
    costo_promedio DECIMAL(19, 4) NOT NULL DEFAULT 0,
    
    -- Auditoría y Concurrencia
    ultima_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Restricciones
    CONSTRAINT uk_inventario_unico UNIQUE (proyecto_id, recurso_external_id, unidad_base, bodega_id),
    CONSTRAINT ck_cantidad_positiva CHECK (cantidad_fisica >= 0)
);

-- Tabla Entidad: MOVIMIENTO_INVENTARIO (Log de Kardex)
CREATE TABLE movimiento_inventario (
    id UUID PRIMARY KEY,
    inventario_item_id UUID NOT NULL,
    tipo VARCHAR(30) NOT NULL, -- ENTRADA_COMPRA, SALIDA_CONSUMO, AJUSTE, etc.
    
    -- Datos Financieros/Físicos
    cantidad DECIMAL(19, 4) NOT NULL,
    costo_unitario DECIMAL(19, 4) NOT NULL,
    costo_total DECIMAL(19, 4) NOT NULL,
    
    -- Trazabilidad
    compra_detalle_id UUID,
    requisicion_id UUID,
    requisicion_item_id UUID,
    partida_id UUID,
    transferencia_id UUID,
    actividad_id UUID,
    
    -- Texto
    referencia VARCHAR(255) NOT NULL,
    justificacion TEXT,
    
    fecha_hora TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- FK
    CONSTRAINT fk_movimiento_item FOREIGN KEY (inventario_item_id) REFERENCES inventario_item(id),
    
    -- Validation logic handled in app, simple check here
    CONSTRAINT ck_movimiento_cantidad_positiva CHECK (cantidad > 0)
);

-- Índices para búsquedas frecuentes
CREATE INDEX idx_inventario_proyecto ON inventario_item(proyecto_id);
CREATE INDEX idx_movimiento_item ON movimiento_inventario(inventario_item_id);
CREATE INDEX idx_movimiento_fecha ON movimiento_inventario(fecha_hora);
