# Ambiguity Detection Protocol

> **Goal**: Systematically identify and flag unclear specifications.

## 1. What is an Ambiguity?

An ambiguity is any specification that prevents a deterministic implementation.

### Criteria

1. **Multiple Interpretations**: Logic can be read in >1 way.
2. **Missing Constraints**: "Validate X" without saying _how_.
3. **Contradiction**: Notebook A says "Required", Notebook B says "Optional".
4. **Edge Case Blindness**: Happy path defined, but error state undefined.

## 2. Flagging Syntax

Use the following block explicitly in any Canonical Notebook.

```markdown
[AMBIGUITY_DETECTED: Short Description]

**Context**: Quote the unclear text.
**Impact**: Why can't we code this?
**Possible Interpretations**:

1. Option A
2. Option B
   **Resolution Needed By**: [Date/Sprint]
```

## 3. When to Flag

- **Notebook Creation**: If you don't know, don't guess. Flag it.
- **Code Generation**: If Cursor/Copilot hallucinates a rule, it's likely an ambiguity. Flag it.
- **Audit**: If code logic != notebook logic, and both seem "plausible", flag it.

## 4. Examples

- ❌ **Ambiguous**: "The system must handle high load." (Define "high load")
- ✅ **Flagged**:
  ```markdown
  [AMBIGUITY_DETECTED: Load definition missing]
  **Context**: "Handle high load"
  **Impact**: Cannot design scaling strategy.
  **Possible Interpretations**: 100 TPS? 10k TPS?
  ```
