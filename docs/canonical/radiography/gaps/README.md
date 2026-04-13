# Estudios de gaps por módulo (programa v2)

> **Last Updated**: 2026-04-12  
> **Rama típica**: `feature/gaps-<slug>` (una por módulo o por ola).  
> **Regla**: PR **I1** (implementación) incluye código y canónicos/radiografía en el mismo PR. PR **G0** solo añade o actualiza estudios `.md` aquí.

## Artefactos relacionados

- **Tablero único de los 17 módulos** (%, tier, enlace al notebook): [SCOREBOARD_17.md](../SCOREBOARD_17.md)
- **Metodología** hallazgos abiertos/cerrados: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md)
- **Índice canónico ↔ código**: [MODULE_CODE_ALIGNMENT_INDEX.md](../MODULE_CODE_ALIGNMENT_INDEX.md)
- **Plantilla** para nuevos estudios: [_TEMPLATE.md](./_TEMPLATE.md)

## Convención de nombres

| Patrón | Uso |
|--------|-----|
| `<MODULO>_GAP_STUDY.md` | `MODULO` en MAYÚSCULAS corto (ej. `RRHH`, `COMPRAS`, `PRODUCCION`) |

## Orden de trabajo sugerido

Orden ascendente por **% oficial** en `SCOREBOARD_17.md` (empate: tier P0 antes que P3).

- **RRHH** (Ola 1, 2026-04-12): [RRHH_GAP_STUDY.md](./RRHH_GAP_STUDY.md)

## Criterios de madurez (Ola 2)

Los incrementos de % en cabeceras canónicas y en `MODULE_SPECS_CURRENT.md` deben ir acompañados de cierre verificable de ítems P0/P1 del gap study correspondiente y evidencia code-first (REST, migraciones o tests). No confundir **madurez de producto** con **cobertura documental de reglas** (`docs/audits/current/CANONICAL_NOTEBOOKS_CHANGELOG.md`).
