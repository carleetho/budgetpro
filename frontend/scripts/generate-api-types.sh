#!/usr/bin/env bash
# Genera tipos TypeScript desde docs/api/openapi.yaml (as-built).
# Prerrequisito: spec real (no placeholder). Regenerar con:
#   ./scripts/generate-openapi-spec.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SPEC="${ROOT}/docs/api/openapi.yaml"
OUT="${ROOT}/frontend/src/types/api.ts"

if [[ ! -f "$SPEC" ]]; then
  echo "ERROR: missing $SPEC" >&2
  exit 1
fi

if grep -q "Placeholder versionado" "$SPEC"; then
  echo "WARN: docs/api/openapi.yaml is still a placeholder."
  echo "      Types will be empty until you regenerate the spec with the app running."
fi

mkdir -p "$(dirname "$OUT")"
cd "${ROOT}/frontend"
npx --yes openapi-typescript "$SPEC" -o "$OUT"
echo "Types written to frontend/src/types/api.ts"
