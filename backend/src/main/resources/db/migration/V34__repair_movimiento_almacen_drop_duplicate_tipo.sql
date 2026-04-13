-- Repara despliegues que aplicaron V27 con columna duplicada `tipo` (misma semántica que `tipo_movimiento`).
-- Instalaciones nuevas ya reciben el esquema corregido desde V27; este script es idempotente.

ALTER TABLE movimiento_almacen DROP CONSTRAINT IF EXISTS chk_mov_almacen_tipo2;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'movimiento_almacen'
      AND column_name = 'tipo'
  ) THEN
    ALTER TABLE movimiento_almacen DROP CONSTRAINT IF EXISTS chk_mov_almacen_partida_salida;
    ALTER TABLE movimiento_almacen DROP COLUMN tipo;
  END IF;
END$$;

ALTER TABLE movimiento_almacen DROP CONSTRAINT IF EXISTS chk_mov_almacen_partida_salida;
ALTER TABLE movimiento_almacen ADD CONSTRAINT chk_mov_almacen_partida_salida
  CHECK (tipo_movimiento <> 'SALIDA' OR partida_id IS NOT NULL);
