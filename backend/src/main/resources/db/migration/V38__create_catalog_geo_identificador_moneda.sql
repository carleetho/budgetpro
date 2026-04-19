-- Opción B / S10: catálogos transversales para cabecera presupuesto (distrito, cliente, moneda).
-- Version: V38

-- GEO: jerarquía departamento → provincia → distrito (grano FK en presupuesto = distrito).
CREATE TABLE IF NOT EXISTS departamento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT uq_departamento_codigo UNIQUE (codigo)
);

CREATE INDEX IF NOT EXISTS idx_departamento_codigo ON departamento(codigo);

CREATE TABLE IF NOT EXISTS provincia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    departamento_id UUID NOT NULL REFERENCES departamento(id) ON DELETE CASCADE,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT uq_provincia_en_depto UNIQUE (departamento_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_provincia_departamento ON provincia(departamento_id);

CREATE TABLE IF NOT EXISTS distrito (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    provincia_id UUID NOT NULL REFERENCES provincia(id) ON DELETE CASCADE,
    codigo VARCHAR(10) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    CONSTRAINT uq_distrito_en_provincia UNIQUE (provincia_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_distrito_provincia ON distrito(provincia_id);

COMMENT ON TABLE departamento IS 'Catálogo geográfico nivel 1 (Opción B).';
COMMENT ON TABLE provincia IS 'Catálogo geográfico nivel 2.';
COMMENT ON TABLE distrito IS 'Catálogo geográfico nivel 3; FK distrito_id en presupuesto.';

-- Identificadores: cliente / proveedor / subcontratista (FK cliente_id en presupuesto).
CREATE TABLE IF NOT EXISTS identificador (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razon_social VARCHAR(500) NOT NULL,
    abreviatura VARCHAR(50),
    tipo VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    CONSTRAINT ck_identificador_tipo CHECK (tipo IN ('CLIENTE', 'PROVEEDOR', 'SUBCONTRATISTA'))
);

CREATE INDEX IF NOT EXISTS idx_identificador_tipo ON identificador(tipo);
CREATE INDEX IF NOT EXISTS idx_identificador_razon ON identificador(razon_social);

COMMENT ON TABLE identificador IS 'Catálogo de identificadores (Opción B); presupuesto.cliente_id.';

-- Monedas (FK moneda_base_id / moneda_alterna_id en presupuesto).
CREATE TABLE IF NOT EXISTS moneda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo_iso VARCHAR(3) NOT NULL,
    simbolo VARCHAR(8),
    es_base_catalogo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_moneda_codigo_iso UNIQUE (codigo_iso)
);

CREATE INDEX IF NOT EXISTS idx_moneda_codigo ON moneda(codigo_iso);

COMMENT ON TABLE moneda IS 'Catálogo de monedas ISO 4217 (Opción B).';
