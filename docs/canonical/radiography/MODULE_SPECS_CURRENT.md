# MODULE_SPECS_CURRENT.md — Current State Radiography

> **Scope**: Module Maturity  
> **Last Updated**: 2026-04-13  
> **Authors**: Ing. CL, Antigravity, BudgetPro (code-first sync)  
> **Scoreboard (17 notebooks, orden gaps)**: [SCOREBOARD_17.md](./SCOREBOARD_17.md) · **Programa gaps v2**: [gaps/README.md](./gaps/README.md)

## 1. Maturity Assessment

| Module               | Core Responsibility                    | Current Maturity     | Status                                                       |
| -------------------- | -------------------------------------- | -------------------- | ------------------------------------------------------------ |
| **1. Presupuesto**   | Financial Planning, WBS, Cost Analysis | **Completed (80%)**  | CRUD + aprobar→`CONGELADO`, hashes integridad; `GET .../control-costos`, `.../explosion-insumos`; sobrecosto y config laboral enlazados. Ver `PRESUPUESTO_MODULE_CANONICAL.md`. |
| **2. EVM**           | Physical progress tracking, Valuations | **Completed (95%)**  | CPI/SPI/EAC/ETC/VAC ✅, S-Curve ✅, forecast ✅, cierre E-04 ✅, serie temporal. Métrica `evm.progress.registered.count` ✅ (`EvmMetrics`). Pendiente menor: agregación dashboard. |
| **3. Cronograma**    | Time planning, Dependency management   | **Functional (60%)** | Critical Path y Gantt; C-04 días hábiles alineados a `WorkingDayCalculator`. |
| **4. Estimacion**    | Billing, Sequential approvals          | **Functional (75%)** | `POST .../estimaciones`, `PUT /api/v1/proyectos/estimaciones/{id}/aprobar`, `GET /api/v1/proyectos/{id}/estimaciones`, `GET /api/v1/proyectos/estimaciones/{id}`. Ver `ESTIMACION_MODULE_CANONICAL.md`. |
| **5. Compras**       | Procurement, Stock ingress             | **Functional (75%)** | OC + recepciones; **`ProveedorController`** CRUD; listado OC con paginación (`page`, `size`); `POST .../ordenes-compra/{id}/rechazar`. Ver `COMPRAS_MODULE_CANONICAL.md`. |
| **6. Billetera**     | Cash flow management                   | **Functional (70%)** | `POST .../movimientos`; **`GET .../{id}/saldo`** y **`GET .../{id}/movimientos`** (`BilleteraQueryController`). EGRESO genérico 🟡 vs dominio. Ver `BILLETERA_MODULE_CANONICAL.md`. |
| **7. RRHH**          | Labor management                       | **Functional (50%)** | Superficie `/api/v1/rrhh/**`; R-03 `RegimenCivilSolapeValidator`; FSR paralelo vía `LaboralController` (§8.1 canónico). `POST .../asignaciones` / `POST .../asistencias`. [RRHH_GAP_STUDY.md](./gaps/RRHH_GAP_STUDY.md). |
| **8. Inventario**    | Stock tracking                         | **Functional (70%)** | `GET .../inventario`, `POST/GET /api/v1/almacen/movimientos`; **`POST /api/v1/transferencias/entre-bodegas`** y **`/entre-proyectos`**. Ver `INVENTARIO_MODULE_CANONICAL.md`. |
| **9. APU**           | Unit cost breakdown por partida       | **Functional (90%)** | `POST .../apu`, `GET` por partida y por id, `PUT .../rendimiento`. Deuda: bulk/OpenAPI. Ver `APU_MODULE_CANONICAL.md`. |
| **10. Recursos**     | Catálogo maestro de insumos            | **Functional (70%)** | `POST/GET /api/v1/recursos`, `GET/PUT /api/v1/recursos/{id}`. Ver `RECURSOS_MODULE_CANONICAL.md`. |
| **11. Cross-Cutting** | Auth, Audit, Base Config               | **Completed (90%)**  | Hexagonal, Auth, Validation; errores REST parcialmente normalizados (`ErrorResponses`). Ver `CROSS_CUTTING_MODULE_CANONICAL.md`; [CROSS_CUTTING_GAP_STUDY.md](./gaps/CROSS_CUTTING_GAP_STUDY.md). |
| **12. Producción**   | Site production, RPC reporting         | **Functional (55%)** | `ProduccionController` (`/api/v1/.../produccion`) y `ReporteProduccionController` (`/api/v1/produccion/reportes`); migración `V29`. Deuda contrato dual. Ver `PRODUCCION_MODULE_CANONICAL.md`; [PRODUCCION_GAP_STUDY.md](./gaps/PRODUCCION_GAP_STUDY.md). |
| **13. Marketing**    | Leads, funnel demo                     | **Functional (55%)** | `POST /api/public/v1/demo-request`; `GET /api/v1/marketing/leads` (interno). Sin mutación REST de estados de lead (O-09). Ver `MARKETING_MODULE_CANONICAL.md`; [MARKETING_GAP_STUDY.md](./gaps/MARKETING_GAP_STUDY.md). |
| **14. Partidas**     | WBS items, vínculo a presupuesto       | **Functional (65%)** | `GET /partidas/{id}`, `GET /partidas/wbs`, creación/avances/APU anidados; sin listado paginado plano por presupuesto. Ver `PARTIDAS_MODULE_CANONICAL.md`; [PARTIDAS_GAP_STUDY.md](./gaps/PARTIDAS_GAP_STUDY.md). |
| **15. Auditoría**    | Trazabilidad de integridad             | **Functional (70%)** | `AuditEntity` / logs persistidos; sin API REST de lectura gobernada. Ver `AUDITORIA_MODULE_CANONICAL.md`; [AUDITORIA_GAP_STUDY.md](./gaps/AUDITORIA_GAP_STUDY.md). |
| **16. Seguridad**    | Autenticación, JWT, sesión             | **Functional (75%)** | Login, registro, perfil (`/me`). Ver `SEGURIDAD_MODULE_CANONICAL.md`; [SEGURIDAD_GAP_STUDY.md](./gaps/SEGURIDAD_GAP_STUDY.md). |
| **17. Alertas**      | Alertas de análisis de presupuesto     | **Functional (90%)** | `GET /api/v1/analisis/alertas/{presupuestoId}`; Flyway `V31`. Ver `ALERTAS_MODULE_CANONICAL.md`; [ALERTAS_GAP_STUDY.md](./gaps/ALERTAS_GAP_STUDY.md). |

## 2. Summary of Gaps

- **Producción (RPC):** coexisten `ProduccionController` y `ReporteProduccionController`; conviene unificar contrato (ver `PRODUCCION_MODULE_CANONICAL.md` §1.1).
- **RRHH**: Avance ~50%; R-03 cerrado en dominio (`RegimenCivilSolapeValidator`); seguir con nómina/costos y régimen civil detallado con evidencia code-first.
- **Partidas:** falta listado por `presupuestoId` en REST (hoy `GET` por id y `GET /wbs` según implementación).
- **Reporting:** analítica cross-módulo aún mínima.

## 3. Recommendation

- **Next Focus**: OpenAPI/Swagger al día con rutas 2026-04-12; roadmap Compras (comparativo precios); unificación Producción.
- **Tech Debt**: EGRESO billetera alineado a `Billetera.egresar`; estándar de payload de error 100% `ErrorResponses`.

## 4. Catálogo canónico ↔ código (clasificación)

Inventario de **17** notebooks en `docs/canonical/modules/`, mapeo a paquetes Java, REST y **tier** de auditoría:  
**[MODULE_CODE_ALIGNMENT_INDEX.md](./MODULE_CODE_ALIGNMENT_INDEX.md)**.

## 5. Criterios para subir el % (Ola 2)

El **% oficial por módulo** vive en **[SCOREBOARD_17.md](./SCOREBOARD_17.md)** y debe coincidir con la cabecera `Status` del canónico tras cada **I1**.

Los umbrales mínimos **+5%** y **+10%** (cierre P0 / P0+P1, evidencia code-first) y la separación entre **madurez de producto** y **cobertura documental de reglas** están detallados en **[gaps/README.md — Criterios de madurez (Ola 2)](./gaps/README.md#criterios-de-madurez-ola-2)**.
