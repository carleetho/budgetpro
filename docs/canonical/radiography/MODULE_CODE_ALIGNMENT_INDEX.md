# MODULE_CODE_ALIGNMENT_INDEX.md — Catálogo canónico ↔ código

> **Scope**: Auditoría reverse-drift / cola de sincronización  
> **Last Updated**: 2026-04-12  
> **Authors**: Antigravity (code-first scan), BudgetPro (sync canónica)

## 1. Clasificación (prioridad de revisión)

| Tier | Criterio | Acción |
| ---- | -------- | ------ |
| **P0** | Alto churn financiero / bloqueante para otros módulos | Auditar primero: Presupuesto, Partidas, integraciones OC |
| **P1** | En `MODULE_SPECS_CURRENT` pero cabecera canónica **Last Updated ≤ 2026-01-31** | Re-leer dominio + REST vs §6–§8 del notebook |
| **P2** | Notebook existe pero **no tiene fila dedicada** en `MODULE_SPECS_CURRENT` | Añadir fila o apéndice en radiografía; evitar “módulo fantasma” |
| **P3** | Formato “notebook” heterogéneo (sin bloque Status unificado) | Normalizar cabecera cuando se toque el archivo |

## 2.0 Inventario técnico code-first (2026-04-12)

Referencias verificadas en `backend/.../infrastructure/rest` y migraciones `V27`–`V33`.

| Área | Superficie HTTP / persistencia |
| ---- | ------------------------------ |
| **APU** | `GET /api/v1/partidas/{partidaId}/apu`, `GET /api/v1/apu/{apuId}`; `POST` crear; `PUT .../rendimiento` |
| **Recursos** | `GET /api/v1/recursos`, `GET /api/v1/recursos/{id}`, `PUT /api/v1/recursos/{id}`, `POST` crear |
| **Billetera** | `POST /api/v1/billeteras/{id}/movimientos`; `GET .../{id}/saldo`, `GET .../{id}/movimientos` (`BilleteraQueryController`) |
| **Estimación** | `POST /proyectos/{id}/estimaciones`, `PUT /proyectos/estimaciones/{id}/aprobar`, `GET /proyectos/{id}/estimaciones`, `GET /proyectos/estimaciones/{id}` |
| **Partidas** | `GET /api/v1/partidas/{id}`, `GET /api/v1/partidas/wbs` (+ `POST` crear, avances, APU anidados) |
| **Almacén** | `POST /api/v1/almacen/movimientos`, `GET /api/v1/almacen/movimientos?almacenId=&recursoId=` |
| **Transferencias** | `POST /api/v1/transferencias/entre-bodegas`, `POST /api/v1/transferencias/entre-proyectos` |
| **Compras / OC** | `ProveedorController` CRUD bajo `/api/v1/proveedores`; `GET /ordenes-compra` con `page`/`size`; `POST /ordenes-compra/{id}/rechazar` |
| **Marketing (interno)** | `GET/POST` bajo `/api/v1/marketing/leads` (`MarketingLeadController`) además del lead público |
| **Flyway reciente** | `V27` almacén, `V28` estimación, `V29` producción, `V30` marketing lead, `V31` alertas, `V32` reajuste, `V33` cronograma operativo |
| **EVM observabilidad** | Contador Micrometer `evm.progress.registered.count` (`EvmMetrics` + `ValuacionCerradaEventListener`) |

## 2. Inventario completo (17 módulos)

| # | Módulo (canónico) | Dominio / paquete principal | API REST (indicativa) | Tier | Observación breve |
| --- | --- | --- | --- | --- | --- |
| 1 | `PRESUPUESTO_MODULE_CANONICAL.md` | `domain.finanzas.presupuesto`, `partida` (WBS) | `/api/v1/presupuestos` (+ control-costos, explosion-insumos, sobrecosto), `/api/v1/partidas` | P0/P1 | Sin cambio estructural en esta sync; Partidas ganó GET id + WBS en código |
| 2 | `PARTIDAS_MODULE_CANONICAL.md` | `domain.finanzas.partida` | `POST` partidas/avances/apu; `GET /partidas/{id}`, `GET /partidas/wbs` | P0 | Sync 2026-04-12: lectura parcial expuesta; sin listado por presupuesto |
| 3 | `APU_MODULE_CANONICAL.md` | `domain.finanzas.apu` | `POST .../apu`, `GET` por partida y por id, `PUT .../rendimiento` | P2 | Sync 2026-04-12: madurez ~90%; deuda bulk/OpenAPI |
| 4 | `RECURSOS_MODULE_CANONICAL.md` | `domain.finanzas.recurso`, `TipoRecurso` en `domain.shared` | `POST/GET/PUT /api/v1/recursos` | P2 | Sync 2026-04-12: catálogo lectura + actualización REST |
| 5 | `CRONOGRAMA_MODULE_CANONICAL.md` | `domain.finanzas.cronograma` | `/api/v1/proyectos/.../cronograma` | P1 | C-04 Working days alineado a `WorkingDayCalculator` en código |
| 6 | `ESTIMACION_MODULE_CANONICAL.md` | `domain.finanzas.estimacion` | `POST/GET .../estimaciones`, `PUT .../estimaciones/{id}/aprobar` | P1 | Sync 2026-04-12: `ConsultarEstimacionUseCase` expuesto REST |
| 7 | `BILLETERA_MODULE_CANONICAL.md` | `domain.finanzas` (`MovimientoCaja`), billetera app | `POST .../movimientos`; `GET .../saldo`, `GET .../movimientos` | P1 | Sync 2026-04-12: consultas vía `BilleteraQueryController`; EGRESO API 🟡 |
| 8 | `COMPRAS_MODULE_CANONICAL.md` | `domain.logistica.compra` | `/api/v1/compras`, `/api/v1/ordenes-compra`, `/api/v1/proveedores` | — | Sync 2026-04-12: proveedores CRUD, paginación OC, rechazo REST |
| 9 | `INVENTARIO_MODULE_CANONICAL.md` | `domain.logistica.inventario`, almacén, transferencia | inventario proyecto, almacén movimientos, `POST /transferencias/*` | P1 | Sync 2026-04-12: GET movimientos almacén; transferencias REST |
| 10 | `EVM_MODULE_CANONICAL.md` | `domain.finanzas.evm` | `/api/v1/evm` | — | Métrica `evm.progress.registered.count` implementada (listener) |
| 11 | `PRODUCCION_MODULE_CANONICAL.md` | `application.produccion`, JPA RPC | `POST/GET .../proyectos/{id}/produccion`, `/api/v1/produccion/reportes/*` | P2 | Dual controller; sin cambio en esta sync |
| 12 | `RRHH_MODULE_CANONICAL.md` | `domain.rrhh` | `/api/v1/rrhh/*` | P1 | Canónico ≈35% |
| 13 | `ALERTAS_MODULE_CANONICAL.md` | `domain.finanzas.alertas` | `GET /api/v1/analisis/alertas/{presupuestoId}` | P2 | Flyway `V31__create_alertas_schema.sql` en repo |
| 14 | `AUDITORIA_MODULE_CANONICAL.md` | `AuditEntity`, `IntegrityAuditLog` | transversal (sin REST auditoría) | P3 | Sin cambio en esta sync |
| 15 | `MARKETING_MODULE_CANONICAL.md` | `LeadService`, `LeadEntity` | `POST /api/public/v1/demo-request` + `/api/v1/marketing/leads` | P3 | Sync 2026-04-12: API interna de leads; DDL `V30` |
| 16 | `SEGURIDAD_MODULE_CANONICAL.md` | Spring Security, JWT | `POST .../login`, `POST .../register`, `GET .../me` | P3 | Sin cambio en esta sync |
| 17 | `CROSS_CUTTING_MODULE_CANONICAL.md` | hexagonal, reglas transversales | varios | P1 | `GlobalExceptionHandler` + `ErrorResponses` (cuerpo error unificado parcial) |

## 3. Revisiones aplicadas

### Pasada 2026-04-08 (histórico)

- **`MODULE_SPECS_CURRENT.md`**: fila RRHH alineada al canónico (~35%); enlace a este índice.
- **`CRONOGRAMA_MODULE_CANONICAL.md`**: matiz en C-04 vs `WorkingDayCalculator` (EVM).
- **`MATURITY_VISUALIZATION.md`**: ampliación parcial de matriz (Compras, ajuste RRHH/EVM nota).
- Radiografía módulos financieros y logística (detalle en historial git).

### Pasada 2026-04-12 (sync código ↔ canónicos)

- **`MODULE_CODE_ALIGNMENT_INDEX.md`**: §2.0 inventario code-first; tabla §2 actualizada; cierre cola 2026-04-08 obsoleta.
- **`MODULE_SPECS_CURRENT.md`**: madurez y gaps alineados a REST/migraciones actuales.
- **`DATA_MODEL_CURRENT.md`**: `V27`–`V33` documentados; notas de brecha DDL almacén/marketing/reajuste cerradas en repo.
- **Módulos**: `APU`, `RECURSOS`, `BILLETERA`, `ESTIMACION`, `PARTIDAS`, `INVENTARIO`, `COMPRAS`, `MARKETING`, `EVM` (secciones REST/deuda).
- **`docs/canonical/radiography/CODE_DOC_REVIEW_LOG.md`**: hallazgos revisión comparativa (ver §5).

## 4. Cola siguiente (orden sugerido)

1. Actualizar artefactos OpenAPI / Swagger donde rutas nuevas no estén publicadas.
2. **`MATURITY_VISUALIZATION.md`**: matriz por módulo vs porcentajes de `MODULE_SPECS_CURRENT`.
3. **Compras “Next”**: comparativo de precios, workflow avanzado (roadmap canónico).
4. **Producción**: unificar contrato dual `ProduccionController` vs `ReporteProduccionController`.

### 4.1. Seguimiento técnico

- Unificar versionado Flyway duplicado `V17__` o renombrar con cuidado en entornos ya migrados.
- Validar en BD desplegada que `V28`–`V33` aplican sin conflicto con esquemas creados manualmente en entornos legacy.

## 5. Revisión comparativa doc ↔ código

Ver registro explícito de hallazgos y estado: **[CODE_DOC_REVIEW_LOG.md](./CODE_DOC_REVIEW_LOG.md)**.
