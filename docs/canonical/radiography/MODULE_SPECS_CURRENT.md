# MODULE_SPECS_CURRENT.md — Current State Radiography

> **Scope**: Module Maturity  
> **Last Updated**: 2026-04-08  
> **Authors**: Ing. CL, Antigravity

## 1. Maturity Assessment

| Module               | Core Responsibility                    | Current Maturity     | Status                                                       |
| -------------------- | -------------------------------------- | -------------------- | ------------------------------------------------------------ |
| **1. Presupuesto**   | Financial Planning, WBS, Cost Analysis | **Completed (80%)**  | CRUD + aprobar→`CONGELADO`, hashes integridad; `GET .../control-costos`, `.../explosion-insumos`; sobrecosto y config laboral enlazados. Ver `PRESUPUESTO_MODULE_CANONICAL.md` (sync 2026-04-08). |
| **2. EVM**           | Physical progress tracking, Valuations | **Completed (80%)**  | CPI/SPI/EAC/ETC/VAC ✅, S-Curve (UC-E04) ✅, forecast (UC-E05) ✅, cierre de período E-04 (CRON + manual) ✅, `evm_time_series` materializado. Pendiente: métrica `evm.progress.registered.count` y agregación dashboard (roadmap). |
| **3. Cronograma**    | Time planning, Dependency management   | **Functional (60%)** | Critical Path and Gantt visualization implemented.           |
| **4. Estimacion**    | Billing, Sequential approvals          | **Functional (60%)** | `POST .../estimaciones`, `PUT /api/v1/proyectos/estimaciones/{id}/aprobar` + billetera; sin GET listado. Ver `ESTIMACION_MODULE_CANONICAL.md` (sync 2026-04-08). |
| **5. Compras**       | Procurement, Stock ingress             | **Functional (60%)** | OC (`/api/v1/ordenes-compra`), catálogo `Proveedor`, workflow y eventos; compra directa + `POST /api/v1/compras/{id}/recepciones`. Pendiente: paginación, CRUD REST proveedores, comparativo/aprobaciones avanzadas (ver `COMPRAS_MODULE_CANONICAL.md`). |
| **6. Billetera**     | Cash flow management                   | **Functional (50%)** | `POST /api/v1/billeteras/{id}/movimientos` (PEN/USD/EUR). Sin GET saldo/movimientos en API; EGRESO genérico 🟡 vs dominio. Ver `BILLETERA_MODULE_CANONICAL.md` (sync 2026-04-08). |
| **7. RRHH**          | Labor management                       | **Partial (35%)**    | Alineado a `RRHH_MODULE_CANONICAL.md`: empleados, asistencias, cuadrillas, costos, nóminas/config; reglas de obra en evolución (no usar 20% “skeletal” como única verdad). |
| **8. Inventario**    | Stock tracking                         | **Functional (55%)** | `GET .../inventario` + `POST /api/v1/almacen/movimientos` (salidas con partida). Transferencias dominio sin REST. Ver `INVENTARIO_MODULE_CANONICAL.md` (sync 2026-04-08). |
| **9. APU**           | Unit cost breakdown por partida       | **Functional (85%)** | `POST /api/v1/partidas/{id}/apu`, `PUT /api/v1/apu/{snapshotId}/rendimiento`. Sin GET/list/bulk. Ver `APU_MODULE_CANONICAL.md` (sync 2026-04-08). |
| **10. Recursos**     | Catálogo maestro de insumos            | **Functional (45%)** | Solo `POST /api/v1/recursos`. Sin listado ni edición REST. Ver `RECURSOS_MODULE_CANONICAL.md` + apéndice A (sync 2026-04-08). |
| **11. Cross-Cutting** | Auth, Audit, Base Config               | **Completed (90%)**  | Strong foundation (Hexagonal, Auth, Validation).             |

## 2. Summary of Gaps

- **Producción (RPC):** coexisten `ProduccionController` (`/api/v1/proyectos/.../produccion`) y `ReporteProduccionController` (`/api/v1/produccion/reportes`); conviene unificar contrato (ver `PRODUCCION_MODULE_CANONICAL.md` §1.1, sync 2026-04-08).
- **RRHH**: Avance real ~35% según canónico; priorizar reglas R-03 y madurez 50% antes de generación ciega de lógica (ver advertencias en notebook).
- **Completeness**: Most modules handle the "Happy Path" well but lack edge case handling found in mature ERPs.
- **Reporting**: Advanced analytics (Cross-module) is minimal.

## 3. Recommendation

- **Next Focus**: Cerrar deuda Compras (paginación, API proveedores, endpoint rechazo OC) y subir hacia **80%** alineado al roadmap del módulo. **Catálogo:** `GET /recursos` y CRUD mínimo para destrabar APU/Inventario en UI. EVM epic (REQ-61/62/63/64) complete.
- **Tech Debt**: Standardize Error Handling across newer modules.

## 4. Catálogo canónico ↔ código (clasificación)

Inventario de **17** notebooks en `docs/canonical/modules/`, mapeo a paquetes Java, REST y **tier** de auditoría:  
**[MODULE_CODE_ALIGNMENT_INDEX.md](./MODULE_CODE_ALIGNMENT_INDEX.md)**.
