# AI Agent Integration Protocol

> **Goal**: Ensure Agents (Antigravity, BrainGrid, Cursor) obey the Canonical Notebooks.

## 1. Context Selection Strategy

Agents have limited context windows. Load only what matters.

### Priority 1: Critical (Always Load)

- **Module Notebook**: `docs/canonical/modules/[MODULE]_MODULE_CANONICAL.md`
- **Architecture**: `docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md`

### Priority 2: Important (Task Dependent)

- **Data Modeling**: `docs/canonical/radiography/DATA_MODEL_CURRENT.md`
- **API Design**: `docs/canonical/radiography/INTEGRATION_PATTERNS_CURRENT.md`
- **Business Logic**: `docs/canonical/radiography/DOMAIN_INVARIANTS_CURRENT.md`

## 2. Handling Knowledge Gaps

### Rule 1: Respect `[AMBIGUITY_DETECTED]`

- **Action**: STOP.
- **Output**: "The specification for [X] is flagged as ambiguous in [Notebook]. Please clarify."

### Rule 2: Missing Specifications

- **Action**: ASK.
- **Output**: "I cannot find the validation rule for [Y] in Section 2 (Invariants). Please provide it."

### Rule 3: Code vs Notebook Conflict

- **Authority**: NOTEBOOK > CODE.
- **Output**: "The code does X, but the Notebook specifies Y. I will proceed with Y unless instructed otherwise."

## 3. BrainGrid Requirements Agent Workflow

1. **Check**: Does `docs/canonical/` exist?
2. **Investigation**: Load Module Notebook. Extract Invariants/Contracts.
3. **Generation**:
   - Copy Invariants verbatim.
   - Reference Section numbers.
   - Flag any conflicts with current implementation.
