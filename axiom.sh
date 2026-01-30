#!/bin/bash
# AXIOM Unified Control Suite
# One-stop solution for BudgetPro protection.

set -e

# ANSI Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Discovery
START_DIR=$(pwd)
AXIOM_DIR="$START_DIR/tools/axiom"
export PYTHONPATH="$START_DIR"

show_help() {
    echo -e "${GREEN}🛡️  AXIOM Unified Control Suite${NC}"
    echo -e "Uso: ${YELLOW}./axiom.sh [comando] [opciones]${NC}\n"
    echo "Comandos:"
    echo "  run        Validación AXIOM (Default)"
    echo "  dashboard  Lanza el Dashboard interactivo"
    echo "  install    Instala hooks y dependencias"
    echo "  help       Muestra esta ayuda"
}

run_sentinel() {
    echo -e "${GREEN}🛡️ Iniciando AXIOM Sentinel...${NC}"
    set +e
    python3 "$AXIOM_DIR/axiom_sentinel.py" "$@"
    EXIT_CODE=$?
    set -e
    return $EXIT_CODE
}

run_dashboard() {
    echo -e "${GREEN}📊 Iniciando AXIOM Dashboard...${NC}"
    # Auto-dependency check
    deps=("streamlit" "plotly" "pandas" "reportlab" "streamlit-plotly-events")
    for dep in "${deps[@]}"; do
        if ! python3 -c "import ${dep//-/_}" &> /dev/null; then
            echo -e "${YELLOW}⚠️ Instalando $dep...${NC}"
            pip3 install "$dep" --quiet
        fi
    done
    # Run headless/skip welcome to avoid hanging on email prompt
    streamlit run "$AXIOM_DIR/dashboard.py" --browser.gatherUsageStats false
}

run_install() {
    bash "$AXIOM_DIR/install.sh"
    chmod +x backend/mvnw 2>/dev/null || true
}

COMMAND=$1
if [ -n "$COMMAND" ]; then shift; fi

case "$COMMAND" in
    run|"") run_sentinel "$@"; exit $? ;;
    dashboard|dash) run_dashboard ;;
    install) run_install ;;
    help|--help|-h) show_help ;;
    *)
        if [[ "$COMMAND" == --* ]]; then
             run_sentinel "$COMMAND" "$@"
             exit $?
        fi
        echo -e "${RED}❌ Comando desconocido: $COMMAND${NC}"; show_help; exit 2
        ;;
esac
