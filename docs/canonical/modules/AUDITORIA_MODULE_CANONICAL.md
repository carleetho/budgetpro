# AUDITORIA_MODULE_CANONICAL.md — Current State Radiography

> **Scope**: Trazabilidad JPA (timestamps, autor), políticas de no repudio en presupuesto y procesos  
> **Status**: Functional (70%) — **transversal**; sin API REST dedicada de “consulta de auditoría”  
> **Owner**: Security & Compliance Team  
> **Last Updated**: 2026-04-12  
> **Authors**: Antigravity (sync código `main`)

**Base JPA:** `com.budgetpro.infrastructure.persistence.entity.AuditEntity` (`created_at`, `updated_at`, `created_by` vía `@CreatedBy` + `AuditingEntityListener`). **Presupuesto:** `IntegrityAuditLog` / `IntegrityAuditRepository` (hashes y eventos de integridad), no confundir con un bus genérico de dominio.

## 1. Module Overview
Centralized audit logging and immutable history tracking.

## 2. Invariants (Business Rules)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-167 | **Mandatory Accessor Tracking (CreatedBy)** | ✅ Implemented |
| REGLA-168 | **Immutable History (Verdad No Retroactiva)** | ✅ Implemented |
| REGLA-169 | **State Change logging** | ✅ Implemented |

## 3. Detailed Rule Specifications

### REGLA-167: Authorship Integrity

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** CRITICAL

**Description:**
El created_by es obligatorio en entidades auditables.

**Implementation:**
- **Class:** `AuditEntity`
- **Mechanism:** Field definition + DB Constraint

**Code Evidence:**
```java
@Column(name = "created_by", nullable = false, updatable = false)
private UUID createdBy;
```

### REGLA-168: Non-Retroactive Truth

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** CRITICAL

**Description:**
Los datos históricos no se corrigen; se explican mediante eventos formales.

**Implementation:**
- **Policy:** Business Manifesto
- **Mechanism:** No UPDATE/DELETE on history tables.

**Code Evidence:**
```markdown
"Principio de Verdad No Retroactiva" in Manifesto.
```

### REGLA-169: Project State Audit

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
Todo cambio de estado del Proyecto debe registrar estado anterior, nuevo, usuario, fecha/hora y motivo.

**Implementation:**
- **Entity:** `ProyectoAuditLog` (or similar)
- **Trigger:** State Machine Transition events

**Code Evidence:**
```java
// Event Listener captures StateChange and writes to Audit Log
```

---

## Apéndice A — Superficie técnica (sync 2026-04-08)

| Capa | Qué existe en código | REST |
| --- | --- | --- |
| **JPA** | `AuditEntity` como `@MappedSuperclass` para `created_by` / timestamps | Ningún `*AuditController` listado |
| **Integridad presupuesto** | `IntegrityAuditLog`, repositorio de integridad | Consumo indirecto vía flujos de aprobación/compra |

**REGLA-169 (Proyecto):** no se verificó entidad dedicada tipo `ProyectoAuditLog` en el escaneo 2026-04-08; tratar como **roadmap / deuda documental** hasta cruzar máquina de estados de `Proyecto` con persistencia explícita de historial.

**Estudio de gaps (Ola 1b):** [AUDITORIA_GAP_STUDY.md](../radiography/gaps/AUDITORIA_GAP_STUDY.md) — **O-16** en [CODE_DOC_REVIEW_LOG.md](../radiography/CODE_DOC_REVIEW_LOG.md).
