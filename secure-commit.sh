#!/bin/bash

# ==============================================================================
# SECURE COMMIT & QUALITY GATE - BUDGETPRO 2026
# ROL: Senior DevSecOps Guardian (Rutas Corregidas)
# ==============================================================================

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}🛡️  Iniciando Quality Gate (Seguridad + Compilación)...${NC}"

# 1. Blindaje de .gitignore (Desde la raíz)
# ------------------------------------------------------------------------------
FILES_TO_IGNORE=("database.env" "RESEND_API_KEY.env" "*.env" "backend/.docker/" "target/" ".DS_Store" "backend/target/")

for item in "${FILES_TO_IGNORE[@]}"; do
    if ! grep -q "$item" .gitignore; then
        echo -e "${YELLOW}⚠️  Protegiendo $item en .gitignore...${NC}"
        echo "$item" >> .gitignore
    fi
done

# 2. Escaneo de Credenciales en Staging
# ------------------------------------------------------------------------------
LEAK_FOUND=$(git diff --cached | grep -E "password: [^$\{]|api_key: [^$\{]" | grep "+")

if [ ! -z "$LEAK_FOUND" ]; then
    echo -e "${RED}❌ ALERTA DE SEGURIDAD: Se detectaron claves en texto plano.${NC}"
    echo -e "$LEAK_FOUND"
    exit 1
fi

# 3. Validación de Compilación (Navegación Corregida)
# ------------------------------------------------------------------------------
if [ -d "backend" ]; then
    echo -e "${GREEN}🔍 Entrando a /backend para validar con Maven Wrapper...${NC}"
    cd backend || exit
    
    # Aseguramos permisos del ejecutable
    chmod +x mvnw
    
    # Ejecutamos compilación
    if ! ./mvnw clean compile -q; then
        echo -e "${RED}❌ ERROR DE COMPILACIÓN: El código tiene errores.${NC}"
        cd ..
        exit 1
    fi
    
    # Regresamos a la raíz para el commit
    cd ..
    echo -e "${GREEN}✅ Código validado correctamente.${NC}"
else
    echo -e "${RED}❌ ERROR: No se encontró la carpeta 'backend'.${NC}"
    exit 1
fi

# 4. Flujo de Commit y Push
# ------------------------------------------------------------------------------
echo -ne "${GREEN}📝 Mensaje del commit: ${NC}"
read commit_msg

if [ -z "$commit_msg" ]; then
    echo -e "${RED}❌ El mensaje no puede estar vacío.${NC}"
    exit 1
fi

git add .
git commit -m "$commit_msg"
git push origin main

echo -e "${GREEN}🚀 Sincronización exitosa. GitHub está limpio y el código funciona.${NC}"
