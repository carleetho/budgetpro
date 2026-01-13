-- Migración Flyway V11: Crear tablas para estimaciones y valuaciones (Cobro al Cliente)
-- Basado en metodología Suárez Salazar (Cap. 1.3520 - Gráfica de Ingresos, Cap. 1.3730 - Fondo de Retención)

-- Crear tabla estimacion (N:1 con proyecto)
CREATE TABLE estimacion (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    numero_estimacion INT NOT NULL,
    fecha_corte DATE NOT NULL,
    periodo_inicio DATE NOT NULL,
    periodo_fin DATE NOT NULL,
    monto_bruto NUMERIC(19,4) NOT NULL DEFAULT 0,
    amortizacion_anticipo NUMERIC(19,4) NOT NULL DEFAULT 0,
    retencion_fondo_garantia NUMERIC(19,4) NOT NULL DEFAULT 0,
    monto_neto_pagar NUMERIC(19,4) NOT NULL DEFAULT 0,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_estimacion_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id) ON DELETE CASCADE,
    CONSTRAINT chk_estimacion_periodo_valido
        CHECK (periodo_fin >= periodo_inicio),
    CONSTRAINT chk_estimacion_monto_bruto_positivo
        CHECK (monto_bruto >= 0),
    CONSTRAINT chk_estimacion_amortizacion_positiva
        CHECK (amortizacion_anticipo >= 0),
    CONSTRAINT chk_estimacion_retencion_positiva
        CHECK (retencion_fondo_garantia >= 0),
    CONSTRAINT chk_estimacion_monto_neto_positivo
        CHECK (monto_neto_pagar >= 0),
    CONSTRAINT chk_estimacion_estado_valido
        CHECK (estado IN ('BORRADOR', 'APROBADA', 'PAGADA')),
    CONSTRAINT uq_estimacion_numero
        UNIQUE (proyecto_id, numero_estimacion)
);

CREATE INDEX idx_estimacion_proyecto ON estimacion(proyecto_id);
CREATE INDEX idx_estimacion_numero ON estimacion(proyecto_id, numero_estimacion);

-- Crear tabla detalle_estimacion (N:1 con estimacion, 1:1 con partida por estimación)
CREATE TABLE detalle_estimacion (
    id UUID PRIMARY KEY,
    estimacion_id UUID NOT NULL,
    partida_id UUID NOT NULL,
    cantidad_avance NUMERIC(19,4) NOT NULL DEFAULT 0,
    precio_unitario NUMERIC(19,4) NOT NULL DEFAULT 0,
    importe NUMERIC(19,4) NOT NULL DEFAULT 0,
    acumulado_anterior NUMERIC(19,4) NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_detalle_estimacion_estimacion
        FOREIGN KEY (estimacion_id) REFERENCES estimacion(id) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_estimacion_partida
        FOREIGN KEY (partida_id) REFERENCES partida(id) ON DELETE CASCADE,
    CONSTRAINT chk_detalle_cantidad_positiva
        CHECK (cantidad_avance >= 0),
    CONSTRAINT chk_detalle_precio_positivo
        CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_importe_positivo
        CHECK (importe >= 0),
    CONSTRAINT chk_detalle_acumulado_positivo
        CHECK (acumulado_anterior >= 0),
    CONSTRAINT uq_detalle_estimacion_partida
        UNIQUE (estimacion_id, partida_id)
);

CREATE INDEX idx_detalle_estimacion_estimacion ON detalle_estimacion(estimacion_id);
CREATE INDEX idx_detalle_estimacion_partida ON detalle_estimacion(partida_id);
