# Canonical Synchronization Workflow

> **Goal**: Keep Canonical Notebooks as the "Living Source of Truth".

## 0. Code-First Validation

Cuando evalúes el estado de un módulo (madurez, features, cobertura), **valida contra código y git** antes de confiar en los `.md`. La documentación puede estar desincronizada. La fuente de verdad para "qué está implementado" es el código; el canonical debe reflejarlo 1:1.

## 1. When to Update

Update notebooks **BEFORE** or **DURING** the PR, never after.

| Change Type       | Action in Notebook                                        |
| ----------------- | --------------------------------------------------------- |
| **New Feature**   | Update **Use Cases** (Mark as P0/Completed).              |
| **Business Rule** | Update **Invariants** (Add new rule ID, e.g., P-07).      |
| **API/DTO**       | Update **REST Endpoints** and **Data Contracts**.         |
| **Bug Fix**       | If logic changed, update **Invariants** or **Tech Debt**. |

## 2. How to Update

1. **Locate**: Find the relevant module in `docs/canonical/modules/`.
2. **Edit**: Update the specific section (e.g., changing a Schema property).
3. **Commit**: Include the `.md` change in the **Same Commit/PR** as the Java code.
4. **Verify**: Use `REVIEW_CHECKLIST.md`.

## 3. Review Process

1. **PR Template**: Developer fills out the "Canonical Notebook Updates" section.
2. **Reviewer Check**: Reviewer verifies that:
   - If code changes logic -> Notebook is updated.
   - If Notebook is updated -> Code matches it.
3. **Approval**: PR is only approved when alignment is 100%.

## 5. Sync masiva (recuperación de drift)

Si acumularon muchos cambios de código sin notebook en el mismo ciclo, ejecutar una pasada **code-first** y actualizar el hub [`radiography/MODULE_CODE_ALIGNMENT_INDEX.md`](radiography/MODULE_CODE_ALIGNMENT_INDEX.md) + [`CODE_DOC_REVIEW_LOG.md`](radiography/CODE_DOC_REVIEW_LOG.md). Preferir commits atómicos por módulo (ver `scripts/atomic-commits-canonical-docs-sync.sh`).

## 4. Drift Management

If you find a discrepancy (Drift):

- **Small (<5 mins)**: Fix it immediately in your current PR.
- **Large (>1 hour)**: Log a "Documentation Fix" ticket and link it to the notebook.
