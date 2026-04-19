-- Cada nuevo PRESUPUESTO recibe automáticamente un SUBPRESUPUESTO "Principal" (Opción B).
-- Version: V41 — PostgreSQL 15 (EXECUTE FUNCTION).

CREATE OR REPLACE FUNCTION budgetpro_trg_presupuesto_insert_principal()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM subpresupuesto s
        WHERE s.presupuesto_id = NEW.id AND s.nombre = 'Principal'
    ) THEN
        INSERT INTO subpresupuesto (id, presupuesto_id, nombre, orden, version, created_at, updated_at, created_by)
        VALUES (gen_random_uuid(), NEW.id, 'Principal', 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NEW.created_by);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_presupuesto_insert_principal ON presupuesto;

CREATE TRIGGER trg_presupuesto_insert_principal
    AFTER INSERT ON presupuesto
    FOR EACH ROW
    EXECUTE FUNCTION budgetpro_trg_presupuesto_insert_principal();

COMMENT ON FUNCTION budgetpro_trg_presupuesto_insert_principal() IS 'Garantiza subpresupuesto Principal para partidas/trigger (Opción B).';
