# CODE_DOC_REVIEW_LOG.md — Revisión comparativa código ↔ canónicos

> **Scope**: Hallazgos de alineación (post-sync documental)  
> **Last Updated**: 2026-04-13  
> **Authors**: BudgetPro (code-first + checklists)

## 1. Metodología

- Inventario REST desde `*Controller.java` bajo `infrastructure/rest`.
- Contraste con `docs/canonical/modules/*_MODULE_CANONICAL.md` y radiografía (`MODULE_SPECS_CURRENT`, `DATA_MODEL_CURRENT`, `MODULE_CODE_ALIGNMENT_INDEX`).
- Referencias: `docs/canonical/REVIEW_CHECKLIST.md`, `docs/canonical/AI_VALIDATION_CHECKLIST.md` (aplicación manual en esta pasada).
- Programa de gaps por módulo (plantilla, orden de trabajo): [`gaps/README.md`](./gaps/README.md) · tablero de madurez: [`SCOREBOARD_17.md`](./SCOREBOARD_17.md).

## 2. Hallazgos corregidos (doc desfasada)

| ID | Antes (doc) | Después (código + doc) |
| --- | --- | --- |
| H-01 | APU “sin GET” | `GET /partidas/{id}/apu`, `GET /apu/{apuId}` documentados |
| H-02 | Recursos solo `POST` | `GET` listado/detalle, `PUT` actualización |
| H-03 | Billetera sin consulta REST | `BilleteraQueryController`: saldo + movimientos |
| H-04 | Estimación sin `GET` | `ConsultarEstimacionUseCase` expuesto |
| H-05 | Partidas sin lectura REST | `GET /partidas/{id}`, `GET /partidas/wbs` |
| H-06 | Inventario: transferencias sin REST | `TransferenciaController` + GET movimientos almacén |
| H-07 | Compras: sin API proveedor / paginación / rechazo | `ProveedorController`, `page`/`size`, `POST .../rechazar` |
| H-08 | `DATA_MODEL`: almacén/marketing/reajuste “sin DDL” | `V27`, `V30`, `V32` referenciados |
| H-09 | Marketing: migración `V18` citada para leads | Corregido a `V30__create_marketing_lead.sql` |
| H-10 | EVM: métrica `evm.progress.registered.count` pendiente | Implementada (`EvmMetrics`) |
| H-11 | RRHH: `GET .../asistencias` sin `empleadoId` ni `proyectoId` devolvía 200 vacío | **400** + `ErrorResponses` (`MISSING_ATTENDANCE_FILTERS`); `FiltrosConsultaAsistenciaIncompletosException` + test `AsistenciaControllerTest` (2026-04-13) |
| H-12 | RRHH: asignación empleado ↔ proyecto sin REST | `POST /api/v1/rrhh/empleados/{empleadoId}/asignaciones` + `AsignarEmpleadoProyectoRequest`; conflicto solape → **409** `ASIGNACION_PROYECTO_CONFLICTO`; `EmpleadoControllerTest` (2026-04-13) |
| H-13 | RRHH GF-01: doc vs rutas `configuracion-laboral` / mito `/personal` | `RRHH_MODULE_CANONICAL.md` §8.1 documenta `LaboralController` paralelo y ausencia de `/api/v1/personal` (2026-04-13) |

## 3. Hallazgos abiertos (código o contrato a vigilar)

**Cola por módulo** (solo IDs + enlaces a gap studies): [gaps/README.md — Cola ejecutable](./gaps/README.md#cola-ejecutable-hallazgos-o-).

| ID | Tema | Notas |
| --- | --- | --- |
| O-01 | **Paginación en memoria / catálogo completo** | `OrdenCompraController`, `MarketingLeadController` usan `findAll` + `subList`; `ObtenerRecursoUseCaseImpl.listar` usa `recursoRepository.findAll()` sin paginación en `GET /api/v1/recursos` — riesgo de rendimiento. |
| O-02 | **`ProyectoNotFoundException`** | Respuesta distinta a `ErrorResponses` en `GlobalExceptionHandler` (deuda cross-cutting). |
| O-03 | **EGRESO billetera** | API acepta `EGRESO`; use case puede invocar dominio con parámetros incompletos — seguir deuda en `BILLETERA_MODULE_CANONICAL.md`. |
| O-04 | **Flyway `V17__` duplicado** | Conviven dos scripts; riesgo de orden/ambiente — ver `DATA_MODEL_CURRENT.md` §2. |
| O-07 | **Producción: contratos REST duales** | `ProduccionController` bajo `/api/v1` vs `ReporteProduccionController` bajo `/api/v1/produccion/reportes`; PATCH vs POST en aprobar/rechazar; IDs desde auditor vs body; listado `/reportes` filtra/pagina en memoria tras `findByProyectoId` — [gaps/PRODUCCION_GAP_STUDY.md](./gaps/PRODUCCION_GAP_STUDY.md) (GF-01, GF-02, GF-05). |
| O-08 | **Producción: `GET /produccion/reportes` sin proyectoId** | Responde 200 con lista vacía si falta `proyectoId` — [gaps/PRODUCCION_GAP_STUDY.md](./gaps/PRODUCCION_GAP_STUDY.md) (GF-04). |
| O-09 | **Marketing: sin API autenticada para transición de estado de lead** | Solo creación pública (`NUEVO`) y `GET` internos; REGLA-108 sin mutación REST — [gaps/MARKETING_GAP_STUDY.md](./gaps/MARKETING_GAP_STUDY.md) (GF-01). |
| O-10 | **Cronograma: `POST .../cronograma/baseline` en canónico §8 sin REST** | `CronogramaController` solo expone `POST .../actividades` y `GET .../cronograma`; sin mapping `.../baseline` en controladores auditados — [gaps/CRONOGRAMA_GAP_STUDY.md](./gaps/CRONOGRAMA_GAP_STUDY.md) (GF-01). |
| O-11 | **Partidas: sin listado REST plano por presupuesto** | Solo `GET /partidas/{id}`, `GET /partidas/wbs?presupuestoId=` y creación; sin `GET /partidas?presupuestoId=` paginado — [gaps/PARTIDAS_GAP_STUDY.md](./gaps/PARTIDAS_GAP_STUDY.md) (GF-01). |
| O-12 | **Inventario: observabilidad §9 y evento `InventarioBajoAlert` sin evidencia en código** | Canónico lista métricas/logs y evento dominio 🔴; `grep` en backend sin `InventarioBajoAlert` / `inventory.value.total`; Micrometer dedicado a otros módulos (`EvmMetrics`, `CatalogMetrics`, `IntegrityMetrics`) — [gaps/INVENTARIO_GAP_STUDY.md](./gaps/INVENTARIO_GAP_STUDY.md) (DT-02, DT-03). |
| O-15 | **Billetera: consulta REST acoplada a JPA** | `BilleteraQueryController` usa `BilleteraJpaRepository` y entidades `*Entity` en el adaptador REST en lugar de un puerto de aplicación dedicado para saldo/listado — [gaps/BILLETERA_GAP_STUDY.md](./gaps/BILLETERA_GAP_STUDY.md) (DT-01). |
| O-16 | **Auditoría: sin API REST de consulta** | Trazabilidad vía `AuditEntity` / logs; ningún `*AuditController` para lectura gobernada — [gaps/AUDITORIA_GAP_STUDY.md](./gaps/AUDITORIA_GAP_STUDY.md) (GF-01, DT-01). |

## 4. REGLA-* (muestra)

Muestreo `grep REGLA-` en `backend/src/main/java` vs menciones en canónicos: sin divergencia nueva detectada en esta pasada para IDs citados en módulos tocados (Compras REGLA-153, Partidas congeladas, etc.). **Seguimiento:** repetir grep focalizado antes de cada release mayor.

## 5. Estado checklists

| Checklist | Resultado |
| --- | --- |
| `REVIEW_CHECKLIST.md` | Aplicado en revisión de consistencia rutas HTTP / códigos / nombres de use case |
| `AI_VALIDATION_CHECKLIST.md` | Aplicado: verificación de drift inverso (doc adelantada vs código) corregida en los ítems H-* |
