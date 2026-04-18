-- Listados de presupuesto por tenant + proyecto (GF-02)
ALTER TABLE proyecto ADD COLUMN IF NOT EXISTS tenant_id UUID;

UPDATE proyecto
SET tenant_id = '00000000-0000-0000-0000-000000000001'::uuid
WHERE tenant_id IS NULL;

ALTER TABLE proyecto ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_proyecto_tenant_id ON proyecto (tenant_id);
