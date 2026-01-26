#!/bin/bash

echo "🔍 Validating Refactoring Results..."

# 1. Re-run Audit Suite
python3 scripts/discover_domain.py --repo-root ../.. --output domain_inventory.json
python3 scripts/analyze_purity.py --input domain_inventory.json --output purity_report.json
python3 scripts/analyze_structure.py --input domain_inventory.json --output structure_report.json
python3 scripts/analyze_coupling.py --input domain_inventory.json --output coupling_report.json
python3 scripts/generate_report.py --output DOMAIN_AUDIT_REPORT_POST.md

# 2. Check for Specific Improvements
echo "📊 Improvement Check:"

INFRA_LEAKS=$(grep -c "🔴 CRITICAL" purity_report.json)
MISPLACED=$(grep -c "🟠 HIGH" structure_report.json)

echo "  - Infrastructure Leaks remaining: $INFRA_LEAKS"
echo "  - Misplaced Impls remaining: $MISPLACED"

if [ "$MISPLACED" -eq "0" ]; then
    echo "✅ Structural refactoring successful (0 misplaced files)."
fi

echo "✨ Validation complete. See DOMAIN_AUDIT_REPORT_POST.md for details."
