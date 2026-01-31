#!/bin/bash
# scripts/pre-commit-cursorrules.sh - Verifica sincronización antes de commit

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Verificar si axiom.yaml o .cursorrules están siendo commiteados
AXIOM_CHANGED=$(git diff --cached --name-only | grep -E '^axiom\.yaml$')
CURSORRULES_CHANGED=$(git diff --cached --name-only | grep -E '^\.cursorrules$')

# Si ninguno de los dos archivos cambió, salir exitosamente
if [ -z "$AXIOM_CHANGED" ] && [ -z "$CURSORRULES_CHANGED" ]; then
    exit 0
fi

echo -e "${BLUE}🔍 AXIOM - Verificando sincronización de .cursorrules${NC}"
echo ""

# Si axiom.yaml cambió pero .cursorrules no
if [ -n "$AXIOM_CHANGED" ] && [ -z "$CURSORRULES_CHANGED" ]; then
    echo -e "${YELLOW}⚠️  ADVERTENCIA: axiom.yaml cambió pero .cursorrules no${NC}"
    echo ""
    echo -e "${YELLOW}Opciones:${NC}"
    echo "  1. Ejecutar: ./scripts/update_cursorrules.sh para sincronizar"
    echo "  2. Agregar .cursorrules al commit: git add .cursorrules"
    echo "  3. Continuar sin sincronizar (no recomendado)"
    echo ""
    read -p "¿Deseas actualizar .cursorrules automáticamente? (s/n): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        echo -e "${BLUE}🔄 Actualizando .cursorrules...${NC}"
        ./scripts/update_cursorrules.sh
        
        # Agregar .cursorrules al staging area
        git add .cursorrules
        echo -e "${GREEN}✅ .cursorrules actualizado y agregado al commit${NC}"
    else
        echo -e "${YELLOW}⚠️  Continuando sin sincronizar (no recomendado)${NC}"
    fi
fi

# Si .cursorrules cambió, verificar que esté sincronizado
if [ -n "$CURSORRULES_CHANGED" ]; then
    echo -e "${BLUE}🔍 Verificando sincronización...${NC}"
    
    # Ejecutar verificación
    if ./scripts/verify_cursorrules.sh > /dev/null 2>&1; then
        echo -e "${GREEN}✅ .cursorrules está sincronizado con axiom.yaml${NC}"
        exit 0
    else
        echo -e "${RED}❌ ERROR: .cursorrules NO está sincronizado con axiom.yaml${NC}"
        echo ""
        echo -e "${YELLOW}Ejecuta:${NC}"
        echo "  ./scripts/verify_cursorrules.sh  # Para ver detalles"
        echo "  ./scripts/update_cursorrules.sh  # Para sincronizar"
        echo ""
        echo -e "${RED}Commit bloqueado. Sincroniza los archivos primero.${NC}"
        exit 1
    fi
fi

exit 0
