# MARKETING Module - Canonical Specification

> **Status**: Functional (100%)
> **Owner**: Growth Team
> **Last Updated**: 2026-02-09

## 1. Module Overview
Manages Leads and Public Inquiries.

## 2. Invariants (Business Rules)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-108 | **Lead State Machine** | ✅ Implemented |
| REGLA-123 | **Lead Data Integrity** | ✅ Implemented |
| REGLA-124 | **Public Creation Constraints** | ✅ Implemented |

## 3. Detailed Rule Specifications

### REGLA-108: Lead Validity States

**Status:** ✅ Verified
**Type:** Gobierno
**Severity:** HIGH

**Description:**
El estado de Lead debe estar en {NUEVO, CONTACTADO, CONVERTIDO}.

**Implementation:**
- **Database:** `V18__create_marketing_lead.sql`
- **Constraint:** CHECK

**Code Evidence:**
```sql
CHECK (estado IN ('NUEVO', 'CONTACTADO', 'CONVERTIDO'))
```

### REGLA-123: Lead Mandatory Fields

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En Lead: nombre_contacto obligatorio; email debe ser válido; fecha_solicitud no nula.

**Implementation:**
- **Entity:** `LeadEntity`
- **Annotations:** `@NotBlank`, `@Email`, `@CreationTimestamp`

**Code Evidence:**
```java
@NotBlank String nombreContacto;
@Email String email;
```

### REGLA-124: Public API Constraints

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Para crear lead público: validaciones de entrada estrictas para seguridad.

**Implementation:**
- **DTO:** `CrearLeadRequest`
- **Annotations:** `@Size`, `@NotBlank`

**Code Evidence:**
```java
@Size(max = 100) String nombreContacto;
// Prevents overflow attacks
```
