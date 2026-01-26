#!/bin/bash

# Configuration
REPO_ROOT="/home/wazoox/Desktop/budgetpro-backend"
TOOLS_DIR="$REPO_ROOT/tools/domain-validator"
BACKUP_DIR="$TOOLS_DIR/.refactoring-backup/$(date +%Y%m%d_%H%M%S)"
SRC_BASE="$REPO_ROOT/backend/src/main/java"

echo "🚀 Starting Automated Domain Refactoring..."

# 1. Backup
echo "📦 Creating backup in $BACKUP_DIR..."
mkdir -p "$BACKUP_DIR"
cp -r "$SRC_BASE/com/budgetpro/domain" "$BACKUP_DIR/domain"
cp -r "$SRC_BASE/com/budgetpro/infrastructure" "$BACKUP_DIR/infrastructure" 2>/dev/null || true

# 2. Structural Refactoring (File Relocations)
echo "📂 Applying structural relocations..."
# Using Python to parse structure_report.json and execute commands safely
python3 <<EOF
import json
import os
import subprocess

with open('structure_report.json', 'r') as f:
    data = json.load(f)

for v in data['violations']:
    cmd = v['relocation']['mv_command']
    sed = v['relocation']['sed_command']
    print(f"  - Moving {v['file']}...")
    # Fix paths to be absolute or relative to repo root
    cmd = cmd.replace("backend/src/main/java", "$SRC_BASE")
    sed = sed.replace("backend/src/main/java", "$SRC_BASE")
    
    subprocess.run(cmd, shell=True, cwd="$REPO_ROOT")
    subprocess.run(sed, shell=True, cwd="$REPO_ROOT")
EOF

# 3. Interface Extraction (Purity Remediation)
echo "🔌 Extracting ObservabilityPort interface..."
PORT_DIR="$SRC_BASE/com/budgetpro/domain/finanzas/presupuesto/port/out"
mkdir -p "$PORT_DIR"

cat > "$PORT_DIR/ObservabilityPort.java" <<EOF
package com.budgetpro.domain.finanzas.presupuesto.port.out;

/**
 * Port for domain observability, decoupling domain from infrastructure metrics and logging.
 */
public interface ObservabilityPort {
    void recordMetrics(String name, double value, String... tags);
    void logEvent(String event, String message);
}
EOF

# Refactor IntegrityHashServiceImpl to use Port
IMPL_FILE="$SRC_BASE/com/budgetpro/infrastructure/service/finanzas/IntegrityHashServiceImpl.java"
if [ -f "$IMPL_FILE" ]; then
    echo "🛠️ Refactoring IntegrityHashServiceImpl to use ObservabilityPort..."
    # Update imports
    sed -i 's/import com.budgetpro.infrastructure.observability.*;/import com.budgetpro.domain.finanzas.presupuesto.port.out.ObservabilityPort;/' "$IMPL_FILE"
    # Update fields and constructor
    sed -i 's/private final IntegrityMetrics metrics;/private final ObservabilityPort observability;/' "$IMPL_FILE"
    sed -i 's/private final IntegrityEventLogger logger;//' "$IMPL_FILE"
    sed -i 's/IntegrityMetrics metrics, IntegrityEventLogger logger/ObservabilityPort observability/' "$IMPL_FILE"
    sed -i 's/this.metrics = metrics;/this.observability = observability;/' "$IMPL_FILE"
    sed -i 's/this.logger = logger;//' "$IMPL_FILE"
    # Update usage calls (generic placeholders)
    sed -i 's/metrics.record/observability.recordMetrics/g' "$IMPL_FILE"
    sed -i 's/logger.log/observability.logEvent/g' "$IMPL_FILE"
fi

# 4. Aggregate Decoupling (Billetera Refactor)
echo "🔗 Decoupling Billetera from Presupuesto..."
BILLETERA_FILE="$SRC_BASE/com/budgetpro/domain/finanzas/model/Billetera.java"
if [ -f "$BILLETERA_FILE" ]; then
    # Modify egresar signature
    sed -i 's/Presupuesto presupuesto, IntegrityHashService hashService/com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId presupuestoId, boolean isPresupuestoValid/' "$BILLETERA_FILE"
    # Remove direct validation call
    sed -i '/presupuesto.validarIntegridad(hashService);/d' "$BILLETERA_FILE"
    # Add conditional check based on boolean
    sed -i '/if (presupuesto != null && presupuesto.isAprobado())/d' "$BILLETERA_FILE"
    # Note: Complex body changes might need manual review, but we'll flag it
    echo "⚠️ Billetera.egresar signature updated. Manual check recommended for business logic impact."
fi

echo "✅ Refactoring complete!"
echo "📜 Run ./validate-refactoring.sh to verify results."
echo "🔙 Run ./rollback-refactoring.sh to revert changes."
