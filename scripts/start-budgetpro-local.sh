#!/usr/bin/env bash
# Arranca PostgreSQL/Redis (Docker), backend Spring Boot y frontend Next.js en local.
# Requisitos: Docker daemon activo, `database.env` y `RESEND_API_KEY.env` en la raíz del repo.

set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p "$ROOT/.runtime"

if ! docker info >/dev/null 2>&1; then
  echo "ERROR: No hay conexión al daemon de Docker."
  echo "  Inicia Docker (ej.: sudo systemctl start docker  o  abre Docker Desktop) y vuelve a ejecutar:"
  echo "  bash scripts/start-budgetpro-local.sh"
  exit 1
fi

echo "==> Docker Compose (postgres + redis)"
docker compose up -d

echo "==> Esperando PostgreSQL…"
for _ in $(seq 1 40); do
  if docker compose exec -T postgres pg_isready -U postgres -d budgetpro >/dev/null 2>&1; then
    echo "    PostgreSQL listo."
    break
  fi
  sleep 1
done

echo "==> Backend (JAR)"
if [[ ! -f backend/target/budgetpro-backend-1.0.0-SNAPSHOT.jar ]]; then
  (cd backend && ./mvnw -q -DskipTests package)
fi

if ss -tlnp 2>/dev/null | grep -q ':8080 '; then
  echo "ADVERTENCIA: Ya hay un proceso en :8080 — no se inicia otro backend."
else
  (
    cd "$ROOT/backend"
    set -a
    # shellcheck source=/dev/null
    source "$ROOT/database.env"
    # shellcheck source=/dev/null
    source "$ROOT/RESEND_API_KEY.env"
    set +a
    nohup java -jar target/budgetpro-backend-1.0.0-SNAPSHOT.jar >>"$ROOT/.runtime/backend.log" 2>&1 &
    echo $! >"$ROOT/.runtime/backend.pid"
  )
  echo "    Backend PID $(cat "$ROOT/.runtime/backend.pid") — log: .runtime/backend.log"
fi

echo "==> Frontend (Next.js, puerto 3000)"
export NEXT_PUBLIC_API_BASE_URL="${NEXT_PUBLIC_API_BASE_URL:-http://localhost:8080/api/v1}"
if ss -tlnp 2>/dev/null | grep -q ':3000 '; then
  echo "ADVERTENCIA: Ya hay un proceso en :3000 — no se inicia otro frontend."
else
  (
    cd "$ROOT/frontend"
    [[ -d .next ]] || npm run build
    nohup npm run start -- --port 3000 >>"$ROOT/.runtime/frontend.log" 2>&1 &
    echo $! >"$ROOT/.runtime/frontend.pid"
  )
  echo "    Frontend PID $(cat "$ROOT/.runtime/frontend.pid") — log: .runtime/frontend.log"
fi

echo ""
echo "URLs:"
echo "  API:  http://localhost:8080/api/v1"
echo "  UI:   http://localhost:3000"
echo ""
echo "Detener:"
echo "  kill \$(cat .runtime/backend.pid) 2>/dev/null; kill \$(cat .runtime/frontend.pid) 2>/dev/null; docker compose down"
