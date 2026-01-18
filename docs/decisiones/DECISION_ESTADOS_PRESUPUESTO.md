# DECISIÓN CANÓNICA — ESTADOS PRESUPUESTO

## Fuentes canónicas
- `docs/modules/PRESUPUESTO_SPECS.md`
- `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`
- `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`

## Decisión
- Conjunto canónico: `BORRADOR`, `CONGELADO`, `INVALIDADO`.
- Estados legacy solo en persistencia: `EN_EDICION`, `APROBADO`, `ANULADO`.

## Mapeo legacy
- `EN_EDICION` → `BORRADOR`
- `APROBADO` → `CONGELADO`
- `ANULADO` → `INVALIDADO`

