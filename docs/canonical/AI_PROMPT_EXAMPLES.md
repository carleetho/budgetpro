# AI Prompt Examples

> **Goal**: Teach users (and agents) how to prompt correctly.

## 1. Requirements Generation (BrainGrid)

**✅ Correct Pattern:**

> "Create requirements for Budget Approval.
> Context: `PRESUPUESTO_MODULE_CANONICAL.md`.
>
> - Enforce Invariant #3 (Thresholds).
> - Respect State constraints (Section 4).
> - Complete Use Case UC-04."

**❌ Incorrect Pattern:**

> "How should budget approval work? Write some requirements."
> _(Result: Hallucination based on generic knowledge)_

## 2. Code Generation (Antigravity)

**✅ Correct Pattern:**

> "Antigravity, please implement `validateBudget()` in `PresupuestoValidator.java`.
> First, read `PRESUPUESTO_MODULE_CANONICAL.md` Section 2.
>
> - Ensure you enforce Invariant #1.
> - Check Section 4 for disallowed state transitions."

**❌ Incorrect Pattern:**

> "Add validation to the budget class."
> _(Result: Generic null checks, missing business rules)_

## 3. Integration Design

**✅ Correct Pattern:**

> "Design the REST endpoint for creating a Purchase.
>
> - Follow `INTEGRATION_PATTERNS_CURRENT.md` for error handling.
> - Check `COMPRAS_MODULE_CANONICAL.md` for payload schema."

**❌ Incorrect Pattern:**

> "Create a purchase API."
> _(Result: Inconsistent URL structure, wrong status codes)_
