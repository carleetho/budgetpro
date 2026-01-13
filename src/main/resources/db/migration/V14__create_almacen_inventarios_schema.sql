-- Migración Flyway V14: Crear tablas para Control de Almacén e Inventarios
-- Basado en metodología Suárez Salazar - Control de consumo físico vs teórico

-- Crear tipo enumerado para tipo de movimiento
CREATE TYPE tipo_movimiento_almacen AS ENUM (
    'ENTRADA', -- Entrada de material al almacén
    'SALIDA'  -- Salida de material del almacén
);

-- Crear tabla almacen (almacenes físicos)
CREATE TABLE almacen (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    ubicacion TEXT, -- Ubicación física del almacén
    responsable_id UUID, -- ID del responsable (opcional, puede ser usuario)
    activo BOOLEAN NOT NULL DEFAULT true,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_almacen_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE CASCADE,
    CONSTRAINT uq_almacen_codigo_proyecto UNIQUE (proyecto_id, codigo)
);

CREATE INDEX idx_almacen_proyecto ON almacen(proyecto_id);
CREATE INDEX idx_almacen_codigo ON almacen(codigo);
CREATE INDEX idx_almacen_activo ON almacen(activo);

-- Crear tabla movimiento_almacen (movimientos de entrada/salida)
CREATE TABLE movimiento_almacen (
    id UUID PRIMARY KEY,
    almacen_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    tipo_movimiento tipo_movimiento_almacen NOT NULL,
    fecha_movimiento DATE NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL,
    precio_unitario NUMERIC(19,4) NOT NULL, -- Precio unitario al momento del movimiento
    importe_total NUMERIC(19,4) NOT NULL, -- Calculado: cantidad * precio_unitario
    numero_documento VARCHAR(100), -- Número de factura, remisión, etc.
    partida_id UUID, -- Partida asociada (para salidas)
    centro_costo_id UUID, -- Centro de costo asociado (opcional)
    observaciones TEXT,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_movimiento_almacen
        FOREIGN KEY (almacen_id) REFERENCES almacen(id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id) ON DELETE CASCADE,
    CONSTRAINT fk_movimiento_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE SET NULL,
    CONSTRAINT chk_movimiento_cantidad_positiva
        CHECK (cantidad > 0),
    CONSTRAINT chk_movimiento_precio_positivo
        CHECK (precio_unitario >= 0),
    CONSTRAINT chk_movimiento_importe_positivo
        CHECK (importe_total >= 0)
);

CREATE INDEX idx_movimiento_almacen ON movimiento_almacen(almacen_id);
CREATE INDEX idx_movimiento_recurso ON movimiento_almacen(recurso_id);
CREATE INDEX idx_movimiento_tipo ON movimiento_almacen(tipo_movimiento);
CREATE INDEX idx_movimiento_fecha ON movimiento_almacen(fecha_movimiento);
CREATE INDEX idx_movimiento_partida ON movimiento_almacen(partida_id);

-- Crear tabla kardex (control de inventario por recurso y almacén)
CREATE TABLE kardex (
    id UUID PRIMARY KEY,
    almacen_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    fecha_movimiento DATE NOT NULL,
    movimiento_id UUID NOT NULL, -- Movimiento que generó este registro
    tipo_movimiento tipo_movimiento_almacen NOT NULL,
    cantidad_entrada NUMERIC(19,6) NOT NULL DEFAULT 0,
    cantidad_salida NUMERIC(19,6) NOT NULL DEFAULT 0,
    precio_unitario NUMERIC(19,4) NOT NULL, -- Precio unitario del movimiento
    saldo_cantidad NUMERIC(19,6) NOT NULL, -- Stock después del movimiento
    saldo_valor NUMERIC(19,4) NOT NULL, -- Valor del stock (costo promedio ponderado)
    costo_promedio_ponderado NUMERIC(19,4) NOT NULL, -- CPP después del movimiento
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_kardex_almacen
        FOREIGN KEY (almacen_id) REFERENCES almacen(id) ON DELETE CASCADE,
    CONSTRAINT fk_kardex_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id) ON DELETE CASCADE,
    CONSTRAINT fk_kardex_movimiento
        FOREIGN KEY (movimiento_id) REFERENCES movimiento_almacen(id) ON DELETE CASCADE,
    CONSTRAINT chk_kardex_cantidad_entrada_positiva
        CHECK (cantidad_entrada >= 0),
    CONSTRAINT chk_kardex_cantidad_salida_positiva
        CHECK (cantidad_salida >= 0),
    CONSTRAINT chk_kardex_saldo_cantidad_positivo
        CHECK (saldo_cantidad >= 0),
    CONSTRAINT chk_kardex_saldo_valor_positivo
        CHECK (saldo_valor >= 0),
    CONSTRAINT chk_kardex_cpp_positivo
        CHECK (costo_promedio_ponderado >= 0),
    CONSTRAINT chk_kardex_tipo_movimiento_coherente
        CHECK (
            (tipo_movimiento = 'ENTRADA' AND cantidad_entrada > 0 AND cantidad_salida = 0) OR
            (tipo_movimiento = 'SALIDA' AND cantidad_salida > 0 AND cantidad_entrada = 0)
        )
);

CREATE INDEX idx_kardex_almacen_recurso ON kardex(almacen_id, recurso_id);
CREATE INDEX idx_kardex_fecha ON kardex(fecha_movimiento);
CREATE INDEX idx_kardex_movimiento ON kardex(movimiento_id);
CREATE INDEX idx_kardex_recurso ON kardex(recurso_id);

-- Crear vista materializada para stock actual (opcional, para consultas rápidas)
-- Se actualiza manualmente o mediante trigger
CREATE MATERIALIZED VIEW stock_actual AS
SELECT 
    k.almacen_id,
    k.recurso_id,
    k.saldo_cantidad AS cantidad,
    k.saldo_valor AS valor_total,
    k.costo_promedio_ponderado AS costo_promedio,
    k.fecha_movimiento AS ultima_actualizacion
FROM kardex k
INNER JOIN (
    SELECT almacen_id, recurso_id, MAX(fecha_movimiento) AS max_fecha
    FROM kardex
    GROUP BY almacen_id, recurso_id
) ultimo ON k.almacen_id = ultimo.almacen_id 
    AND k.recurso_id = ultimo.recurso_id 
    AND k.fecha_movimiento = ultimo.max_fecha;

CREATE UNIQUE INDEX idx_stock_actual_almacen_recurso ON stock_actual(almacen_id, recurso_id);
CREATE INDEX idx_stock_actual_recurso ON stock_actual(recurso_id);
