-- Migración Flyway V2: Crear esquema de Finanzas (billetera y movimiento_caja)
-- Basado en el ERD Físico Definitivo (08_erd_fisico_definitivo_sql.md)

-- Crear tabla proyecto (requerida para FK de billetera)
-- NOTA: Esta tabla debe existir antes de crear billetera debido a la FK
CREATE TABLE IF NOT EXISTS proyecto (
    id UUID PRIMARY KEY,
    nombre TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- Crear tabla billetera
-- Relación 1:1 con proyecto (proyecto_id UNIQUE)
-- NOTA: version es BIGINT (aunque el ERD dice INT) porque en Java usamos Long (64 bits)
-- Hibernate con @Version requiere Long → BIGINT en PostgreSQL
CREATE TABLE billetera (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL UNIQUE,
    saldo_actual NUMERIC(19,4) NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_billetera_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id)
);

-- Crear tabla movimiento_caja
-- Registra todos los movimientos de fondos de una billetera
CREATE TABLE movimiento_caja (
    id UUID PRIMARY KEY,
    billetera_id UUID NOT NULL,
    monto NUMERIC(19,4) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('INGRESO', 'EGRESO')),
    fecha TIMESTAMP NOT NULL DEFAULT now(),
    referencia VARCHAR(255) NOT NULL,
    evidencia_url VARCHAR(500),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',

    created_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_movimiento_caja_billetera
        FOREIGN KEY (billetera_id) REFERENCES billetera(id) ON DELETE CASCADE
);

-- Crear índices para mejorar rendimiento de consultas
CREATE INDEX idx_billetera_proyecto_id ON billetera(proyecto_id);
CREATE INDEX idx_movimiento_caja_billetera ON movimiento_caja(billetera_id);
CREATE INDEX idx_movimiento_caja_fecha ON movimiento_caja(fecha DESC);
CREATE INDEX idx_movimiento_caja_tipo ON movimiento_caja(tipo);
CREATE INDEX idx_movimiento_caja_estado ON movimiento_caja(estado);

-- Check constraint para garantizar que el saldo nunca sea negativo
-- (validación adicional a nivel BD, aunque el dominio ya lo valida)
ALTER TABLE billetera ADD CONSTRAINT chk_saldo_no_negativo 
    CHECK (saldo_actual >= 0);
