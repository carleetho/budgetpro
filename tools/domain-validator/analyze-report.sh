#!/bin/bash
# Script para analizar report.json del Domain Validator
# Genera un análisis estructurado de violaciones y módulos

set -euo pipefail

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

REPORT_FILE="${1:-report.json}"

if [ ! -f "$REPORT_FILE" ]; then
    echo -e "${RED}❌ Error: Archivo no encontrado: $REPORT_FILE${NC}" >&2
    echo "Uso: $0 [report.json]" >&2
    exit 1
fi

# Verificar que Python está disponible
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ Error: Python3 no está instalado${NC}" >&2
    exit 1
fi

echo -e "${BLUE}📊 Analizando reporte: $REPORT_FILE${NC}\n"

python3 << PYTHON_SCRIPT
import json
import sys
from collections import defaultdict

report_file = "$REPORT_FILE"
with open(report_file, 'r') as f:
    data = json.load(f)

print("=" * 80)
print("📋 RESUMEN EJECUTIVO")
print("=" * 80)
print(f"Estado General: {data['status']}")
print(f"Exit Code: {data['exitCode']}")
print(f"Versión Roadmap: {data['canonical_version']}")
print(f"Timestamp: {data['timestamp']}")
print(f"Repositorio: {data['repository_path']}")
print()

# Contar violaciones
critical = sum(1 for v in data['violations'] if v['severity'] == 'CRITICAL')
warnings = sum(1 for v in data['violations'] if v['severity'] == 'WARNING')
total = len(data['violations'])

print(f"Total Violaciones: {total}")
print(f"  🔴 Críticas: {critical}")
print(f"  🟡 Advertencias: {warnings}")
print()

# Estado de módulos
complete = sum(1 for m in data['module_statuses'] if m['implementation_status'] == 'COMPLETE')
in_progress = sum(1 for m in data['module_statuses'] if m['implementation_status'] == 'IN_PROGRESS')
not_started = sum(1 for m in data['module_statuses'] if m['implementation_status'] == 'NOT_STARTED')

print(f"Estado de Módulos:")
print(f"  ✅ COMPLETE: {complete}")
print(f"  🟡 IN_PROGRESS: {in_progress}")
print(f"  ⚪ NOT_STARTED: {not_started}")
print()

print("=" * 80)
print("🔴 VIOLACIONES CRÍTICAS POR MÓDULO")
print("=" * 80)

# Agrupar por módulo
by_module = defaultdict(list)
for v in data['violations']:
    if v['severity'] == 'CRITICAL':
        by_module[v['module_id']].append(v)

for module in sorted(by_module.keys()):
    violations = by_module[module]
    print(f"\n📦 {module.upper()} ({len(violations)} críticas)")
    for i, v in enumerate(violations, 1):
        print(f"  {i}. [{v['type']}] {v['message']}")
        if v.get('suggestion'):
            print(f"     💡 {v['suggestion']}")

print()
print("=" * 80)
print("📊 VIOLACIONES POR TIPO")
print("=" * 80)

by_type = defaultdict(lambda: {'critical': 0, 'warning': 0})
for v in data['violations']:
    by_type[v['type']][v['severity'].lower()] += 1

for vtype in sorted(by_type.keys()):
    counts = by_type[vtype]
    print(f"{vtype}:")
    print(f"  🔴 Críticas: {counts['critical']}")
    print(f"  🟡 Advertencias: {counts['warning']}")

print()
print("=" * 80)
print("🎯 PRIORIZACIÓN DE ACCIONES")
print("=" * 80)

# Identificar falsos positivos conocidos
false_positives = []
real_issues = []

for v in data['violations']:
    if v['severity'] == 'CRITICAL':
        msg = v['message'].lower()
        if 'billetera' in msg and 'proyecto' in v['module_id']:
            false_positives.append(("Billetera en proyecto", v))
        elif 'estadopresupuesto' in msg:
            false_positives.append(("EstadoPresupuesto no detectado", v))
        elif 'naturalezagasto' in msg:
            false_positives.append(("NaturalezaGasto no detectado", v))
        elif 'apusnapshot' in msg and 'presupuesto' in v['module_id']:
            false_positives.append(("APUSnapshot en presupuesto", v))
        else:
            real_issues.append(v)

if false_positives:
    print("\n🔧 FALSOS POSITIVOS (Corregir Detección):")
    for name, v in false_positives:
        print(f"  • {name}")
        print(f"    Módulo: {v['module_id']}")
        print(f"    Mensaje: {v['message']}")

if real_issues:
    print(f"\n⚠️  PROBLEMAS REALES ({len(real_issues)} críticas):")
    by_module_real = defaultdict(list)
    for v in real_issues:
        by_module_real[v['module_id']].append(v)
    
    for module in sorted(by_module_real.keys()):
        print(f"  📦 {module}: {len(by_module_real[module])} problemas")

print()
print("=" * 80)
print("📈 ESTADO DE MÓDULOS DETALLADO")
print("=" * 80)

for module in sorted(data['module_statuses'], key=lambda x: x['module_id']):
    status = module['implementation_status']
    icon = "✅" if status == "COMPLETE" else "🟡" if status == "IN_PROGRESS" else "⚪"
    print(f"\n{icon} {module['module_id']}: {status}")
    print(f"   Entidades: {len(module['detected_entities'])}")
    print(f"   Servicios: {len(module['detected_services'])}")
    print(f"   Endpoints: {len(module['detected_endpoints'])}")
    if module.get('missing_dependencies'):
        print(f"   ⚠️  Dependencias faltantes: {', '.join(module['missing_dependencies'])}")

print()
print("=" * 80)
PYTHON_SCRIPT

echo -e "\n${GREEN}✅ Análisis completado${NC}"
