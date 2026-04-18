# PRESUPUESTO_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline


| Campo               | Valor                                                                            |
| ------------------- | -------------------------------------------------------------------------------- |
| Módulo              | Presupuesto (crear, aprobar/congelar, control, explosión)                        |
| % oficial (tablero) | **80%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md)                                |
| Notebook            | [PRESUPUESTO_MODULE_CANONICAL.md](../../modules/PRESUPUESTO_MODULE_CANONICAL.md) |
| Fecha revisión      | 2026-04-12 (baseline) · **2026-04-17** (post-implementación GF-01/GF-02)        |
| Autor / revisores   | Code-first: `PresupuestoController`, `SobrecostoController`, `LaboralController` |


## 2. Superficie de código (evidencia)

- `**PresupuestoController`** — `/api/v1/presupuestos`  
  - `POST /`, `GET /{id}`, `POST /{id}/aprobar` (204), `GET /{id}/control-costos`, `GET /{id}/explosion-insumos`  
  - `GET /` con query **`tenantId`**, **`proyectoId`**, `page`, `size` — listado paginado (validación tenant ↔ proyecto; columna `proyecto.tenant_id`).
- `**SobrecostoController**` — `PUT /api/v1/presupuestos/{id}/sobrecosto` (prefijo canónico §8).  
- `**LaboralController**` — `PUT /api/v1/configuracion-laboral` y `PUT /api/v1/proyectos/{id}/configuracion-laboral`: **mismo caso de uso y contrato extendido** que `PUT /api/v1/rrhh/configuracion/global` y `.../proyectos/{id}` (`ConfigurarLaboralExtendidaUseCase`). Persistencia única en configuración laboral extendida (RRHH). **Residual:** siguen existiendo **dos prefijos URL** por compatibilidad de clientes Presupuesto/sobrecosto vs perímetro `/api/v1/rrhh/...`.

## 3. Gaps funcionales (REST / producto)


| ID    | Tema                                         | Observado (código)                                                                                                                                              | Severidad |
| ----- | -------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- |
| GF-01 | **Dos superficies de configuración laboral** | **Cerrado (modelo único):** lectura/escritura FSR alineada a RRHH extendido; `CalcularSalarioRealService` vía `LaborFsRReaderPort` → config extendida. **Residual:** dos prefijos HTTP (`LaboralController` vs `/api/v1/rrhh/configuracion/**`) — mismo payload/UC; riesgo bajo de documentación y de descubrimiento de API. | P2 → **cerrado (datos/UC)**; residual rutas |
| GF-02 | **Listados masivos de presupuestos**         | **Cerrado:** `GET /api/v1/presupuestos?tenantId=&proyectoId=&page=&size=` (`ListarPresupuestosPaginadosUseCase`). | P3 → **cerrado** |


## 4. Gaps de reglas / invariantes


| ID    | Notas                                                                                         |
| ----- | --------------------------------------------------------------------------------------------- |
| GR-01 | Congelamiento e integridad: seguir canónico y `AprobarPresupuestoUseCase`; sin hallazgo nuevo |


## 5. Deuda técnica y riesgos


| ID    | Tema                                                                  |
| ----- | --------------------------------------------------------------------- |
| DT-01 | Métricas §9 (`budget.`*) — sin evidencia Micrometer en escaneo rápido |
| DT-02 | APUs legacy — canónico §11                                            |


## 6. Candidatos de cierre (priorizado)

1. ~~**P2**~~: Unificación **de datos y UC** FSR — **hecho** (2026-04-17). Opcional futuro: deprecar HTTP `LaboralController` o redirigir 308 a `/api/v1/rrhh/configuracion/**` si el producto acepta breaking únicamente en prefijo.
2. ~~**P3**~~: Índice paginado presupuestos — **hecho** (2026-04-17).

## 7. Definición de hecho para subir %

- **Hacia ~85%**: modelo FSR único + índice paginado — **cumplido en código** (2026-04-17). Pendiente para “madurez” del tablero: **DT-01** (Micrometer §9), **DT-02** (APUs legacy), y eventual **sincronización del notebook** `PRESUPUESTO_MODULE_CANONICAL.md` §8 (cuerpo extendido / listado) si se desea cero deriva doc↔código.

## 8. Referencias cruzadas

- [RRHH_GAP_STUDY.md](./RRHH_GAP_STUDY.md) (config RRHH), tablero [SCOREBOARD_17.md](../SCOREBOARD_17.md).

## 9. Gaps aún abiertos (resumen 2026-04-17)

| Ámbito | ID | Estado |
| ------ | -- | ------ |
| Reglas / invariantes | **GR-01** | Sin brecha nueva reportada; seguir validando congelamiento vs canónico en cada cambio. |
| Deuda técnica | **DT-01** | Métricas `budget.*` / Micrometer — abierto. |
| Deuda técnica | **DT-02** | APUs legacy — abierto (canónico §11). |
| Producto / API | **GF-01 residual** | Dos **rutas** REST para el mismo comportamiento; mitigado en **persistencia y UC**. |
| Multi-tenant | — | `tenant_id` en `proyecto` con default único; evolución: asignación explícita por tenant en creación/gestión de proyecto y políticas de autorización. |

