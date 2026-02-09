# Audit Methodology Documentation

**Document Purpose:** Enable repeatable, systematic audits of canonical documentation, business rules, and domain hardening  
**Date:** 2026-02-07  
**Version:** 1.0  
**Author:** Antigravity AI Assistant

---

## 1. Overview

This document captures the methodology used in the Phase 2 Comprehensive Documentation Audit (Tasks 1-11) to enable future audits with consistent quality, efficiency, and repeatability.

### 1.1. Audit Framework

**4-Stage Approach:**

```
Stage 1: Discovery     →  Stage 2: Cross-Validation  →  Stage 3: Effectiveness  →  Stage 4: Synthesis
(Days 1-2)                (Days 3-4)                     (Day 5-6)                  (Day 7)
```

| Stage                | Activities                                               | Outputs                                                    | Tools/Techniques                                     |
| -------------------- | -------------------------------------------------------- | ---------------------------------------------------------- | ---------------------------------------------------- |
| **Discovery**        | Module audits, code inspection, rule extraction          | 9 module reports, gap catalog                              | Code-to-doc tracing, AST analysis                    |
| **Cross-Validation** | Radiography audits, AXIOM baseline, rules reconciliation | 5 radiography reports, AXIOM baseline, traceability matrix | Cross-document consistency, GitHub workflow analysis |
| **Effectiveness**    | AI grounding testing, hallucination risk assessment      | Grounding validation report, AI safety thresholds          | Sample query testing, hallucination catalog          |
| **Synthesis**        | Gap analysis, remediation roadmap, final report          | Gap analysis, roadmap, final report, methodology           | Prioritization matrix, effort estimation             |

---

## 2. Stage 1: Discovery (Module-by-Module Audits)

### 2.1. Objective

Assess each module's canonical notebook for completeness, accuracy, and gaps against actual code implementation.

### 2.2. Process

**Step 1: Canonical Notebook Review (30 min per module)**

1. Read canonical notebook end-to-end
2. Identify all documented business rules ('Rule IDs', statuses)
3. Note claimed maturity percentage
4. List documented use cases

**Step 2: Code Inspection (1-2 hours per module)**

1. Navigate to `backend/src/main/java/com/budgetpro/domain/[module]`
2. Inspect domain entities:
   - Constructors → invariant validation
   - `validarInvariantes()` methods → business rules
   - Domain methods → workflows and constraints
3. Inspect application layer (`com.budgetpro.application`):
   - Use case implementations → integration with entities
   - Precondition checks → application-layer enforcement
   - Event publishing → integration contracts
4. Cross-reference with canonical:
   - Which rules are documented? (Status: ✅)
   - Which rules exist in code but not in canonical? (Status: 📝 Discovered)
   - Which canonical rules are not implemented? (Status: 🔴 Missing)

**Step 3: Gap Identification (30 min)**

1. Create gap list categorized by severity:
   - **CRITICAL:** Data corruption, financial integrity, security
   - **HIGH:** Business logic violations, process bypasses
   - **MEDIUM:** Feature incompleteness, unclear documentation
   - **LOW:** Technical debt, non-blocking improvements
2. For each gap, document:
   - Gap ID (e.g., P-01, ES-01)
   - Description
   - Evidence (Class::Method, line number)
   - Impact
   - Recommended fix

**Step 4: Module Report Creation (30 min)**

1. Use template: `docs/audits/2026-02-07_AUDIT_[MODULE]_MODULE.md`
2. Sections:
   - Executive Summary (maturity %, key findings)
   - Rules Verified vs. Code
   - Newly Discovered Rules
   - Gaps by Priority
   - Recommendations

**Estimated Effort:** 3-4 hours per module × 9 modules = **27-36 hours** (3-4 days with parallelization)

---

### 2.3. Code-to-Documentation Tracing Technique

**Objective:** Ensure 100% of findings have verifiable code evidence.

**Method:**

1. For each business rule, identify the implementing code:
   - **Invariants:** `Entity::validarInvariantes()` method
   - **Workflows:** Application service methods (e.g., `CrearPartidaUseCaseImpl::ejecutar`)
   - **Calculations:** Value object or service methods (e.g., `CalculadorFSR::calcular`)

2. Document technical trace:
   - **Format:** `ClassName::methodName` (line number optional)
   - **Example:** `Presupuesto::congelar` → Rule P-01 (No modification when frozen)

3. Verify implementation status:
   - **✅ Verified:** Code implements rule correctly
   - **🟡 Partial:** Code partially implements (e.g., entity only, not application layer)
   - **🔴 Missing:** Rule documented but no code found

**Tool Support:**

- `view_code_item` for class/method navigation
- `grep_search` for finding method references
- Manual inspection for complex logic

**Quality Gate:** No gap documented without Class::Method reference.

---

### 2.4. Module Audit Template

```markdown
# Audit Report: [Module Name] Module

**Date:** [Date]  
**Module:** [Module Name]  
**Canonical Spec:** `docs/canonical/modules/[MODULE]_MODULE_CANONICAL.md`  
**Auditor:** [Name]

## 1. Executive Summary

[Maturity %, key findings, overall assessment]

## 2. Business Rules Verification

| Rule ID | Description   | Canonical Status | Actual Status | Severity                   | Evidence        |
| ------- | ------------- | ---------------- | ------------- | -------------------------- | --------------- |
| [ID]    | [Description] | [✅/🟡/🔴]       | [✅/🟡/🔴]    | [CRITICAL/HIGH/MEDIUM/LOW] | [Class::Method] |

## 3. Newly Discovered Rules

| Rule ID | Description   | Evidence        | Priority                   |
| ------- | ------------- | --------------- | -------------------------- |
| [ID]    | [Description] | [Class::Method] | [CRITICAL/HIGH/MEDIUM/LOW] |

## 4. Gaps by Priority

### CRITICAL

[List with evidence]

### HIGH

[List with evidence]

### MEDIUM

[List with evidence]

### LOW

[List with evidence]

## 5. Recommendations

[Specific, actionable recommendations]
```

---

## 3. Stage 2: Cross-Validation

### 3.1. Radiography Document Audits

**Objective:** Validate cross-cutting documentation (domain invariants, state machines, integrations, architecture, security).

**Process:**

**Step 1: Radiography Review (1 hour per document)**

1. Read radiography document end-to-end
2. Identify all documented cross-cutting rules
3. Note coverage percentage (rules documented / total inventory)

**Step 2: Cross-Reference with Canonical (1 hour)**

1. For each radiography rule, check if documented in canonical notebooks
2. Identify mismatches:
   - Rule in radiography but not in canonical
   - Rule in canonical but not in radiography
   - Contradictory descriptions

**Step 3: Code Validation (1 hour)**

1. For each radiography rule, verify code implementation
2. Check if cross-cutting concerns are consistently applied (e.g., auditability, fail-fast validation)

**Step 4: Gap Report (30 min)**

1. Document radiography-specific gaps
2. Recommend updates to align radiography ↔ canonical ↔ code

**Estimated Effort:** 3.5 hours per document × 5 documents = **17.5 hours** (2 days)

---

### 3.2. AXIOM Hardening Baseline

**Objective:** Quantify current domain hardening coverage and identify unprotected files.

**Process:**

**Step 1: Identify All Domain Files (30 min)**

1. Run: `find backend/src/main/java/com/budgetpro/domain -name "*.java" | wc -l`
2. List bounded contexts: Presupuesto, Estimacion, Cronograma, Billetera, RRHH, Logistica, Catalogo, etc.

**Step 2: Analyze Semgrep Configuration (1 hour)**

1. Review `.github/workflows/semgrep.yml`
2. Identify which files are subject to **blocking** rules (ERROR level)
3. Distinguish between:
   - **Blocking enforcement:** Immutability rules (e.g., `private final` fields, no setters)
   - **Warning only:** Suggestions that don't block builds

**Step 3: Calculate Coverage (1 hour)**

1. Count files with blocking enforcement
2. Calculate: (Hardened files / Total domain files) × 100
3. Breakdown by bounded context

**Step 4: Identify Unprotected Files (1 hour)**

1. For each bounded context, list files without hardening
2. Estimate violation count (run Semgrep in audit mode if possible)

**Step 5: Baseline Report (1 hour)**

1. Document current coverage
2. List unprotected files by context
3. Establish path to 100% (6-phase rollout)

**Estimated Effort:** 4.5 hours (0.5 days)

---

### 3.3. Business Rules Reconciliation

**Objective:** Cross-reference 161 verified rules against code, canonical, and radiography.

**Process:**

**Step 1: Load Business Rules Inventory (30 min)**

1. Open `INVENTARIO_REGLAS_EXISTENTES_FASE1.md`
2. Extract all 161 rule IDs and descriptions
3. Create tracking spreadsheet/CSV

**Step 2: Code Validation (6 hours)**

1. For each of 161 rules, locate implementing code:
   - Search by rule description keywords
   - Grep for entity/method names
   - Validate implementation status (✅/🟡/🔴)
2. Document technical trace (Class::Method)

**Step 3: Canonical Cross-Reference (3 hours)**

1. For each rule, check if documented in canonical notebooks
2. Note which modules have the rule documented
3. Identify documentation gaps (rules in inventory but not in canonical)

**Step 4: Newly Discovered Rules (2 hours)**

1. Compile all rules found in code but NOT in 161-rule inventory
2. Assign new rule IDs (e.g., B-04, C-05, E-05)
3. Document evidence

**Step 5: Traceability Matrix (2 hours)**

1. Create CSV with columns:
   - Rule ID, Description, Source, Status, Technical Trace, Canonical Reference, Module, Severity, Code Location, Notes
2. Populate with verified rules (from module audits)
3. Validate completeness

**Estimated Effort:** 13.5 hours (1.5 days)

---

## 4. Stage 3: Effectiveness Testing

### 4.1. AI Grounding Validation

**Objective:** Measure how effectively canonical notebooks prevent AI hallucinations.

**Process:**

**Step 1: Define Sample Queries (2 hours)**

1. Create 27 queries (3 per module) simulating AI development scenarios:
   - **Simple:** "How do I create a budget?"
   - **Medium:** "What happens when approval updates wallet?"
   - **Complex:** "How do I detect circular schedule dependencies?"
2. Cover diverse scenarios:
   - Invariant enforcement, workflows, formulas, edge cases, integration contracts

**Step 2: Test Grounding (4 hours)**

1. For each query, simulate AI response using ONLY canonical notebook context
2. Classify result:
   - **Complete Grounding (✅):** All preconditions, invariants, and steps correctly stated
   - **Partial Grounding (🟡):** Core logic correct but missing edge cases/preconditions
   - **Failed Grounding (🔴):** AI hallucinates incorrect logic or business rules

**Step 3: Identify Hallucination Risks (2 hours)**

1. For each failed/partial grounding, document the hallucination:
   - **ID:** PRES-H01, EST-H02, CRON-H03, etc.
   - **Severity:** CRITICAL, HIGH, MEDIUM, LOW
   - **Description:** What AI would hallucinate vs. reality
   - **Root Cause:** Missing preconditions, implicit assumptions, vague guidance

**Step 4: Calculate Metrics (1 hour)**

1. **Hallucination Prevention Rate:** (Complete + Partial) / Total × 100
2. **Context Completeness Score:** Per-module assessment (1-5 scale):
   - 5.0: Excellent (all context, few risks)
   - 4.0: Good (minor gaps)
   - 3.0: Adequate (moderate gaps)
   - 2.0: Poor (significant gaps)
   - 1.0: Critical (dangerous for AI)

**Step 5: Grounding Report (2 hours)**

1. Overall metrics (prevention rate, context score)
2. Per-module scores
3. Hallucination risk catalog (23 risks)
4. Recommendations (18 improvements)

**Estimated Effort:** 11 hours (1.5 days)

---

### 4.2. Context Completeness Scoring Rubric

| Score               | Criteria                                                                                                               | AI Safety                     |
| ------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------- |
| **5.0 (Excellent)** | All use cases documented, preconditions explicit, formulas stated, integration contracts clear, <5% hallucination risk | ✅ Safe for code generation   |
| **4.0 (Good)**      | Most use cases documented, some preconditions missing, formulas mostly stated, 5-10% risk                              | ✅ Safe for code generation   |
| **3.0 (Adequate)**  | Core use cases documented, implicit assumptions present, formulas vague, 10-20% risk                                   | 🟡 Assisted with review       |
| **2.0 (Poor)**      | Many use cases missing, significant assumptions, formulas absent, 20-40% risk                                          | 🟡 Questions only recommended |
| **1.0 (Critical)**  | Skeletal documentation, most features missing, >40% hallucination risk                                                 | ❌ AI PROHIBITED              |

---

## 5. Stage 4: Synthesis

### 5.1. Comprehensive Gap Analysis

**Objective:** Consolidate all findings into actionable gap analysis.

**Process:**

**Step 1: Aggregate Gaps (2 hours)**

1. Compile gaps from:
   - 9 module audits
   - 5 radiography audits
   - AXIOM hardening baseline
   - Business rules reconciliation
   - Grounding effectiveness assessment
2. Total count (61 gaps in this audit)

**Step 2: Categorize and Prioritize (2 hours)**

1. By severity: CRITICAL (12), HIGH (23), MEDIUM (18), LOW (8)
2. By category:
   - Canonical notebooks gaps
   - AXIOM hardening gaps
   - Process/integration gaps

**Step 3: Create Gap Analysis Report (4 hours)**

1. Executive summary (coverage metrics, critical findings)
2. Per-module gap analysis (9 modules)
3. AXIOM hardening assessment (11.7% coverage, 188 unprotected files)
4. Business rules reconciliation (215 total rules)
5. Grounding effectiveness (3.2/5.0 score, 77% prevention)
6. Cross-cutting insights (systemic patterns)

**Estimated Effort:** 8 hours (1 day)

---

### 5.2. Prioritized Remediation Roadmap

**Objective:** Transform gaps into actionable implementation plan.

**Process:**

**Step 1: Define Phases (2 hours)**

1. Align with requirement structure:
   - Phase 0: Critical Gaps (Week 1)
   - Phase 1: AXIOM Hardening (Weeks 2-5)
   - Phase 2: Documentation Audit (Weeks 6-9)
   - Phase 3: Validation & Handoff (Week 10)

**Step 2: Effort Estimation (3 hours)**

1. For each gap, estimate effort:
   - **Simple fix:** 30 min - 2 hours (e.g., add precondition section)
   - **Medium fix:** 3-6 hours (e.g., implement missing check)
   - **Complex fix:** 1-3 days (e.g., full use case implementation)
2. Aggregate by phase

**Step 3: Dependency Mapping (2 hours)**

1. Identify dependencies (e.g., radiography updates before canonical updates)
2. Identify parallel work opportunities
3. Create Mermaid dependency diagram

**Step 4: Success Metrics (1 hour)**

1. Define measurable outcomes per phase
2. Establish validation checkpoints
3. Create KPI table (baseline → target)

**Step 5: Roadmap Document (4 hours)**

1. Phase-by-phase breakdown with timelines
2. Resource allocation (team composition)
3. Risk mitigation strategies
4. Gantt chart

**Estimated Effort:** 12 hours (1.5 days)

---

### 5.3. Final Audit Report

**Objective:** Create executive-ready stakeholder report.

**Process:**

**Step 1: Acceptance Criteria Validation (2 hours)**

1. Review requirement's 6 acceptance criteria
2. For each criterion, document evidence of completion
3. Create validation checklist

**Step 2: Consolidate Findings (3 hours)**

1. Executive summary (1-2 pages)
2. Key findings consolidation (gap analysis summary)
3. Remediation roadmap summary
4. Risk assessment
5. Success metrics
6. Implementation handoff

**Step 3: Quality Review (1 hour)**

1. Verify all findings have code evidence
2. Check traceability matrix completeness
3. Validate timeline compliance

**Step 4: Stakeholder Packaging (2 hours)**

1. Executive summary (1 page)
2. Technical briefing slides (optional)
3. Implementation kickoff agenda

**Estimated Effort:** 8 hours (1 day)

---

## 6. Tools and Techniques

### 6.1. Code Navigation Tools

| Tool                | Purpose                           | Usage                             |
| ------------------- | --------------------------------- | --------------------------------- |
| `view_file_outline` | See all classes/methods in a file | First step when inspecting a file |
| `view_code_item`    | View specific class or method     | Drill down to rule implementation |
| `grep_search`       | Find method/class references      | Locate where rules are enforced   |
| `find_by_name`      | Discover domain files             | Identify all entities in a module |

### 6.2. Evidence Collection

**Screenshot/Code Snippets:**

- For each gap, include code snippet showing the issue
- For verified rules, include code snippet showing correct implementation
- Use markdown code blocks with line numbers

**Technical Traces:**

- Format: `ClassName::methodName` (e.g., `Presupuesto::congelar`)
- Include line numbers when critical (e.g., line 87: missing check)
- Link to file when possible: `file:///path/to/File.java#L45-L67`

---

## 7. Quality Gates and Validation

### 7.1. Per-Module Audit Quality Gate

Before considering a module audit complete, verify:

- [ ] All documented rules cross-referenced with code (✅/🟡/🔴 status assigned)
- [ ] All discovered rules documented with evidence
- [ ] All gaps have severity rating (CRITICAL/HIGH/MEDIUM/LOW)
- [ ] All gaps have code reference (Class::Method)
- [ ] Module report follows template structure
- [ ] Estimated maturity percentage validated against actual documentation

### 7.2. Overall Audit Quality Gate

Before finalizing the audit, verify:

- [ ] All 6 acceptance criteria from requirement met
- [ ] Traceability matrix covers 161+ rules
- [ ] Gap analysis includes all modules (9) and radiography (5)
- [ ] Remediation roadmap has phases, timelines, and effort estimates
- [ ] All findings evidence-based (100% code references)
- [ ] Timeline compliance confirmed (1-week deadline)
- [ ] Final report is stakeholder-ready (executive summary, actionable recommendations)

---

## 8. Lessons Learned and Improvements

### 8.1. What Worked Well (Replicate in Future Audits)

**✅ Parallel Execution:**

- Audited 9 modules concurrently (Tasks 2 and 7)
- Reduced calendar time from 7 days to 7 days (vs. 20 days sequential)

**✅ Evidence-Based Approach:**

- 100% code references eliminated speculation
- Class::Method traces enable rapid validation

**✅ Systematic Methodology:**

- Repeatable per-module template
- Consistent metrics (maturity %, grounding score)

**✅ Grounding Validation:**

- 27 sample queries confirmed canonical notebooks effective (77%)
- Hallucination risk catalog provides actionable improvements

---

### 8.2. Challenges and Mitigations

**🔴 Challenge: Documentation Lag**

- **Issue:** 78.3% of rules undocumented
- **Mitigation:** Automated rule discovery (parse `validarInvariantes()` methods)
- **Future:** CI integration to detect undocumented rules on commit

**🟡 Challenge: AXIOM Coverage Inflation**

- **Issue:** Self-reported "23%" was inflated (warnings vs. blocking)
- **Mitigation:** Strict verification (only count ERROR-level Semgrep rules)
- **Future:** Automated coverage reporting dashboard

**🟡 Challenge: Implicit Assumptions**

- **Issue:** 35% of grounding gaps due to unstated domain knowledge
- **Mitigation:** Add "Common Mistakes" sections to canonical notebooks
- **Future:** AI query testing in CI to detect implicit assumptions

---

### 8.3. Recommendations for Future Audits

**Recommendation 1: Quarterly Audits**

- Schedule regular audits (every 3 months)
- Focus on incremental validation vs. comprehensive scope
- Track improvement metrics (documentation coverage %, grounding score)

**Recommendation 2: Automated Rule Discovery**

- Develop AST parser for `validarInvariantes()` methods
- Auto-generate rule stubs for canonical promotion
- Detect drift when code changes without doc updates

**Recommendation 3: Continuous Grounding Validation**

- CI integration: Test 10 queries per module on every commit
- Alert when grounding score drops below 3.5/5.0
- Monthly grounding reports to leadership

**Recommendation 4: AXIOM Coverage Dashboard**

- Real-time coverage metric (% hardened)
- Violations count per bounded context
- CI time tracking (<15s target)

---

## 9. Appendices

### Appendix A: Audit Checklist

**Pre-Audit:**

- [ ] Define scope (modules, radiography docs, business rules inventory)
- [ ] Set timeline (1 week urgent vs. 2 weeks comprehensive)
- [ ] Assign auditor(s)
- [ ] Gather baseline documents (canonical notebooks, radiography, rules inventory)

**Discovery Stage (Days 1-2):**

- [ ] Module audits completed (9 modules)
- [ ] Gap catalog created
- [ ] Discovered rules documented

**Cross-Validation Stage (Days 3-4):**

- [ ] Radiography audits completed (5 documents)
- [ ] AXIOM baseline established
- [ ] Business rules reconciled (161 rules)
- [ ] Traceability matrix created

**Effectiveness Stage (Days 5-6):**

- [ ] Grounding validation completed (27 queries)
- [ ] Hallucination risks cataloged (23 risks)
- [ ] Context completeness scores assigned

**Synthesis Stage (Day 7):**

- [ ] Gap analysis report created
- [ ] Remediation roadmap delivered
- [ ] Final audit report completed
- [ ] Acceptance criteria validated (6 criteria)

**Post-Audit:**

- [ ] Stakeholder review scheduled
- [ ] Implementation handoff prepared
- [ ] Lessons learned documented

### Appendix B: Useful Commands

**Find all domain files:**

```bash
find backend/src/main/java/com/budgetpro/domain -name "*.java" | wc -l
```

**Search for specific rule enforcement:**

```bash
grep -rn "validarInvariantes" backend/src/main/java/com/budgetpro/domain
```

**Count rules in canonical notebook:**

```bash
grep -c "^##\s*Rule" docs/canonical/modules/PRESUPUESTO_MODULE_CANONICAL.md
```

**List files with Semgrep violations:**

```bash
semgrep scan --config .semgrep/rules/ --sarif > violations.sarif
```

---

**End of Audit Methodology Documentation**

**Version:** 1.0  
**Next Review:** After next audit cycle (3 months)  
**Owner:** Engineering Documentation Team
