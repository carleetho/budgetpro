#!/bin/bash

# Colores para que se vea bonito
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 INICIANDO ENTORNO DE DESARROLLO BUDGETPRO...${NC}"

# 1. Levantar Base de Datos (Docker)
echo -e "${YELLOW}🐳 [1/3] Levantando Base de Datos PostgreSQL...${NC}"
cd backend || exit

# Cargar variables de entorno (Resend / notificaciones)
if [ -f "RESEND_API_KEY.env" ]; then
    set -a
    # shellcheck disable=SC1091
    source "RESEND_API_KEY.env"
    set +a
    echo -e "${GREEN}✅ Variables de entorno cargadas desde RESEND_API_KEY.env.${NC}"
else
    echo -e "${YELLOW}⚠️  No se encontró RESEND_API_KEY.env. Variables de Resend no cargadas.${NC}"
fi
docker compose up -d

# Verificación de salud de Docker
if [ $? -ne 0 ]; then
    echo -e "\033[0;31m❌ Error: Docker no parece estar corriendo o falló el compose.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Base de Datos operativa.${NC}"

# 2. Instrucción para el Frontend
echo -e "${YELLOW}⚛️  [2/3] Preparando Frontend...${NC}"
echo -e "   ⚠️  AVISO: Para correr el Frontend, abre una ${BLUE}NUEVA TERMINAL${NC} y ejecuta:"
echo -e "   ${GREEN}cd frontend && npm run dev${NC}"
echo -e "   (Dejamos esta terminal exclusiva para los logs del Backend)"

# 3. Levantar Backend (Spring Boot)
echo -e "${YELLOW}☕ [3/3] Arrancando Backend Spring Boot...${NC}"
echo -e "   Los logs aparecerán aquí abajo. Presiona CTRL+C para detener."
echo -e "-------------------------------------------------------"

# Ejecutar Spring Boot
./mvnw spring-boot:run
