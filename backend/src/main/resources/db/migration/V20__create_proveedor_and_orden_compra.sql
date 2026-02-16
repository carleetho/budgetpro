-- Migration: Create proveedor and orden_compra tables
-- Version: V20
-- Description: Creates proveedor entity table and orden_compra aggregate with detalles

-- PROVEEDOR
CREATE TABLE IF NOT EXISTS proveedor (
    id UUID PRIMARY KEY,
    razon_social VARCHAR(200) NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    contacto VARCHAR(200),
    direccion VARCHAR(500),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    CONSTRAINT uk_proveedor_ruc UNIQUE (ruc),
    CONSTRAINT ck_proveedor_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'))
);

CREATE INDEX IF NOT EXISTS idx_proveedor_ruc ON proveedor(ruc);
CREATE INDEX IF NOT EXISTS idx_proveedor_estado ON proveedor(estado);
CREATE INDEX IF NOT EXISTS idx_proveedor_razon_social ON proveedor(razon_social);

-- ORDEN_COMPRA
CREATE TABLE IF NOT EXISTS orden_compra (
    id UUID PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    proyecto_id UUID NOT NULL,
    proveedor_id UUID NOT NULL REFERENCES proveedor(id),
    fecha DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    monto_total DECIMAL(19,4) NOT NULL DEFAULT 0,
    condiciones_pago VARCHAR(500),
    observaciones VARCHAR(1000),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    updated_by UUID NOT NULL,
    CONSTRAINT uk_orden_compra_numero UNIQUE (numero),
    CONSTRAINT ck_orden_compra_estado CHECK (estado IN ('BORRADOR', 'SOLICITADA', 'APROBADA', 'ENVIADA', 'RECIBIDA'))
);

CREATE INDEX IF NOT EXISTS idx_orden_compra_proyecto ON orden_compra(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_orden_compra_estado ON orden_compra(estado);
CREATE INDEX IF NOT EXISTS idx_orden_compra_proveedor ON orden_compra(proveedor_id);
CREATE INDEX IF NOT EXISTS idx_orden_compra_fecha ON orden_compra(fecha);

-- DETALLE_ORDEN_COMPRA
CREATE TABLE IF NOT EXISTS detalle_orden_compra (
    id UUID PRIMARY KEY,
    orden_compra_id UUID NOT NULL REFERENCES orden_compra(id) ON DELETE CASCADE,
    partida_id UUID NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    cantidad DECIMAL(19,6) NOT NULL,
    unidad VARCHAR(20),
    precio_unitario DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    orden INTEGER NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_detalle_orden_compra_orden ON detalle_orden_compra(orden_compra_id);
CREATE INDEX IF NOT EXISTS idx_detalle_orden_compra_partida ON detalle_orden_compra(partida_id);

-- Add comments
COMMENT ON TABLE proveedor IS 'Tabla de proveedores de bienes o servicios';
COMMENT ON COLUMN proveedor.id IS 'ID único del proveedor';
COMMENT ON COLUMN proveedor.ruc IS 'RUC (número de identificación tributaria) - único';
COMMENT ON COLUMN proveedor.estado IS 'Estado del proveedor: ACTIVO, INACTIVO, BLOQUEADO';

COMMENT ON TABLE orden_compra IS 'Tabla de órdenes de compra con máquina de estados';
COMMENT ON COLUMN orden_compra.numero IS 'Número secuencial de orden (ej. PO-2024-001) - único';
COMMENT ON COLUMN orden_compra.estado IS 'Estado de la orden: BORRADOR, SOLICITADA, APROBADA, ENVIADA, RECIBIDA';

COMMENT ON TABLE detalle_orden_compra IS 'Detalles (líneas) de una orden de compra';
COMMENT ON COLUMN detalle_orden_compra.orden IS 'Orden de visualización del detalle en la lista';
