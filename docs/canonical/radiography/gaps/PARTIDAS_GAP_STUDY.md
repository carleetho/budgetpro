# PARTIDAS_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 + ajuste menor canónico §4 (tabla GET APU).  
> **Rama**: `feature/gaps-partidas-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Partidas (WBS) |
| % oficial (tablero) | **65%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [PARTIDAS_MODULE_CANONICAL.md](../../modules/PARTIDAS_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `PartidaController`, `AvanceController`, `ApuController` |

## 2. Superficie de código (evidencia)

- **`PartidaController`** — `@RequestMapping("/api/v1/partidas")`  
  - `POST /` crear partida (201 + `Location`)  
  - `GET /wbs?presupuestoId=` — árbol WBS  
  - `GET /{id}` — detalle por UUID  
- **`AvanceController`** — mismo prefijo `/api/v1/partidas`  
  - `POST /{partidaId}/avances`  
- **`ApuController`** — `@RequestMapping("/api/v1")`  
  - `POST /partidas/{partidaId}/apu`, `PUT /apu/{apuSnapshotId}/rendimiento`, `GET /partidas/{partidaId}/apu`, `GET /apu/{apuId}`  

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Listado plano por presupuesto** | No existe `GET /api/v1/partidas?presupuestoId=` (paginado o no); lectura agregada vía **WBS** o **GET por id** únicamente | P0 (bloquea UX de cliente y coincide con `MODULE_SPECS_CURRENT` §2) |
| GF-02 | **Actualización / borrado REST de partida** | Sin `PUT`/`PATCH`/`DELETE` en `PartidaController` | P1 |
| GF-03 | **Tabla canónica §4** | Faltaban filas **GET** de APU hasta esta sync (código ya expuesto) | P3 (doc) |
| GF-04 | **Fricción `nivel` en creación** | Deuda listada en canónico §6: comando REST exige `nivel` mientras el dominio tiene ramas para `nivel == null` | P2 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-037 / 038 / metrado / padre mismo presupuesto: coherentes con `CrearPartidaUseCase` y doc §3; sin hallazgo nuevo en esta pasada |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Dependencia fuerte con presupuesto congelado / metrado — ya en canónico §5 |
| DT-02 | OpenAPI: asegurar rutas APU + partidas en un solo spec |

## 6. Candidatos de cierre (priorizado)

1. **P0 (I1)**: `GET /api/v1/partidas` con `presupuestoId` obligatorio + paginación o contrato “lista plana” documentado; tests de contrato.
2. **P1**: Diseñar mutación controlada de partida (PUT/PATCH) o documentar “solo vía proceso X”.
3. **P2**: Opcional `nivel` derivado de `padreId` en REST.

## 7. Definición de hecho para subir %

- **Hacia ~75%**: GF-01 cerrado con API estable + OpenAPI + al menos un test de integración.
- **~65%** razonable mientras el acceso masivo siga solo por WBS y por id.

## 8. Referencias cruzadas

- Radiografía: [MODULE_SPECS_CURRENT.md](../MODULE_SPECS_CURRENT.md) (bullet Partidas).
- Hallazgos: [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-11**).
