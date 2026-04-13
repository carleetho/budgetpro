# Estudios de gaps por módulo (programa v2)

> **Last Updated**: 2026-04-13  
> **Rama típica**: `feature/gaps-<slug>` (una por módulo o por ola).  
> **Regla**: PR **I1** (implementación) incluye código y canónicos/radiografía en el mismo PR. PR **G0** solo añade o actualiza estudios `.md` aquí.

## Artefactos relacionados

- **Tablero único de los 17 módulos** (%, tier, enlace al notebook): [SCOREBOARD_17.md](../SCOREBOARD_17.md)
- **Metodología** hallazgos abiertos/cerrados: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md)
- **Índice canónico ↔ código**: [MODULE_CODE_ALIGNMENT_INDEX.md](../MODULE_CODE_ALIGNMENT_INDEX.md)
- **Plantilla** para nuevos estudios: [_TEMPLATE.md](./_TEMPLATE.md)

## Cola ejecutable (hallazgos O-*)

Índice **code-first**: cada fila enlaza al gap study del módulo; el texto del hallazgo vive en [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (no duplicar aquí). Orden de módulos = orden ascendente de % en [SCOREBOARD_17.md](../SCOREBOARD_17.md).

| Módulo | O-* (en este repo) | Gap study |
| ------ | ------------------ | --------- |
| Producción | O-07, O-08 | [PRODUCCION_GAP_STUDY.md](./PRODUCCION_GAP_STUDY.md) |
| Marketing | O-01 (listado), O-09 | [MARKETING_GAP_STUDY.md](./MARKETING_GAP_STUDY.md) |
| Cronograma | O-10 | [CRONOGRAMA_GAP_STUDY.md](./CRONOGRAMA_GAP_STUDY.md) |
| Partidas | O-11 | [PARTIDAS_GAP_STUDY.md](./PARTIDAS_GAP_STUDY.md) |
| Almacén / inventario | O-12 | [INVENTARIO_GAP_STUDY.md](./INVENTARIO_GAP_STUDY.md) |
| Billetera | O-03, O-15 | [BILLETERA_GAP_STUDY.md](./BILLETERA_GAP_STUDY.md) |
| Recursos | O-01 (catálogo) | [RECURSOS_GAP_STUDY.md](./RECURSOS_GAP_STUDY.md) |
| Auditoría | O-16 | [AUDITORIA_GAP_STUDY.md](./AUDITORIA_GAP_STUDY.md) |
| Compras | O-01 (listado OC) | [COMPRAS_GAP_STUDY.md](./COMPRAS_GAP_STUDY.md) |
| Cross-Cutting | O-02 | [CROSS_CUTTING_GAP_STUDY.md](./CROSS_CUTTING_GAP_STUDY.md) |
| Transversal / datos | O-04 | [DATA_MODEL_CURRENT.md](../DATA_MODEL_CURRENT.md) (Flyway); cerrar en PR infra acotado |

Los módulos sin fila propia en esta tabla **no** tienen hoy un O-* dedicado en §3; nuevos hallazgos deben añadirse primero al review log y luego enlazarse aquí. **RRHH** (2026-04-13): sin O-* abiertos en §3; seguimiento en [RRHH_GAP_STUDY.md](./RRHH_GAP_STUDY.md) (GF-04 P0, GR-02).

## Convención de nombres

| Patrón | Uso |
|--------|-----|
| `<MODULO>_GAP_STUDY.md` | `MODULO` en MAYÚSCULAS corto (ej. `RRHH`, `COMPRAS`, `PRODUCCION`) |

## Orden de trabajo sugerido

Orden ascendente por **% oficial** en `SCOREBOARD_17.md` (empate: tier P0 antes que P3).

- **RRHH** (Ola 1, 2026-04-12): [RRHH_GAP_STUDY.md](./RRHH_GAP_STUDY.md)
- **Producción** (Ola 1 cadena, 2026-04-12): [PRODUCCION_GAP_STUDY.md](./PRODUCCION_GAP_STUDY.md)
- **Marketing** (Ola 1 cadena, 2026-04-12): [MARKETING_GAP_STUDY.md](./MARKETING_GAP_STUDY.md)
- **Cronograma** (Ola 1 cadena, 2026-04-12): [CRONOGRAMA_GAP_STUDY.md](./CRONOGRAMA_GAP_STUDY.md)
- **Partidas** (Ola 1 cadena, 2026-04-12): [PARTIDAS_GAP_STUDY.md](./PARTIDAS_GAP_STUDY.md)
- **Almacén / inventario** (Ola 1 cadena, 2026-04-12): [INVENTARIO_GAP_STUDY.md](./INVENTARIO_GAP_STUDY.md)
- **Billetera** (Ola 1b, 2026-04-12): [BILLETERA_GAP_STUDY.md](./BILLETERA_GAP_STUDY.md)
- **Recursos** (Ola 1b, 2026-04-12): [RECURSOS_GAP_STUDY.md](./RECURSOS_GAP_STUDY.md)
- **Auditoría** (Ola 1b, 2026-04-12): [AUDITORIA_GAP_STUDY.md](./AUDITORIA_GAP_STUDY.md)
- **Compras** (Ola 1b, 2026-04-12): [COMPRAS_GAP_STUDY.md](./COMPRAS_GAP_STUDY.md)
- **Estimación** (Ola 1b, 2026-04-12): [ESTIMACION_GAP_STUDY.md](./ESTIMACION_GAP_STUDY.md)
- **Seguridad** (Ola 1b, 2026-04-12): [SEGURIDAD_GAP_STUDY.md](./SEGURIDAD_GAP_STUDY.md)
- **Presupuesto** (Ola 1b, 2026-04-12): [PRESUPUESTO_GAP_STUDY.md](./PRESUPUESTO_GAP_STUDY.md)
- **APU** (Ola 1b, 2026-04-12): [APU_GAP_STUDY.md](./APU_GAP_STUDY.md)
- **Alertas** (Ola 1b, 2026-04-12): [ALERTAS_GAP_STUDY.md](./ALERTAS_GAP_STUDY.md)
- **Cross-Cutting** (Ola 1b, 2026-04-12): [CROSS_CUTTING_GAP_STUDY.md](./CROSS_CUTTING_GAP_STUDY.md)
- **EVM** (Ola 1b, 2026-04-12): [EVM_GAP_STUDY.md](./EVM_GAP_STUDY.md)

## Criterios de madurez (Ola 2)

### Madurez de producto vs cobertura de reglas

| Concepto | Qué mide | Qué **no** sube el % oficial |
| -------- | ---------- | ------------------------------ |
| **Madurez de producto** | Comportamiento verificable en código: REST, casos de uso, persistencia (Flyway), invariantes y reglas de negocio **demostradas** con rutas bajo `backend/` o tests. El **% oficial** es el del [SCOREBOARD_17.md](../SCOREBOARD_17.md), alineado a la cabecera `Status` del canónico del módulo. | — |
| **Cobertura documental / “reglas en papel”** | Exhaustividad del notebook, tablas de reglas en markdown o entradas en `docs/audits/current/CANONICAL_NOTEBOOKS_CHANGELOG.md`. | Sincronizar solo texto **sin** cambio de comportamiento en el backend **no** justifica subir el % del scoreboard. |

### Umbrales mínimos para revisar el número (sin inflar)

Los saltos son **acumulativos respecto al gap study vigente**: antes de pedir +10% debe cumplirse lo exigido para +5%.

| Salto | Condición mínima (DoD) | Evidencia esperada en PR **I1** |
| ----- | ------------------------ | -------------------------------- |
| **+5%** | Cierre de **todos los P0** del `*_GAP_STUDY.md` del módulo (o re-clasificación a P1/P2 con justificación **code-first** en el propio gap study). | Rutas concretas (`Controller`, use case, `db/migration`), o tests que ejecuten el comportamiento; cabecera `Status` del canónico y fila del scoreboard actualizadas **al mismo valor**; `CODE_DOC_REVIEW_LOG.md` actualizado si aplica (cerrar H-*, abrir/cerrar O-*). |
| **+10%** | Lo anterior **más** cierre de **todos los P1** abiertos en el gap study (o N/A documentado con cita a canónico/código). | Igual que +5%, más verificación explícita en el PR (comando de tests, captura de contrato REST, o nota de verificación manual **acotada**); si cambia el mensaje de la fila en `MODULE_SPECS_CURRENT.md`, ese archivo en el mismo PR. |

### Reglas operativas

- **G0** (solo gap study): puede documentar hallazgos sin mover el %.
- **I1**: código + canónico + radiografía afectada en un solo PR; el % no sube por anticipado en un G0.
- **Empates y doble verdad**: una convención — si divergen canónico y scoreboard, gana el **scoreboard** hasta que un I1 los reconcilie en el mismo commit.
