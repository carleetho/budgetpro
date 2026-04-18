-- Migración para crear la tabla analisis_sobrecosto
CREATE TABLE analisis_sobrecosto (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL,
    porcentaje_indirectos_oficina_central NUMERIC(19, 4) NOT NULL,
    porcentaje_indirectos_oficina_campo NUMERIC(19, 4) NOT NULL,
    porcentaje_financiamiento NUMERIC(19, 4) NOT NULL,
    financiamiento_calculado BOOLEAN NOT NULL,
    porcentaje_utilidad NUMERIC(19, 4) NOT NULL,
    porcentaje_fianzas NUMERIC(19, 4) NOT NULL,
    porcentaje_impuestos_reflejables NUMERIC(19, 4) NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT fk_analisis_sobrecosto_presupuesto FOREIGN KEY (presupuesto_id) REFERENCES presupuesto (id),
    CONSTRAINT uq_analisis_sobrecosto_presupuesto UNIQUE (presupuesto_id)
);

CREATE INDEX idx_analisis_sobrecosto_presupuesto ON analisis_sobrecosto (presupuesto_id);
