# AI Validation Checklist

> **Goal**: Ensure AI output is safe and compliant.

## 1. Pre-Generation

- [ ] **Context Loaded**: Is the relevant Module Notebook in the prompt context?
- [ ] **Ambiguity Scan**: Did we check for `[AMBIGUITY_DETECTED]` flags?
- [ ] **Roadmap Check**: Is this feature in the "Current" or "Next" phase?

## 2. Validation of Output (Requirements)

- [ ] **References**: Does the text cite specific Notebook Sections (e.g., "See Inv-01")?
- [ ] **Invariants**: Are business rules copied appropriately, not reinvented?
- [ ] **Terminology**: Are Entity names consistent with `DATA_MODEL_CURRENT.md`?

## 3. Validation of Output (Code)

- [ ] **Layers**: Does the code respect `ARCHITECTURAL_CONTRACTS_CURRENT.md`?
- [ ] **Contracts**: Do Method Signatures match Section 7 (Domain Services)?
- [ ] **API**: Do Endpoints match Section 8 (REST)?

## 4. Red Flags

- ⚠️ AI suggests "Standard Practice" instead of "Canonical Spec".
- ⚠️ AI invents a new field without updating Data Contracts.
- ⚠️ AI ignores a State Transition constraint.
