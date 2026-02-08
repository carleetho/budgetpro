# AXIOM Domain Hardening Coverage Dashboard

**Overall**: 38/209 files (18.2%) 🟡  
**Last Updated**: 2026-02-07  
**Status**: Phase 1A Complete ✅ (Catalogo hardened)

---

## Coverage by Bounded Context

| Bounded Context      | Files | Hardened | Coverage | Status             | Phase                   |
| -------------------- | ----- | -------- | -------- | ------------------ | ----------------------- |
| **Presupuesto**      | 13    | 13       | 100% ✅  | **Strict (ERROR)** | Baseline                |
| **Estimacion**       | 8     | 8        | 100% ✅  | **Strict (ERROR)** | Baseline                |
| **Catalogo**         | 17    | 17       | 100% ✅  | **Strict (ERROR)** | Phase 1A Complete ✅    |
| **Cronograma**       | 14    | 0        | 0% ⬜    | Planned (WARNING)  | Phase 1C (Week 5)       |
| **Logistica**        | 68    | 0        | 0% ⬜    | Planned (WARNING)  | Phase 1B (Week 4)       |
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

| Phase        | Target Coverage | Contexts Enabled        | Timeline | Status     |
| ------------ | --------------- | ----------------------- | -------- | ---------- |
| **Baseline** | 10.0% (21/209)  | presupuesto, estimacion | Complete | ✅ Done    |
| **Phase 1A** | 19.4% (42/209)  | + catalogo              | Week 3   | ⬜ Planned |
| **Phase 1B** | 52.6% (112/209) | + logistica             | Week 4   | ⬜ Planned |
| **Phase 1C** | 60.3% (126/209) | + cronograma            | Week 5   | ⬜ Planned |
| **Phase 1D** | 71.8% (150/209) | + rrhh                  | Week 5   | ⬜ Planned |
| **Phase 1E** | 100% (209/209)  | + finanzas_other        | Week 5   | ⬜ Planned |

---

## Metrics (JSON for CI Parsing)

```json
{
  "total_files": 209,
  "hardened_files": 21,
  "coverage_percentage": 10.0,
  "contexts": [
    {
      "context": "presupuesto",
      "files": 13,
      "strict_mode": true,
      "coverage": "100%",
      "phase": "baseline"
    },
    {
      "context": "estimacion",
      "files": 8,
      "strict_mode": true,
      "coverage": "100%",
      "phase": "baseline"
    },
    {
      "context": "cronograma",
      "files": 14,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1C"
    },
    {
      "context": "catalogo",
      "files": 17,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1A"
    },
    {
      "context": "logistica",
      "files": 68,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1B"
    },
    {
      "context": "rrhh",
      "files": 24,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1D"
    },
    {
      "context": "finanzas_other",
      "files": 65,
      "strict_mode": false,
      "coverage": "0%",
      "phase": "1E"
    }
  ],
  "last_updated": "2026-02-07T21:38:00"
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
