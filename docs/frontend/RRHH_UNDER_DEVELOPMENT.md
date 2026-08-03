# RRHH — Under Development (aviso FE / IA)

**Estado as-built:** madurez radiografía ~50% (`MODULE_SPECS_CURRENT` / notebook RRHH).  
**No confundir** con cifras históricas de task (p. ej. “20%”): la fuente de verdad de madurez es la radiografía, no el texto del plan.

## Regla anti-alucinación

- **No** generar pantallas completas de nómina, FSR, liquidaciones o dashboards RRHH con IA.
- **No** inventar reglas laborales, tasas o flujos de aprobación no documentados en `RRHH_MODULE_CANONICAL.md` / `RRHH_GAP_STUDY.md`.
- Consumir solo endpoints existentes bajo `/api/v1/rrhh/**`; tratar contratos como **inestables**.

## Superficie OpenAPI

Todos los controllers RRHH están etiquetados:

`@Tag(name = "RRHH (Under Development)", …)` + `bearer-jwt`

| Controller | Base path |
|---|---|
| EmpleadoController | `/api/v1/rrhh/empleados` |
| AsistenciaController | `/api/v1/rrhh/asistencias` |
| NominaController | `/api/v1/rrhh/nominas` |
| CuadrillaController | `/api/v1/rrhh/cuadrillas` |
| ConfiguracionLaboralExtendidaController | `/api/v1/rrhh/configuracion` |
| CostosLaboralesController | `/api/v1/rrhh/costos` |

## Roadmap sugerido (FE)

1. **Solo lectura / listados** de empleados y asistencias cuando el contrato se estabilice.
2. Formularios mínimos con validación alineada al DTO backend (Bean Validation), sin lógica laboral en el cliente.
3. Nómina / FSR / costos: **revisión humana obligatoria** antes de cualquier UI generada.
4. Integración canónica FE: diferida a Task 13 Phase 3 (documentar solo lo construido).

## Referencias

- `docs/frontend/AI_SAFETY_GUIDE.md`
- `docs/frontend/VALIDATION_STRATEGY.md`
- `docs/canonical/modules/RRHH_MODULE_CANONICAL.md` (si existe en el árbol canónico)
