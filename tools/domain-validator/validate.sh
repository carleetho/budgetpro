#!/bin/bash
# Script de automatización para ejecutar Domain Validator
# Uso: ./validate.sh [opciones]

set -euo pipefail

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración por defecto
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VALIDATOR_DIR="$SCRIPT_DIR"
JAR_FILE="$VALIDATOR_DIR/target/domain-validator-1.0.0-SNAPSHOT.jar"
REPO_PATH="$VALIDATOR_DIR/../../backend"
OUTPUT_FORMAT="text"
OUTPUT_FILE=""
STRICT_MODE=false
BUILD_IF_MISSING=true
AUTO_FIX=false

# Función para mostrar ayuda
show_help() {
    cat << EOF
${BLUE}Domain Validator - Script de Automatización${NC}

${GREEN}Uso:${NC}
    ./validate.sh [opciones]

${GREEN}Opciones:${NC}
    -p, --repo-path PATH      Ruta al directorio backend (default: ../../backend)
    -f, --format FORMAT       Formato de salida: text, json (default: text)
    -o, --output FILE         Archivo de salida (solo para JSON)
    -s, --strict              Modo estricto: advertencias también bloquean
    -b, --no-build            No construir JAR si no existe
    -h, --help                Mostrar esta ayuda
    -v, --version             Mostrar versión

${GREEN}Ejemplos:${NC}
    # Validación básica
    ./validate.sh

    # Validación con salida JSON
    ./validate.sh --format json --output report.json

    # Validación estricta con ruta personalizada
    ./validate.sh --strict --repo-path /path/to/backend

    # Validación sin construir JAR
    ./validate.sh --no-build

${GREEN}Exit Codes:${NC}
    0 - Validación pasada sin violaciones
    1 - Violaciones críticas detectadas
    2 - Advertencias detectadas
    3 - Error durante el análisis
EOF
}

# Función para construir el JAR
build_jar() {
    echo -e "${BLUE}🔨 Construyendo Domain Validator...${NC}"
    cd "$VALIDATOR_DIR"
    
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Error: Maven no está instalado${NC}" >&2
        exit 1
    fi
    
    mvn clean package -DskipTests
    echo -e "${GREEN}✅ JAR construido exitosamente${NC}"
}

# Función para verificar que el JAR existe
check_jar() {
    if [ ! -f "$JAR_FILE" ]; then
        if [ "$BUILD_IF_MISSING" = true ]; then
            echo -e "${YELLOW}⚠️  JAR no encontrado. Construyendo...${NC}"
            build_jar
        else
            echo -e "${RED}❌ Error: JAR no encontrado en $JAR_FILE${NC}" >&2
            echo -e "${YELLOW}💡 Ejecuta: mvn clean package${NC}" >&2
            exit 1
        fi
    fi
}

# Función para verificar Java
check_java() {
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Error: Java no está instalado${NC}" >&2
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        echo -e "${RED}❌ Error: Se requiere Java 17 o superior. Versión actual: $JAVA_VERSION${NC}" >&2
        exit 1
    fi
}

# Función principal de validación
run_validation() {
    echo -e "${BLUE}🔍 Ejecutando validación...${NC}"
    echo -e "${GREEN}📁 Repositorio: $REPO_PATH${NC}"
    echo -e "${GREEN}📊 Formato: $OUTPUT_FORMAT${NC}"
    [ "$STRICT_MODE" = true ] && echo -e "${YELLOW}⚡ Modo estricto: activado${NC}"
    echo ""
    
    # Construir comando base
    CMD="java -jar \"$JAR_FILE\" validate --repo-path \"$REPO_PATH\""
    
    # Agregar opciones
    [ "$STRICT_MODE" = true ] && CMD="$CMD --strict"
    [ "$OUTPUT_FORMAT" = "json" ] && CMD="$CMD --output-format json"
    [ -n "$OUTPUT_FILE" ] && CMD="$CMD --output-file \"$OUTPUT_FILE\""
    
    # Ejecutar validación
    eval $CMD
    EXIT_CODE=$?
    
    echo ""
    case $EXIT_CODE in
        0)
            echo -e "${GREEN}✅ Validación completada: PASSED${NC}"
            ;;
        1)
            echo -e "${RED}❌ Validación completada: CRITICAL_VIOLATIONS${NC}"
            ;;
        2)
            echo -e "${YELLOW}⚠️  Validación completada: WARNINGS${NC}"
            ;;
        3)
            echo -e "${RED}❌ Error durante el análisis${NC}"
            ;;
        *)
            echo -e "${RED}❌ Exit code desconocido: $EXIT_CODE${NC}"
            ;;
    esac
    
    return $EXIT_CODE
}

# Parsear argumentos
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--repo-path)
            REPO_PATH="$2"
            shift 2
            ;;
        -f|--format)
            OUTPUT_FORMAT="$2"
            shift 2
            ;;
        -o|--output)
            OUTPUT_FILE="$2"
            shift 2
            ;;
        -s|--strict)
            STRICT_MODE=true
            shift
            ;;
        -b|--no-build)
            BUILD_IF_MISSING=false
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        -v|--version)
            if [ -f "$JAR_FILE" ]; then
                java -jar "$JAR_FILE" --version 2>/dev/null || echo "1.0.0-SNAPSHOT"
            else
                echo "1.0.0-SNAPSHOT"
            fi
            exit 0
            ;;
        *)
            echo -e "${RED}❌ Opción desconocida: $1${NC}" >&2
            show_help
            exit 1
            ;;
    esac
done

# Verificar que el directorio del repositorio existe
if [ ! -d "$REPO_PATH" ]; then
    echo -e "${RED}❌ Error: Directorio no encontrado: $REPO_PATH${NC}" >&2
    exit 1
fi

# Verificar Java
check_java

# Verificar y construir JAR si es necesario
check_jar

# Ejecutar validación
run_validation
exit $?
