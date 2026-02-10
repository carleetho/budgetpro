# APU Module - Canonical Specification

> **Status**: Functional (100%)
> **Owner**: Cost Engineering Team
> **Last Updated**: 2026-02-09

## 1. Module Maturity Roadmap

| Phase       | Timeline  | Target State            | Deliverables                      |
| ----------- | --------- | ----------------------- | --------------------------------- |
| **Current** | Now       | 100% (Core Logic)       | Unit Prices, Resources, Analysis  |
| **Next**    | +1 Month  | Optimization            | Bulk Updates, AI Suggestions      |

## 2. Invariants (Business Rules)

| ID | Rule | Status |
| --- | --- | --- |
| REGLA-035 | **PartidaId mandatory & Insumos list not null** | ✅ Implemented |
| REGLA-036 | **Subtotal = Qty * UnitPrice; Non-negative values** | ✅ Implemented |
| REGLA-094 | **Insumos list mandatory for creation** | ✅ Implemented |
| REGLA-095 | **Input validation (ResourceId, Qty, Price)** | ✅ Implemented |
| REGLA-120 | **Inventory Egress reduces APU balance** | 🟡 Policy |

## 3. Data Contracts

### Entity: APU
- `id`: UUID
- `partidaId`: UUID (Unique per Project Strategy)
- `rendimiento`: BigDecimal (e.g., m3/day)

## 4. Detailed Rule Specifications

### REGLA-035: APU Integrity

**Status:** ✅ Verified
**Type:** Dominio
**Severity:** CRITICAL

**Description:**
En APU, el partidaId es obligatorio y la lista de insumos no puede ser nula.

**Implementation:**
- **Invariants:** `APU.validarInvariantes`
- **Validation:** Assertions

**Code Evidence:**
```java
if (partidaId == null) { throw new IllegalArgumentException("El partidaId no puede ser nulo"); }
if (insumos == null) { throw new IllegalArgumentException("La lista de insumos no puede ser nula"); }
```

### REGLA-036: Financial Consistency

**Status:** ✅ Verified
**Type:** Financiera
**Severity:** CRITICAL

**Description:**
En APU, el subtotal de insumo es cantidad * precio unitario; cantidad y precio unitario no pueden ser negativos.

**Implementation:**
- **Entity:** `ApuInsumo`
- **Method:** `calcularSubtotal`

**Code Evidence:**
```java
public BigDecimal calcularSubtotal() {
    return this.cantidad.multiply(this.precioUnitario);
}
```

### REGLA-094: Creation Request

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
Para crear APU: lista de insumos obligatoria.

**Implementation:**
- **DTO:** `CrearApuRequest`
- **Validation:** `@NotNull`

**Code Evidence:**
```java
@NotNull
private List<ApuInsumoRequest> insumos;
```

### REGLA-095: Input Item Validation

**Status:** ✅ Verified
**Type:** Técnica
**Severity:** MEDIUM

**Description:**
En insumo APU request: recursoId, cantidad y precioUnitario obligatorios; cantidad y precioUnitario no negativos.

**Implementation:**
- **DTO:** `ApuInsumoRequest`
- **Validation:** `@NotNull`, `@DecimalMin`

**Code Evidence:**
```java
@NotNull UUID recursoId,
@DecimalMin("0.0") BigDecimal cantidad,
@DecimalMin("0.0") BigDecimal precioUnitario
```
