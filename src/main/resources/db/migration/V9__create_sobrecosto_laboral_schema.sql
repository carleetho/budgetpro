-- Migración Flyway V9: Crear tablas para configuración laboral y análisis de sobrecosto
-- Basado en metodología Suárez Salazar (configurable para El Salvador)

-- Crear tabla configuracion_laboral (FSR Dinámico)
CREATE TABLE configuracion_laboral (
    id UUID PRIMARY KEY,
    proyecto_id UUID UNIQUE, -- null para configuración global (singleton)
    dias_aguinaldo INT NOT NULL DEFAULT 0,
    dias_vacaciones INT NOT NULL DEFAULT 0,
    porcentaje_seguridad_social NUMERIC(19,4) NOT NULL DEFAULT 0, -- ISSS + AFP (El Salvador)
    dias_no_trabajados INT NOT NULL DEFAULT 0, -- Feriados locales
    dias_laborables_ano INT NOT NULL DEFAULT 251, -- 365 - 104 domingos - 10 feriados
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_dias_aguinaldo_positivo CHECK (dias_aguinaldo >= 0),
    CONSTRAINT chk_dias_vacaciones_positivo CHECK (dias_vacaciones >= 0),
    CONSTRAINT chk_porcentaje_ss_valido CHECK (porcentaje_seguridad_social >= 0 AND porcentaje_seguridad_social <= 100),
    CONSTRAINT chk_dias_no_trabajados_positivo CHECK (dias_no_trabajados >= 0),
    CONSTRAINT chk_dias_laborables_positivo CHECK (dias_laborables_ano > 0)
);

CREATE INDEX idx_config_laboral_proyecto ON configuracion_laboral(proyecto_id);
-- Índice único para garantizar solo una configuración global (proyecto_id IS NULL)
CREATE UNIQUE INDEX idx_config_laboral_global ON configuracion_laboral(proyecto_id) WHERE proyecto_id IS NULL;

-- Crear tabla analisis_sobrecosto (Pie de Precio Unitario)
CREATE TABLE analisis_sobrecosto (
    id UUID PRIMARY KEY,
    presupuesto_id UUID NOT NULL UNIQUE, -- Relación 1:1 con presupuesto
    -- Indirectos
    porcentaje_indirectos_oficina_central NUMERIC(19,4) NOT NULL DEFAULT 0,
    porcentaje_indirectos_oficina_campo NUMERIC(19,4) NOT NULL DEFAULT 0,
    -- Financiamiento
    porcentaje_financiamiento NUMERIC(19,4) NOT NULL DEFAULT 0,
    financiamiento_calculado BOOLEAN NOT NULL DEFAULT false, -- true si se calcula por fórmula
    -- Utilidad
    porcentaje_utilidad NUMERIC(19,4) NOT NULL DEFAULT 0,
    -- Cargos Adicionales
    porcentaje_fianzas NUMERIC(19,4) NOT NULL DEFAULT 0,
    porcentaje_impuestos_reflejables NUMERIC(19,4) NOT NULL DEFAULT 0, -- IVA, FOVIAL, etc.
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_analisis_sobrecosto_presupuesto
        FOREIGN KEY (presupuesto_id) REFERENCES presupuesto(id) ON DELETE CASCADE,
    CONSTRAINT chk_porcentaje_indirectos_central_valido 
        CHECK (porcentaje_indirectos_oficina_central >= 0 AND porcentaje_indirectos_oficina_central <= 100),
    CONSTRAINT chk_porcentaje_indirectos_campo_valido 
        CHECK (porcentaje_indirectos_oficina_campo >= 0 AND porcentaje_indirectos_oficina_campo <= 100),
    CONSTRAINT chk_porcentaje_financiamiento_valido 
        CHECK (porcentaje_financiamiento >= 0 AND porcentaje_financiamiento <= 100),
    CONSTRAINT chk_porcentaje_utilidad_valido 
        CHECK (porcentaje_utilidad >= 0 AND porcentaje_utilidad <= 100),
    CONSTRAINT chk_porcentaje_fianzas_valido 
        CHECK (porcentaje_fianzas >= 0 AND porcentaje_fianzas <= 100),
    CONSTRAINT chk_porcentaje_impuestos_valido 
        CHECK (porcentaje_impuestos_reflejables >= 0 AND porcentaje_impuestos_reflejables <= 100)
);

CREATE INDEX idx_analisis_sobrecosto_presupuesto ON analisis_sobrecosto(presupuesto_id);
