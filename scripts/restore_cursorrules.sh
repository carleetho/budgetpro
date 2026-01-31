#!/bin/bash
# scripts/restore_cursorrules.sh - Restaura .cursorrules desde backup

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔙 AXIOM - Restaurar .cursorrules desde Backup${NC}"
echo "================================================"
echo ""

# Buscar archivos de backup
BACKUPS=($(ls -t .cursorrules.backup.* 2>/dev/null || true))

if [ ${#BACKUPS[@]} -eq 0 ]; then
    echo -e "${RED}❌ No se encontraron backups de .cursorrules${NC}"
    echo ""
    echo "Los backups se crean automáticamente cuando ejecutas:"
    echo "  ./scripts/update_cursorrules.sh"
    echo ""
    exit 1
fi

echo -e "${YELLOW}📦 Backups disponibles:${NC}"
echo ""

# Mostrar lista de backups
for i in "${!BACKUPS[@]}"; do
    BACKUP="${BACKUPS[$i]}"
    TIMESTAMP=$(echo "$BACKUP" | sed 's/.cursorrules.backup.//')
    SIZE=$(ls -lh "$BACKUP" | awk '{print $5}')
    
    # Intentar obtener la fecha de modificación de forma portable
    if [[ "$OSTYPE" == "darwin"* ]]; then
        DATE=$(stat -f "%Sm" -t "%Y-%m-%d %H:%M:%S" "$BACKUP")
    else
        DATE=$(date -r "$BACKUP" "+%Y-%m-%d %H:%M:%S" 2>/dev/null || stat -c "%y" "$BACKUP" | cut -d. -f1)
    fi
    
    echo "  $((i+1)). $BACKUP"
    echo "     Fecha: $DATE"
    echo "     Tamaño: $SIZE"
    echo ""
done

# Seleccionar backup
echo -e "${YELLOW}Selecciona el backup a restaurar (1-${#BACKUPS[@]}):${NC}"
read -p "> " SELECTION

# Validar selección
if ! [[ "$SELECTION" =~ ^[0-9]+$ ]] || [ "$SELECTION" -lt 1 ] || [ "$SELECTION" -gt ${#BACKUPS[@]} ]; then
    echo -e "${RED}❌ Selección inválida${NC}"
    exit 1
fi

SELECTED_BACKUP="${BACKUPS[$((SELECTION-1))]}"

echo ""
echo -e "${YELLOW}📋 Backup seleccionado:${NC}"
echo "  $SELECTED_BACKUP"
echo ""

# Confirmar restauración
read -p "¿Estás seguro de restaurar este backup? (s/n): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Ss]$ ]]; then
    echo -e "${YELLOW}❌ Restauración cancelada${NC}"
    exit 0
fi

# Crear backup del archivo actual antes de restaurar
if [ -f ".cursorrules" ]; then
    CURRENT_BACKUP=".cursorrules.backup.before_restore.$(date +%Y%m%d_%H%M%S)"
    echo -e "${YELLOW}📦 Creando backup del archivo actual...${NC}"
    cp .cursorrules "$CURRENT_BACKUP"
    echo -e "${GREEN}✓${NC} Backup creado: $CURRENT_BACKUP"
    echo ""
fi

# Restaurar backup
echo -e "${BLUE}🔄 Restaurando backup...${NC}"
cp "$SELECTED_BACKUP" .cursorrules
echo -e "${GREEN}✓${NC} Backup restaurado exitosamente"

echo ""
echo -e "${BLUE}🔍 Verificando sincronización...${NC}"

# Verificar si está sincronizado
if ./scripts/verify_cursorrules.sh > /dev/null 2>&1; then
    echo -e "${GREEN}✅ El backup restaurado está sincronizado con axiom.yaml${NC}"
else
    echo -e "${YELLOW}⚠️  ADVERTENCIA: El backup restaurado NO está sincronizado con axiom.yaml${NC}"
    echo ""
    echo "Esto puede significar que:"
    echo "  1. axiom.yaml cambió después de crear el backup"
    echo "  2. El backup es de una versión anterior"
    echo ""
    echo "Opciones:"
    echo "  1. Mantener el backup restaurado (no sincronizado)"
    echo "  2. Actualizar .cursorrules para sincronizar con axiom.yaml actual"
    echo ""
    read -p "¿Qué deseas hacer? (1/2): " -n 1 -r
    echo ""
    
    if [[ $REPLY == "2" ]]; then
        echo -e "${BLUE}🔄 Actualizando .cursorrules...${NC}"
        ./scripts/update_cursorrules.sh
    else
        echo -e "${YELLOW}⚠️  Manteniendo backup restaurado (no sincronizado)${NC}"
    fi
fi

echo ""
echo -e "${GREEN}✅ Restauración completada${NC}"
echo ""
echo "📋 Resumen:"
echo "  - Backup restaurado: $SELECTED_BACKUP"
if [ -n "$CURRENT_BACKUP" ]; then
    echo "  - Backup del archivo anterior: $CURRENT_BACKUP"
fi
echo ""
