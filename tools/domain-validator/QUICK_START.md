# Domain Validator Tools - Usage Guide

This guide explains how to use the discovery and purity analysis tools to audit the `budgetpro` domain layer.

## Overview

The audit process consists of two steps:

1. **Discovery:** Scans the domain directory and identifies all Java files, grouping them by bounded context.
2. **Purity Analysis:** Scans the discovered files for architectural violations (Spring imports, infrastructure leakage, heavy libraries, and JPA annotations).

---

## 1. Domain Discovery

Use `discover_domain.py` to create an inventory of all Java files in the domain layer.

### Commands

```bash
# Navigate to the tool directory
cd tools/domain-validator

# Run the discovery script
python3 scripts/discover_domain.py --repo-root ../.. --output domain_inventory.json
```

### Key Output

- **[domain_inventory.json](file:///home/wazoox/Desktop/budgetpro-backend/tools/domain-validator/domain_inventory.json):** A JSON file containing the list of files grouped by context (finanzas, logistica, etc.) and file count metrics.

---

## 2. Purity Analysis

Use `analyze_purity.py` to scan the inventory for violations.

### Commands

```bash
# Run the purity analysis (requires domain_inventory.json)
python3 scripts/analyze_purity.py --input domain_inventory.json --output purity_report.json
```

### Key Output

- **[purity_report.json](file:///home/wazoox/Desktop/budgetpro-backend/tools/domain-validator/purity_report.json):** A detailed report of all detected violations, including:
  - **Context:** The bounded context where the violation occurred.
  - **File Path:** Absolute path to the offending file.
  - **Line Number:** Exact line where the violation was found.
  - **Violation Type:** `SPRING_IMPORT`, `INFRASTRUCTURE_IMPORT`, `HEAVY_LIBRARY_IMPORT`, or `PERSISTENCE_ANNOTATION`.

---

## 3. Structural Audit

Use `analyze_structure.py` to identify misplaced implementation files.

### Commands

```bash
# Run the structural audit (requires domain_inventory.json)
python3 scripts/analyze_structure.py --input domain_inventory.json --output structure_report.json
```

### Key Output

- **[structure_report.json](file:///home/wazoox/Desktop/budgetpro-backend/tools/domain-validator/structure_report.json):** Lists misplaced implementation files and relocation commands.

---

## 4. Coupling Analysis

Use `analyze_coupling.py` to detect tight coupling between aggregates.

### Commands

```bash
# Run the coupling analysis (requires domain_inventory.json)
python3 scripts/analyze_coupling.py --input domain_inventory.json --output coupling_report.json
```

### Key Output

- **[coupling_report.json](file:///home/wazoox/Desktop/budgetpro-backend/tools/domain-validator/coupling_report.json):** Identifies cross-context entity imports and direct aggregate-to-aggregate references, providing refactoring hints.

---

## 5. Consolidated Report

Use `generate_report.py` to aggregate all results into a single document.

### Commands

```bash
# Run the report generator
python3 scripts/generate_report.py --output DOMAIN_AUDIT_REPORT.md
```

### Key Output

- **[DOMAIN_AUDIT_REPORT.md](file:///home/wazoox/Desktop/budgetpro-backend/tools/domain-validator/DOMAIN_AUDIT_REPORT.md):** The final audit report with Violation Matrix, Health Status, and Action Plan.

---

## Violation Types Detected

| Code  | Type                     | Description                                                             |
| :---- | :----------------------- | :---------------------------------------------------------------------- |
| **A** | `SPRING_IMPORT`          | `org.springframework.*` - Leakage of framework into domain.             |
| **B** | `INFRASTRUCTURE_IMPORT`  | `com.budgetpro.infrastructure.*` - Direct dependency on infrastructure. |
| **C** | `HEAVY_LIBRARY_IMPORT`   | Jackson, Apache POI, SQL, or HTTP clients used in domain.               |
| **D** | `PERSISTENCE_ANNOTATION` | `@Entity`, `@Table`, `@Id`, etc. - JPA annotations in domain classes.   |

## Quick Full Run

```bash
python3 scripts/discover_domain.py --repo-root ../.. --output domain_inventory.json && \
python3 scripts/analyze_purity.py --input domain_inventory.json --output purity_report.json
```
