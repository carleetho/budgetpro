#!/bin/bash

# BudgetPro Semgrep Scan - Category Specific
# Usage: ./scan-category.sh <category>
# Categories: security, domain, architecture, performance, quality

CATEGORY=$1
ROOT_DIR=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
RULES_DIR="$ROOT_DIR/.semgrep/rules"

if [ -z "$CATEGORY" ]; then
    echo "❌ Error: No category specified."
    echo "Usage: $0 <security|domain|architecture|performance|quality>"
    exit 1
fi

if [ ! -d "$RULES_DIR/$CATEGORY" ]; then
    echo "❌ Error: Category '$CATEGORY' does not exist."
    echo "Available categories: security, domain, architecture, performance, quality"
    exit 1
fi

echo "🔍 Scanning category: $CATEGORY..."
semgrep scan --config "$RULES_DIR/$CATEGORY" "$ROOT_DIR"
