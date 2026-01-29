#!/bin/bash

# Script para instalar los git hooks en el repositorio local

HOOKS_DIR=$(git rev-parse --git-path hooks)
SOURCE_DIR="scripts/git-hooks"

echo "Installing Git hooks..."

if [ -f "$SOURCE_DIR/pre-commit" ]; then
    cp "$SOURCE_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
    chmod +x "$HOOKS_DIR/pre-commit"
    echo "✅ pre-commit hook installed."
else
    echo "❌ Error: Source pre-commit hook not found in $SOURCE_DIR"
    exit 1
fi

echo "Done."
