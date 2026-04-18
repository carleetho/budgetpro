# INVENTARIO_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad en radiografía).  
> **Rama**: `feature/gaps-inventario-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Almacén / inventario |
| % oficial (tablero) | **70%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [INVENTARIO_MODULE_CANONICAL.md](../../modules/INVENTARIO_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `InventarioController`, `AlmacenController`, `TransferenciaController`, `ConsultarInventarioUseCaseImpl`, `grep` observabilidad/eventos |

## 2. Superficie de código (evidencia)

- **Dominio / aplicación:** `com.budgetpro.domain.logistica.inventario`, `com.budgetpro.domain.logistica.bodega`, `com.budgetpro.application.inventario`, `com.budgetpro.application.almacen`; transferencias orquestadas vía `com.budgetpro.domain.logistica.transferencia.service.TransferenciaService`.
- **REST** (`infrastructure/rest`):
  - `InventarioController` — `@RequestMapping("/api/v1/proyectos")` → `GET /{proyectoId}/inventario` (`ConsultarInventarioUseCase`).
  - `AlmacenController` — `@RequestMapping("/api/v1/almacen")` → `POST /movimientos` (201 + `Location`), `GET /movimientos` (`almacenId` obligatorio, `recursoId` opcional).
  - `TransferenciaController` — `@RequestMapping("/api/v1/transferencias")` → `POST /entre-bodegas`, `POST /entre-proyectos` (204 sin cuerpo).
- **Migraciones Flyway relevantes:** `V20.1__create_almacen_schema.sql`, `V23__add_movimiento_almacen_id_to_recepcion_detalle.sql`, `V34__repair_movimiento_almacen_drop_duplicate_tipo.sql` (canónico cita también checks en evolución del esquema; alinear detalle con `DATA_MODEL_CURRENT` si difiere de `V14` histórico).
- **Observabilidad / eventos:** búsqueda en `backend/**/*.java` sin coincidencias para `InventarioBajoAlert`, `InventarioBajo`, ni métrica documentada `inventory.value.total`; clases Micrometer localizadas: `EvmMetrics`, `CatalogMetrics`, `IntegrityMetrics` (sin clase dedicada inventario).

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **UC-I03 “consumo obra” vs movimiento genérico** | El canónico §6 marca UC-I03 🟡: salida cubierta por `POST /api/v1/almacen/movimientos` con `tipoMovimiento` SALIDA e imputación; **no** hay recurso REST separado “consumo obra” además de almacén | P2 (producto / claridad contrato) |
| GF-02 | **`GET .../inventario` sin paginación** | `ConsultarInventarioUseCaseImpl` carga `findByProyectoId` y mapea lista completa; riesgo de payload en proyectos grandes (distinto de O-01: no es `findAll` global) | P2 |
| GF-03 | **Transferencias 204** | `TransferenciaController` devuelve cuerpo vacío; clientes no reciben id de asiento en respuesta (aceptable si idempotencia/referencia van en cabecera o log; documentar en OpenAPI) | P3 |

## 4. Gaps de reglas / invariantes

| ID | Regla (canónico) | Notas |
|----|------------------|-------|
| GR-01 | REGLA-049 / REGLA-064 / REGLA-085 | Coherentes con entidades y DDL citados en canónico; sin hallazgo nuevo en esta pasada |
| GR-02 | I-03 FIFO vs PMP | 🟡 decisión de política en canónico §2; código sigue PMP según roadmap |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas |
|----|------|-------|
| DT-01 | **Concurrencia / locking** | Canónico §11: alta concurrencia mismo ítem — seguimiento |
| DT-02 | **Evento `InventarioBajoAlert` (§3 canónico 🔴)** | Sin publicador/listener identificado en esta pasada | Enlace **O-12** |
| DT-03 | **Observabilidad §9 canónico** | Métricas/logs listados; sin evidencia Micrometer/logs dedicados inventario en escaneo rápido | Enlace **O-12** |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Decidir contrato UC-I03: mantener solo almacén + documentar OpenAPI, o añadir subrecurso explícito “consumo” con mismo caso de uso interno.
2. **P2**: Paginación o límite documentado en `GET .../inventario` si el volumen de ítems crece.
3. **P2**: Spike **InventarioBajoAlert** (umbral, cola, integración Alertas) + métricas reales o ajustar §9 del canónico a “planificado”.
4. **P3**: OpenAPI: cuerpo/headers de `POST /transferencias/*`.

## 7. Definición de hecho para subir %

- **Hacia ~75%**: roadmap §1 *Next* (salida obra / transfers ya parcialmente cubierto) + cierre medible de al menos un P2 (paginación o UC-I03 documentado en API pública) + evidencia de observabilidad mínima o canónico §9 acotado a lo implementado.
- **~70%** razonable mientras UC-I03 siga 🟡, I-03 sea decisión abierta y eventos/métricas no estén implementados.

## 8. Referencias cruzadas

- Tablero: [SCOREBOARD_17.md](../SCOREBOARD_17.md).
- Hallazgos: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-12**).
