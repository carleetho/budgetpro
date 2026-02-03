# Onboarding Reading Sequence

> **Methodology**: Progressive Disclosure. Start high-level, then drill down.

## Phase 1: The Foundation (Day 1)

_Goal: Understand the "Rules of the Game"_

1. **`docs/canonical/README.md`** (10 mins)
   - Why do we have these notebooks?
2. **`ARCHITECTURAL_CONTRACTS_CURRENT.md`** (20 mins)
   - Hexagonal layers. Strict dependency rules.
3. **`DOMAIN_INVARIANTS_CURRENT.md`** (30 mins)
   - The "Constitution" of BudgetPro.
4. **`DATA_MODEL_CURRENT.md`** (40 mins)
   - Key Entities (Presupuesto, Partida, Estimacion).

## Phase 2: The Specifics (Day 2)

_Goal: Become an expert in ONE module._
_Pick the module you are assigned to (e.g., Presupuesto)._

1. **`PRESUPUESTO_MODULE_CANONICAL.md`** (1 hour)
   - Read every section. Pay attention to "Invariants" and "Use Cases".
2. **Code Walkthrough** (2 hours)
   - Compare `backend/src/.../presupuesto` with the Notebook.
   - Look for the Invariants in the `Validator` classes.

## Phase 3: The Connections (Day 3)

_Goal: Understand the ecosystem._

1. **`INTEGRATION_PATTERNS_CURRENT.md`** (45 mins)
   - REST API standards.
2. **`CROSS_CUTTING_MODULE_CANONICAL.md`** (30 mins)
   - Auth and Validation.
