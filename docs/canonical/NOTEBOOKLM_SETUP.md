# NotebookLM Setup Guide for BudgetPro

> **Target Audience**: Development Lead / Architect
> **Purpose**: Configure the AI Knowledge Engine with Canonical Notebooks.

## 1. Prerequisites

- Google Account with access to [NotebookLM](https://notebooklm.google.com).
- Access to the `budgetpro-backend` repository.

## 2. Instance Creation

1. Go to **[NotebookLM](https://notebooklm.google.com)**.
2. Click **"New Notebook"**.
3. Name it: **"BudgetPro Canonical Knowledge Engine"**.

## 3. Source Ingestion (The 14 Pillars)

You must ingest all 14 canonical notebooks to ensure complete coverage.

### 3.1. Radiography Notebooks (5 Files)

Upload the following files from `docs/canonical/radiography/`:

1. `DOMAIN_INVARIANTS_CURRENT.md`
2. `ARCHITECTURAL_CONTRACTS_CURRENT.md`
3. `DATA_MODEL_CURRENT.md`
4. `INTEGRATION_PATTERNS_CURRENT.md`
5. `MODULE_SPECS_CURRENT.md`

### 3.2. Module Notebooks (9 Files)

Upload the following files from `docs/canonical/modules/`:

1. `PRESUPUESTO_MODULE_CANONICAL.md`
2. `EVM_MODULE_CANONICAL.md`
3. `CRONOGRAMA_MODULE_CANONICAL.md`
4. `ESTIMACION_MODULE_CANONICAL.md`
5. `COMPRAS_MODULE_CANONICAL.md`
6. `BILLETERA_MODULE_CANONICAL.md`
7. `RRHH_MODULE_CANONICAL.md`
8. `INVENTARIO_MODULE_CANONICAL.md`
9. `CROSS_CUTTING_MODULE_CANONICAL.md`

### 3.3. Master Prompt

1. Upload `docs/canonical/NOTEBOOKLM_MASTER_PROMPT.md` as a source.
2. **Important**: Select this source and copy its content.
3. In the chat interface, paste the content to initialize the AI personality for the session.

## 4. Verification

- Verify that **15 sources** are listed (14 notebooks + 1 prompt).
- Run **Test Query 1** from the [Usage Guide](./NOTEBOOKLM_USAGE_GUIDE.md) to confirm grounding.

## 5. Maintenance

- **Manual Sync**: Re-upload modified notebooks when significant changes occur in the `main` branch.
- **Drift**: If the code drifts from these docs, the AI will answer based on docs. Keep docs updated!
