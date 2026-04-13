# SCOREBOARD_17 — Madurez única (17 notebooks)

> **Scope**: Una sola tabla para ordenar el programa de gaps (bajo → alto %).  
> **Last Updated**: 2026-04-12  
> **Fuente del %**: cabecera `**Status**` de cada [`*_MODULE_CANONICAL.md`](../modules/); reconciliado con [`MODULE_SPECS_CURRENT.md`](./MODULE_SPECS_CURRENT.md) donde exista fila.  
> **Tier**: [`MODULE_CODE_ALIGNMENT_INDEX.md`](./MODULE_CODE_ALIGNMENT_INDEX.md) §2 (valor `—` = no asignado en índice).

## Tabla (orden de trabajo = orden de filas)

| Orden | Módulo | % | Nivel (resumen) | Tier | Notebook | Gap study |
| ----- | ------ | --- | ----------------- | ---- | -------- | --------- |
| 1 | RRHH | 35 | Partial | P1 | [RRHH_MODULE_CANONICAL.md](../modules/RRHH_MODULE_CANONICAL.md) | [RRHH_GAP_STUDY.md](./gaps/RRHH_GAP_STUDY.md) |
| 2 | Producción | 55 | Functional | P2 | [PRODUCCION_MODULE_CANONICAL.md](../modules/PRODUCCION_MODULE_CANONICAL.md) | [PRODUCCION_GAP_STUDY.md](./gaps/PRODUCCION_GAP_STUDY.md) |
| 3 | Marketing | 55 | Functional | P3 | [MARKETING_MODULE_CANONICAL.md](../modules/MARKETING_MODULE_CANONICAL.md) | pendiente |
| 4 | Cronograma | 60 | Functional | P1 | [CRONOGRAMA_MODULE_CANONICAL.md](../modules/CRONOGRAMA_MODULE_CANONICAL.md) | pendiente |
| 5 | Partidas | 65 | Functional | P0 | [PARTIDAS_MODULE_CANONICAL.md](../modules/PARTIDAS_MODULE_CANONICAL.md) | pendiente |
| 6 | Almacén / inventario | 70 | Functional | P1 | [INVENTARIO_MODULE_CANONICAL.md](../modules/INVENTARIO_MODULE_CANONICAL.md) | pendiente |
| 7 | Billetera | 70 | Functional | P1 | [BILLETERA_MODULE_CANONICAL.md](../modules/BILLETERA_MODULE_CANONICAL.md) | pendiente |
| 8 | Recursos | 70 | Functional | P2 | [RECURSOS_MODULE_CANONICAL.md](../modules/RECURSOS_MODULE_CANONICAL.md) | pendiente |
| 9 | Auditoría | 70 | Functional | P3 | [AUDITORIA_MODULE_CANONICAL.md](../modules/AUDITORIA_MODULE_CANONICAL.md) | pendiente |
| 10 | Compras | 75 | Functional | — | [COMPRAS_MODULE_CANONICAL.md](../modules/COMPRAS_MODULE_CANONICAL.md) | pendiente |
| 11 | Estimación | 75 | Functional | P1 | [ESTIMACION_MODULE_CANONICAL.md](../modules/ESTIMACION_MODULE_CANONICAL.md) | pendiente |
| 12 | Seguridad | 75 | Functional | P3 | [SEGURIDAD_MODULE_CANONICAL.md](../modules/SEGURIDAD_MODULE_CANONICAL.md) | pendiente |
| 13 | Presupuesto | 80 | Complete | P0/P1 | [PRESUPUESTO_MODULE_CANONICAL.md](../modules/PRESUPUESTO_MODULE_CANONICAL.md) | pendiente |
| 14 | APU | 90 | Functional | P2 | [APU_MODULE_CANONICAL.md](../modules/APU_MODULE_CANONICAL.md) | pendiente |
| 15 | Alertas | 90 | Functional | P2 | [ALERTAS_MODULE_CANONICAL.md](../modules/ALERTAS_MODULE_CANONICAL.md) | pendiente |
| 16 | Cross-Cutting | 90 | Completed | P1 | [CROSS_CUTTING_MODULE_CANONICAL.md](../modules/CROSS_CUTTING_MODULE_CANONICAL.md) | pendiente |
| 17 | EVM | 95 | Complete | — | [EVM_MODULE_CANONICAL.md](../modules/EVM_MODULE_CANONICAL.md) | pendiente |

## Notas

- **Empates (55%, 70%, 75%, 90%)**: desempate por tier (menor número de P = más urgencia de auditoría), luego nombre del módulo.
- **Partidas**: el dígito **65%** alinea la cabecera del canónico con este tablero (Ola 0); el gap study validará con code-first.
- **Inventario**: fila etiquetada “Almacén / inventario” para coincidir con el notebook `INVENTARIO_MODULE_CANONICAL.md` del índice de 17.
- **Estudios**: archivos bajo [`gaps/`](./gaps/README.md); enlazar desde la columna *Gap study* cuando exista `*_GAP_STUDY.md`.

## Enlaces

- Radiografía resumida: [MODULE_SPECS_CURRENT.md](./MODULE_SPECS_CURRENT.md)
- Matriz visual (subconjunto): [MATURITY_VISUALIZATION.md](../MATURITY_VISUALIZATION.md)
