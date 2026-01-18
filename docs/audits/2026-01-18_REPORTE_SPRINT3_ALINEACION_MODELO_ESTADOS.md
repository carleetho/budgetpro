# Reporte — Sprint 3 (Alineación de Modelo y Estados)

## Responsable
- Arquitecto Backend / DBA

## Alcance
- Estados de Proyecto y Presupuesto
- Mapeos legacy en converters
- Migraciones relevantes (V2, V16)

## Evidencia revisada
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/converter/EstadoProyectoConverter.java`
- `backend/src/main/java/com/budgetpro/infrastructure/persistence/converter/EstadoPresupuestoConverter.java`
- `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`
- `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`

## Hallazgos
### Proyecto
- Dominio usa estados canónicos: `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.
- Converter mapea legacy a canónicos:
  - `EJECUCION → ACTIVO`
  - `PAUSADO → SUSPENDIDO`
  - `FINALIZADO → CERRADO`
- V2 define estados canónicos en CHECK.
- V16 migra estados a legacy en BD y ajusta CHECK a legacy.

### Presupuesto
- Dominio usa estados canónicos: `BORRADOR`, `CONGELADO`, `INVALIDADO`.
- Converter mapea legacy a canónicos:
  - `EN_EDICION → BORRADOR`
  - `APROBADO → CONGELADO`
  - `ANULADO → INVALIDADO`
- V2 define estados legacy (`EN_EDICION`, `APROBADO`).
- V16 cambia CHECK a legacy (`BORRADOR`, `APROBADO`, `ANULADO`).

## Resultado
- La compatibilidad se mantiene vía converters.
- El dominio no expone estados legacy.
- Persistencia conserva legacy por compatibilidad de datos.

## Riesgos
- Persistencia contiene CHECK legacy en V16; requiere que el mapeo siga activo para no filtrar valores al dominio.
