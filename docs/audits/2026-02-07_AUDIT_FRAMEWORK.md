# Audit Framework & Methodology

**Date:** 2026-02-07
**Author:** Antigravity (AI Assistant)
**Scope:** Canonical Notebooks & AXIOM Hardening Audit

## 1. Introduction

This document defines the methodology for auditing the BudgetPro codebase against the Canonical Notebooks (Knowledge Engine) and AXIOM hard rules. The goal is to establish a clear baseline of compliance and identify gaps for remediation.

## 2. Audit Objectives

- **Verify Grounding:** Ensure code implements the business rules defined in Canonical Notebooks.
- **Identify Drift:** Detect discrepancies between documentation (Notebooks) and implementation (Code).
- **Harden AXIOM:** Validate that the architecture adheres to AXIOM constraints.
- **Establish Traceability:** Map every rule to a specific code location.

## 3. Audit Methodology

### 3.1. Code-to-Documentation Tracing

- **Action:** For each rule in the Traceability Matrix, search the codebase for its implementation.
- **Tooling:** `grep`, AST analysis (where possible), manual code review.
- **Outcome:** A link to the specific file/line implementing the rule, or a "MISSING" status.

### 3.2. Cross-Document Consistency Check

- **Action:** Compare "Invariants" across relevant Canonical Notebooks (e.g., Presupuesto vs. EVM) to ensure no conflicting rules exist.
- **Outcome:** Identification of logical inconsistencies in the specification.

### 3.3. Database Schema Analysis

- **Action:** Compare the `DATA_MODEL_CURRENT.md` notebook against the actual JPA entities and Liquibase changesets.
- **Outcome:** List of schema deviations (missing columns, wrong types, missing constraints).

### 3.4. GitHub Workflow Analysis

- **Action:** Review `.github/workflows` to ensure CI/CD pipelines enforce the quality gates defined in AXIOM (e.g., blast radius, preventing big bang commits).
- **Outcome:** Verification of automated governance application.

## 4. File Naming Convention & Organization

All audit artifacts must follow this convention to ensure chronological sorting and clear ownership:

- **Directory:** `docs/audits/`
- **Format:** `YYYY-MM-DD_[TYPE]_[MODULE].extension`

**Examples:**

- `2026-02-07_TRACEABILITY_MATRIX.csv` (Master Matrix)
- `2026-02-07_AUDIT_REPORT_PRESUPUESTO.md` (Module Audit)
- `2026-02-07_GAP_ANALYSIS_EVM.md` (Specific Analysis)

## 5. Success Criteria

An audit cycle is considered complete when:

1.  The **Traceability Matrix** is fully populated for the target scope.
2.  A **Gap Report** is generated summarizing findings.
3.  Each gap is assigned a **Priority** (CRITICAL, HIGH, MEDIUM, LOW).
4.  Remediation tasks are created for CRITICAL and HIGH gaps.
