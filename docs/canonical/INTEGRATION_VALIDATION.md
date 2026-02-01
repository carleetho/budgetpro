# Integration Validation & Baseline

> **Goal**: Measure and reduce "Assumption-Based Errors" in AI-generated code.

## 1. Definition: Assumption-Based Error

Code that implements logic or data structures that are:

1. **Contradictory** to the specifications in Canonical Notebooks.
2. **Invented** (Hallucinated) and not present in the Notebooks or Codebase.

## 2. Baseline Measurement Methodology

**Metric**: `% of AI PRs with Assumption Errors`

### Process (2-Week Baseline)

1. **Tag**: Tag PRs generated significantly by AI with label `ai-generated`.
2. **Review**: In code review, flag errors `[Assumption Error]`.
3. **Calculate**:
   $$ Error Rate = \frac{\text{PRs with >0 Assumption Errors}}{\text{Total AI PRs}} \times 100 $$

**Target**: Reduce from ~40% (Estimated) to <5% via Canonical Context.

## 3. Validation Procedure (The Loop)

1. **Generate**: Dev uses Cursor + Notebooks.
2. **Verify**: Dev checks against `VALIDATION_CHECKLIST.md`.
3. **Review**: Tech Lead reviews against Notebooks.
4. **Correction**:
   - If Code is wrong -> Fix Code.
   - If Notebook is ambiguous -> Fix Notebook (Clarify).

## 4. Escalation

- If an AI assistant persistently hallucinates despite context, flag the specific prompt pattern in `AI_ASSISTANT_INTEGRATION.md` as "Ineffective".
