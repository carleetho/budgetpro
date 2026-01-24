-- Migración para actualizar compra_detalle a usar referencias externas de recursos
-- Reemplaza recurso_id (UUID FK) con recurso_external_id (VARCHAR) y agrega recurso_nombre

-- Paso 1: Agregar nuevas columnas (nullable inicialmente para migración)
ALTER TABLE compra_detalle
    ADD COLUMN IF NOT EXISTS recurso_external_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS recurso_nombre VARCHAR(500);

-- Paso 2: Migrar datos existentes
-- Para registros existentes, generamos un external_id basado en el UUID del recurso
-- y obtenemos el nombre del recurso desde la tabla recurso
UPDATE compra_detalle cd
SET recurso_external_id = COALESCE(
    'LEGACY_' || cd.recurso_id::text,
    'UNKNOWN_' || cd.id::text
),
recurso_nombre = COALESCE(
    (SELECT r.nombre FROM recurso r WHERE r.id = cd.recurso_id),
    'Recurso Migrado'
)
WHERE cd.recurso_external_id IS NULL;

-- Paso 3: Hacer las columnas NOT NULL después de la migración
ALTER TABLE compra_detalle
    ALTER COLUMN recurso_external_id SET NOT NULL,
    ALTER COLUMN recurso_nombre SET NOT NULL;

-- Paso 4: Eliminar el índice antiguo en recurso_id
DROP INDEX IF EXISTS idx_compra_detalle_recurso;

-- Paso 5: Crear nuevo índice en recurso_external_id
CREATE INDEX IF NOT EXISTS idx_compra_detalle_recurso_external 
    ON compra_detalle(recurso_external_id);

-- Paso 6: Eliminar la foreign key constraint (si existe)
ALTER TABLE compra_detalle
    DROP CONSTRAINT IF EXISTS fk_compra_detalle_recurso;

-- Paso 7: Eliminar la columna recurso_id
ALTER TABLE compra_detalle
    DROP COLUMN IF EXISTS recurso_id;
