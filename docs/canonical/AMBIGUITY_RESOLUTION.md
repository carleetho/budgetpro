# Ambiguity Resolution Workflow

> **Goal**: Turn `[AMBIGUITY_DETECTED]` into `[SPECIFICATION]`.

## 1. The Decision Tree

1. **Is it Standard?** -> Check `CONSTRUCTION_STANDARDS_REFERENCE.md`.
2. **Is it Pattern?** -> Check existing code behavior.
3. **Is it Business?** -> Ask Domain Expert / PO.

## 2. Workflows

### Path A: Industry Standard Exists (Fast Path)

1. Found standard (e.g., PMI says "EAC = AC + ETC").
2. **Action**: Apply standard immediately.
3. **Update**: Replace flag with spec + Citation.

### Path B: Business Decision Required (Slow Path)

1. No standard applies (e.g., "Approval Threshold").
2. **Action**: Create Jira Task / Slack Decision Record.
3. **Update**: Wait for decision, then update Notebook.

## 3. Resolution Log Template

Append this to the `Technical Debt & Risks` or a dedicated `Changelog` section in the module notebook.

```markdown
### Resolved: [Ambiguity ID/Title]

- **Date**: YYYY-MM-DD
- **Decision**: [The chosen logic]
- **Rationale**: [Why? Ref standard/business rule]
- **Status**: Flag removed.
```

## 4. Escalation

- **L1**: Tech Lead (Technical ambiguity).
- **L2**: Product Owner (Feature ambiguity).
- **L3**: Stakeholder (Legal/Compliance ambiguity).
