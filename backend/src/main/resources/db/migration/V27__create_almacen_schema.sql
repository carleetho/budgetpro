-- Almacén + movimientos + kardex (faltante en set Flyway del repo)
-- Nota: el proyecto tiene flyway.out-of-order=true; estos objetos pueden existir ya en entornos previos.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'tipo_movimiento_almacen') THEN
    CREATE TYPE tipo_movimiento_almacen AS ENUM ('ENTRADA', 'SALIDA', 'DEVOLUCION');
  END IF;
END$$;

CREATE TABLE IF NOT EXISTS almacen (
  id UUID PRIMARY KEY,
  proyecto_id UUID NOT NULL,
  codigo VARCHAR(50) NOT NULL,
  nombre VARCHAR(200) NOT NULL,
  ubicacion TEXT,
  responsable_id UUID,
  activo BOOLEAN NOT NULL DEFAULT TRUE,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT uq_almacen_codigo_proyecto UNIQUE (proyecto_id, codigo)
);

CREATE INDEX IF NOT EXISTS idx_almacen_proyecto ON almacen (proyecto_id);
CREATE INDEX IF NOT EXISTS idx_almacen_codigo ON almacen (codigo);
CREATE INDEX IF NOT EXISTS idx_almacen_activo ON almacen (activo);

CREATE TABLE IF NOT EXISTS movimiento_almacen (
  id UUID PRIMARY KEY,
  almacen_id UUID NOT NULL REFERENCES almacen(id),
  recurso_id UUID NOT NULL,
  tipo_movimiento VARCHAR(30) NOT NULL,
  fecha_movimiento DATE NOT NULL,
  fecha TIMESTAMP NOT NULL,
  cantidad NUMERIC(19,4) NOT NULL,
  precio_unitario NUMERIC(19,4) NOT NULL,
  costo_unitario NUMERIC(19,4) NOT NULL,
  importe_total NUMERIC(19,4) NOT NULL,
  numero_documento VARCHAR(100),
  partida_id UUID,
  centro_costo_id UUID,
  observaciones TEXT,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by UUID,
  updated_by UUID,
  CONSTRAINT chk_mov_almacen_cantidad CHECK (cantidad > 0),
  CONSTRAINT chk_mov_almacen_precio CHECK (precio_unitario >= 0),
  CONSTRAINT chk_mov_almacen_importe CHECK (importe_total >= 0),
  CONSTRAINT chk_mov_almacen_tipo CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'DEVOLUCION')),
  -- SALIDA exige partida; ENTRADA/DEVOLUCION no (equivalente claro a implicación, una sola columna de tipo)
  CONSTRAINT chk_mov_almacen_partida_salida CHECK (tipo_movimiento <> 'SALIDA' OR partida_id IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_movimiento_almacen ON movimiento_almacen (almacen_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_recurso ON movimiento_almacen (recurso_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_tipo ON movimiento_almacen (tipo_movimiento);
CREATE INDEX IF NOT EXISTS idx_movimiento_fecha ON movimiento_almacen (fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_movimiento_partida ON movimiento_almacen (partida_id);

CREATE TABLE IF NOT EXISTS kardex (
  id UUID PRIMARY KEY,
  almacen_id UUID NOT NULL REFERENCES almacen(id),
  recurso_id UUID NOT NULL,
  fecha_movimiento DATE NOT NULL,
  movimiento_id UUID NOT NULL REFERENCES movimiento_almacen(id),
  tipo_movimiento tipo_movimiento_almacen NOT NULL,
  cantidad_entrada NUMERIC(19,6) NOT NULL DEFAULT 0,
  cantidad_salida NUMERIC(19,6) NOT NULL DEFAULT 0,
  precio_unitario NUMERIC(19,4) NOT NULL,
  saldo_cantidad NUMERIC(19,6) NOT NULL,
  saldo_valor NUMERIC(19,4) NOT NULL,
  costo_promedio_ponderado NUMERIC(19,4) NOT NULL,
  version INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_kardex_qty_nonneg CHECK (cantidad_entrada >= 0 AND cantidad_salida >= 0 AND saldo_cantidad >= 0),
  CONSTRAINT chk_kardex_val_nonneg CHECK (saldo_valor >= 0 AND costo_promedio_ponderado >= 0 AND precio_unitario >= 0)
);

CREATE INDEX IF NOT EXISTS idx_kardex_almacen_recurso ON kardex (almacen_id, recurso_id);
CREATE INDEX IF NOT EXISTS idx_kardex_fecha ON kardex (fecha_movimiento);
CREATE INDEX IF NOT EXISTS idx_kardex_movimiento ON kardex (movimiento_id);
CREATE INDEX IF NOT EXISTS idx_kardex_recurso ON kardex (recurso_id);

