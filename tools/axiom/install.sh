#!/bin/bash
# AXIOM Setup Script

set -e

echo "==========================================="
echo "   AXIOM Architectural Governance Setup    "
echo "==========================================="

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"

# Ensure python3 is available
if ! command -v python3 &> /dev/null; then
    echo "Error: python3 is not installed."
    exit 1
fi

echo "[1/3] Installing dependencies..."
pip install -q pyyaml jsonschema jinja2

echo "[2/3] Verifying configuration..."
CONFIG_PATH="$PROJECT_ROOT/tools/axiom/axiom.config.yaml"
if [ ! -f "$CONFIG_PATH" ]; then
    echo "Creating default configuration..."
    # Copy example or generate (assuming it was created in Task 1, validation will happen next)
    echo "Warning: Configuration file not found at $CONFIG_PATH"
else
    echo "Configuration found at $CONFIG_PATH"
fi

# Run the installation hook for .cursorrules
echo "[3/3] Setting up AI Assistant integration..."
python3 "$SCRIPT_DIR/install_hook.py"

echo ""
echo "==========================================="
echo "        AXIOM Setup Complete!              "
echo "==========================================="
echo "Next steps:"
echo "  1. Review tools/axiom/axiom.config.yaml"
echo "  2. Run 'python3 tools/axiom/sync_cursorrules.py' if you update the config."
