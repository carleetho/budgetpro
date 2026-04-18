-- Vincula cada asignación al recurso_proxy de costeo (identidad estable en lecturas).
-- Filas previas: UUID persistente por fila (no correlacionado con recurso_proxy en datos legados).
ALTER TABLE asignaciones_proyecto
    ADD COLUMN recurso_proxy_id UUID;

UPDATE asignaciones_proyecto
SET recurso_proxy_id = gen_random_uuid()
WHERE recurso_proxy_id IS NULL;

ALTER TABLE asignaciones_proyecto
    ALTER COLUMN recurso_proxy_id SET NOT NULL;

CREATE INDEX idx_asignaciones_proyecto_recurso_proxy ON asignaciones_proyecto (recurso_proxy_id);
