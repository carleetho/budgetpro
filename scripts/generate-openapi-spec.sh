#!/usr/bin/env bash
# Genera docs/api/openapi.yaml desde una instancia BudgetPro en ejecución (REQ-47 Task 3).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${ROOT}/docs/api/openapi.yaml"
BASE_URL="${OPENAPI_BASE_URL:-http://localhost:8080}"
HEALTH_URL="${OPENAPI_HEALTH_URL:-${BASE_URL}/actuator/health}"
SPEC_URL="${OPENAPI_SPEC_URL:-${BASE_URL}/v3/api-docs.yaml}"
WAIT_SECS="${OPENAPI_WAIT_SECS:-90}"

mkdir -p "$(dirname "$OUT")"

echo "Waiting for application at ${HEALTH_URL} (timeout ${WAIT_SECS}s)..."
deadline=$((SECONDS + WAIT_SECS))
until curl -sf "$HEALTH_URL" >/dev/null; do
  if (( SECONDS >= deadline )); then
    echo "ERROR: application did not become healthy in time" >&2
    exit 1
  fi
  sleep 2
done

echo "Generating OpenAPI specification from ${SPEC_URL}..."
tmp="$(mktemp)"
curl -sf "$SPEC_URL" -o "$tmp"
# Sanity: must look like OpenAPI YAML
if ! grep -qE '^openapi:[[:space:]]' "$tmp"; then
  echo "ERROR: response does not look like OpenAPI YAML" >&2
  head -n 20 "$tmp" >&2 || true
  exit 1
fi
mv "$tmp" "$OUT"
echo "OpenAPI spec written to ${OUT}"
