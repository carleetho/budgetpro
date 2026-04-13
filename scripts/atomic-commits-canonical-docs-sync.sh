#!/usr/bin/env bash
# Commits atómicos SOLO documentación canónica (sync 2026-04-12).
# Ejecutar desde la raíz del repo: ./scripts/atomic-commits-canonical-docs-sync.sh
#
# Prerrequisitos:
#   - Código Java ya commiteado en commits separados (AXIOM blast radius).
#   - ./axiom.sh pasa (los .md no suelen disparar validación Java, pero verifica hooks).
# Si git falla con "unknown option trailer": docs/TROUBLESHOOTING_GIT_COMMIT.md
#
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

echo "=== Commit 1: Radiografía + índice alineación + log revisión ==="
git add \
  docs/canonical/radiography/MODULE_CODE_ALIGNMENT_INDEX.md \
  docs/canonical/radiography/MODULE_SPECS_CURRENT.md \
  docs/canonical/radiography/DATA_MODEL_CURRENT.md \
  docs/canonical/radiography/CODE_DOC_REVIEW_LOG.md
git commit -m "docs(radiography): sync MODULE_SPECS, DATA_MODEL, alignment index

- Code-first inventory §2.0 and updated 17-module table
- Flyway V27–V33 documented; almacén/marketing/reajuste DDL notes corrected
- Comparative review log (CODE_DOC_REVIEW_LOG)"

echo "=== Commit 2: Módulos finanzas (APU, Recursos, Billetera, Estimación, EVM, Presupuesto) ==="
git add \
  docs/canonical/modules/APU_MODULE_CANONICAL.md \
  docs/canonical/modules/RECURSOS_MODULE_CANONICAL.md \
  docs/canonical/modules/BILLETERA_MODULE_CANONICAL.md \
  docs/canonical/modules/ESTIMACION_MODULE_CANONICAL.md \
  docs/canonical/modules/EVM_MODULE_CANONICAL.md \
  docs/canonical/modules/PRESUPUESTO_MODULE_CANONICAL.md
git commit -m "docs(canonical): sync finanzas modules REST and maturity

- APU GET endpoints; BilleteraQueryController; Estimacion GET list/detail
- Recursos GET/PUT; EVM metric; Presupuesto Partidas table"

echo "=== Commit 3: Módulos partidas + logística + marketing ==="
git add \
  docs/canonical/modules/PARTIDAS_MODULE_CANONICAL.md \
  docs/canonical/modules/INVENTARIO_MODULE_CANONICAL.md \
  docs/canonical/modules/COMPRAS_MODULE_CANONICAL.md \
  docs/canonical/modules/MARKETING_MODULE_CANONICAL.md
git commit -m "docs(canonical): sync partidas, inventario, compras, marketing

- Partidas GET and WBS; almacén GET movimientos; transferencias REST
- Proveedor CRUD, OC pagination and reject; marketing internal GET + V30"

echo "=== Commit 4: Cross-cutting, maturity viz, SYNC workflow, README, radiografía secundaria ==="
git add \
  docs/canonical/modules/CROSS_CUTTING_MODULE_CANONICAL.md \
  docs/canonical/MATURITY_VISUALIZATION.md \
  docs/canonical/SYNC_WORKFLOW.md \
  docs/canonical/README.md \
  docs/canonical/radiography/DOMAIN_INVARIANTS_CURRENT.md \
  docs/canonical/radiography/ARCHITECTURAL_CONTRACTS_CURRENT.md \
  docs/canonical/radiography/INTEGRATION_PATTERNS_CURRENT.md
git commit -m "docs(canonical): cross-cutting, maturity, sync workflow, README

- GlobalExceptionHandler / ErrorResponses; drift recovery §5
- Maturity visualization; README 17 modules; radiography pointers"

echo ""
echo "=== Listo. Estado git: ==="
git status
