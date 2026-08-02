-- Alinear tablas legado con entidades JPA actuales (RecursoEntity, InventarioItemEntity).
-- Corrige 500 en GET /api/v1/recursos (columnas ausentes) y lecturas de inventario_item.

-- ========== recurso ==========
ALTER TABLE recurso
    ADD COLUMN IF NOT EXISTS nombre_normalizado TEXT,
    ADD COLUMN IF NOT EXISTS atributos JSONB,
    ADD COLUMN IF NOT EXISTS estado VARCHAR(30);

-- Backfill nombre_normalizado (misma idea: trim + upper)
UPDATE recurso
SET nombre_normalizado = UPPER(TRIM(REGEXP_REPLACE(COALESCE(nombre, id::text), '\s+', ' ', 'g')))
WHERE nombre_normalizado IS NULL OR BTRIM(nombre_normalizado) = '';

-- Evitar colisiones de unique tras normalizar
UPDATE recurso r
SET nombre_normalizado = r.nombre_normalizado || '_' || REPLACE(r.id::text, '-', '')
WHERE r.id IN (
    SELECT id FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY nombre_normalizado ORDER BY id) AS rn
        FROM recurso
        WHERE nombre_normalizado IS NOT NULL
    ) d
    WHERE rn > 1
);

ALTER TABLE recurso
    ALTER COLUMN nombre_normalizado SET NOT NULL;

UPDATE recurso SET atributos = '{}'::jsonb WHERE atributos IS NULL;
ALTER TABLE recurso
    ALTER COLUMN atributos SET DEFAULT '{}'::jsonb,
    ALTER COLUMN atributos SET NOT NULL;

UPDATE recurso SET estado = 'ACTIVO' WHERE estado IS NULL OR BTRIM(estado) = '';
ALTER TABLE recurso
    ALTER COLUMN estado SET DEFAULT 'ACTIVO',
    ALTER COLUMN estado SET NOT NULL;

UPDATE recurso SET unidad_base = COALESCE(NULLIF(BTRIM(unidad_base), ''), NULLIF(BTRIM(unidad), ''), 'UND')
WHERE unidad_base IS NULL OR BTRIM(unidad_base) = '';
ALTER TABLE recurso
    ALTER COLUMN unidad_base SET DEFAULT 'UND';
-- Solo forzar NOT NULL si no quedan nulos
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM recurso WHERE unidad_base IS NULL) THEN
        ALTER TABLE recurso ALTER COLUMN unidad_base SET NOT NULL;
    END IF;
END $$;

UPDATE recurso SET costo_referencia = 0 WHERE costo_referencia IS NULL;
ALTER TABLE recurso
    ALTER COLUMN costo_referencia SET DEFAULT 0;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM recurso WHERE costo_referencia IS NULL) THEN
        ALTER TABLE recurso ALTER COLUMN costo_referencia SET NOT NULL;
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_recurso_nombre_normalizado ON recurso (nombre_normalizado);
CREATE INDEX IF NOT EXISTS idx_recurso_tipo ON recurso (tipo);
CREATE INDEX IF NOT EXISTS idx_recurso_estado ON recurso (estado);

-- ========== inventario_item (nombres de columnas vs JPA) ==========
ALTER TABLE inventario_item
    ADD COLUMN IF NOT EXISTS recurso_id UUID,
    ADD COLUMN IF NOT EXISTS recurso_nombre VARCHAR(500),
    ADD COLUMN IF NOT EXISTS recurso_clasificacion VARCHAR(100),
    ADD COLUMN IF NOT EXISTS ubicacion VARCHAR(200),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Copiar desde columnas legacy V10 si existen
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'inventario_item' AND column_name = 'nombre'
    ) THEN
        UPDATE inventario_item
        SET recurso_nombre = COALESCE(recurso_nombre, nombre)
        WHERE recurso_nombre IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'inventario_item' AND column_name = 'clasificacion'
    ) THEN
        UPDATE inventario_item
        SET recurso_clasificacion = COALESCE(recurso_clasificacion, clasificacion)
        WHERE recurso_clasificacion IS NULL;
    END IF;
END $$;

UPDATE inventario_item
SET recurso_nombre = COALESCE(recurso_nombre, 'SIN_NOMBRE')
WHERE recurso_nombre IS NULL;

UPDATE inventario_item
SET recurso_clasificacion = COALESCE(recurso_clasificacion, 'SIN_CLASIFICACION')
WHERE recurso_clasificacion IS NULL;

UPDATE inventario_item
SET created_at = COALESCE(created_at, ultima_actualizacion, NOW()),
    updated_at = COALESCE(updated_at, ultima_actualizacion, NOW())
WHERE created_at IS NULL OR updated_at IS NULL;

ALTER TABLE inventario_item
    ALTER COLUMN recurso_nombre SET NOT NULL,
    ALTER COLUMN recurso_clasificacion SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM inventario_item WHERE created_at IS NULL) THEN
        ALTER TABLE inventario_item ALTER COLUMN created_at SET DEFAULT NOW();
        ALTER TABLE inventario_item ALTER COLUMN created_at SET NOT NULL;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM inventario_item WHERE updated_at IS NULL) THEN
        ALTER TABLE inventario_item ALTER COLUMN updated_at SET DEFAULT NOW();
        ALTER TABLE inventario_item ALTER COLUMN updated_at SET NOT NULL;
    END IF;
END $$;
