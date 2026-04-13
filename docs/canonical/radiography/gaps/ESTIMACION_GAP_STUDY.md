# ESTIMACION_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Estimación (valuaciones / cobros) |
| % oficial (tablero) | **75%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [ESTIMACION_MODULE_CANONICAL.md](../../modules/ESTIMACION_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `EstimacionController`, casos de uso `Generar` / `Aprobar` / `Consultar` |

## 2. Superficie de código (evidencia)

- **`EstimacionController`** — `@RequestMapping("/api/v1/proyectos")`  
  - `POST /{proyectoId}/estimaciones` (201)  
  - `PUT /estimaciones/{estimacionId}/aprobar` (204) — **ruta bajo prefijo `/proyectos` sin repetir `{proyectoId}`** (canónico §8 ya lo advierte)  
  - `GET /{proyectoId}/estimaciones`  
  - `GET /estimaciones/{estimacionId}`  

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Listado sin paginación** | `listarPorProyecto` devuelve lista completa — canónico §11 | P2 |
| GF-02 | **UC-ES04 certificado de pago** | Canónico 🔴 — sin endpoint dedicado en esta pasada | P2 |
| GF-03 | **UC-ES03 como caso de uso separado** | Lógica en generación/aprobación; sin REST específico (canónico 🟡) | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-016 y máquina de estados estimación: seguir canónico §12; sin hallazgo nuevo |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Redondeo acumulado — canónico §11 |
| DT-02 | Evento post-aprobación si se desacopla reporting — canónico §11 |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Paginación/filtros en `GET .../estimaciones`.
2. **P2**: Spike UC-ES04 (PDF/JSON + almacenamiento) o mantener 🔴 explícito en roadmap.

## 7. Definición de hecho para subir %

- **Hacia ~80%**: GF-01 cerrado + avance en UC-ES04 documentado en OpenAPI.
- **~75%** mientras listado completo y certificado falten.

## 8. Referencias cruzadas

- Tablero: [SCOREBOARD_17.md](../SCOREBOARD_17.md).
