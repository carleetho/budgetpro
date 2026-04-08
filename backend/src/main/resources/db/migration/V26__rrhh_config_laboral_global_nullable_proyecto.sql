-- REQ-65: global labor config uses proyecto_id IS NULL; V15 incorrectly had NOT NULL.
ALTER TABLE configuracion_laboral_extendida
    ALTER COLUMN proyecto_id DROP NOT NULL;
