-- Opción B: tabla subpresupuesto + columnas cabecera presupuesto (S10 / multimoneda / vigencia).
-- Version: V39

CREATE TABLE IF NOT EXISTS subpresupuesto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    presupuesto_id UUID NOT NULL REFERENCES presupuesto(id) ON DELETE CASCADE,
    nombre VARCHAR(200) NOT NULL,
    orden INTEGER NOT NULL DEFAULT 0,
    total_presupuestado DECIMAL(19,4),
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    CONSTRAINT uq_subpresupuesto_presupuesto_nombre UNIQUE (presupuesto_id, nombre)
);

CREATE INDEX IF NOT EXISTS idx_subpresupuesto_presupuesto ON subpresupuesto(presupuesto_id);

COMMENT ON TABLE subpresupuesto IS 'Especialidades bajo presupuesto (Estructuras, Eléctricas, …); WBS cuelga aquí (Opción B).';

-- Cabecera presupuesto: catálogos y parámetros S10 (columnas nullable hasta carga UI/API).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'codigo') THEN
        ALTER TABLE presupuesto ADD COLUMN codigo VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'cliente_id') THEN
        ALTER TABLE presupuesto ADD COLUMN cliente_id UUID REFERENCES identificador(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'distrito_id') THEN
        ALTER TABLE presupuesto ADD COLUMN distrito_id UUID REFERENCES distrito(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'fecha_elaboracion') THEN
        ALTER TABLE presupuesto ADD COLUMN fecha_elaboracion DATE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'plazo_dias') THEN
        ALTER TABLE presupuesto ADD COLUMN plazo_dias INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'jornada_diaria') THEN
        ALTER TABLE presupuesto ADD COLUMN jornada_diaria DECIMAL(5,2) NOT NULL DEFAULT 8.0;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'moneda_base_id') THEN
        ALTER TABLE presupuesto ADD COLUMN moneda_base_id UUID REFERENCES moneda(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'moneda_alterna_id') THEN
        ALTER TABLE presupuesto ADD COLUMN moneda_alterna_id UUID REFERENCES moneda(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'factor_cambio') THEN
        ALTER TABLE presupuesto ADD COLUMN factor_cambio DECIMAL(19,8);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'requiere_formula_polinomica') THEN
        ALTER TABLE presupuesto ADD COLUMN requiere_formula_polinomica BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'tipo_apu') THEN
        ALTER TABLE presupuesto ADD COLUMN tipo_apu VARCHAR(20) NOT NULL DEFAULT 'EDIFICACIONES';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'decimales_precios') THEN
        ALTER TABLE presupuesto ADD COLUMN decimales_precios SMALLINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'decimales_metrados') THEN
        ALTER TABLE presupuesto ADD COLUMN decimales_metrados SMALLINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'decimales_incidencias') THEN
        ALTER TABLE presupuesto ADD COLUMN decimales_incidencias SMALLINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'presupuesto' AND column_name = 'es_contractual_vigente') THEN
        ALTER TABLE presupuesto ADD COLUMN es_contractual_vigente BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

-- Consistencia tipo APU (edificaciones / carreteras).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'ck_presupuesto_tipo_apu'
    ) THEN
        ALTER TABLE presupuesto ADD CONSTRAINT ck_presupuesto_tipo_apu
            CHECK (tipo_apu IN ('EDIFICACIONES', 'CARRETERAS'));
    END IF;
END $$;

-- A lo sumo un presupuesto contractual vigente por proyecto (medición / imputaciones).
CREATE UNIQUE INDEX IF NOT EXISTS uq_presupuesto_vigente_por_proyecto
    ON presupuesto (proyecto_id)
    WHERE es_contractual_vigente = TRUE;

COMMENT ON COLUMN presupuesto.es_contractual_vigente IS 'Opción B: único TRUE por proyecto (partial unique index).';

-- Backfill: proyectos con un solo presupuesto → marcar como vigente.
UPDATE presupuesto p
SET es_contractual_vigente = TRUE
WHERE p.es_contractual_vigente = FALSE
  AND (SELECT COUNT(*) FROM presupuesto x WHERE x.proyecto_id = p.proyecto_id) = 1;
