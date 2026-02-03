# Notebook Review Checklist

> **Goal**: Ensure code and canonical notebooks stay in sync.

## 1. Code Review Section

- [ ] **Spec Compliance**: Does the code implement specifications from canonical notebooks?
- [ ] **No Hidden Logic**: Are there business rules in code that are missing from the notebook?
- [ ] **Consistency**: Do field names in code match the JSON Schema in the notebook?
- [ ] **Architecture**: Does the code respect the layers defined in `ARCHITECTURAL_CONTRACTS_CURRENT.md`?

## 2. Notebook Review Section

_Applicable if specifications changed_

- [ ] **Updates Made**: Are the relevant sections (Invariants, Endpoints) updated?
- [ ] **Status Tagged**: Are new features marked with their status (e.g., ✅ or 🟡)?
- [ ] **Drift Check**: Did we remove any "Target State" items that are now "Current State"?
- [ ] **Debt Logged**: If this PR introduces technical debt, is it added to Section 11?

## 3. Alignment Validation

- [ ] **Single Truth**: Do code and notebooks tell the same story?
- [ ] **Ambiguity**: Are any new ambiguities flagged with `[AMBIGUITY_DETECTED]`?
