# MODULE_SPECS_CURRENT.md — Current State Radiography

> **Scope**: Module Maturity  
> **Last Updated**: 2026-04-12  
> **Authors**: Ing. CL, Antigravity, BudgetPro (code-first sync)

## 1. Maturity Assessment

| Module               | Core Responsibility                    | Current Maturity     | Status                                                       |
| -------------------- | -------------------------------------- | -------------------- | ------------------------------------------------------------ |
| **1. Presupuesto**   | Financial Planning, WBS, Cost Analysis | **Completed (80%)**  | CRUD + aprobar→`CONGELADO`, hashes integridad; `GET .../control-costos`, `.../explosion-insumos`; sobrecosto y config laboral enlazados. Ver `PRESUPUESTO_MODULE_CANONICAL.md`. |
| **2. EVM**           | Physical progress tracking, Valuations | **Completed (95%)**  | CPI/SPI/EAC/ETC/VAC ✅, S-Curve ✅, forecast ✅, cierre E-04 ✅, serie temporal. Métrica `evm.progress.registered.count` ✅ (`EvmMetrics`). Pendiente menor: agregación dashboard. |
| **3. Cronograma**    | Time planning, Dependency management   | **Functional (60%)** | Critical Path y Gantt; C-04 días hábiles alineados a `WorkingDayCalculator`. |
| **4. Estimacion**    | Billing, Sequential approvals          | **Functional (75%)** | `POST .../estimaciones`, `PUT /api/v1/proyectos/estimaciones/{id}/aprobar`, `GET /api/v1/proyectos/{id}/estimaciones`, `GET /api/v1/proyectos/estimaciones/{id}`. Ver `ESTIMACION_MODULE_CANONICAL.md`. |
| **5. Compras**       | Procurement, Stock ingress             | **Functional (75%)** | OC + recepciones; **`ProveedorController`** CRUD; listado OC con paginación (`page`, `size`); `POST .../ordenes-compra/{id}/rechazar`. Ver `COMPRAS_MODULE_CANONICAL.md`. |
| **6. Billetera**     | Cash flow management                   | **Functional (70%)** | `POST .../movimientos`; **`GET .../{id}/saldo`** y **`GET .../{id}/movimientos`** (`BilleteraQueryController`). EGRESO genérico 🟡 vs dominio. Ver `BILLETERA_MODULE_CANONICAL.md`. |
| **7. RRHH**          | Labor management                       | **Partial (35%)**    | Alineado a `RRHH_MODULE_CANONICAL.md`. |
| **8. Inventario**    | Stock tracking                         | **Functional (70%)** | `GET .../inventario`, `POST/GET /api/v1/almacen/movimientos`; **`POST /api/v1/transferencias/entre-bodegas`** y **`/entre-proyectos`**. Ver `INVENTARIO_MODULE_CANONICAL.md`. |
| **9. APU**           | Unit cost breakdown por partida       | **Functional (90%)** | `POST .../apu`, `GET` por partida y por id, `PUT .../rendimiento`. Deuda: bulk/OpenAPI. Ver `APU_MODULE_CANONICAL.md`. |
| **10. Recursos**     | Catálogo maestro de insumos            | **Functional (70%)** | `POST/GET /api/v1/recursos`, `GET/PUT /api/v1/recursos/{id}`. Ver `RECURSOS_MODULE_CANONICAL.md`. |
| **11. Cross-Cutting** | Auth, Audit, Base Config               | **Completed (90%)**  | Hexagonal, Auth, Validation; errores REST parcialmente normalizados (`ErrorResponses`). |

## 2. Summary of Gaps

- **Producción (RPC):** coexisten `ProduccionController` y `ReporteProduccionController`; conviene unificar contrato (ver `PRODUCCION_MODULE_CANONICAL.md` §1.1).
- **RRHH**: Avance ~35%; priorizar reglas R-03 antes de expansión ciega.
- **Partidas:** falta listado por `presupuestoId` en REST (hoy `GET` por id y `GET /wbs` según implementación).
- **Reporting:** analítica cross-módulo aún mínima.

## 3. Recommendation

- **Next Focus**: OpenAPI/Swagger al día con rutas 2026-04-12; roadmap Compras (comparativo precios); unificación Producción.
- **Tech Debt**: EGRESO billetera alineado a `Billetera.egresar`; estándar de payload de error 100% `ErrorResponses`.

## 4. Catálogo canónico ↔ código (clasificación)

Inventario de **17** notebooks en `docs/canonical/modules/`, mapeo a paquetes Java, REST y **tier** de auditoría:  
**[MODULE_CODE_ALIGNMENT_INDEX.md](./MODULE_CODE_ALIGNMENT_INDEX.md)**.
