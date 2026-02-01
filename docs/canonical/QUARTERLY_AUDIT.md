# Quarterly Audit Process: Canonical Alignment

> **Goal**: Detect and remediate "Drift" between Code and Notebooks.
> **Frequency**: First week of Q1, Q2, Q3, Q4.

## 1. Audit Procedure

1. **Select Module**: Pick 2 high-churn modules (e.g., Presupuesto, EVM).
2. **Compare**:
   - **Code**: Read `src/main/java/com/budgetpro/domain/...`
   - **Notebook**: Read `docs/canonical/modules/...`
3. **Check**:
   - Are all `✅` Invariants actually enforced in code?
   - Are all `@Controller` endpoints listed in the notebook?
   - Are new columns in DB reflected in Data Contracts?

## 2. Metric: Drift Percentage

$$ Drift \% = \frac{\text{Misaligned Items}}{\text{Total Items Checked}} \times 100 $$

**Target**: < 10%.

## 3. Remediation

- **< 5% Drift**: Fix immediately.
- **5-10% Drift**: Create "Docs Cleanup" task (Sprint Priority).
- **> 10% Drift**: **ESCALATION**. Pause feature work on this module until docs are synced.

## 4. Escalation Path

If Drift > 10%:

1. Notify Tech Lead.
2. Schedule "Synchronization Sprint".
3. Review `SYNC_WORKFLOW.md` to understand why the PR process failed.
