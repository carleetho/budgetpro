#!/bin/bash
# Script para formatear comentarios de PR desde el reporte JSON de validación

set -euo pipefail

if [ $# -lt 1 ]; then
    echo "Uso: $0 <validation-report.json>" >&2
    exit 1
fi

REPORT_FILE="$1"

if [ ! -f "$REPORT_FILE" ]; then
    echo "❌ Error: Archivo de reporte no encontrado: $REPORT_FILE" >&2
    exit 1
fi

# Verificar que jq está instalado
if ! command -v jq &> /dev/null; then
    echo "❌ Error: jq no está instalado. Instálalo con: apt-get install jq" >&2
    exit 1
fi

# Extraer información del reporte
STATUS=$(jq -r '.status' "$REPORT_FILE")
VALIDATION_ID=$(jq -r '.validation_id' "$REPORT_FILE")
TIMESTAMP=$(jq -r '.timestamp' "$REPORT_FILE")
CANONICAL_VERSION=$(jq -r '.canonical_version' "$REPORT_FILE")
TOTAL_VIOLATIONS=$(jq '.violations | length' "$REPORT_FILE")
CRITICAL_COUNT=$(jq '[.violations[] | select(.severity == "CRITICAL")] | length' "$REPORT_FILE")
WARNING_COUNT=$(jq '[.violations[] | select(.severity == "WARNING")] | length' "$REPORT_FILE")
INFO_COUNT=$(jq '[.violations[] | select(.severity == "INFO")] | length' "$REPORT_FILE")

# Determinar emoji y color según estado
case "$STATUS" in
    "PASSED")
        STATUS_EMOJI="✅"
        STATUS_COLOR=":green_circle:"
        ;;
    "WARNINGS")
        STATUS_EMOJI="⚠️"
        STATUS_COLOR=":yellow_circle:"
        ;;
    "CRITICAL_VIOLATIONS")
        STATUS_EMOJI="❌"
        STATUS_COLOR=":red_circle:"
        ;;
    *)
        STATUS_EMOJI="⚠️"
        STATUS_COLOR=":orange_circle:"
        ;;
esac

# Construir comentario
cat << EOF
## 📋 Validación del Roadmap Canónico

${STATUS_EMOJI} **Estado**: ${STATUS} ${STATUS_COLOR}

**Versión del Roadmap**: ${CANONICAL_VERSION}  
**ID de Validación**: \`${VALIDATION_ID}\`  
**Timestamp**: ${TIMESTAMP}

### 📊 Resumen

- **Total de Violaciones**: ${TOTAL_VIOLATIONS}
- **🔴 Críticas**: ${CRITICAL_COUNT}
- **🟡 Advertencias**: ${WARNING_COUNT}
- **🔵 Información**: ${INFO_COUNT}

EOF

# Agregar violaciones críticas
if [ "$CRITICAL_COUNT" -gt 0 ]; then
    cat << EOF
### 🔴 Violaciones Críticas (Bloquean Merge)

Estas violaciones **deben** resolverse antes de poder hacer merge:

EOF
    
    jq -r '.violations[] | select(.severity == "CRITICAL") | 
        "#### 📌 Módulo: `\(.module_id)`\n" +
        "- **Tipo**: `\(.type)`\n" +
        "- **Mensaje**: \(.message)\n" +
        (if .suggestion then "- **Sugerencia**: \(.suggestion)\n" else "" end) +
        (if .context then "- **Contexto**: \(.context | tostring)\n" else "" end) +
        "\n---\n"' "$REPORT_FILE"
fi

# Agregar advertencias
if [ "$WARNING_COUNT" -gt 0 ]; then
    cat << EOF
### 🟡 Advertencias (Revisión Recomendada)

Estas advertencias no bloquean el merge pero se recomienda revisarlas:

EOF
    
    jq -r '.violations[] | select(.severity == "WARNING") | 
        "#### ⚠️ Módulo: `\(.module_id)`\n" +
        "- **Tipo**: `\(.type)`\n" +
        "- **Mensaje**: \(.message)\n" +
        (if .suggestion then "- **Sugerencia**: \(.suggestion)\n" else "" end) +
        "\n---\n"' "$REPORT_FILE"
fi

# Agregar estado de módulos
cat << EOF
### 📦 Estado de Módulos

EOF

jq -r '.module_statuses[] | 
    "#### `\(.module_id)`\n" +
    "- **Estado**: `\(.implementation_status)`\n" +
    "- **Entidades detectadas**: \(.detected_entities | length)\n" +
    "- **Servicios detectados**: \(.detected_services | length)\n" +
    "- **Endpoints detectados**: \(.detected_endpoints | length)\n" +
    (if (.missing_dependencies | length) > 0 then "- **Dependencias faltantes**: \(.missing_dependencies | join(", "))\n" else "" end) +
    "\n---\n"' "$REPORT_FILE"

# Footer
cat << EOF

---

**💡 Nota**: Este reporte fue generado automáticamente por el validador de roadmap canónico.  
Para más información, consulta [ROADMAP_CANONICO.md](../../docs/context/ROADMAP_CANONICO.md)

EOF
