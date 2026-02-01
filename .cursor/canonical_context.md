# Canonical Notebooks Index for Cursor

> **Usage**: Reference this file (`@canonical_context.md`) to quickly find the right context file for your task.

## 1. Radiography (Current State)

> Use these for understanding the "As-Is" architecture and constraints.

- **[Invariants](../docs/canonical/radiography/DOMAIN_INVARIANTS_CURRENT.md)**: Business rules and constraints.
- **[Architecture](../docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md)**: Layer strictness and contracts.
- **[Data Model](../docs/canonical/radiography/DATA_MODEL_CURRENT.md)**: Entity schemas and state machines.
- **[Integration](../docs/canonical/radiography/INTEGRATION_PATTERNS_CURRENT.md)**: REST APIs and external adapters.
- **[Module Specs](../docs/canonical/radiography/MODULE_SPECS_CURRENT.md)**: Maturity assessment.

## 2. Prescriptive Modules (Target State)

> Use these when implementing features for a specific domain.

- **[Presupuesto](../docs/canonical/modules/PRESUPUESTO_MODULE_CANONICAL.md)**
- **[EVM](../docs/canonical/modules/EVM_MODULE_CANONICAL.md)**
- **[Cronograma](../docs/canonical/modules/CRONOGRAMA_MODULE_CANONICAL.md)**
- **[Estimacion](../docs/canonical/modules/ESTIMACION_MODULE_CANONICAL.md)**
- **[Compras](../docs/canonical/modules/COMPRAS_MODULE_CANONICAL.md)**
- **[Billetera](../docs/canonical/modules/BILLETERA_MODULE_CANONICAL.md)**
- **[RRHH](../docs/canonical/modules/RRHH_MODULE_CANONICAL.md)**
- **[Inventario](../docs/canonical/modules/INVENTARIO_MODULE_CANONICAL.md)**
- **[Cross-Cutting](../docs/canonical/modules/CROSS_CUTTING_MODULE_CANONICAL.md)**

## 3. Context Selection Strategy

- **New Feature**: Load `Relevant Module Canonical` + `Architecture`.
- **Database Change**: Load `Data Model` + `Relevant Module Canonical`.
- **API Change**: Load `Integration Patterns` + `Relevant Module Canonical`.
