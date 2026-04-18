# Presupuesto — backlog producto (Ola 2, capa B)

> **Tipo**: artefacto de producto / REQ semilla (no sustituye workshop con ingeniería).  
> **Fecha**: 2026-04-17  
> **Enlaza**: [PRESUPUESTO_GAP_STUDY.md](./PRESUPUESTO_GAP_STUDY.md) §10, [PRESUPUESTO_MODULE_CANONICAL.md](../../modules/PRESUPUESTO_MODULE_CANONICAL.md) §8.

## Propósito

Tras el workshop con **un ingeniero de presupuesto**, sustituir o refinar las filas “Ejemplo / semilla” por acciones reales de una semana. Cada fila debe terminar con **criterios de aceptación (CA)** verificables (test de API, test E2E o checklist manual firmado).

**Leyenda `Ext. canónico`:** `Sí` = requiere ampliar notebook canónico o contrato REST no descrito hoy; `No` = se cubre con APIs existentes + UX; `Parcial` = puede bastar UX pero hay ambigüedad (marcar en comentarios).

## Semilla — diez acciones típicas (rellenar post-workshop)

| ID | Acción (obra / oficina) | ¿Ext. canónico? | APIs / notas hoy (2026-04-17) | CA (borrador — completar en workshop) |
| -- | ------------------------ | --------------- | ------------------------------- | -------------------------------------- |
| REQ-P2-01 | Listar presupuestos del proyecto con paginación | No | `GET /api/v1/presupuestos?tenantId=&proyectoId=&page=&size=` | Dado tenant+proyecto válidos, la primera página devuelve `content` y metadatos `totalElements` coherentes con BD. |
| REQ-P2-02 | Ver árbol WBS completo | No | `GET /api/v1/partidas/wbs?presupuestoId=` | El árbol refleja jerarquía y códigos únicos según reglas existentes. |
| REQ-P2-03 | Filtrar partidas por capítulo / nivel en UI | Parcial | Solo WBS completo; filtrado en cliente o nuevo contrato | **Si** se exige filtro server-side: `Sí` + especificar query en canónico. **Si** basta cliente: CA de rendimiento máx. N nodos. |
| REQ-P2-04 | Editar metrado o descripción de una partida existente | Sí | Canónico §11: sin `PUT` partida en `PartidaController` | Tras acuerdo: endpoint + invariantes documentados; test cambia metrado en `BORRADOR`. |
| REQ-P2-05 | Eliminar o mover partida en WBS | Sí | Sin `DELETE` / reorden documentado | Tras acuerdo: transiciones y impacto en APU hijos. |
| REQ-P2-06 | Duplicar presupuesto para variante | Sí | UC-P08 🔴 | CA solo tras UC-P08 en canónico + implementación. |
| REQ-P2-07 | Exportar a Excel para dueño de obra | Sí (UC-P09) | UC-P09 🔴 | Archivo generado, columnas mínimas acordadas con Finanzas (ver gap §10). |
| REQ-P2-08 | Configurar indirectos y FSR desde pantalla única | No | `PUT .../sobrecosto` + `PUT` laboral extendida (dos prefijos; preferir `/rrhh/...` en nuevos clientes) | Tras guardar, `GET` presupuesto refleja totales esperados; FSR persiste vía mismo modelo RRHH. |
| REQ-P2-09 | Ver Plan vs Real y explosión en misma sesión | No | `GET .../control-costos`, `GET .../explosion-insumos` | Usuario obtiene ambos informes sin error para presupuesto `CONGELADO` con datos mínimos de prueba. |
| REQ-P2-10 | Buscar partida por código en listados grandes | Parcial | `GET /partidas/{id}` por UUID; sin búsqueda por código | **Si** se exige por `item`: `Sí` + listado filtrado o índice; si basta índice en cliente tras WBS: CA de tiempo de carga. |

## DoD Ola 2 (del plan estratégico)

- Cada REQ priorizado tiene **CA** y **dueño**.
- Cada REQ en curso tiene **test de contrato** o **test automatizado** o evidencia de QA adjunta al PR.
- Las filas `Sí` en **Ext. canónico** no entran a desarrollo **Modo B** sin texto en `PRESUPUESTO_MODULE_CANONICAL.md` (o documento explícitamente autorizado por Finanzas).
