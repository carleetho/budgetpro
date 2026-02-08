# AXIOM Hardening Patterns

This document describes the standard patterns and architectural justifications used to suppress AXIOM domain hardening violations (specifically `nosemgrep` suppressions). These patterns ensure that exceptions to immutability rules are deliberate, documented, and consistent.

## 1. State Machine Pattern

**Problem:** Domain entities often need to track lifecycle state changes (e.g., `DRAFT` -> `APPROVED` -> `CLOSED`), which inherently requires mutability of the state field.
**Resolution:** Mark the state field as mutable but controlled via method encapsulation.
**Justification:** "State machine transition" or "State flag".

```java
private EstadoPresupuesto estado; // nosemgrep: ... - State machine transition
```

## 2. Security Pattern (Integrity Hashes)

**Problem:** Fields used for cryptographic integrity (e.g., hashes) cannot be set in the constructor as they depend on the object's final state or are generated lazily upon approval.
**Resolution:** Allow mutability but restrict assignment to specific "sealing" methods (e.g., `approve()`).
**Justification:** "Security pattern (set on approval)" or "Security pattern (dynamic hash)".

```java
private String integrityHashApproval; // nosemgrep: ... - Security pattern (set on approval)
```

## 3. Optimistic Locking

**Problem:** JPA requires a `@Version` field for optimistic locking, which must be mutable by the persistence provider.
**Resolution:** Standard mutable field for versioning.
**Justification:** "Optimistic locking".

```java
private Long version; // nosemgrep: ... - Optimistic locking
```

## 4. Managed Relationships (JPA)

**Problem:** `@OneToMany` lists or collections are often managed by Hibernate/JPA, which requires them to be mutable or at least accessible. While we prefer `ImmutableList` in getters, the field itself might need to be mutable for ORM proxying or lazy loading mechanics.
**Resolution:** Allow mutable list fields, but expose only unmodifiable views in getters.
**Justification:** "Child entities managed list" or "JPA managed relationship".

```java
private List<DetalleEstimacion> detalles; // nosemgrep: ... - Child entities managed list

public List<DetalleEstimacion> getDetalles() {
    return Collections.unmodifiableList(detalles);
}
```

## 5. Calculation Cache / Derived Fields

**Problem:** Some fields are strictly derived from others (e.g., `totalAmount = sum(items)`). Recalculating them on every access is computationally expensive. We cache the result in a field.
**Resolution:** Mutable field updated only via specific recalculation logic.
**Justification:** "Calculated field" or "Performance cache".

```java
private BigDecimal montoBruto; // nosemgrep: ... - Calculated field
```

## 6. Business Logic Updates

**Problem:** Certain business requirements explicitly demand that data be updateable (e.g., renaming a draft project, updating evidence URL).
**Resolution:** Mutable field with validation in the setter/update method.
**Justification:** "Business logic requires renaming" or "Updatable evidence".

```java
private String nombre; // nosemgrep: ... - Business logic requires renaming
```

## 7. Audit Trail

**Problem:** Fields like `updatedAt` or `updatedBy` change on every modification.
**Resolution:** Mutable fields utilized by auditing listeners.
**Justification:** "Audit trail" or "Audit metadata".

```java
private LocalDateTime updatedAt; // nosemgrep: ... - Audit trail
```

---

**Usage Rule:**
Any suppression of `entity-final-fields` MUST be accompanied by one of these standard justifications. Ad-hoc suppressions like "fixing error" are strictly prohibited.
