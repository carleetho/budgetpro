# PRESUPUESTO_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Presupuesto (crear, aprobar/congelar, control, explosión) |
| % oficial (tablero) | **80%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [PRESUPUESTO_MODULE_CANONICAL.md](../../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `PresupuestoController`, `SobrecostoController`, `LaboralController` |

## 2. Superficie de código (evidencia)

- **`PresupuestoController`** — `/api/v1/presupuestos`  
  - `POST /`, `GET /{id}`, `POST /{id}/aprobar` (204), `GET /{id}/control-costos`, `GET /{id}/explosion-insumos`  
- **`SobrecostoController`** — `PUT /api/v1/presupuestos/{id}/sobrecosto` (prefijo canónico §8).  
- **`LaboralController`** — `PUT /api/v1/configuracion-laboral` y `PUT /api/v1/proyectos/{id}/configuracion-laboral` (**superficie legacy paralela** a RRHH extendido en `/api/v1/rrhh/configuracion/**`).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Dos superficies de configuración laboral** | `LaboralController` + módulo RRHH — riesgo de documentación/cliente divergente (similar en espíritu a deuda Producción, menor criticidad si dominios distintos) | P2 |
| GF-02 | **Listados masivos de presupuestos** | No hay `GET /presupuestos` paginado por tenant/proyecto en `PresupuestoController` (solo por id) | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | Congelamiento e integridad: seguir canónico y `AprobarPresupuestoUseCase`; sin hallazgo nuevo |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Métricas §9 (`budget.*`) — sin evidencia Micrometer en escaneo rápido |
| DT-02 | APUs legacy — canónico §11 |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Deprecar o documentar “canónico cliente” para FSR: `LaboralController` vs `/api/v1/rrhh/...`.
2. **P3**: Índice/listado de presupuestos si el front lo requiere.

## 7. Definición de hecho para subir %

- **Hacia ~85%**: gobierno de una sola superficie FSR + índice paginado si aplica producto.

## 8. Referencias cruzadas

- [RRHH_GAP_STUDY.md](./RRHH_GAP_STUDY.md) (config RRHH), tablero [SCOREBOARD_17.md](../SCOREBOARD_17.md).
