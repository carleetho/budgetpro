# APU_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | APU (Análisis Precio Unitario / snapshots) |
| % oficial (tablero) | **90%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [APU_MODULE_CANONICAL.md](../../modules/APU_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `ApuController` |

## 2. Superficie de código (evidencia)

- **`ApuController`** — `@RequestMapping("/api/v1")`  
  - `POST /partidas/{partidaId}/apu` (201)  
  - `PUT /apu/{apuSnapshotId}/rendimiento` (204)  
  - `GET /partidas/{partidaId}/apu`  
  - `GET /apu/{apuId}`  

## 3. Gaps funcionales (REST / producto)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Sin mutación REST de insumos post-creación** | Solo `rendimiento` vía `PUT`; composición de insumos tras snapshot puede requerir flujo nueva versión (según política de producto) | P2 |
| GF-02 | **Sin DELETE / deprecar snapshot** | No hay `DELETE` en controlador; inmutabilidad presupuestaria puede justificarlo — documentar en OpenAPI | P3 |
| GF-03 | **Prefijo disperso** | Rutas bajo `/api/v1` mezcladas con partidas — ya conocido en ecosistema BudgetPro | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | Coherencia con partidas congeladas y snapshots — seguir canónico; sin hallazgo nuevo |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | Versionado explícito de APU en API pública si el negocio lo exige |
| DT-02 | OpenAPI único con Partidas/APU |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Decidir si “nueva versión APU” es siempre `POST` nuevo snapshot y documentarlo.
2. **P3**: Endpoint de lectura histórico de snapshots si UI lo necesita.

## 7. Definición de hecho para subir %

- **Hacia ~95%**: política de versiones + contratos de insumo alineados a dominio.
- **~90%** razonable con CRUD mínimo vigente.

## 8. Referencias cruzadas

- [PARTIDAS_GAP_STUDY.md](./PARTIDAS_GAP_STUDY.md), tablero [SCOREBOARD_17.md](../SCOREBOARD_17.md).
