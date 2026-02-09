# Audit Report: Data Model Radiography

**Date:** 2026-02-07
**Auditor:** Antigravity
**Subject:** `docs/canonical/radiography/DATA_MODEL_CURRENT.md`
**Reference:** `docs/audits/INVENTARIO_REGLAS_EXISTENTES_FASE1.md`

## 1. Executive Summary

The `DATA_MODEL_CURRENT.md` document provides a decent high-level overview of the relationships but lacks the **rich semantic constraints** that exist in the actual database schema. The 161-rule inventory reveals a highly robust database layer with numerous `CHECK` constraints and `UNIQUE` indexes that are not documented in the data model.

## 2. Completeness Assessment

| Metric            | Status              | Notes                                                                  |
| :---------------- | :------------------ | :--------------------------------------------------------------------- |
| **Entities**      | ✅ Mostly Complete  | Major aggregates listed.                                               |
| **Relationships** | ✅ Complete         | Main relationships (1:N, 1:1) captured correctly.                      |
| **Attributes**    | 🟡 Partial          | key attributes listed, but types/nullability often missing.            |
| **Constraints**   | 🔴 **Critical Gap** | `CHECK` constraints (e.g., non-negative) and `UNIQUE` indexes missing. |

## 3. Missing Elements (Gap Analysis)

### 3.1. Database Constraints (The "Invisible" Domain Layer)

The database enforces severe business rules via `CHECK` constraints (REGLA-058 to REGLA-070, REGLA-135) that should be documented as part of the data model's integrity guarantees:

- **Financial Integrity**: `CHECK (monto >= 0)`, `CHECK (precio >= 0)` exist on almost all financial tables but are not mentioned.
- **State Integrity**: `CHECK (estado IN (...))` ensures invalid states cannot exist physically.
- **Logic Integrity**: `CHECK (fecha_fin >= fecha_inicio)` in Cronograma.

### 3.2. Uniqueness Guarantees

Critical business keys enforced by `UNIQUE` indexes (REGLA-055, REGLA-128, REGLA-130 to REGLA-137) are missing:

- `UNIQUE (proyecto_id, codigo)` for Valuations.
- `UNIQUE (proyecto_id, numero_estimacion)`.
- `UNIQUE (almacen_id, recurso_id)` for Stock.

### 3.3. Mandatory Fields

The model document lists attributes but doesn't distinguish between nullable and mandatory (REGLA-073, REGLA-076).

## 4. Recommendations

1. **Enrich Entity Tables**: Add a "Constraints" column to the entity tables in `DATA_MODEL_CURRENT.md` to list the `CHECK` and `UNIQUE` rules.
2. **Document State Enums**: Explicitly list the allowed values for state columns, matching the DB `CHECK` constraints.
3. **Diagram Update**: Update Mermaid diagrams to show cardinality strictly and potentially denote weak entities.
