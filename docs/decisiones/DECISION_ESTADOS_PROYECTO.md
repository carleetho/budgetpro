# DECISIÓN CANÓNICA — ESTADOS PROYECTO

## Fuentes canónicas
- `docs/modules/PROYECTO_SPECS.md`
- `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`
- `docs/audits/FASE2_DIAGNOSTICO_DOMINIO_BUDGETPRO.md`
- `docs/audits/FASE3_INVENTARIO_CANONICO_REGLAS_EXISTENTES.md`

## Decisión
- Conjunto canónico: `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.
- Estados legacy solo en persistencia: `PAUSADO`, `EJECUCION`, `FINALIZADO`.

## Mapeo legacy
- `PAUSADO` → `SUSPENDIDO`
- `EJECUCION` → `ACTIVO`
- `FINALIZADO` → `CERRADO`

