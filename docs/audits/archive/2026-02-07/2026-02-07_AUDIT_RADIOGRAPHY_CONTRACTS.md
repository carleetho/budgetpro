# Audit Report: Architectural Contracts Radiography

**Date:** 2026-02-07
**Auditor:** Antigravity
**Subject:** `docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md`
**Reference:** `docs/audits/INVENTARIO_REGLAS_EXISTENTES_FASE1.md`

## 1. Executive Summary

`ARCHITECTURAL_CONTRACTS_CURRENT.md` correctly outlines the Hexagonal Architecture intent but fails to capture the **Contract enforcement mechanisms** revealed in the inventory. The inventory shows a heavy reliance on **Java Bean Validation (`javax.validation`) in the Interface Layer (Controllers/DTOs)** acting as the first line of defense (Contract), which is not explicitly detailed in the architecture doc.

## 2. Completeness Assessment

| Metric                | Status         | Notes                                                                                         |
| :-------------------- | :------------- | :-------------------------------------------------------------------------------------------- |
| **Layer Definitions** | ✅ Complete    | Concepts are clear.                                                                           |
| **Boundary Rules**    | 🟡 Partial     | Mentions dependency rules but misses the "Validation at the Gate" pattern.                    |
| **DTO Contracts**     | 🔴 **Missing** | The extensive use of DTO validation (REGLA-076 to REGLA-099) is a key architectural contract. |

## 3. Findings

### 3.1. The "Validation at the Gate" Pattern

The inventory (REGLA-076 to REGLA-099) proves that BudgetPro uses a strict **Input Contract** pattern:

- All DTOs are validated using `@Valid`, `@NotNull`, `@Positive`, etc.
- This layer prevents invalid data from even reaching the Application/Domain layers.
- **Architectural Significance**: This effectively protects the Domain from simple formatting errors and nulls, allowing the Domain to focus on complex business invariants. This strategy is **undocumented** in the radiography.

### 3.2. Governance Rules as Contracts

Rules like REGLA-100 ("Online Only") and REGLA-052 (Security paths) are architectural decisions that functionally act as implementation contracts.

### 3.3. Violation Detection

The radiography mentions "UseCase strictness" as a risk. The Audit of Compras/Inventario (Task 2) confirmed that `UseCase` implementations are indeed the primary entry point, but identified some bypasses.

## 4. Recommendations

1. **Document Input Contracts**: Explicitly document the "Validation at the Gate" strategy using Bean Validation on DTOs as a standard Architectural Contract.
2. **Define Error Contracts**: Document how validation errors (400 Bad Request) vs Domain errors (409 Conflict / 422 Unprocessable) are mapped, ensuring a consistent API contract.
