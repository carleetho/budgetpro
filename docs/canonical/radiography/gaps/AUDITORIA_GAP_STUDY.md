# AUDITORIA_GAP_STUDY.md — Estudio de brechas (Ola 1, cadena)

> **Tipo de PR**: G0 (documentación + trazabilidad).  
> **Rama**: `feature/gaps-wave2-remaining-ola1`  
> **Fecha**: 2026-04-12

## 1. Baseline

| Campo | Valor |
|--------|--------|
| Módulo | Auditoría (trazabilidad transversal) |
| % oficial (tablero) | **70%** — [SCOREBOARD_17.md](../SCOREBOARD_17.md) |
| Notebook | [AUDITORIA_MODULE_CANONICAL.md](../../modules/AUDITORIA_MODULE_CANONICAL.md) |
| Fecha revisión | 2026-04-12 |
| Autor / revisores | Code-first: `grep` `*Audit*`, `AuditEntity`, `infrastructure/rest` (sin `*AuditController`) |

## 2. Superficie de código (evidencia)

- **JPA base:** `AuditEntity` (`@MappedSuperclass`) — `created_by`, timestamps en entidades que lo extienden.
- **Integridad presupuesto:** `IntegrityAuditLog` / repositorios asociados (flujos de aprobación; consumo indirecto).
- **REST:** ningún controlador dedicado bajo `infrastructure/rest` para “consulta de auditoría” o export de pista — alineado al canónico Apéndice A.

## 3. Gaps funcionales (REST / gobierno)

| ID | Tema | Observado (código) | Severidad |
|----|------|-------------------|-----------|
| GF-01 | **Sin API de lectura para compliance** | No hay recurso REST para listar/filtrar eventos de auditoría por entidad, usuario o rango temporal | P2 (producto / compliance) |
| GF-02 | **REGLA-169 vs persistencia explícita** | Canónico indica deuda documental hasta cruzar máquina de estados `Proyecto` con historial dedicado | P2 |

## 4. Gaps de reglas / invariantes

| ID | Notas |
|----|--------|
| GR-01 | REGLA-167 / 168: mecanismos JPA y políticas de no repudio a nivel de diseño; sin brecha nueva en esta pasada |

## 5. Deuda técnica y riesgos

| ID | Tema | Notas |
|----|------|-------|
| DT-01 | **Hallazgo centralizado** | Ausencia de API REST dedicada — ver **O-16** |
| DT-02 | Correlación logs externos vs `created_by` | Operación en entornos multi-servicio no auditada aquí |

## 6. Candidatos de cierre (priorizado)

1. **P2**: Spike API read-only paginada (quién/cuándo/qué) sobre tablas de auditoría o vista materializada.
2. **P2**: Cerrar evidencia REGLA-169 con entidad o log de transición de estado de proyecto.

## 7. Definición de hecho para subir %

- **Hacia ~75%**: contrato REST mínimo de consulta + permisos rol `AUDITOR` + tests de autorización.
- **~70%** razonable mientras la auditoría sea solo técnica (JPA + logs) sin producto de consulta.

## 8. Referencias cruzadas

- [CODE_DOC_REVIEW_LOG.md](../CODE_DOC_REVIEW_LOG.md) §3 (**O-16**).
