# AXIOM Domain Hardening Coverage Dashboard

**Overall**: 217/217 files (100.0%) 🟢  
**Last Updated**: 2026-02-08  
**Status**: Lockdown Complete ✅ (All Contexts Hardened)
**Documentation**: 100% Synchronized (161 Rules) 📚

---

## Coverage by Bounded Context

| Context                | Files   | Hardened | Coverage | Status             |
| ---------------------- | ------- | -------- | -------- | ------------------ |
| **Presupuesto**        | 13      | 13       | 100% ✅  | **Strict (ERROR)** |
| **Estimacion**         | 8       | 8        | 100% ✅  | **Strict (ERROR)** |
| **Catalogo**           | 17      | 17       | 100% ✅  | **Strict (ERROR)** |
| **Logistica**          | 68      | 68       | 100% ✅  | **Strict (ERROR)** |
| **Cronograma**         | 14      | 14       | 100% ✅  | **Strict (ERROR)** |
| **RRHH**               | 24      | 24       | 100% ✅  | **Strict (ERROR)** |
| **Finanzas (Avance)**  | 13      | 13       | 100% ✅  | **Strict (ERROR)** |
| **Finanzas (Alertas)** | 6       | 6        | 100% ✅  | **Strict (ERROR)** |
| **Finanzas (Other)**   | 46      | 46       | 100% ✅  | **Strict (ERROR)** |
| **Proyecto**           | 4       | 4        | 100% ✅  | **Strict (ERROR)** |
| **Shared**             | 4       | 4        | 100% ✅  | **Strict (ERROR)** |
| **TOTAL**              | **217** | **217**  | **100%** | **LOCKED DOWN**    |

---

## Performance Metrics

- **Generator Script**: ~0.2s
- **Semgrep Scan**: ~5.1s
- **Total Pipeline**: ~5.5s (Target <15s Achieved)

---

## Documentation

- **Patterns**: [`docs/hardening/AXIOM_HARDENING_PATTERNS.md`](../hardening/AXIOM_HARDENING_PATTERNS.md)
- **Edge Cases**: [`docs/hardening/AXIOM_EDGE_CASES.md`](../hardening/AXIOM_EDGE_CASES.md)

---

## Metrics (JSON for CI Parsing)

```json
{
  "total_files": 217,
  "hardened_files": 217,
  "coverage_percentage": 100.0,
  "contexts": [
    { "context": "presupuesto", "coverage": "100%", "status": "strict" },
    { "context": "estimacion", "coverage": "100%", "status": "strict" },
    { "context": "catalogo", "coverage": "100%", "status": "strict" },
    { "context": "logistica", "coverage": "100%", "status": "strict" },
    { "context": "cronograma", "coverage": "100%", "status": "strict" },
    { "context": "rrhh", "coverage": "100%", "status": "strict" },
    { "context": "finanzas_avance", "coverage": "100%", "status": "strict" },
    { "context": "finanzas_alertas", "coverage": "100%", "status": "strict" },
    { "context": "finanzas_other", "coverage": "100%", "status": "strict" },
    { "context": "proyecto", "coverage": "100%", "status": "strict" },
    { "context": "shared", "coverage": "100%", "status": "strict" }
  ],
  "last_updated": "2026-02-08T12:30:00"
}
```
