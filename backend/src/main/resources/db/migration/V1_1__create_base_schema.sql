-- Migration: Create base schema (Proyecto, Presupuesto, Partida, Legacy Tables)
-- Version: V1.1
-- Description: Creates the fundamental entities and legacy tables required for migrations

-- PROYECTO
CREATE TABLE IF NOT EXISTS proyecto (
    id UUID PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    ubicacion VARCHAR(500),
    estado VARCHAR(30) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    presupuesto_total DECIMAL(19,4) NOT NULL DEFAULT 0,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    CONSTRAINT uq_proyecto_nombre UNIQUE (nombre)
);

CREATE INDEX IF NOT EXISTS idx_proyecto_estado ON proyecto(estado);

-- PRESUPUESTO
CREATE TABLE IF NOT EXISTS presupuesto (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL REFERENCES proyecto(id),
    nombre VARCHAR(200) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    es_linea_base BOOLEAN NOT NULL DEFAULT FALSE,
    es_contractual BOOLEAN NOT NULL DEFAULT FALSE,
    version INTEGER NOT NULL DEFAULT 0,
    
    -- Integrity Fields
    integrity_hash_approval VARCHAR(64),
    integrity_hash_execution VARCHAR(64),
    integrity_hash_generated_at TIMESTAMP,
    integrity_hash_generated_by UUID,
    integrity_hash_algorithm VARCHAR(20),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_presupuesto_proyecto ON presupuesto(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_presupuesto_estado ON presupuesto(estado);

-- PARTIDA
CREATE TABLE IF NOT EXISTS partida (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL REFERENCES presupuesto(id),
    padre_id UUID REFERENCES partida(id),
    codigo VARCHAR(50) NOT NULL,
    item VARCHAR(50),
    descripcion TEXT NOT NULL,
    unidad VARCHAR(20),
    
    metrado_original DECIMAL(19,4) NOT NULL DEFAULT 0,
    metrado_vigente DECIMAL(19,4) NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(19,4) NOT NULL DEFAULT 0,
    gastos_reales DECIMAL(19,4) NOT NULL DEFAULT 0,
    compromisos_pendientes DECIMAL(19,4) NOT NULL DEFAULT 0,
    
    nivel INTEGER,
    version INTEGER NOT NULL DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_partida_presupuesto ON partida(presupuesto_id);
CREATE INDEX IF NOT EXISTS idx_partida_padre ON partida(padre_id);
CREATE INDEX IF NOT EXISTS idx_partida_codigo ON partida(presupuesto_id, codigo);

-- LEGACY TABLES (Required for V4, V5, V6 migrations)

-- RECURSO (Legacy)
CREATE TABLE IF NOT EXISTS recurso (
    id UUID PRIMARY KEY,
    nombre VARCHAR(500),
    codigo VARCHAR(50),
    tipo VARCHAR(50),
    unidad VARCHAR(20),
    unidad_base VARCHAR(20),
    costo_referencia DECIMAL(19,4),
    
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

-- COMPRA
CREATE TABLE IF NOT EXISTS compra (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL, -- Logical reference
    fecha DATE NOT NULL,
    proveedor VARCHAR(200) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    total DECIMAL(19,4) NOT NULL DEFAULT 0,
    
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

CREATE INDEX IF NOT EXISTS idx_compra_proyecto ON compra(proyecto_id);
CREATE INDEX IF NOT EXISTS idx_compra_fecha ON compra(fecha);

-- COMPRA_DETALLE (Legacy schema with recurso_id)
CREATE TABLE IF NOT EXISTS compra_detalle (
    id UUID PRIMARY KEY,
    compra_id UUID NOT NULL REFERENCES compra(id),
    recurso_id UUID, -- Legacy reference to recurso
    partida_id UUID, -- Logical reference to partida
    
    naturaleza_gasto VARCHAR(30) NOT NULL DEFAULT 'MATERIALES',
    relacion_contractual VARCHAR(30) NOT NULL DEFAULT 'COMPRA_DIRECTA',
    rubro_insumo VARCHAR(50) NOT NULL DEFAULT 'OTROS',
    
    cantidad DECIMAL(19,6) NOT NULL DEFAULT 0,
    precio_unitario DECIMAL(19,4) NOT NULL DEFAULT 0,
    subtotal DECIMAL(19,4) NOT NULL DEFAULT 0,
    unidad VARCHAR(20),
    
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_compra_detalle_compra ON compra_detalle(compra_id);
CREATE INDEX IF NOT EXISTS idx_compra_detalle_recurso ON compra_detalle(recurso_id);

-- APU (Legacy)
CREATE TABLE IF NOT EXISTS apu (
    id UUID PRIMARY KEY,
    partida_id UUID REFERENCES partida(id),
    rendimiento DECIMAL(19,4),
    unidad VARCHAR(20),
    
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

-- APU_INSUMO (Legacy)
CREATE TABLE IF NOT EXISTS apu_insumo (
    id UUID PRIMARY KEY,
    apu_id UUID REFERENCES apu(id),
    recurso_id UUID, -- Legacy reference
    cantidad DECIMAL(19,6),
    precio_unitario DECIMAL(19,4),
    subtotal DECIMAL(19,4),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);

-- PROGRAMA_OBRA (Legacy - Required for V11)
CREATE TABLE IF NOT EXISTS programa_obra (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL REFERENCES proyecto(id),
    fecha_inicio DATE,
    fecha_fin_estimada DATE,
    duracion_total_dias INTEGER,
    
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uq_programa_obra_proyecto UNIQUE (proyecto_id)
);

CREATE INDEX IF NOT EXISTS idx_programa_obra_proyecto ON programa_obra(proyecto_id);

-- 11. BILLETERA & MOVIMIENTOS
CREATE TABLE billetera (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    saldo_actual DECIMAL(19, 4) NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_billetera_proyecto FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT uq_billetera_proyecto UNIQUE (proyecto_id)
);

CREATE INDEX idx_billetera_proyecto ON billetera(proyecto_id);

CREATE TABLE movimiento_caja (
    id UUID PRIMARY KEY,
    billetera_id UUID NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    monto DECIMAL(19, 4) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha TIMESTAMP NOT NULL,
    referencia VARCHAR(500) NOT NULL,
    evidencia_url VARCHAR(1000),
    estado VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_movimiento_caja_billetera FOREIGN KEY (billetera_id) REFERENCES billetera(id)
);

CREATE INDEX idx_movimiento_caja_billetera ON movimiento_caja(billetera_id);
CREATE INDEX idx_movimiento_caja_fecha ON movimiento_caja(fecha);
CREATE INDEX idx_movimiento_caja_tipo ON movimiento_caja(tipo);
