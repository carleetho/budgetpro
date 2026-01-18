#!/bin/bash

# ==============================================================================
# SCRIPT DE DESARROLLO - BUDGETPRO CORE (EDICIÓN SEGURA 2026)
# OBJETIVO: Inyectar secretos de DB y API sin exponerlos en el código.
# ==============================================================================

# Colores para salida de terminal
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' 

echo -e "${GREEN}🚀 Iniciando entorno BudgetPro...${NC}"

# 1. Función interna de carga segura
# ------------------------------------------------------------------------------
load_env() {
    local env_file=$1
    if [ -f "$env_file" ]; then
        echo -e "${GREEN}🔐 Cargando variables desde $env_file...${NC}"
        # Exporta variables ignorando comentarios y líneas vacías
        export $(grep -v '^#' "$env_file" | xargs)
    else
        echo -e "${RED}❌ ERROR: No se encontró el archivo $env_file.${NC}"
        echo -e "${YELLOW}Crea el archivo basándote en los requerimientos de seguridad.${NC}"
        exit 1
    fi
}

# 2. Carga de Secretos (Asegúrate de que estén en .gitignore)
# ------------------------------------------------------------------------------
load_env "database.env"
load_env "RESEND_API_KEY.env"

# 3. Validación de Variables Críticas
# ------------------------------------------------------------------------------
# Verificamos que las variables que espera application.yml no estén vacías
if [ -z "$DB_PASSWORD" ] || [ -z "$RESEND_API_KEY" ]; then
    echo -e "${RED}❌ ERROR: Variables críticas (DB o API) faltantes en los archivos .env.${NC}"
    exit 1
fi

# 4. Ejecución del Backend
# ------------------------------------------------------------------------------
echo -e "${GREEN}📦 Ejecutando Maven Spring-Boot...${NC}"

# Validamos existencia de carpeta backend
if [ -d "backend" ]; then
    cd backend || exit
    mvn spring-boot:run
else
    echo -e "${RED}❌ ERROR: No se encuentra la carpeta 'backend' en el directorio actual.${NC}"
    exit 1
fi
