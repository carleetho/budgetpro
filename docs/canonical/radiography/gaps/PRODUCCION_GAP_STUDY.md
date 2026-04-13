# PRODUCCION_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad en radiografía).  
> **Rama**: `feature/gaps-produccion-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Producción (RPC) |
| % oficial (tablero) | **55%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [PRODUCCION_MODULE_CANONICAL.md](../../modules/PRODUCCION_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `ProduccionController`, `ReporteProduccionController`, `ProduccionService` |

## 2. Superficie de código (evidencia)

- **Aplicación / dominio**: `com.budgetpro.application.produccion.service.ProduccionService`, validaciones en `ProduccionValidator` (referenciadas en canónico).
- **REST**:
  - `com.budgetpro.infrastructure.rest.controller.ProduccionController` — `@RequestMapping("/api/v1")`  
    - `POST /proyectos/{proyectoId}/produccion`, `GET /proyectos/{proyectoId}/produccion`, `GET /produccion/{id}`, `PATCH /produccion/{id}/aprobar`, `PATCH /produccion/{id}/rechazar`
  - `com.budgetpro.infrastructure.rest.produccion.controller.ReporteProduccionController` — `@RequestMapping("/api/v1/produccion/reportes")`  
    - `GET /`, `GET /{reporteId}`, `POST /`, `PUT /{reporteId}`, `DELETE /{reporteId}`, `POST /{reporteId}/aprobar`, `POST /{reporteId}/rechazar`
- **Persistencia**: `ReporteProduccionEntity`, `DetalleRPCEntity`; `ReporteProduccionJpaRepository`.
- **Migración**: `V29__create_produccion_schema.sql`.

## 3. Gaps funcionales (REST / contrato)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Dos superficies públicas** para el mismo servicio | Misma capa de aplicación (`ProduccionService`) detrás de rutas y verbos distintos; riesgo de clientes divergentes | P0 (deuda ya reconocida en canónico §1.1) |
| GF-02 | **Aprobar / rechazar: verbo y cuerpo** | Legacy: `PATCH .../aprobar|rechazar` + auditoría vía `AuditorAware<UUID>`; reportes: `POST .../aprobar|rechazar` + `aprobadorId` en body | P1 |
| GF-03 | **Actualizar y eliminar** | `PUT`/`DELETE` solo en `/api/v1/produccion/reportes/...`; el controlador bajo `/api/v1` no expone equivalente directo en las mismas rutas | P2 |
| GF-04 | **Listado sin `proyectoId`** (`ReporteProduccionController#listar`) | Si `proyectoId == null` → **200** con lista vacía (comentario en código) | P2 |
| GF-05 | **Filtrado en memoria** | `ReporteProduccionController#listar` carga `findByProyectoId` y aplica filtros/paginación con `subList` en JVM | P2 (escala; patrón similar a O-01 en OC/Marketing) |

## 4. Gaps de reglas / invariantes

| ID | Regla (canónico) | Notas |
|----|------------------|--------|
| GR-01 | REGLA-001 … REGLA-005 (muestra) | Canónico marca ✅ con evidencia en `ProduccionValidator`; sin hallazgo nuevo en esta pasada |
| GR-02 | Coherencia entre APIs | Riesgo operativo: cliente que use solo legacy no ve mismos DTOs/flows que `/reportes` — gobierno de producto, no violación de invariante aislada |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas |
|----|------|--------|
| DT-01 | Unificación de contrato | Objetivo: un solo prefijo + convención HTTP + OpenAPI único (canónico §1.1 **Deuda**) |
| DT-02 | OpenAPI / Swagger | Mantener una fuente generada alineada al contrato elegido post-unificación |
| DT-03 | Paginación en memoria | GF-05; evaluar query paginada o criterios en repositorio antes de volumen alto |

## 6. Candidatos de cierre (priorizado)

1. **P0 (I1)**: Decisión de producto: **deprecar** una superficie o documentar “canónico cliente = X” con fecha límite; plan de migración.
2. **P1**: Alinear verbos y semántica de aprobación (`PATCH` vs `POST`) o capa de adaptación interna.
3. **P2**: Sustituir 200+lista vacía por **400** + problema detallado cuando falte `proyectoId`, o documentar contrato explícitamente en OpenAPI.
4. **P2**: Reducir carga en memoria en listados (paginación en BD o límites documentados).

## 7. Definición de hecho para subir %

- **Hacia ~65–70%**: contrato **único** publicado + OpenAPI + al menos un flujo E2E (crear → aprobar) cubierto por test de contrato o integración en la ruta elegida.
- Sin unificación acordada, el % debe permanecer en el rango **Functional (~55%)** salvo cierre explícito de GF-01 en backlog.

## 8. Referencias cruzadas

- Tablero: [SCOREBOARD_17.md](../SCOREBOARD_17.md) (fila Producción).
- Hallazgos: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (O-07, O-08).
- Canónico §1.1 enlazado desde el mismo PR.

## 9. Ola Producción / Marketing — PRs incrementales (plan)

Secuencia sugerida para **no** big-bang en **GF-01** (dual API). Cada fila = un PR I1 o G0 acotado.

| # | PR | Entrega | Notas |
| --- | --- | --- | --- |
| P1 | `feature/i1-produccion-reportes-proyectoId` | **O-08 / GF-04**: exigir `proyectoId` (400 + problema) o contrato explícito documentado + test | No toca aún unificación de prefijos |
| P2 | `feature/i1-produccion-listado-bd` | Reducir **GF-05**: paginación o límites en repositorio en `ReporteProduccionController#listar` | Mitiga patrón O-01 |
| P3 | `feature/i1-produccion-aprobar-semantica` | **GF-02 / O-07** (parcial): alinear verbo/cuerpo entre `PATCH .../produccion` y `POST .../reportes/.../aprobar` vía adaptador o deprecación anunciada | Requiere decisión de producto |
| P4 | `feature/i1-produccion-contrato-unico` | **GF-01**: una superficie pública acordada + migración de clientes + OpenAPI único | PR grande pero **último** en la secuencia |

**Marketing** (**O-01** listado en memoria; **O-09** transición de estado): coordinar en ventana distinta o después de P2 si el mismo equipo toca paginación; ver [MARKETING_GAP_STUDY.md §9](./MARKETING_GAP_STUDY.md).
