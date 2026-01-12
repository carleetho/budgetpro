-- Migración Flyway V4: Crear tablas compra y compra_detalle
-- Basado en el ERD Físico Definitivo (_docs/context/08_erd_fisico_definitivo_sql.md)

-- Crear tabla compra
-- Según ERD definitivo: id UUID, proyecto_id UUID, total NUMERIC(19,4), created_at, updated_at
-- NOTA: El ERD no incluye presupuesto_id, estado ni version, pero el dominio los requiere.
-- Se agregan presupuesto_id y estado para soportar el modelo de dominio.
-- version se omite por ahora (se puede agregar después si es necesario).
CREATE TABLE compra (
    id UUID PRIMARY KEY,
    proyecto_id UUID NOT NULL,
    presupuesto_id UUID NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'REGISTRADA',
    total NUMERIC(19,4) NOT NULL,
    version INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_compra_proyecto
        FOREIGN KEY (proyecto_id) REFERENCES proyecto(id),
    CONSTRAINT fk_compra_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id)
);

CREATE INDEX idx_compra_proyecto ON compra(proyecto_id);
CREATE INDEX idx_compra_presupuesto ON compra(presupuesto_id);

-- Crear tabla compra_detalle
-- Según ERD definitivo: id UUID, compra_id UUID, recurso_id UUID, cantidad NUMERIC(19,6), precio_unitario NUMERIC(19,4), created_at, updated_at
CREATE TABLE compra_detalle (
    id UUID PRIMARY KEY,
    compra_id UUID NOT NULL,
    recurso_id UUID NOT NULL,
    cantidad NUMERIC(19,6) NOT NULL,
    precio_unitario NUMERIC(19,4) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT fk_compra_detalle_compra
        FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    CONSTRAINT fk_compra_detalle_recurso
        FOREIGN KEY (recurso_id) REFERENCES recurso(id)
);

CREATE INDEX idx_compra_detalle_compra ON compra_detalle(compra_id);
CREATE INDEX idx_compra_detalle_recurso ON compra_detalle(recurso_id);
