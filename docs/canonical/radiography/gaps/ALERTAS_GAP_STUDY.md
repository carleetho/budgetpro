# ALERTAS_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Alertas (análisis paramétrico presupuesto) |
| % oficial (tablero) | **90%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [ALERTAS_MODULE_CANONICAL.md](../../modules/ALERTAS_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `AnalisisController`, `AnalizarPresupuestoUseCase` |

## 2. Superficie de código (evidencia)

- **`AnalisisController`** — `@RequestMapping("/api/v1/analisis")`  
  - `GET /alertas/{presupuestoId}` → `AnalizarPresupuestoUseCase.analizar` — respuesta **síncrona** `AnalisisPresupuestoResponse` (200).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Superficie mínima (solo GET)** | Sin `POST` re-ejecutar, sin listado de análisis históricos vía REST (canónico Apéndice A) | P2 |
| GF-02 | **Carga síncrona** | Análisis potencialmente pesado en request HTTP sin cola/async expuesta | P2 |
| GF-03 | **Ruta bajo `/analisis` vs nombre módulo “Alertas”** | Naming para clientes/OpenAPI — documentar prefijo canónico | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-026/027/028/116: motor paramétrico en dominio; sin brecha nueva en esta pasada |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Persistencia de resultados y consulta histórica (si aplica producto) |
| DT-02 | Timeouts / límites de tiempo en gateway para `GET` pesado |

## 6. Candidatos de cierre (priorizado)

1. **P2**: `POST /analisis/alertas/{presupuestoId}/ejecutar` async + job id, o caching con ETag.
2. **P2**: `GET` histórico paginado si se persiste `AnalisisPresupuesto`.

## 7. Definición de hecho para subir %

- **Hacia ~95%**: API async o histórico + SLAs documentados.
- **~90%** válido para motor on-demand síncrono.

## 8. Referencias cruzadas

- Tablero [SCOREBOARD_17.md](../SCOREBOARD_17.md).
