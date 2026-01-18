#!/bin/bash
# Script de Limpieza "Nuclear Reset" para PostgreSQL
# Elimina completamente los datos de PostgreSQL para resolver checksum mismatch de Flyway

set -e

echo "🔴 INICIANDO RESET NUCLEAR DE BASE DE DATOS..."

# 1. Detener contenedores
echo "📦 Deteniendo contenedores..."
cd "$(dirname "$0")"
docker compose down -v 2>/dev/null || docker-compose down -v 2>/dev/null || true

# 2. Eliminar directorio de datos (Bind Mount)
echo "🗑️  Eliminando directorio de datos PostgreSQL..."
if [ -d ".docker/postgres-data" ]; then
    sudo rm -rf .docker/postgres-data
    echo "✅ Directorio .docker/postgres-data eliminado"
else
    echo "⚠️  Directorio .docker/postgres-data no existe"
fi

# 3. Crear directorio vacío con permisos correctos
echo "📁 Creando directorio vacío..."
mkdir -p .docker/postgres-data
chmod 700 .docker/postgres-data

# 4. Levantar contenedor limpio
echo "🚀 Levantando contenedor PostgreSQL limpio..."
docker compose up -d budgetpro-db 2>/dev/null || docker-compose up -d budgetpro-db 2>/dev/null

# 5. Esperar a que PostgreSQL esté listo
echo "⏳ Esperando a que PostgreSQL esté listo..."
sleep 5
until docker compose exec -T budgetpro-db pg_isready -U postgres 2>/dev/null || docker-compose exec -T budgetpro-db pg_isready -U postgres 2>/dev/null; do
    echo "   Esperando PostgreSQL..."
    sleep 2
done

echo ""
echo "✅ RESET COMPLETADO"
echo ""
echo "📊 Para verificar que la BD está vacía, ejecuta:"
echo "   docker compose exec budgetpro-db psql -U postgres -d budgetpro -c \"\\dt\""
echo ""
echo "🚀 Ahora puedes ejecutar: ./mvnw spring-boot:run"
