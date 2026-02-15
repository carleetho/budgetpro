#!/bin/bash

# API Smoke Test Script for BudgetPro Backend
# Usage: ./scripts/api_smoke_test.sh

BASE_URL="http://localhost:8080/api/v1"
ADMIN_EMAIL="admin_demo@budgetpro.com"
ADMIN_PASS="Secret123!"

# Helper function to extract JSON values without jq
extract_json_value() {
    local key=$1
    # Simple grep/sed extraction. Handles "key":"value"
    grep -o "\"$key\":\"[^\"]*\"" | cut -d'"' -f4
}

echo "🚀 Iniciando Smoke Test de la API..."

# 1. Health Check
echo "---------------------------------------------------"
echo "🔍 Verificando estado del servidor..."
HEALTH_RES=$(curl -s http://localhost:8080/actuator/health)
HEALTH=$(echo "$HEALTH_RES" | extract_json_value "status")

if [ "$HEALTH" == "UP" ]; then
    echo "✅ Servidor ONLINE (Health: UP)"
else
    echo "❌ Servidor OFFLINE o no responde. Asegúrate de ejecutar ./dev.sh primero."
    echo "   Respuesta: $HEALTH_RES"
    exit 1
fi

# 2. Register Admin User
echo "---------------------------------------------------"
echo "👤 Registrando usuario administrador ($ADMIN_EMAIL)..."
REGISTER_RES=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"nombreCompleto\": \"Admin Demo\",
        \"email\": \"$ADMIN_EMAIL\",
        \"password\": \"$ADMIN_PASS\",
        \"rol\": \"ADMIN\"
    }")

# Check if already exists (409) or created (200/201)
if echo "$REGISTER_RES" | grep -q "exist"; then
    echo "⚠️  El usuario ya existe (continuando...)"
else
    if echo "$REGISTER_RES" | grep -q "id"; then
        echo "✅ Usuario registrado exitosamente."
    else
        echo "⚠️  Respuesta inesperada en registro: $REGISTER_RES"
    fi
fi

# 3. Login & Get Token
echo "---------------------------------------------------"
echo "🔑 Iniciando sesión..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$ADMIN_EMAIL\",
        \"password\": \"$ADMIN_PASS\"
    }")

TOKEN=$(echo "$LOGIN_RES" | extract_json_value "token")

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo "✅ Login exitoso. Token recibido."
else
    echo "❌ Fallo en login. Respuesta: $LOGIN_RES"
    exit 1
fi

# 4. Create Project
PROYECTO_NOMBRE="Proyecto Demo $(date +%s)"
echo "---------------------------------------------------"
echo "🏗️  Creando proyecto: $PROYECTO_NOMBRE"
CREATE_PROJ_RES=$(curl -s -X POST "$BASE_URL/proyectos" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"nombre\": \"$PROYECTO_NOMBRE\",
        \"ubicacion\": \"Ciudad Demo\"
    }")

PROYECTO_ID=$(echo "$CREATE_PROJ_RES" | extract_json_value "id")

if [ -n "$PROYECTO_ID" ] && [ "$PROYECTO_ID" != "null" ]; then
    echo "✅ Proyecto creado. ID: $PROYECTO_ID"
else
    echo "❌ Error al crear proyecto. Respuesta: $CREATE_PROJ_RES"
fi

# 5. List Projects
echo "---------------------------------------------------"
echo "📋 Listando proyectos (Raw JSON)..."
curl -s -X GET "$BASE_URL/proyectos" \
    -H "Authorization: Bearer $TOKEN"

echo ""
echo "---------------------------------------------------"
echo "✅ Smoke Test completado."
