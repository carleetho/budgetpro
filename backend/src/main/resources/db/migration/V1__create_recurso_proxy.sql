CREATE TABLE recurso_proxy (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) NOT NULL,
    catalog_source VARCHAR(50) NOT NULL,
    nombre_snapshot VARCHAR(500) NOT NULL,
    tipo_snapshot VARCHAR(50) NOT NULL,
    unidad_snapshot VARCHAR(50) NOT NULL,
    precio_snapshot DECIMAL(19,4) NOT NULL,
    snapshot_date TIMESTAMP NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NOT NULL,
    CONSTRAINT uq_recurso_proxy_external UNIQUE (external_id, catalog_source)
);

CREATE INDEX idx_recurso_proxy_external ON recurso_proxy(external_id, catalog_source);
CREATE INDEX idx_recurso_proxy_estado ON recurso_proxy(estado);
CREATE INDEX idx_recurso_proxy_tipo ON recurso_proxy(tipo_snapshot);
