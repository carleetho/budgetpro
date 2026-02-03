# NotebookLM Usage Guide

> **Role**: Lead Requirements Engineer & Construction Domain Architect
> **Inputs**: 14 Canonical Notebooks

## 1. Standard Workflow

1. **Consult First**: Before starting a ticket, query NotebookLM for existing invariants and patterns.
2. **Refine Context**: If the answer is vague, reference the specific module (e.g., "In the context of EVM...").
3. **Verify**: Check the **Citations** provided. If no citation is given, treat the answer as a potential hallucination.

## 2. Prompting Strategy

- **Bad Prompt**: "How does budget work?" (Too broad)
- **Good Prompt**: "What are the invariants for freezing a budget in the Presupuesto module?" (Specific, technical)

## 3. Interpreting Responses

- **Direct Answer**: The AI should be technical and concise.
- **[AMBIGUITY_DETECTED]**: Means the notebooks are missing this info. Do not invent it; ask the PO.
- **Citations**: Click the number [1] to jump to the source Markdown file.

## 4. Validation Test Suite

Run these queries to validate the instance configuration.

| ID     | Query                                                                  | Expected Outcome                                                                |
| ------ | ---------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| **Q1** | _"What are the invariants for budget approval in Presupuesto module?"_ | List of `P-01`...`P-06` with status ✅/🔴.                                      |
| **Q2** | _"What REST endpoints are currently implemented for Estimación?"_      | List from `INTEGRATION_PATTERNS_CURRENT.md` + `ESTIMACION_MODULE_CANONICAL.md`. |
| **Q3** | _"What is the state machine for Compra entity?"_                       | `PENDIENTE` -> `APROBADA` etc. from `DATA_MODEL_CURRENT.md`.                    |
| **Q4** | _"What are the P0 use cases for EVM module in the next 3 months?"_     | Prioritized list from `EVM_MODULE_CANONICAL.md`.                                |
| **Q5** | _"How does Billetera integrate with Presupuesto?"_                     | Indirectly via Estimacion/Compras. Should flag if direct link exists.           |
| **Q6** | _"What is the technical debt for RRHH module?"_                        | Reference to "Complex Regime" risks from `RRHH_MODULE_CANONICAL.md`.            |

## 5. Ambiguity Test

- **Query**: _"What is the specific algorithm for calculating 'Horas de Lluvia' impact?"_
- **Expected**: [AMBIGUITY_DETECTED] (This detail is not in current skeletal docs).
