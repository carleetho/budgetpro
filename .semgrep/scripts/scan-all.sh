#!/bin/bash

# BudgetPro Semgrep Scan - Full Project
# Runs all custom rules using the local profile (warnings only)

ROOT_DIR=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
CONFIG_DIR="$ROOT_DIR/.semgrep/config"

echo "🚀 Running full Semgrep scan..."
semgrep scan --config "$CONFIG_DIR/local.yaml" "$ROOT_DIR"
