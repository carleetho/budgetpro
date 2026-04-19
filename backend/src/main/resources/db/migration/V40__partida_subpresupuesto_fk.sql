-- Opción B: partida.subpresupuesto_id NOT NULL; subpresupuesto "Principal" por cada presupuesto existente.
-- Version: V40

-- 1) Garantizar fila Principal por presupuesto (idempotente).
INSERT INTO subpresupuesto (id, presupuesto_id, nombre, orden, version, created_at, updated_at, created_by)
SELECT gen_random_uuid(), p.id, 'Principal', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, p.created_by
FROM presupuesto p
WHERE NOT EXISTS (
    SELECT 1 FROM subpresupuesto s
    WHERE s.presupuesto_id = p.id AND s.nombre = 'Principal'
);

-- 2) Columna nueva en partida.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'partida' AND column_name = 'subpresupuesto_id'
    ) THEN
        ALTER TABLE partida ADD COLUMN subpresupuesto_id UUID;
    END IF;
END $$;

-- 3) Poblar desde el subpresupuesto Principal del mismo presupuesto.
UPDATE partida pa
SET subpresupuesto_id = s.id
FROM subpresupuesto s
WHERE s.presupuesto_id = pa.presupuesto_id
  AND s.nombre = 'Principal'
  AND pa.subpresupuesto_id IS NULL;

-- 4) Integridad: toda partida debe tener subpresupuesto.
DO $$
DECLARE
    n_missing INTEGER;
BEGIN
    SELECT COUNT(*) INTO n_missing FROM partida WHERE subpresupuesto_id IS NULL;
    IF n_missing > 0 THEN
        RAISE EXCEPTION 'V40: % partidas sin subpresupuesto_id tras backfill', n_missing;
    END IF;
END $$;

ALTER TABLE partida ALTER COLUMN subpresupuesto_id SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_partida_subpresupuesto'
    ) THEN
        ALTER TABLE partida
            ADD CONSTRAINT fk_partida_subpresupuesto
            FOREIGN KEY (subpresupuesto_id) REFERENCES subpresupuesto(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_partida_subpresupuesto ON partida(subpresupuesto_id);

COMMENT ON COLUMN partida.subpresupuesto_id IS 'Opción B: FK a subpresupuesto; presupuesto_id se mantiene por compatibilidad.';
