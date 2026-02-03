# First Week Hands-On Tasks

> **Goal**: Merge code that is aligned with the specs.

## Task 1: The Documentation Fix (Day 4)

_Getting used to the PR process._

1. **Find** a minor discrepancy between Code and Notebook (e.g., a missing field in `DATA_MODEL_CURRENT.md`).
2. **Fix** the Notebook.
3. **Submit PR** with the `Documentation` type.
4. **Outcome**: You learned the `SYNC_WORKFLOW.md`.

## Task 2: The Validation Rule (Day 5)

_Implementing a missing business rule._

1. **Identify** a "Missing" (🔴) Invariant in your assigned module.
2. **Create Test**: Write a failing test in the Domain layer.
3. **Implement**: Add the check in the `Validator` class.
4. **Update Notebook**: Change status to ✅ in the PR.
5. **Outcome**: You touched Domain logic and kept docs in sync.

## Task 3: The API Explorer (Day 5)

_Understanding the ports._

1. **Pick** a GET endpoint.
2. **Write** an Integration Test (MockMvc) ensuring it returns the correct structure.
3. **Verify** against `INTEGRATION_PATTERNS_CURRENT.md`.
