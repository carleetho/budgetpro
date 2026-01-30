#!/bin/bash
# AXIOM Installer
# Sets up the AXIOM environment, hooks, and dependencies.

set -e

# ANSI Colors
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Instalando AXIOM Sentinel...${NC}"

# 1. Verify Repository Root
if [ ! -d ".git" ]; then
    echo -e "${RED}❌ Error: No se encontró el directorio .git. Ejecutar desde la raíz del repositorio.${NC}"
    exit 2
fi

# 2. Create Directory Structure
echo "Creando estructura de directorios..."
mkdir -p .budgetpro
mkdir -p tools/axiom/validators
mkdir -p tools/axiom/reporters
mkdir -p tools/axiom/fixers
echo -e "${GREEN}✅ Directorios creados${NC}"

# 3. Initialize Python Packages
echo "Inicializando paquetes Python..."
touch tools/axiom/validators/__init__.py
touch tools/axiom/reporters/__init__.py
touch tools/axiom/fixers/__init__.py
echo -e "${GREEN}✅ Paquetes Python inicializados${NC}"

# 4. Copy Configuration
echo "Configurando AXIOM..."
if [ -f "axiom.config.yaml" ]; then
    cp -n axiom.config.yaml .budgetpro/axiom.config.yaml
    echo -e "${GREEN}✅ Configuración copiada a .budgetpro/${NC}"
else
    echo -e "${YELLOW}⚠️ axiom.config.yaml no encontrado en raíz del proyecto (usando defaults)${NC}"
fi

# 5. Install Pre-commit Hook
echo "Instalando hook pre-commit..."
HOOK_PATH=".git/hooks/pre-commit"
TEMPLATE_PATH="tools/axiom/pre_commit_template.sh"

if [ -f "$HOOK_PATH" ]; then
    # Check if it's already our hook to avoid unnecessary backups or loops?
    # For simplicity, always backup if it exists and differs, or just backup.
    echo "Respadando hook existente..."
    mv "$HOOK_PATH" "$HOOK_PATH.backup"
fi

if [ -f "$TEMPLATE_PATH" ]; then
    cp "$TEMPLATE_PATH" "$HOOK_PATH"
    chmod +x "$HOOK_PATH"
    echo -e "${GREEN}✅ Hook pre-commit instalado${NC}"
else
    echo -e "${RED}❌ Error: Plantilla de hook no encontrada en $TEMPLATE_PATH${NC}"
    exit 1
fi

# 6. Set Executable Permissions
echo "Ajustando permisos..."
chmod +x axiom.sh 2>/dev/null || true
chmod +x tools/axiom/install.sh
echo -e "${GREEN}✅ Permisos configurados${NC}"

# 7. Install Dependencies
echo "Instalando dependencias..."
pip3 install pyyaml --quiet
echo -e "${GREEN}✅ Dependencias instaladas${NC}"

# 8. Run Verification
echo "Verificando instalación..."
if [ -f "./axiom.sh" ]; then
    # Allow exit code 0 (success) or 1 (violations), but fail on 2 (error)
    set +e
    ./axiom.sh --dry-run > /dev/null 2>&1
    EXIT_CODE=$?
    set -e
    
    if [ $EXIT_CODE -eq 0 ] || [ $EXIT_CODE -eq 1 ]; then
        echo -e "${GREEN}✅ Verificación exitosa${NC}"
    else
        echo -e "${YELLOW}⚠️ Verificación falló (Código $EXIT_CODE) - revisar instalación${NC}"
    fi
else
    echo -e "${YELLOW}⚠️ Script manual axiom.sh no encontrado${NC}"
fi

echo -e "\n${GREEN}✨ Instalación completada exitosamente.${NC}"
echo "Ejecutar ./axiom.sh para validar cambios."
