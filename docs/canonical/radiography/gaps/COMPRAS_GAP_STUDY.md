# COMPRAS_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Compras (directa + OC + proveedor + recepción) |
| % oficial (tablero) | **75%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [COMPRAS_MODULE_CANONICAL.md](../../modules/COMPRAS_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `CompraController`, `OrdenCompraController`, `RecepcionController`, `ProveedorController` |

## 2. Superficie de código (evidencia)

- **`CompraController`** — `/api/v1/compras` (compra directa + recepciones según mappings).
- **`OrdenCompraController`** — `/api/v1/ordenes-compra` (CRUD, flujo solicitar/aprobar/enviar/confirmar, **`page`/`size`**, rechazo).
- **`RecepcionController`** — `/api/v1/compras` (recepciones asociadas a compra).
- **`ProveedorController`** — `/api/v1/proveedores` (CRUD).
- **Dominio:** `ProcesarCompraService`, `SaldoInsuficienteException`, etc.

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) / doc | Severidad |
|----|------|---------------------------|-----------|
| GF-01 | **Paginación OC en aplicación** | Canónico §11: con ciertos filtros sigue habiendo slice en memoria tras carga amplia — mismo riesgo que **O-01** | P2 |
| GF-02 | **Compra directa `proveedor` string** | Canónico §11 legacy vs `Proveedor` entidad para OC — deuda de migración ya reconocida | P2 |
| GF-03 | **Métricas §9** | `purchase.total.amount` sin evidencia Micrometer en escaneo rápido | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | Flujos de estado OC y recepción: coherentes con canónico §6–7; sin hallazgo nuevo en esta pasada |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Queries paginadas nativas para listado OC a volumen alto |
| DT-02 | Unificar narrativa OpenAPI (`orden-compra-api.yaml`) con todos los paths vivos |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Cerrar patrón **O-01** para `OrdenCompraController` con paginación en BD.
2. **P2**: Guía de migración proveedor en compras directas (ya referenciada en canónico).

## 7. Definición de hecho para subir %

- **Hacia ~80%**: paginación server-side medible + métricas reales o §9 ajustado a implementado.
- **~75%** mientras O-01 afecte listados bajo ciertos filtros.

## 8. Referencias cruzadas

- [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-01**).
