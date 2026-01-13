-- Migración Flyway V13: Crear tablas para Reajuste de Costos (Escalatorias)
-- Basado en metodología Suárez Salazar (Cap. 3.000 - Reajuste de Costos)
-- Fórmula polinómica: Pr = Po × (I1 / Io)

-- Crear tipo enumerado para tipo de índice
CREATE TYPE tipo_indice_precios AS ENUM (
    'INPC', -- Índice Nacional de Precios al Consumidor
    'INPP', -- Índice Nacional de Precios Productor
    'CUSTOM' -- Índice personalizado
);

-- Crear tabla indice_precios (catálogo mensual de índices de precios)
CREATE TABLE indice_precios (
    id UUID PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    tipo tipo_indice_precios NOT NULL,
    fecha_base DATE NOT NULL, -- Fecha del índice (año-mes)
    valor NUMERIC(19,6) NOT NULL, -- Valor del índice
    activo BOOLEAN NOT NULL DEFAULT true,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_indice_precios_codigo_fecha UNIQUE (codigo, fecha_base)
);

CREATE INDEX idx_indice_precios_codigo ON indice_precios(codigo);
CREATE INDEX idx_indice_precios_fecha ON indice_precios(fecha_base);
CREATE INDEX idx_indice_precios_tipo ON indice_precios(tipo);
CREATE INDEX idx_indice_precios_activo ON indice_precios(activo);

-- Crear tabla estimacion_reajuste (estimación de reajuste de costos)
CREATE TABLE estimacion_reajuste (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    presupuesto_id UUID NOT NULL, -- Presupuesto base a reajustar
    numero_estimacion INT NOT NULL, -- Número consecutivo de estimación de reajuste
    fecha_corte DATE NOT NULL, -- Fecha de corte para el cálculo
    indice_base_codigo VARCHAR(50) NOT NULL, -- Código del índice base (de licitación)
    indice_base_fecha DATE NOT NULL, -- Fecha del índice base
    indice_actual_codigo VARCHAR(50) NOT NULL, -- Código del índice actual
    indice_actual_fecha DATE NOT NULL, -- Fecha del índice actual
    valor_indice_base NUMERIC(19,6) NOT NULL, -- Io: Índice base
    valor_indice_actual NUMERIC(19,6) NOT NULL, -- I1: Índice actual
    monto_base NUMERIC(19,4) NOT NULL, -- Po: Monto base del presupuesto
    monto_reajustado NUMERIC(19,4) NOT NULL, -- Pr: Monto reajustado (calculado)
    diferencial NUMERIC(19,4) NOT NULL, -- Diferencial a cobrar: Pr - Po
    porcentaje_variacion NUMERIC(5,2) NOT NULL, -- Porcentaje de variación: ((I1/Io) - 1) * 100
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR', -- BORRADOR, APROBADA, APLICADA
    observaciones TEXT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_estimacion_reajuste_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE CASCADE,
    CONSTRAINT fk_estimacion_reajuste_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id) ON DELETE CASCADE,
    CONSTRAINT chk_estimacion_reajuste_monto_base_positivo
        CHECK (monto_base >= 0),
    CONSTRAINT chk_estimacion_reajuste_monto_reajustado_positivo
        CHECK (monto_reajustado >= 0),
    CONSTRAINT chk_estimacion_reajuste_valor_indice_base_positivo
        CHECK (valor_indice_base > 0),
    CONSTRAINT chk_estimacion_reajuste_valor_indice_actual_positivo
        CHECK (valor_indice_actual > 0),
    CONSTRAINT chk_estimacion_reajuste_estado_valido
        CHECK (estado IN ('BORRADOR', 'APROBADA', 'APLICADA')),
    CONSTRAINT uq_estimacion_reajuste_numero
        UNIQUE (proyecto_id, numero_estimacion)
);

CREATE INDEX idx_estimacion_reajuste_proyecto ON estimacion_reajuste(proyecto_id);
CREATE INDEX idx_estimacion_reajuste_presupuesto ON estimacion_reajuste(presupuesto_id);
CREATE INDEX idx_estimacion_reajuste_fecha_corte ON estimacion_reajuste(fecha_corte);
CREATE INDEX idx_estimacion_reajuste_estado ON estimacion_reajuste(estado);

-- Crear tabla detalle_reajuste_partida (detalle de reajuste por partida)
CREATE TABLE detalle_reajuste_partida (
    id UUID PRIMARY KEY,
    estimacion_reajuste_id UUID NOT NULL,
    partida_id UUID NOT NULL,
    monto_base NUMERIC(19,4) NOT NULL, -- Po: Monto base de la partida
    monto_reajustado NUMERIC(19,4) NOT NULL, -- Pr: Monto reajustado
    diferencial NUMERIC(19,4) NOT NULL, -- Diferencial: Pr - Po
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_detalle_reajuste_estimacion
        FOREIGN KEY (estimacion_reajuste_id) REFERENCES estimacion_reajuste(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_reajuste_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE,
    CONSTRAINT chk_detalle_reajuste_monto_base_positivo
        CHECK (monto_base >= 0),
    CONSTRAINT chk_detalle_reajuste_monto_reajustado_positivo
        CHECK (monto_reajustado >= 0),
    CONSTRAINT uq_detalle_reajuste_partida
        UNIQUE (estimacion_reajuste_id, partida_id)
);

CREATE INDEX idx_detalle_reajuste_estimacion ON detalle_reajuste_partida(estimacion_reajuste_id);
CREATE INDEX idx_detalle_reajuste_partida ON detalle_reajuste_partida(partida_id);
