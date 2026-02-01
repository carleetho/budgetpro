# AI Assistant Integration Patterns

> **Scope**: GitHub Copilot, Antigravity, ChatGPT, etc.

## 1. The Pattern: "Grounding Context"

AI models default to probability. To get correctness, you must inject "Grounding Context" (Truth) before the "Task".

**Formula**: `[Context] + [Constraints] + [Task] = [Code]`

## 2. Integration by Tool

### 2.1. Antigravity (Default / Preferred)

- **Mechanism**: Native Agent Tools (`view_file`, `grep_search`).
- **Context**: Can autonomously read any notebook at any time.
- **Integration**: "I will read `docs/canonical/modules/PRESUPUESTO_MODULE_CANONICAL.md` before writing this code."
- **Pros**: Full autonomy, deep reasoning, can execute "Verification" steps.

### 2.2. Cursor (Secondary)

- **Mechanism**: `@File` references in Chat.
- **Context**: Manual selection of Markdown files.
- **Pros**: Good for quick interactive queries.

### 2.3. GitHub Copilot (Chat)

- **Mechanism**: Open file context.
- **Instruction**: Keep the Canonical Notebook open in a tab. Copilot reads open tabs.
- **Pros**: Low friction.

## 3. Validation Checklist for AI Code

1. **Invariant Check**: Does the code violate any rule in `DOMAIN_INVARIANTS_CURRENT.md`?
2. **Layer Check**: Does the code import Infrastructure classes in Domain? (Strict No)
3. **Spec Alignment**: Do the field names match the JSON Schema in the Module Notebook?

## 4. Comparison

| Feature          | Cursor | Antigravity | Copilot |
| ---------------- | ------ | ----------- | ------- |
| **Deep Context** | High   | High        | Medium  |
| **Autonomy**     | Low    | High        | Low     |
| **Setup Cost**   | Low    | None        | Low     |
