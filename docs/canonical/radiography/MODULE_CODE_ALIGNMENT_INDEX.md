# MODULE_CODE_ALIGNMENT_INDEX.md — Catálogo canónico ↔ código

> **Scope**: Auditoría reverse-drift / cola de sincronización  
> **Last Updated**: 2026-04-08  
> **Authors**: Antigravity (code-first scan)

## 1. Clasificación (prioridad de revisión)

| Tier | Criterio | Acción |
| ---- | -------- | ------ |
| **P0** | Alto churn financiero / bloqueante para otros módulos | Auditar primero: Presupuesto, Partidas, integraciones OC |
| **P1** | En `MODULE_SPECS_CURRENT` pero cabecera canónica **Last Updated ≤ 2026-01-31** | Re-leer dominio + REST vs §6–§8 del notebook |
| **P2** | Notebook existe pero **no tiene fila dedicada** en `MODULE_SPECS_CURRENT` | Añadir fila o apéndice en radiografía; evitar “módulo fantasma” |
| **P3** | Formato “notebook” heterogéneo (sin bloque Status unificado) | Normalizar cabecera cuando se toque el archivo |

## 2. Inventario completo (17 módulos)

| # | Módulo (canónico) | Dominio / paquete principal | API REST (indicativa) | Tier | Observación breve |
| --- | --- | --- | --- | --- | --- |
| 1 | `PRESUPUESTO_MODULE_CANONICAL.md` | `domain.finanzas.presupuesto`, `partida` (WBS) | `/api/v1/presupuestos` (+ control-costos, explosion-insumos, sobrecosto), `/api/v1/partidas` | P0/P1 | Sync 2026-04-08: REST completo, eventos §3 ajustados a código, `CONGELADO` vs “aprobado” |
| 2 | `PARTIDAS_MODULE_CANONICAL.md` | `domain.finanzas.partida` | `POST /api/v1/partidas`, `POST .../avances`, `POST .../apu` (+ `PUT /api/v1/apu/.../rendimiento`) | P0 | Sync 2026-04-08: §3–§7 REST, UC, P-01, cruce REGLA con presupuesto; sin GET list/árbol |
| 3 | `APU_MODULE_CANONICAL.md` | `domain.finanzas.apu` | `POST .../partidas/{id}/apu`, `PUT /api/v1/apu/{id}/rendimiento` | P2 | Sync 2026-04-08: §4–§7 REST/UC; madurez 85%; en `MODULE_SPECS` fila 9 |
| 4 | `RECURSOS_MODULE_CANONICAL.md` | `domain.finanzas.recurso`, `TipoRecurso` en `domain.shared` | `POST /api/v1/recursos` | P2 | Sync 2026-04-08: cabecera + apéndice A REST; solo alta; fila 10 `MODULE_SPECS` |
| 5 | `CRONOGRAMA_MODULE_CANONICAL.md` | `domain.finanzas.cronograma` | `/api/v1/proyectos/.../cronograma` | P1 | C-04 Working days: ver nota en canónico (EVM tiene `WorkingDayCalculator`) |
| 6 | `ESTIMACION_MODULE_CANONICAL.md` | `domain.finanzas.estimacion` | `POST .../estimaciones`, `PUT .../proyectos/estimaciones/{id}/aprobar` | P1 | Sync 2026-04-08: ruta aprobar bajo `/proyectos`; evento §3 ajustado; sin GET list |
| 7 | `BILLETERA_MODULE_CANONICAL.md` | `domain.finanzas` (`MovimientoCaja`), billetera app | `POST /api/v1/billeteras/{id}/movimientos` | P1 | Sync 2026-04-08: corregido falso GET proyecto/billetera; deuda consulta saldo y EGRESO API |
| 8 | `COMPRAS_MODULE_CANONICAL.md` | `domain.logistica.compra` | `/api/v1/compras`, `/api/v1/ordenes-compra` | — | Sincronizado 2026-04-08 en rama de trabajo |
| 9 | `INVENTARIO_MODULE_CANONICAL.md` | `domain.logistica.inventario`, almacén | `GET .../inventario`, `POST /api/v1/almacen/movimientos` | P1 | Sync 2026-04-08: salida vía almacén; `TransferenciaService` sin REST |
|10 | `EVM_MODULE_CANONICAL.md` | `domain.finanzas.evm` | `/api/v1/evm` | — | Sincronizado (curva S, forecast, E-04) |
|11 | `PRODUCCION_MODULE_CANONICAL.md` | `application.produccion`, JPA RPC | `POST/GET .../proyectos/{id}/produccion`, `/api/v1/produccion/reportes/*` | P2 | Sync 2026-04-08: tabla dual `ProduccionController` + `ReporteProduccionController`; deuda unificar contrato |
|12 | `RRHH_MODULE_CANONICAL.md` | `domain.rrhh` | `/api/v1/rrhh/*` | P1 | Canónico ≈35%; **antes** `MODULE_SPECS` decía 20% — corregido |
|13 | `ALERTAS_MODULE_CANONICAL.md` | `domain.finanzas.alertas` | `GET /api/v1/analisis/alertas/{presupuestoId}` | P2 | Sync 2026-04-08: apéndice REST; `AnalizarPresupuestoUseCase` + `AnalizadorParametricoService` |
|14 | `AUDITORIA_MODULE_CANONICAL.md` | `AuditEntity`, `IntegrityAuditLog` | transversal (sin REST auditoría) | P3 | Sync 2026-04-08: apéndice capas; REGLA-169 proyecto 🟡 |
|15 | `MARKETING_MODULE_CANONICAL.md` | `LeadService`, `LeadEntity` | `POST /api/public/v1/demo-request` | P3 | Sync 2026-04-08: apéndice REST; madurez 40% |
|16 | `SEGURIDAD_MODULE_CANONICAL.md` | Spring Security, JWT | `POST .../login`, `POST .../register`, `GET .../me` | P3 | Sync 2026-04-08: apéndice A `AuthController` + `SecurityConfig` |
|17 | `CROSS_CUTTING_MODULE_CANONICAL.md` | hexagonal, reglas transversales | varios | P1 | Inventario REGLA masivo; cabecera 2026-01-31 |

## 3. Revisiones aplicadas en esta pasada (2026-04-08)

- **`MODULE_SPECS_CURRENT.md`**: fila RRHH alineada al canónico (~35%); enlace a este índice.
- **`CRONOGRAMA_MODULE_CANONICAL.md`**: matiz en C-04 vs `WorkingDayCalculator` (EVM).
- **`MATURITY_VISUALIZATION.md`**: ampliación parcial de matriz (Compras, ajuste RRHH/EVM nota).
- **`PRESUPUESTO_MODULE_CANONICAL.md`**: §3 eventos, §4 máquina `CONGELADO`/`INVALIDADO`, §5 datos/hashes/moneda en Proyecto, §6–§8 use cases y REST (control-costos, explosión, laboral), §10–§11 deuda eventos y Partidas GET.
- **`PARTIDAS_MODULE_CANONICAL.md`**: cabecera radiografía; REGLA-834/`CONGELADO`; tabla REST + APU; `CrearPartidaUseCase` + `FrozenBudgetException`; deuda sin listado GET; cruce REGLA ↔ presupuesto.
- **`ESTIMACION_MODULE_CANONICAL.md`**: cabecera; REST aprobar = `/api/v1/proyectos/estimaciones/{id}/aprobar`; §3 sin `EstimacionAprobadaEvent` Spring; §7 `GeneradorEstimacionService` + use cases; deuda GET.
- **`BILLETERA_MODULE_CANONICAL.md`**: solo `POST .../movimientos`; eliminado GET fantasma; UC-B03 sin REST; EGRESO API 🟡 + comentarios en `RegistrarMovimientoCajaUseCaseImpl`.
- **`INVENTARIO_MODULE_CANONICAL.md`**: 55%; UC-I03 🟡 por `AlmacenController`; UC-I04 🔴 sin REST pese a `TransferenciaService`; §7–§8 alineados a paquetes reales.
- **`APU_MODULE_CANONICAL.md`**: radiografía; madurez 85%; §4–§7 casos de uso, REST, integración partida/recurso, deuda GET/bulk.
- **`RECURSOS_MODULE_CANONICAL.md`**: radiografía; nota homónimo REGLA-037 vs presupuesto; apéndice A REST (`POST` único) y `RecursoControllerAdvice`.
- **`PRODUCCION_MODULE_CANONICAL.md`**: §1.1 tabla dual API; `ProduccionService`; DTOs legacy vs `produccion.dto`; deuda unificación.
- **`ALERTAS_MODULE_CANONICAL.md`**: cabecera; `GET .../analisis/alertas/{presupuestoId}`; apéndice A.
- **`MARKETING_MODULE_CANONICAL.md`**: cabecera 40%; `POST /demo-request`; apéndice A + seguridad pública.
- **`AUDITORIA_MODULE_CANONICAL.md`**: `AuditEntity` / `IntegrityAuditLog`; sin REST; REGLA-169 matizada.
- **`SEGURIDAD_MODULE_CANONICAL.md`**: apéndice `login` / `register` / `me`; punteros `SecurityConfig`.
- **`DATA_MODEL_CURRENT.md`**: EVM (`evm_snapshot`, `evm_time_series`), RRHH `V15`, recepciones `V21`/`V23`, billetera `V1_1`, integridad `presupuesto_integrity_audit`, reajuste/producción/marketing vía JPA; notas brecha DDL `almacen`/`movimiento_almacen`/reajuste/lead/análisis; doble `V17__`; transferencias.

## 4. Cola siguiente (orden sugerido)

1. ~~Presupuesto + Partidas (endpoints y congelamiento).~~ Hecho en esta pasada (2026-04-08).  
2. ~~Estimación + Billetera + Inventario.~~ Hecho (sync 2026-04-08).  
3. ~~APU + Recursos (entrada en `MODULE_SPECS` o apéndice).~~ Hecho (sync 2026-04-08).  
4. ~~Producción + Alertas + Marketing + Auditoría + Seguridad (verificación REST/DTO).~~ Hecho (sync 2026-04-08).  
5. ~~`DATA_MODEL_CURRENT.md`: entidades faltantes por módulo (reajuste, transferencias, RRHH, EVM ya parcial).~~ Hecho (sync 2026-04-08).

### 4.1. Seguimiento técnico (post–data model)

- Unificar versionado Flyway duplicado `V17__` o renombrar con cuidado en entornos ya migrados.  
- Añadir migraciones explícitas faltantes (`almacen`, `movimiento_almacen`, `estimacion_reajuste` / índices, `marketing_lead`, `analisis_presupuesto`) si la BD no las crea por otro canal.
