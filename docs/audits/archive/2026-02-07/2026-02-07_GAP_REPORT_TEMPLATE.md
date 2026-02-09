# Audit Gap Report: [MODULE/SCOPE NAME]

**Date:** 2026-02-07
**Auditor:** Antigravity

## 1. Executive Summary

_High-level overview of the audit findings. State the overall "health" of the module/scope regarding compliance._

- **Total Rules Audited:** [Number]
- **Compliance Rate:** [Percentage]%
- **Critical Gaps:** [Number]

## 2. Canonical Notebooks Gaps

_Discrepancies found between the Canonical Notebooks and the actual implementation._

| Rule ID | Description  | Gap Detail              | Priority | Recommendation |
| :------ | :----------- | :---------------------- | :------- | :------------- |
| [ID]    | [Short Desc] | [What is missing/wrong] | [H/M/L]  | [Fix Action]   |
| ...     | ...          | ...                     | ...      | ...            |

## 3. AXIOM Hardening Gaps

_Violations of AXIOM architectural hardening rules (e.g., layer violations, forbidden dependencies)._

- **Violation:** [Description]
  - **Location:** [File/Class]
  - **Impact:** [How it breaks AXIOM]
  - **Fix:** [Remediation strategy]

## 4. Business Rules Reconciliation

_Comparison of implemented logic vs. documented business rules._

- [ ] **Rule [ID]:** [Status - e.g., Implemented but Logic is Different]
  - _Expected:_ ...
  - _Actual:_ ...

## 5. Grounding Effectiveness Assessment

_Evaluation of how well the AI/Code is "grounded" in the Canonical Notebooks._

- **Ambiguities Detected:** [List areas where notebooks are vague]
- **Hallucination Risks:** [Areas where code assumes logic not in notebooks]
- **Notebook Updates Needed:** [List of notebooks requiring updates based on code realities]
