-- Producción (RPC) (faltante en set Flyway del repo)

CREATE TABLE IF NOT EXISTS reporte_produccion (
  id UUID PRIMARY KEY,
  fecha_reporte DATE NOT NULL,
  responsable_id UUID NOT NULL,
  aprobador_id UUID,
  estado VARCHAR(20) NOT NULL,
  comentario TEXT,
  ubicacion_gps VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by UUID,
  updated_by UUID
);

CREATE INDEX IF NOT EXISTS idx_rpc_fecha ON reporte_produccion (fecha_reporte);
CREATE INDEX IF NOT EXISTS idx_rpc_responsable ON reporte_produccion (responsable_id);
CREATE INDEX IF NOT EXISTS idx_rpc_estado ON reporte_produccion (estado);

CREATE TABLE IF NOT EXISTS detalle_rpc (
  id UUID PRIMARY KEY,
  reporte_id UUID NOT NULL REFERENCES reporte_produccion(id) ON DELETE CASCADE,
  partida_id UUID NOT NULL REFERENCES partida(id),
  cantidad_reportada NUMERIC(19,4) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by UUID,
  updated_by UUID,
  CONSTRAINT chk_detalle_rpc_qty CHECK (cantidad_reportada >= 0),
  CONSTRAINT uq_detalle_rpc_partida UNIQUE (reporte_id, partida_id)
);

CREATE INDEX IF NOT EXISTS idx_detalle_rpc_reporte ON detalle_rpc (reporte_id);
CREATE INDEX IF NOT EXISTS idx_detalle_rpc_partida ON detalle_rpc (partida_id);

