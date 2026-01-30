#!/bin/bash
# workflow.sh - Automatización del flujo BrainGrid + Axiom para BudgetPro

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Función: Iniciar Nueva Tarea (Pasos 1, 2 y 3 de BrainGrid)
function start_task() {
    echo -e "${BLUE}🚀 INICIANDO NUEVO REQUERIMIENTO (Sincronizando main...)${NC}"
    
    # 1. Ir a main y actualizar
    git checkout main
    git pull origin main
    
    echo -e "${YELLOW}Nombre del Branch (Copia y pega de BrainGrid, ej: REQ-14-axiom...):${NC}"
    read -r branch_name
    
    # Limpiar espacios por si acaso
    branch_name=$(echo "$branch_name" | xargs)
    
    if [ -z "$branch_name" ]; then
        echo -e "${RED}❌ El nombre no puede estar vacío.${NC}"
        return
    fi
    
    # 2. Crear branch
    git checkout -b "$branch_name"
    echo -e "${GREEN}✅ Branch '$branch_name' creado. ¡A trabajar con la IA!${NC}"
}

# Función: Guardar Progreso (Paso 5 de BrainGrid + AXIOM)
function save_progress() {
    echo -e "${BLUE}💾 GUARDANDO PROGRESO DE TASK (Ejecutando Axiom...)${NC}"
    
    # 1. Stage de todo
    git add .
    
    # 2. Pedir mensaje
    echo -e "${YELLOW}Mensaje del Commit (ej: feat: implementar validaciones):${NC}"
    read -r commit_msg
    
    if [ -z "$commit_msg" ]; then
        echo -e "${RED}❌ El mensaje es obligatorio.${NC}"
        return
    fi
    
    # 3. Commit (Esto disparará AXIOM automáticamente por el hook pre-commit)
    echo -e "${BLUE}--- Ejecutando Git Commit (Axiom interceptará si hay error) ---${NC}"
    git commit -m "$commit_msg"
    
    # Capturar resultado
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Commit exitoso. Axiom aprobó los cambios.${NC}"
    else
        echo -e "${RED}❌ Commit bloqueado por AXIOM. Revisa los errores arriba.${NC}"
    fi
}

# Función: Subir y Crear PR (Paso 6 de BrainGrid)
function upload_pr() {
    current_branch=$(git branch --show-current)
    
    if [ "$current_branch" == "main" ]; then
        echo -e "${RED}⚠️  Estás en MAIN. Debes estar en un feature branch para hacer PR.${NC}"
        return
    fi

    echo -e "${BLUE}☁️  SUBIENDO '$current_branch' A GITHUB...${NC}"
    
    # 1. Push
    git push -u origin "$current_branch"
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Error al subir cambios.${NC}"
        return
    fi

    # 2. Crear PR (Detectar si tiene GitHub CLI)
    if command -v gh &> /dev/null; then
        echo -e "${BLUE}🔀 Creando Pull Request automáticamente con GitHub CLI...${NC}"
        # --fill usa el título del commit y descripción automática
        gh pr create --fill --web
    else
        echo -e "${YELLOW}⚠️  GitHub CLI ('gh') no está instalado.${NC}"
        echo -e "🔗 Abre este link para crear el PR manualmente:"
        echo -e "\033[4mhttps://github.com/TU_USUARIO/budgetpro/compare/$current_branch?expand=1\033[0m"
    fi
}

# --- MENÚ PRINCIPAL ---
clear
echo -e "${GREEN}=============================================${NC}"
echo -e "      🤖 BUDGETPRO DEV WORKFLOW (BrainGrid)      "
echo -e "${GREEN}=============================================${NC}"
echo "1. 🚀 Iniciar Requerimiento (Branch desde Main)"
echo "2. 💾 Guardar Avance de Task (BrainGrid + Commit)"
echo "3. ☁️  Subir y PR (Push + GitHub)"
echo "4. ❌ Salir"
echo -e "${GREEN}=============================================${NC}"
read -p "Elige una opción (1-4): " option

case $option in
    1) start_task ;;
    2) save_progress ;;
    3) upload_pr ;;
    4) exit 0 ;;
    *) echo -e "${RED}Opción inválida${NC}" ;;
esac