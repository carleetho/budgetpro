-- Marketing lead (demo requests) (faltante en set Flyway del repo)

CREATE TABLE IF NOT EXISTS marketing_lead (
  id UUID PRIMARY KEY,
  nombre_contacto VARCHAR(150) NOT NULL,
  email VARCHAR(200),
  telefono VARCHAR(40),
  nombre_empresa VARCHAR(200),
  rol VARCHAR(120),
  estado VARCHAR(30) NOT NULL,
  fecha_solicitud TIMESTAMP NOT NULL DEFAULT NOW(),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  created_by UUID,
  updated_by UUID
);

CREATE INDEX IF NOT EXISTS idx_marketing_lead_estado ON marketing_lead (estado);
CREATE INDEX IF NOT EXISTS idx_marketing_lead_fecha ON marketing_lead (fecha_solicitud);

