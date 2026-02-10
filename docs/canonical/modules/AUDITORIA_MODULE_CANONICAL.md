# AUDITORÍA Module - Canonical Specification

> **Status**: Functional (100%)
> **Owner**: Security & Compliance Team
> **Last Updated**: 2026-02-09

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
