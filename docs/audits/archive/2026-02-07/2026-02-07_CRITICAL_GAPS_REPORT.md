# Critical Gaps Report (Phase 0)

**Date:** 2026-02-07
**Auditor:** Antigravity
**Scope:** Presupuesto, Estimacion, Compras, Inventario, Radiography Documents

## 1. Executive Summary

Phase 0 audits have revealed a **Systemwide Integrity Risk**. While the Domain Entities (`Presupuesto`, `InventarioItem`) are generally robust with strong invariants, the **Application Layer (Use Cases)** frequently bypasses these protections, creating "Zombie States" where the system technically allows illegal operations (e.g., modifying a frozen budget).

Furthermore, the "Radiography" documentation—intended to be the source of truth for AI Agents—is **critically incomplete (9% coverage)**. Relying on it would cause an AI to hallucinate a much looser system than what actually exists in the database.

### Summary Metrics

| Metric              | Status          | Count/Score                                  |
| :------------------ | :-------------- | :------------------------------------------- |
| **Modules Audited** | 4               | Presupuesto, Estimacion, Compras, Inventario |
| **Rules Verified**  | 17              | 13 Canonical + 4 Inventory                   |
| **Critical Gaps**   | 🔴 **High**     | **5** (2 Code, 3 Doc)                        |
| **Doc Coverage**    | 🔴 **Critical** | **9.3%** (15/161 Rules documented)           |

## 2. Critical Implementation Gaps (Code)

These gaps allow data corruption or illegal business states and must be fixed immediately.

| ID        | Module      | Gap Description                                                                                  | Impact                                                                      | Recommendation                                                |
| :-------- | :---------- | :----------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------- | :------------------------------------------------------------ |
| **P-01**  | Presupuesto | **Frozen Budget Bypass**: `CrearPartidaUseCaseImpl` does not check if the budget is `CONGELADO`. | **High**: Users can alter "Contractual" budgets, invalidating the baseline. | Add `if (presupuesto.isAprobado()) throw ...` in the UseCase. |
| **ES-01** | Estimacion  | **Non-Sequential Approval**: `AprobarEstimacionUseCaseImpl` does not enforce N-1 sequence.       | **High**: Billing gaps can be created, breaking financial continuity.       | Add sequential check against `estimacionRepository`.          |
| **P-03**  | Presupuesto | **Zombie APUs**: APUs can be attached to non-leaf nodes (Padres).                                | **Medium**: Breaks WBS aggregation logic.                                   | Enforce `partida.isHoja()` before APU creation.               |
| **ES-02** | Billetera   | **Missing Debt**: Approval doesn't generate Accounts Payable.                                    | **Medium**: Financial data (Wallet) desynchronizes from Operational data.   | Emit `EstimacionAprobadaEvent` to trigger Wallet updates.     |

## 3. Critical Documentation Gaps (Radiography)

These gaps cripple the ability of AI agents to work effectively.

| Document              | Coverage | Critical Missing Information                                                       | Impact on AI                                                        |
| :-------------------- | :------- | :--------------------------------------------------------------------------------- | :------------------------------------------------------------------ |
| **DOMAIN_INVARIANTS** | **9%**   | Misses 146 rules, including common financial checks (Non-negative) and Governance. | AI will suggest code that violates basic constraints.               |
| **DATA_MODEL**        | Partial  | Misses **ALL** Database Constraints (`CHECK`, `UNIQUE`).                           | AI will not understand the "hard" limits of the schema.             |
| **CONTRACTS**         | Partial  | Misses the "Verification at the Gate" (DTO Validation) pattern.                    | AI will write procedural validation code instead of using `@Valid`. |
| **MODULE_SPECS**      | Low      | Misses Lifecycle Coupling (e.g., "When can I buy?").                               | AI will operate modules in invalid Project states.                  |

## 4. Prioritized Action Plan

### Phase 1: Integrity Patching (Immediate)

1.  **Fix P-01**: Harden `CrearPartidaUseCaseImpl` to respect Frozen state.
2.  **Fix ES-01**: Implement sequential check in `AprobarEstimacionUseCaseImpl`.
3.  **Fix P-03/P-05**: Add leaf-node and uniqueness checks to Presupuesto.

### Phase 2: Documentation Injection

1.  **Mass Ingestion**: Run a script/task to promote the 161 Inventory Rules into `DOMAIN_INVARIANTS_CURRENT.md`.
2.  **DB Reflection**: Update `DATA_MODEL_CURRENT.md` with the `CHECK` and `UNIQUE` constraints found in migrations.

### Phase 3: Financial Integration

1.  **Connect Wallet**: Implement the Event Listener for `EstimacionAprobada` to close Gap ES-02.

## 5. Traceability Status

The Traceability Matrix has been initialized with the 17 audited rules.

- **Verified (Implemented):** 10 rules (e.g., L-01, I-01, P-02)
- **Failed (Missing/Partial):** 7 rules (P-01, P-03, P-05, ES-01, ES-02, ES-03, I-03)

**Next Step:** Proceed to Phase 1 Remediation (Task 5) to fix P-01 and ES-01.
