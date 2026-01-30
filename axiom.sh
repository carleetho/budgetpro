#!/bin/bash
# AXIOM Quick Execution Script
# Runs the AXIOM validation pipeline manually.

set -e

# ANSI Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# 1. Check Python 3 Installation
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ Python 3 no encontrado. Instalar Python 3.x${NC}"
    exit 2
fi

# 2. Check PyYAML Dependency
if ! python3 -c "import yaml" &> /dev/null; then
    echo -e "${YELLOW}⚠️ PyYAML no encontrado. Instalando...${NC}"
    pip3 install pyyaml --quiet
    if ! python3 -c "import yaml" &> /dev/null; then
         echo -e "${RED}❌ Error al instalar PyYAML. Por favor instálelo manualmente (pip3 install pyyaml).${NC}"
         exit 2
    fi
    echo -e "${GREEN}✅ PyYAML instalado correctamente.${NC}"
fi

# 3. Validate Directory Structure
START_DIR=$(pwd)
AXIOM_DIR="$START_DIR/tools/axiom"

if [ ! -d "$AXIOM_DIR" ]; then
    echo -e "${RED}❌ AXIOM no instalado. Ejecutar tools/axiom/install.sh${NC}"
    exit 2
fi

# 4. Check Configuration File
if [ ! -f "$START_DIR/axiom.config.yaml" ]; then
    echo -e "${YELLOW}⚠️ Configuración no encontrada, usando valores por defecto${NC}"
fi

# 5. Display ASCII Art Header
echo -e "${GREEN}"
cat << "EOF"
    _    __  __ ___ ___  __  __ 
   / \   \ \/ /|_ _| _ \|  \/  |
  / _ \   >  <  | ||(_) | |\/| |
 /_/ \_\ /_/\_\|___|___/|_|  |_|
 SATINEL - Centinela de Integridad Arquitectónica
EOF
echo -e "${NC}"

# 6. Execute AXIOM Sentinel
# Ensure PYTHONPATH includes the current directory so tools module can be found
export PYTHONPATH="$START_DIR"

# Capture exit code preventing 'set -e' from exiting immediately on failure
set +e
python3 "$AXIOM_DIR/axiom_sentinel.py" "$@"
EXIT_CODE=$?
set -e

# 7. Display Result and Exit
echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✅ Código de salida: 0 (Éxito)${NC}"
elif [ $EXIT_CODE -eq 1 ]; then
    echo -e "${RED}❌ Código de salida: 1 (Violaciones detectadas)${NC}"
elif [ $EXIT_CODE -eq 2 ]; then
    echo -e "${YELLOW}⚠️ Código de salida: 2 (Error del sistema)${NC}"
else
    echo -e "${RED}❌ Código de salida: $EXIT_CODE (Fallo inesperado)${NC}"
fi

exit $EXIT_CODE
