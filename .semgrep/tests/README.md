# Semgrep Test Framework

This directory mirrors the `rules/` structure and contains test cases for all custom rules.

## Test File Requirements

Every rule file `rules/<category>/<rule>.yaml` must have a corresponding test file `tests/<category>/<rule>.<ext>`.

### Test Case Structure

Include at least:

- **2+ Positive Cases**: Code that SHOULD trigger the rule. Mark with `// ruleid: <rule-id>`.
- **2+ Negative Cases**: Code that SHOULD NOT trigger the rule. Mark with `// ok: <rule-id>`.

## Running Tests

To verify rules locally, run:

```bash
semgrep --test --config .semgrep/rules/
```

This will automatically find tests that match rule files.
