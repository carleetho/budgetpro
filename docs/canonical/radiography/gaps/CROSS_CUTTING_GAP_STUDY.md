# CROSS_CUTTING_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12 · **Actualización Flyway O-04**: 2026-04-18

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Cross-cutting (hexagonal, validación, errores, observabilidad) |
| % oficial (tablero) | **90%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [CROSS_CUTTING_MODULE_CANONICAL.md](../../modules/CROSS_CUTTING_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `GlobalExceptionHandler`, `SecurityConfig`, paquetes `infrastructure/config`, `infrastructure/observability` |

## 2. Superficie de código (evidencia)

- **Errores API:** `GlobalExceptionHandler` — mapeo de excepciones a respuestas (`ErrorResponses` donde aplica).
- **Seguridad:** `SecurityConfig`, `JwtAuthenticationFilter`.
- **Observabilidad:** `EvmMetrics`, `CatalogMetrics`, `IntegrityMetrics`, etc.
- **Deuda ya registrada:** **O-02** (`ProyectoNotFoundException` fuera de formato estándar). ~~**O-04** Flyway `V17__` duplicado~~ → **cerrado (2026-04-18)** vía **H-14** (`V17.1__create_presupuesto_integrity_audit.sql`).

## 3. Gaps funcionales (plataforma)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Homogeneidad de errores** | Excepciones con respuestas distintas — **O-02** | P1 |
| GF-02 | **RBAC avanzado** | Canónico §11 roadmap permisos dinámicos | P2 |
| GF-03 | **Multitenancy** | Target canónico +3 meses — sin evidencia en esta pasada | P3 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-051–056, logging: alineación general con implementación; detalle en [SEGURIDAD_GAP_STUDY.md](./SEGURIDAD_GAP_STUDY.md) |

## 5. Deuda técnica y riesgos

| ID | Tema |
|----|------|
| DT-01 | ~~Migraciones duplicadas — **O-04**~~ → **O-04 cerrado** (2026-04-18); seguir convención de versionado único por script |
| DT-02 | Métricas HTTP genéricas §9 canónico vs instrumentación real — validar en despliegue |

## 6. Candidatos de cierre (priorizado)

1. **P1**: Cerrar **O-02** (handler unificado).
2. ~~**P1**: Resolver **O-04** (orden Flyway).~~ ✅ **CERRADO** (2026-04-18): migración de integridad renombrada a **`V17.1__`**, ver **H-14** en [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md).
3. **P2**: Matriz de permisos.

## 7. Definición de hecho para subir %

- **Hacia ~95%**: **O-04** cerrado (2026-04-18); pendiente **O-02** + RBAC o documento de amenazas aceptadas.

## 8. Referencias cruzadas

- [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-02**); **O-04** cerrado → §2 **H-14**.
