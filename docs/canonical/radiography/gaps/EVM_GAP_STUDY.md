# EVM_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 + ajuste menor canónico §9 (métrica).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | EVM (Earned Value) |
| % oficial (tablero) | **95%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [EVM_MODULE_CANONICAL.md](../../modules/EVM_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `EVMController`, `EvmMetrics`, `EVMCalculationService` |

## 2. Superficie de código (evidencia)

- **`EVMController`** — `/api/v1/evm`  
  - `GET /{proyectoId}` (snapshot; persiste vía servicio de cálculo)  
  - `GET /{proyectoId}/s-curve`, `GET /{proyectoId}/forecast`  
  - `POST /{proyectoId}/cerrar-periodo`  
- **Avance físico vinculado:** `POST /api/v1/partidas/{id}/avances` en `AvanceController` (citado en canónico §8).
- **Métricas:** `EvmMetrics` — contador `evm.progress.registered.count` (**implementado**; coherente con [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) **H-10**).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **GET snapshot con efecto persistencia** | `calcularYPersistir` en GET puede sorprender a clientes “solo lectura” — documentar semántica idempotente / caché | P2 |
| GF-02 | **Consultas pesadas** | Materialización parcialmente abordada (canónico §11); seguir vigilancia de performance | P2 |

## 4. Gaps de reglas / alineación doc

| ID | Regla / nota canónica | Evidencia código | Acción |
|----|------------------------|------------------|--------|
| GR-01 | §9 “`evm.progress.registered.count` (planned, not yet instrumented)” | `EvmMetrics.METRIC_EVM_PROGRESS_REGISTERED_COUNT` + `progressRegistered()` | Actualizar canónico §9 a **implementado** (sync con H-10) |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Cold-start / backfill — canónico §11 |
| DT-02 | Contract-first: documentar efecto lateral del GET en OpenAPI |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Renombrar o duplicar endpoint si se separa “calcular” vs “leer último snapshot”.
2. **P3**: Ajustar redacción canónica §9 (este PR).

## 7. Definición de hecho para subir %

- **95%+** con doc §9 alineada y semántica GET explícita en OpenAPI.

## 8. Referencias cruzadas

- [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §2 (**H-10**).
