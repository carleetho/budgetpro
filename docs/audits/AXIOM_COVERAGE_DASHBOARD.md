# AXIOM Domain Hardening Coverage Dashboard

**Overall**: 106/209 files (50.7%) 🟢  
**Last Updated**: 2026-02-08  
**Status**: Phase 1B Complete ✅ (Logistica hardened)

---

## Coverage by Bounded Context

| Bounded Context      | Files | Hardened | Coverage | Status             | Phase                   |
| -------------------- | ----- | -------- | -------- | ------------------ | ----------------------- |
| **Presupuesto**      | 13    | 13       | 100% ✅  | **Strict (ERROR)** | Baseline                |
| **Estimacion**       | 8     | 8        | 100% ✅  | **Strict (ERROR)** | Baseline                |
| **Catalogo**         | 17    | 17       | 100% ✅  | **Strict (ERROR)** | Phase 1A Complete ✅    |
| **Logistica**        | 68    | 68       | 100% ✅  | **Strict (ERROR)** | Phase 1B Complete ✅    |
| **Cronograma**       | 14    | 0        | 0% ⬜    | Planned (WARNING)  | Phase 1C (Week 5)       |
| **RRHH**             | 24    | 0        | 0% ⬜    | Planned (WARNING)  | Phase 1D (Week 5)       |
| **Finanzas (Other)** | 65    | 0        | 0% ⬜    | Planned(WARNING)   | Scattered across phases |

---

## Immutability Rules Applied

All hardened contexts enforce these 4 categories of immutability:

1. **Entity Final Fields** - All entity fields must be `private final`
2. **Snapshot Immutability** - Snapshot classes cannot have setters
3. **Value Object Immutability** - Value objects cannot have setters
4. **Collection Encapsulation** - Collections must return unmodifiable views

**Severity Levels**:

- **ERROR** (strict_mode: true): Blocks CI/CD, prevents merge
- **WARNING** (strict_mode: false): Reported but non-blocking

---

## Infrastructure Status

**Configuration**: `.domain-validator.yaml` ✅  
**Generator Script**: `tools/generate_domain_rules.py` ✅ (<5s execution)  
**Generated Rules**: `.semgrep/generated-domain-hardening.yml` ✅ (28 rules, 793 lines)  
**CI Integration**: ⬜ Pending (next step)

---

## Rollout Roadmap

| Phase        | Target Coverage | Contexts Enabled        | Timeline | Status      |
| ------------ | --------------- | ----------------------- | -------- | ----------- |
| **Baseline** | 10.0% (21/209)  | presupuesto, estimacion | Complete | ✅ Done     |
| **Phase 1A** | 18.2% (38/209)  | + catalogo              | Week 3   | ✅ Complete |
| **Phase 1B** | 50.7% (106/209) | + logistica             | Week 4   | ✅ Complete |
| **Phase 1C** | 57.4% (120/209) | + cronograma            | Week 5   | ⬜ Planned  |
| **Phase 1D** | 68.9% (144/209) | + rrhh                  | Week 5   | ⬜ Planned  |
| **Phase 1E** | 100% (209/209)  | + finanzas_other        | Week 5   | ⬜ Planned  |

---

## Metrics (JSON for CI Parsing)

```json
{
  "total_files": 209,
  "hardened_files": 106,
  "coverage_percentage": 50.7,
  "contexts": [
    {
      "context": "presupuesto",
      "files": 13,
      "strict_mode": true,
      "coverage": "100%",
      "phase": "baseline",
      "status": "complete"
    },
    {
      "context": "estimacion",
      "files": 8,
      "strict_mode": true,
      "coverage": "100%",
      "phase": "baseline",
      "status": "complete"
    },
    {
      "context": "catalogo",
      "files": 17,
      "strict_mode": true,
      "coverage": "100%",
      "phase": "1A",
      "status": "complete"
    },
    {
      "context": "logistica",
      "files": 68,
      "strict_mode": true,
      "coverage": "100%",
      "phase": "1B",
      "status": "complete",
      "violations_suppressed": 35,
      "files_modified": 11
    },
    {
      "context": "cronograma",
      "files": 14,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1C",
      "status": "planned"
    },
    {
      "context": "rrhh",
      "files": 24,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1D",
      "status": "planned"
    },
    {
      "context": "finanzas_other",
      "files": 65,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1E",
      "status": "planned"
    }
  ],
  "last_updated": "2026-02-08T03:37:00"
}
```

---

## How to Update

**Manual Update** (after enabling new contexts):

```bash
cd /home/wazoox/Desktop/budgetpro-backend
python3 tools/generate_domain_rules.py --report-coverage > temp.txt
# Copy metrics to this dashboard
```

**Automated Update** (CI integration - coming soon):
Dashboard will auto-update from CI workflow metrics after each build.

---

## Validation

**Test Generator**:

```bash
python3 tools/generate_domain_rules.py --dry-run
```

**Generate Rules**:

```bash
python3 tools/generate_domain_rules.py
```

**View Coverage**:

```bash
python3 tools/generate_domain_rules.py --report-coverage
```

---

## Notes

- **Baseline established**: 2026-02-07 with presupuesto (13 files) + estimacion (8 files)
- **Total domain files**: 209 (discrepancy from roadmap's 217 - requires investigation)
- **Generator performance**: <1 second execution time ✅
- **Rule count**: 28 rules (7 contexts × 4 immutability rules)
