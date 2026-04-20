#!/usr/bin/env bash
# Arranca Spring Boot con las mismas variables que Docker Compose / database.env.
# Uso (desde la raíz del repo): ./scripts/start-backend-local.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
set -a
# shellcheck disable=SC1091
source "$ROOT/database.env"
set +a
cd backend
exec ./mvnw spring-boot:run
