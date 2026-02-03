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

## Agent Roles and Responsibilities

### NotebookLM (Knowledge Oracle)

**Role:** Answer "what" and "why" questions based on canonical notebooks
**Input:** User queries
**Output:** Technical answers with citations
**Does NOT:** Generate formal requirements, create tasks, write code

**Example Usage:**

- "What are the invariants for budget approval?"
- "How does Compras integrate with Billetera?"
- "What is the state machine for Estimacion?"

### BrainGrid (Requirements Generator)

**Role:** Transform user requests into formal requirements and executable tasks
**Input:**

- User feature requests
- Canonical notebooks (for specifications)
- NotebookLM responses (optional clarification)
- Current codebase state
  **Output:**
- REQ-XX documents
- Implementation tasks
- Acceptance criteria

**Example Usage:**

- "I need to add budget approval validation" → Generates REQ-XX + Tasks
- "Sync notebook with code change" → Generates sync task

### Cursor/Claude Code (Code Executor)

**Role:** Implement code based on tasks and notebook specifications
**Input:**

- Task description (from BrainGrid)
- Canonical notebooks (as context)
- Current codebase
  **Output:**
- Implemented code
- Updated notebooks (if specs changed)
- PR with both code and doc changes

**Example Usage:**

- Reads Task: "Add BAC validation"
- Loads: PRESUPUESTO_MODULE_CANONICAL.md
- Implements: Validation code following invariants
- Updates: Notebook if needed

## Workflow Decision Tree

**For Simple Documentation Updates:**
NotebookLM (guidance) → Developer (manual execution) → PR

**For Feature Implementation:**
BrainGrid (requirements + tasks) → Cursor (code generation) → PR

**For Onboarding Tasks:**
ONBOARDING_FIRST_WEEK_TASKS.md → NotebookLM (clarification) → Developer (execution) → PR

**For Complex Features:**
User → BrainGrid (consults notebooks) → REQ-XX + Tasks → Cursor (consults notebooks) → Code → PR
