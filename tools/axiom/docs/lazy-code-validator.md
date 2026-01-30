# AXIOM Lazy Code Validator - Developer Guide

The **Lazy Code Validator** is a safeguard integrated into the AXIOM development governance tool. Its primary goal is to prevent incomplete, placeholder, or "lazy" AI-generated code from being committed to the repository.

## Overview

In modern development, AI assistants sometimes generate code skeletons or use placeholders like `TODO` it doesn't implement. This validator detects these patterns in critical regions of the BudgetPro codebase to ensure high technical standards and maintainability.

The validator runs:

1. **Locally**: via the pre-commit hook.
2. **In CI/CD**: via GitHub Actions on every Pull Request.

---

## Detected Patterns

The validator currently scans for three specific anti-patterns:

### 1. Empty Method Detection

Matches methods that have no implementation or only contain comments inside their body.

- **Example (Violation):**
  ```java
  public void calculateTotals() {
      // TODO
  }
  ```
- **Why problematic?** Indicates unfinished work that might be forgotten, leading to silent failures or technical debt.
- **How to fix:** Implement the actual logic or remove the method if it's no longer needed.

### 2. Null Returns in Persistence

Matches `return null;` or `return Optional.empty();` in the data access layer.

- **Applies to:** Files in `infrastructure/persistence/**`.
- **Example (Violation):**
  ```java
  public Optional<Budget> findById(Long id) {
      return Optional.empty(); // Lazy return
  }
  ```
- **Why problematic?** The persistence layer should either return data, an empty collection (if appropriate), or throw a meaningful exception if the state is invalid. Returning `null` or `empty()` as a placeholder bypasses error handling.
- **How to fix:** Implement the actual JPA/database query logic.

### 3. TODO/FIXME in Critical Modules

Matches `// TODO` or `// FIXME` comments in core business modules.

- **Applies to:** `domain/presupuesto/**` and `domain/estimacion/**`.
- **Example (Violation):**
  ```java
  // TODO: Verify tax calculations
  public void approveBudget() { ... }
  ```
- **Why problematic?** Critical domains like Budget (Presupuesto) and Estimation (Estimación) must be complete and verified before merge. Leave-behinds in these modules are not permitted.
- **How to fix:** Complete the implementation and remove the comment before committing.

---

## Configuration

The validator is configured in `axiom.config.yaml` at the project root:

```yaml
validators:
  lazy_code:
    enabled: true
    strictness: "blocking" # blocking | warning
```

- **enabled**: Set to `false` to disable the validator project-wide (not recommended).
- **strictness**: If set to `blocking` (default), violations will prevent the commit.

---

## Bypass Mechanism

If you have a legitimate reason to commit code that triggers a violation (e.g., a very early draft or a true planned TODO), you can bypass the check:

### 1. Local Bypass

Use the `--no-verify` flag during commit:

```bash
git commit -m "WIP: temporary todo" --no-verify
```

### 2. CI/CD Bypass

Requires a commit tag or keyword if configured, but generally, the CI build will stay red until the code is fixed.

> [!WARNING]
> Use the bypass mechanism sparingly. It exists for emergencies and edge cases. Bypassing quality checks regularly leads to technical debt.

---

## Troubleshooting

### Validator is blocking a "legitimate" empty method

Some interfaces or abstract classes might require empty methods. However, AXIOM follows a strict "no empty logic" rule for concrete classes. If you encounter a false positive, consider using a different architectural approach or implement a "No-Op" comment if absolutely necessary (though the validator is tuned to catch most basic comments).

### Hook is not running

Ensure the hook is installed:

```bash
bash tools/axiom/install_hook.sh
```

### Path issues

If the validator isn't detecting patterns in a new module, ensure the path filtering logic in `LazyCodeValidator.py` includes your new module paths.

---

## FAQ

**Q: Why are the error messages in Spanish?**
A: BudgetPro is a project with a strong presence in Spanish-speaking environments. To provide actionable, easy-to-understand feedback to all developers, we use Spanish for error descriptions and suggestions.

**Q: Can I add a new pattern?**
A: Yes. New patterns can be added by modifying the `EMPTY_METHOD_REGEX`, `NULL_RETURN_REGEX`, or `TODO_FIXME_REGEX` in `tools/axiom/validators/lazy_code_validator.py`.

**Q: How do I see the context of the error?**
A: The AXIOM report in your console will automatically show ±2 lines of code around the violation with a `→` pointing to the exact line.
