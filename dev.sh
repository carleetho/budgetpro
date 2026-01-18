#!/bin/bash

# ==============================================================================
# SCRIPT DE DESARROLLO - BUDGETPRO CORE (EDICIÓN SEGURA 2026)
# ROL: Senior DevSecOps Guardian
# ==============================================================================

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' 

echo -e "${GREEN}🚀 Iniciando entorno BudgetPro...${NC}"

# 1. Función interna de carga segura (Ejecutada desde la raíz)
# ------------------------------------------------------------------------------
load_env() {
    local env_file=$1
    if [ -f "$env_file" ]; then
        echo -e "${GREEN}🔐 Cargando variables desde $env_file...${NC}"
        # Exporta variables para que estén disponibles en los subprocesos (Maven)
        export $(grep -v '^#' "$env_file" | xargs)
    else
        echo -e "${RED}❌ ERROR: No se encontró el archivo $env_file en la raíz.${NC}"
        exit 1
    fi
}

# 2. Carga de Secretos
# ------------------------------------------------------------------------------
load_env "database.env"
load_env "RESEND_API_KEY.env"

# 3. Validación de Variables Críticas
# ------------------------------------------------------------------------------
if [ -z "$DB_PASSWORD" ] || [ -z "$RESEND_API_KEY" ]; then
    echo -e "${RED}❌ ERROR: Variables críticas faltantes en los archivos .env.${NC}"
    exit 1
fi

# 4. Ejecución del Backend (Corrección de Ruta y Wrapper)
# ------------------------------------------------------------------------------
echo -e "${GREEN}📦 Preparando ejecución con Maven Wrapper...${NC}"

if [ -d "backend" ]; then
    cd backend || exit
    
    # Aseguramos que el wrapper sea ejecutable
    chmod +x mvnw
    
    # IMPORTANTE: Usamos ./mvnw en lugar de mvn global para evitar el error de Launcher
    echo -e "${GREEN}☕ Levantando Spring Boot...${NC}"
    ./mvnw spring-boot:run
else
    echo -e "${RED}❌ ERROR: No se encuentra la carpeta 'backend'.${NC}"
    exit 1
fi
