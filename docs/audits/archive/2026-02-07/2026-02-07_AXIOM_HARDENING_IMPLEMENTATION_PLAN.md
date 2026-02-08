# AXIOM Hardening Implementation Plan (Phase 1)

**Date:** 2026-02-07
**Status:** DRAFT
**Owner:** AXIOM Governance Team
**Baseline:** 11.7% Coverage (25/213 Files)
**Target:** 100% Coverage

## 1. Executive Summary

This plan defines the strategy to achieve 100% AXIOM domain hardening across all 7 bounded contexts of BudgetPro. Moving from the current **11.7% baseline**, we will execute a **6-phase progressive rollout** driven by a centralized configuration (`.domain-validator.yaml`) and automated Semgrep rule generation. This approach eliminates manual rule maintenance and ensures consistent enforcement of Domain-Driven Design (DDD) principles: Immutability, Encapsulation, and Boundary Integrity.

## 2. Progressive Rollout Strategy (6 Phases)

The rollout is prioritized by **Business Criticality** (Financial impact) and **Volatility** (Rate of change).

| Phase | Target Contexts                                                    | Est. Files | Priority     | Timeline | Success Criteria                                                          |
| :---- | :----------------------------------------------------------------- | :--------- | :----------- | :------- | :------------------------------------------------------------------------ |
| **0** | **Baseline (Current)**<br>_Presupuesto, Estimacion (Partial)_      | 25         | -            | **DONE** | Core financial entities hardened.                                         |
| **1** | **Catalogo**<br>_Core types, APUs, Insumos_                        | ~17        | **CRITICAL** | Day 1    | 100% Catalogo coverage.<br>Snapshot immutability enforced.                |
| **2** | **Logistica & Inventario**<br>_Movement, Kardex, Stock_            | ~52        | **HIGH**     | Day 2    | 100% Logistica coverage.<br>Inventory transaction integrity.              |
| **3** | **Finanzas Sub-contexts**<br>_Avance, Cronograma, Sobrecosto, APU_ | ~76        | **HIGH**     | Day 3    | 100% Finanzas coverage.<br>Financial consistency secured.                 |
| **4** | **RRHH**<br>_Empleado, Nomina, Cuadrilla_                          | ~23        | **MEDIUM**   | Day 4    | 100% RRHH coverage.<br>PII and labor data protection.                     |
| **5** | **General & Support**<br>_Proyecto, Shared, Organization_          | ~20        | **MEDIUM**   | Day 5    | 100% Global coverage.<br>Cross-cutting concerns validated.                |
| **6** | **Lockdown & Optimization**<br>_Full Integrity Mode_               | 213        | **LOW**      | Day 6    | **100% Hardening Verified.**<br>CI checks < 15s.<br>Zero "Soft" warnings. |

## 3. Configuration-Driven Architecture

We will transition from manual `semgrep.yml` editing to a **Generator Pattern**.

### 3.1. Configuration File: `.domain-validator.yaml`

A single source of truth defining which contexts are subject to strict hardening standards.

```yaml
# .domain-validator.yaml Schema
version: 1.0
global:
  excluded_patterns:
    - "**/*Test.java"
    - "**/config/**"

contexts:
  - name: "catalogo"
    path: "backend/src/main/java/com/budgetpro/domain/catalogo"
    strict_mode: true
    rules:
      - "entity-immutability"
      - "value-object-invariants"
      - "no-setters"

  - name: "logistica"
    path: "backend/src/main/java/com/budgetpro/domain/logistica"
    strict_mode: true # Toggled to true per phase


  # ... other contexts
```

### 3.2. Automated Rule Generation

A Python script `tools/generate_domain_rules.py` will:

1.  Read `.domain-validator.yaml`.
2.  Scan the directory tree to identify "Domain Entities" and "Value Objects" (based on package/naming conventions).
3.  Generate a dynamic Semgrep policy file `.semgrep/actions/generated-domain-hardening.yml`.

**Why?** This ensures that as new files are added to a context, they are **automatically** picked up by the hardening rules without manual config updates.

## 4. Technical Specifications

### 4.1. Python Generator Script (`tools/generate_domain_rules.py`)

- **Inputs:**
  - `.domain-validator.yaml`
  - File system scan of `backend/src/main/java/com/budgetpro/domain`
- **Logic:**
  - Iterate through configured contexts.
  - For each context with `strict_mode: true`:
    - Generate distinct Semgrep rules for that path.
    - Inject specific class names or package wildcards into the rule patterns.
- **Outputs:**
  - `.semgrep/actions/generated-domain-hardening.yml`: The actual rules file used by CI.
  - `docs/audits/latest_coverage_report.json`: Metadata for reporting.

### 4.2. Immutability & Pattern Rules

The generator will instantiate these templates for each active context:

1.  **`04-entity-final-fields`**:
    - **Pattern:** All fields in `@Entity` or Domain classes must be `private final`.
    - **Exception:** Fields marked `@Mutable` (must be explicitly whitelisted).
2.  **`05-no-public-setters`**:
    - **Pattern:** No public methods starting with `set` in Domain entities.
    - **Enforcement:** Forces use of "Rich Domain Methods" (e.g., `changeStatus()` instead of `setStatus()`).
3.  **`06-value-object-immutability`**:
    - **Pattern:** Classes in `*.model.*` types or ending in `Id` must be deeply immutable.
4.  **`07-collection-encapsulation`**:
    - **Pattern:** No returning raw mutable collections. Must return `Collections.unmodifiableList(...)` or similar.

## 5. Metrics & Reporting

To address the "23% vs 53%" discrepancy, we will implement **Strict Evidence-Based Reporting**.

### 5.1. Coverage Calculation

```python
coverage_pct = (hardened_files_count / total_domain_files_count) * 100
```

- **Hardened File:** A file residing in a path covered by an ACTIVE (blocking) rule in the generated Semgrep config.
- **Total Domain File:** Any `.java` file under `com/budgetpro/domain` (excluding Tests).

### 5.2. CI/CD Integration

- **Step 1:** Run `tools/generate_domain_rules.py` (Pre-build).
- **Step 2:** Run `semgrep --config .semgrep/actions/generated-domain-hardening.yml`.
- **Step 3:** Fail build if any **ERROR** severity rules are violated in `strict_mode` contexts.
- **Performance:** Generation + Scan must complete in < 15 seconds.

## 6. Validation Checkpoints

### Phase Acceptance Criteria

For a phase to be marked **COMPLETE**:

1.  **.domain-validator.yaml** updated to enable `strict_mode: true` for the target context.
2.  **Zero Violations** reported by Semgrep for that context.
3.  **CI Build Passes** with the new rules active.
4.  **Coverage Metric** increases by the expected file count.

## 7. Operational Complexity Triggers

- **Trigger:** If rule generation takes > 2 seconds.
  - _Response:_ Implement caching for file system scans.
- **Trigger:** If Semgrep scan takes > 10 seconds.
  - _Response:_ Split rules into "Critical" (Blocking) and "Audit" (Async) suites.
- **Trigger:** If specific files cannot be hardened immediately (Legacy debt).
  - _Response:_ Add an `exclusions` list to `.domain-validator.yaml` for granular bypass (with required "Planned Fix Date" comment).

## 8. Next Steps (Immediate)

1.  **Approval:** Review and approve this implementation plan.
2.  **Setup:** Create the `.domain-validator.yaml` skeleton.
3.  **Tooling:** Develop the `tools/generate_domain_rules.py` script.
4.  **Execution:** Launch Phase 1 (Catalogo Hardening).
