#!/bin/bash
# scripts/manage_cursorrules_backups.sh - Gestiona backups de .cursorrules

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

show_menu() {
    echo -e "${BLUE}🗂️  AXIOM - Gestión de Backups de .cursorrules${NC}"
    echo "================================================"
    echo ""
    echo "1. 📋 Listar backups"
    echo "2. 🔙 Restaurar backup"
    echo "3. 🗑️  Eliminar backup"
    echo "4. 🧹 Limpiar backups antiguos (mantener últimos 5)"
    echo "5. 📊 Ver diferencias entre backups"
    echo "6. ❌ Salir"
    echo ""
}

list_backups() {
    echo -e "${YELLOW}📦 Backups disponibles:${NC}"
    echo ""
    
    BACKUPS=($(ls -t .cursorrules.backup.* 2>/dev/null || true))
    
    if [ ${#BACKUPS[@]} -eq 0 ]; then
        echo "  No hay backups disponibles"
        return
    fi
    
    for i in "${!BACKUPS[@]}"; do
        BACKUP="${BACKUPS[$i]}"
        SIZE=$(ls -lh "$BACKUP" | awk '{print $5}')
        
        if [[ "$OSTYPE" == "darwin"* ]]; then
            DATE=$(stat -f "%Sm" -t "%Y-%m-%d %H:%M:%S" "$BACKUP")
        else
            DATE=$(date -r "$BACKUP" "+%Y-%m-%d %H:%M:%S" 2>/dev/null || stat -c "%y" "$BACKUP" | cut -d. -f1)
        fi
        
        echo "  $((i+1)). $BACKUP"
        echo "     Fecha: $DATE | Tamaño: $SIZE"
        echo ""
    done
    
    echo "Total: ${#BACKUPS[@]} backups"
}

delete_backup() {
    BACKUPS=($(ls -t .cursorrules.backup.* 2>/dev/null || true))
    
    if [ ${#BACKUPS[@]} -eq 0 ]; then
        echo -e "${RED}❌ No hay backups para eliminar${NC}"
        return
    fi
    
    list_backups
    echo ""
    echo -e "${YELLOW}Selecciona el backup a eliminar (1-${#BACKUPS[@]}):${NC}"
    read -p "> " SELECTION
    
    if ! [[ "$SELECTION" =~ ^[0-9]+$ ]] || [ "$SELECTION" -lt 1 ] || [ "$SELECTION" -gt ${#BACKUPS[@]} ]; then
        echo -e "${RED}❌ Selección inválida${NC}"
        return
    fi
    
    SELECTED_BACKUP="${BACKUPS[$((SELECTION-1))]}"
    
    echo ""
    read -p "¿Estás seguro de eliminar $SELECTED_BACKUP? (s/n): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Ss]$ ]]; then
        rm "$SELECTED_BACKUP"
        echo -e "${GREEN}✓${NC} Backup eliminado"
    else
        echo -e "${YELLOW}❌ Eliminación cancelada${NC}"
    fi
}

cleanup_old_backups() {
    BACKUPS=($(ls -t .cursorrules.backup.* 2>/dev/null || true))
    
    if [ ${#BACKUPS[@]} -le 5 ]; then
        echo -e "${GREEN}✓${NC} Solo hay ${#BACKUPS[@]} backups, no es necesario limpiar"
        return
    fi
    
    echo -e "${YELLOW}📊 Backups actuales: ${#BACKUPS[@]}${NC}"
    echo -e "${YELLOW}Se mantendrán los 5 más recientes${NC}"
    echo -e "${YELLOW}Se eliminarán: $((${#BACKUPS[@]} - 5)) backups${NC}"
    echo ""
    
    read -p "¿Continuar? (s/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo -e "${YELLOW}❌ Limpieza cancelada${NC}"
        return
    fi
    
    # Eliminar backups antiguos (mantener los 5 más recientes)
    for i in "${!BACKUPS[@]}"; do
        if [ $i -ge 5 ]; then
            rm "${BACKUPS[$i]}"
            echo -e "${GREEN}✓${NC} Eliminado: ${BACKUPS[$i]}"
        fi
    done
    
    echo ""
    echo -e "${GREEN}✅ Limpieza completada${NC}"
}

compare_backups() {
    BACKUPS=($(ls -t .cursorrules.backup.* 2>/dev/null || true))
    
    if [ ${#BACKUPS[@]} -lt 2 ]; then
        echo -e "${RED}❌ Se necesitan al menos 2 backups para comparar${NC}"
        return
    fi
    
    list_backups
    echo ""
    echo -e "${YELLOW}Selecciona el primer backup (1-${#BACKUPS[@]}):${NC}"
    read -p "> " SELECTION1
    
    echo -e "${YELLOW}Selecciona el segundo backup (1-${#BACKUPS[@]}):${NC}"
    read -p "> " SELECTION2
    
    if ! [[ "$SELECTION1" =~ ^[0-9]+$ ]] || [ "$SELECTION1" -lt 1 ] || [ "$SELECTION1" -gt ${#BACKUPS[@]} ]; then
        echo -e "${RED}❌ Primera selección inválida${NC}"
        return
    fi
    
    if ! [[ "$SELECTION2" =~ ^[0-9]+$ ]] || [ "$SELECTION2" -lt 1 ] || [ "$SELECTION2" -gt ${#BACKUPS[@]} ]; then
        echo -e "${RED}❌ Segunda selección inválida${NC}"
        return
    fi
    
    BACKUP1="${BACKUPS[$((SELECTION1-1))]}"
    BACKUP2="${BACKUPS[$((SELECTION2-1))]}"
    
    echo ""
    echo -e "${BLUE}📊 Diferencias entre:${NC}"
    echo "  1. $BACKUP1"
    echo "  2. $BACKUP2"
    echo ""
    
    diff -u "$BACKUP1" "$BACKUP2" || true
}

# Menú principal
while true; do
    show_menu
    read -p "Selecciona una opción (1-6): " OPTION
    echo ""
    
    case $OPTION in
        1)
            list_backups
            ;;
        2)
            ./scripts/restore_cursorrules.sh
            ;;
        3)
            delete_backup
            ;;
        4)
            cleanup_old_backups
            ;;
        5)
            compare_backups
            ;;
        6)
            echo -e "${GREEN}👋 ¡Hasta luego!${NC}"
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Opción inválida${NC}"
            ;;
    esac
    
    echo ""
    read -p "Presiona Enter para continuar..."
done
