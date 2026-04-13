# CODE_DOC_REVIEW_LOG.md — Revisión comparativa código ↔ canónicos

> **Scope**: Hallazgos de alineación (post-sync documental)  
> **Last Updated**: 2026-04-12  
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

## 3. Hallazgos abiertos (código o contrato a vigilar)

| ID | Tema | Notas |
| --- | --- | --- |
| O-01 | **Paginación en memoria** | `OrdenCompraController`, `MarketingLeadController` usan `findAll` + `subList`; riesgo de rendimiento. |
| O-02 | **`ProyectoNotFoundException`** | Respuesta distinta a `ErrorResponses` en `GlobalExceptionHandler` (deuda cross-cutting). |
| O-03 | **EGRESO billetera** | API acepta `EGRESO`; use case puede invocar dominio con parámetros incompletos — seguir deuda en `BILLETERA_MODULE_CANONICAL.md`. |
| O-04 | **Flyway `V17__` duplicado** | Conviven dos scripts; riesgo de orden/ambiente — ver `DATA_MODEL_CURRENT.md` §2. |

## 4. REGLA-* (muestra)

Muestreo `grep REGLA-` en `backend/src/main/java` vs menciones en canónicos: sin divergencia nueva detectada en esta pasada para IDs citados en módulos tocados (Compras REGLA-153, Partidas congeladas, etc.). **Seguimiento:** repetir grep focalizado antes de cada release mayor.

## 5. Estado checklists

| Checklist | Resultado |
| --- | --- |
| `REVIEW_CHECKLIST.md` | Aplicado en revisión de consistencia rutas HTTP / códigos / nombres de use case |
| `AI_VALIDATION_CHECKLIST.md` | Aplicado: verificación de drift inverso (doc adelantada vs código) corregida en los ítems H-* |
