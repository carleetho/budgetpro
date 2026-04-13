# RECURSOS_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Recursos (catálogo maestro) |
| % oficial (tablero) | **70%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [RECURSOS_MODULE_CANONICAL.md](../../modules/RECURSOS_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `RecursoController`, `ObtenerRecursoUseCaseImpl` |

## 2. Superficie de código (evidencia)

- **REST:** `RecursoController` — `@RequestMapping("/api/v1/recursos")`  
  - `POST /` (201 + `Location`)  
  - `GET /{id}`  
  - `GET /` listado  
  - `PUT /{id}`  
- **Aplicación:** `ObtenerRecursoUseCaseImpl.listar()` → `recursoRepository.findAll()` sin filtros ni paginación (canónico Apéndice A ya lo declara).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Catálogo completo en un GET** | `findAll()` mapeado a lista; riesgo de tamaño de respuesta al crecer el catálogo | P2 (ampliado en **O-01**) |
| GF-02 | **Sin DELETE REST** | Canónico y código: no hay endpoint de baja lógica/física vía HTTP | P2 (puede ser intencional; deprecación vía `PUT` estado) |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-037 / 039 / 112 / 116 / 128: coherentes con dominio y `RecursoControllerAdvice`; sin hallazgo nuevo |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Filtros por tipo/estado y paginación server-side para `GET /recursos` |
| DT-02 | OpenAPI: parámetros de búsqueda cuando existan |

## 6. Candidatos de cierre (priorizado)

1. **P2**: `page`/`size` o cursor + índices en tabla de recursos.
2. **P2**: `DELETE` o `PATCH` estado `DEPRECADO` explícito en contrato público.

## 7. Definición de hecho para subir %

- **Hacia ~75%**: listado paginado o filtrado + contrato estable documentado.
- **~70%** razonable con catálogo acotado y `findAll` aceptable operativamente.

## 8. Referencias cruzadas

- [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-01** incluye listado recursos).
