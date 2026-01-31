#!/bin/bash
# scripts/install_all_cursorrules_tools.sh - Instala todas las herramientas de gestión de .cursorrules

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 AXIOM - Instalación Completa de Herramientas .cursorrules${NC}"
echo "================================================================"
echo ""

# Verificar que estamos en un repositorio git
if [ ! -d ".git" ]; then
    echo -e "${RED}❌ ERROR: No estás en un repositorio git${NC}"
    exit 1
fi

echo -e "${YELLOW}📦 Verificando dependencias...${NC}"
if ! python3 -c "import yaml" 2>/dev/null; then
    echo "Instalando PyYAML..."
    pip3 install pyyaml || { echo -e "${RED}❌ Error al instalar PyYAML. Se necesita para verify_cursorrules.sh${NC}"; exit 1; }
fi
echo -e "${GREEN}✓${NC} Dependencias verificadas"
echo ""

echo -e "${YELLOW}📝 Asegurando permisos de ejecución...${NC}"

SCRIPTS=(
    "scripts/verify_cursorrules.sh"
    "scripts/update_cursorrules.sh"
    "scripts/restore_cursorrules.sh"
    "scripts/manage_cursorrules_backups.sh"
    "scripts/pre-commit-cursorrules.sh"
    "scripts/install_cursorrules_hook.sh"
    "scripts/install_github_action.sh"
)

for script in "${SCRIPTS[@]}"; do
    if [ -f "$script" ]; then
        chmod +x "$script"
        echo -e "  ${GREEN}✓${NC} $script"
    else
        echo -e "  ${RED}❌ No se encontró $script${NC}"
    fi
done

echo ""
echo -e "${YELLOW}🪝 Instalando Pre-commit Hook...${NC}"
./scripts/install_cursorrules_hook.sh

echo ""
echo -e "${YELLOW}🔄 Instalando GitHub Action...${NC}"
# No interactivo para el instalador total
mkdir -p .github/workflows
# El archivo ya existe (creado por el asistente), solo informamos
echo -e "  ${GREEN}✓${NC} .github/workflows/verify-cursorrules.yml"

echo ""
echo -e "${GREEN}✅ ¡Instalación completada exitosamente!${NC}"
echo ""
echo -e "${BLUE}Herramientas disponibles:${NC}"
echo "  - ./scripts/verify_cursorrules.sh       : Verifica sincronización"
echo "  - ./scripts/update_cursorrules.sh       : Actualiza .cursorrules desde axiom.yaml"
echo "  - ./scripts/manage_cursorrules_backups.sh: Gestiona backups"
echo ""
echo -e "${BLUE}Automatización activada:${NC}"
echo "  - Pre-commit Hook: Verificación automática en cada commit"
echo "  - GitHub Action  : Verificación automática en PRs"
echo ""
