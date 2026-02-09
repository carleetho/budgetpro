# Phase 3 Violations Report - Remaining Contexts

**Date:** 2026-02-09
**Scan Method:** Static Analysis (Heuristic + Nosemgrep Detection)
**Contexts Scanned:** finanzas_other, proyecto, shared

## Overall Summary
- **Total Violations:** 61
- **ERROR Severity:** 60 (blocking)
- **WARNING Severity:** 1 (non-blocking)
- **Estimated Remediation:** 5.1 hours

## Violations by Context
| Context | Priority | ERROR | WARNING | Total | Effort |
|---------|----------|-------|---------|-------|--------|
| proyecto | CRITICAL | 3 | 0 | 3 | 0.2 hours |
| finanzas_other | HIGH | 57 | 1 | 58 | 4.8 hours |
| shared | HIGH | 0 | 0 | 0 | 0.0 hours |

## Violations by Rule Type
| Rule | Severity | Count | Avg Fix Time | Total Effort |
|------|----------|-------|--------------|--------------|
| entity-final-fields | ERROR | 60 | 5 min | 5.0 hours |
| collection-encapsulation | WARNING | 1 | 5 min | 0.1 hours |

## Detailed Violations
### proyecto Context (CRITICAL)
#### entity-final-fields (ERROR)
- `backend/src/main/java/com/budgetpro/domain/proyecto/model/Proyecto.java:18` - entity-final-fields (Suppressed)
- `backend/src/main/java/com/budgetpro/domain/proyecto/model/Proyecto.java:20` - entity-final-fields (Suppressed)
- `backend/src/main/java/com/budgetpro/domain/proyecto/model/Proyecto.java:22` - entity-final-fields (Suppressed)

### finanzas_other Context (HIGH)
#### entity-final-fields (ERROR)
- `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java:48` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/model/Billetera.java:51` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:11` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:13` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:15` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:17` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:19` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:21` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java:24` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:11` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:13` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:15` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:17` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:19` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:21` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:23` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java:26` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/ApuInsumo.java:23` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/ApuInsumo.java:25` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/ApuInsumo.java:27` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/APU.java:28` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/APU.java:30` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/APU.java:34` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:27` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:29` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:31` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:33` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:35` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:37` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:39` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:41` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:43` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java:46` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/consumo/model/ConsumoPartida.java:29` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/consumo/model/ConsumoPartida.java:31` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/consumo/model/ConsumoPartida.java:33` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/consumo/model/ConsumoPartida.java:35` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/consumo/model/ConsumoPartida.java:38` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/recurso/model/Recurso.java:21` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/recurso/model/Recurso.java:24` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/recurso/model/Recurso.java:28` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/IndicePrecios.java:21` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:33` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:35` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:37` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:39` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:41` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:43` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:45` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:47` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:49` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:51` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:53` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:55` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:57` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:59` - entity-final-fields
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:64` - entity-final-fields

#### collection-encapsulation (WARNING)
- `backend/src/main/java/com/budgetpro/domain/finanzas/reajuste/model/EstimacionReajuste.java:227` - collection-encapsulation

