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
| Fecha revisión      | 2026-04-12 (baseline) · **2026-04-17** (GF-01/GF-02 + sync canónico §8 / `ARQUITECTURA_VISUAL` / README, plan estratégico Ola 1) |
| Autor / revisores   | Code-first: `PresupuestoController`, `SobrecostoController`, `LaboralController` |


## 2. Superficie de código (evidencia)

- `**PresupuestoController`** — `/api/v1/presupuestos`  
  - `POST /`, `GET /{id}`, `POST /{id}/aprobar` (204), `GET /{id}/control-costos`, `GET /{id}/explosion-insumos`  
  - `GET /` con query **`tenantId`**, **`proyectoId`**, `page`, `size` — listado paginado (validación tenant ↔ proyecto; columna `proyecto.tenant_id`).
- `**SobrecostoController**` — `PUT /api/v1/presupuestos/{id}/sobrecosto` (prefijo canónico §8).  
- `**LaboralController**` — `PUT /api/v1/configuracion-laboral` y `PUT /api/v1/proyectos/{id}/configuracion-laboral`: **mismo caso de uso y contrato extendido** que `PUT /api/v1/rrhh/configuracion/global` y `.../proyectos/{id}` (`ConfigurarLaboralExtendidaUseCase`). Persistencia única en configuración laboral extendida (RRHH). **Residual:** siguen existiendo **dos prefijos URL** por compatibilidad de clientes Presupuesto/sobrecosto vs perímetro `/api/v1/rrhh/...`.
- `**ConfiguracionLaboralExtendidaController**` — `PUT /api/v1/rrhh/configuracion/global`, `PUT /api/v1/rrhh/configuracion/proyectos/{id}`, `GET /api/v1/rrhh/configuracion/proyectos/{id}/historial` (mismo modelo extendido; historial solo en prefijo RRHH).

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

- **Hacia ~85%**: modelo FSR único + índice paginado — **cumplido en código** (2026-04-17). Pendiente para “madurez” del tablero: **DT-01** (Micrometer §9), **DT-02** (APUs legacy). **Sincronización notebook §8 + `ARQUITECTURA_VISUAL` + README** — **hecha** (2026-04-17, Ola 1 plan estratégico).

## 8. Referencias cruzadas

- [RRHH_GAP_STUDY.md](./RRHH_GAP_STUDY.md) (config RRHH), tablero [SCOREBOARD_17.md](../SCOREBOARD_17.md).
- Backlog producto Ola 2 (REQ semilla / workshop): [PRESUPUESTO_PRODUCT_BACKLOG_OLA2.md](./PRESUPUESTO_PRODUCT_BACKLOG_OLA2.md).

## 9. Gaps aún abiertos (resumen 2026-04-17)

| Ámbito | ID | Estado |
| ------ | -- | ------ |
| Reglas / invariantes | **GR-01** | Sin brecha nueva reportada; seguir validando congelamiento vs canónico en cada cambio. |
| Deuda técnica | **DT-01** | Métricas `budget.*` / Micrometer — abierto. |
| Deuda técnica | **DT-02** | APUs legacy — abierto (canónico §11). |
| Producto / API | **GF-01 residual** | Dos **rutas** REST para el mismo comportamiento; mitigado en **persistencia y UC**. |
| Multi-tenant | — | `tenant_id` en `proyecto` con default único; evolución: asignación explícita por tenant en creación/gestión de proyecto y políticas de autorización. |

## 10. Acotación fase Next (plan estratégico — sin inventar gráficos)

**Fuente roadmap:** [PRESUPUESTO_MODULE_CANONICAL.md §1](../../modules/PRESUPUESTO_MODULE_CANONICAL.md).

| Entregable §1 Next | Acotación mínima antes de Modo B | Responsable |
| ------------------ | -------------------------------- | ------------- |
| **UC-P09 Excel** | Formato (xlsx), hojas mínimas (cabecera presupuesto, WBS, totales), volumen máximo de filas, encoding; endpoint y auth alineados a políticas existentes del API. | Finanzas + backend |
| **Export PDF** | No constituye UC numerado hasta acordar: plantilla corporativa, páginas obligatorias, si incluye firmas/datos de proyecto; puede ser **misma operación** que Excel (pack descargable) o **servicio aparte** — decisión explícita. | Finanzas |
| **Advanced analytics** | Definir **cortes** (ej. por capítulo WBS, por tipo de recurso, CD vs PV, rango temporal) y **KPI** (lista cerrada). **No** asumir tipos de gráfico (barras/líneas/etc.) en código hasta constar en notebook o REQ firmado. | Finanzas |

**Checklist de workshop (entrada a Ola 2):** ver [PRESUPUESTO_PRODUCT_BACKLOG_OLA2.md](./PRESUPUESTO_PRODUCT_BACKLOG_OLA2.md).

## 11. Ola 4 — Criterios medibles de cierre (deuda dura)

| ID | Criterio de cierre (DoD sugerido) | Evidencia |
| -- | --------------------------------- | --------- |
| **DT-01** | Contadores Micrometer (o equivalente) publicados con los nombres del canónico §9 (`budget.created.count`, `budget.value.total`) + test de regresión o smoke que afirme registro en arranque de caso feliz. | Build + test |
| **DT-02** | Inventario en código de ramas “APU legacy”; cada rama con test de caracterización o eliminación; nota en canónico §11 actualizada (ratio legacy vs snapshot). | PR + gap study |
| **Target versioning v2** | Documento de diseño (agregado, migraciones, compatibilidad con `CONGELADO`) aprobado por Finanzas; ningún merge a `main` sin ADR o sección en notebook Target. | ADR / canónico (si se autoriza) |
| **Target multi-moneda** | Decisión explícita: moneda solo en `Proyecto` (hoy REGLA-071) vs campo en presupuesto; impacto en snapshots y reportes; sin implementación hasta cierre diseño. | ADR |
| **GF-01 residual (opcional)** | Deprecación documentada: `Sunset` header OpenAPI o redirección 308 de `LaboralController` → `/api/v1/rrhh/configuracion/**` **solo** si producto acepta breaking de prefijo; clientes migrados. | Changelog + tests contrato |

