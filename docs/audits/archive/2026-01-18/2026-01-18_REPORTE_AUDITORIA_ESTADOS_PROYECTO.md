# Reporte de Auditoría — ESTADOS DE PROYECTO

Fecha: 2026-01-18  
Alcance: `docs/modules/PROYECTO_SPECS.md`, migraciones SQL, entidades Proyecto.

## Estados reales (código y BD)
- **Código (dominio)**: `BORRADOR`, `PAUSADO`, `EJECUCION`, `FINALIZADO`.
- **Migración SQL (V2)**: `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO` (CHECK en tabla `proyecto`).

## Estados documentados (PROYECTO_SPECS)
- `BORRADOR`, `ACTIVO`, `SUSPENDIDO`, `CERRADO`.

## Riesgos de ejecución
- **Desalineación crítica de estados**: el enum del dominio no coincide con los estados documentados ni con el CHECK de base de datos.
- **Activación sin condiciones**: no existe validación de Presupuesto/Cronograma CONGELADO en el agregado ni en casos de uso.
- **Bloqueos operativos incompletos**: solo se valida estado `EJECUCION` para avance de producción; no hay bloqueos explícitos para compras, inventarios, RRHH u otros módulos en el código revisado.
- **Dependencia de Línea Base no aplicada**: no se observan vínculos explícitos con Presupuesto/Cronograma en el modelo de Proyecto.

