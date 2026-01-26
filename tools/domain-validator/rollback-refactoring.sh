#!/bin/bash

REPO_ROOT="/home/wazoox/Desktop/budgetpro-backend"
TOOLS_DIR="$REPO_ROOT/tools/domain-validator"
SRC_BASE="$REPO_ROOT/backend/src/main/java"
LATEST_BACKUP=$(ls -td "$TOOLS_DIR/.refactoring-backup"/*/ 2>/dev/null | head -1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "❌ No backup found to rollback."
    exit 1
fi

echo "⏪ Rolling back to $LATEST_BACKUP..."

# Restore original directories
rm -rf "$SRC_BASE/com/budgetpro/domain"
rm -rf "$SRC_BASE/com/budgetpro/infrastructure"
cp -r "$LATEST_BACKUP/domain" "$SRC_BASE/com/budgetpro/domain"
cp -r "$LATEST_BACKUP/infrastructure" "$SRC_BASE/com/budgetpro/infrastructure" 2>/dev/null || true

# Remove generated files
# rm -f "$SRC_BASE/com/budgetpro/domain/finanzas/presupuesto/port/out/ObservabilityPort.java"

echo "✅ Rollback complete."
