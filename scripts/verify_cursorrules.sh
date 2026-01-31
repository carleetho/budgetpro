#!/bin/bash
# verify_cursorrules.sh - Verifica que .cursorrules esté sincronizado con axiom.yaml

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 AXIOM - Verificación de .cursorrules${NC}"
echo "================================================"
echo ""

# Verificar que existen los archivos necesarios
echo -e "${YELLOW}📁 Verificando archivos...${NC}"

if [ ! -f ".budgetpro/axiom.config.yaml" ]; then
    echo -e "${RED}❌ ERROR: .budgetpro/axiom.config.yaml no encontrado${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} axiom.config.yaml encontrado"

if [ ! -f ".cursorrules" ]; then
    echo -e "${RED}❌ ERROR: .cursorrules no encontrado${NC}"
    echo -e "${YELLOW}💡 Ejecuta: ./update_cursorrules.sh para crearlo${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} .cursorrules encontrado"

echo ""

# Verificar que Python está instalado
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}❌ ERROR: Python 3 no está instalado${NC}"
    exit 1
fi
echo -e "${GREEN}✓${NC} Python 3 disponible"

echo ""
echo -e "${YELLOW}🔎 Analizando configuración...${NC}"

# Crear script Python temporal para análisis
cat > /tmp/verify_axiom_rules.py << 'PYTHON_SCRIPT'
import yaml
import re
import sys
from pathlib import Path

def load_axiom_config():
    """Carga la configuración de axiom.yaml"""
    try:
        with open('.budgetpro/axiom.config.yaml', 'r') as f:
            return yaml.safe_load(f)
    except Exception as e:
        print(f"❌ Error leyendo axiom.yaml: {e}")
        sys.exit(1)

def load_cursorrules():
    """Carga el contenido de .cursorrules"""
    try:
        with open('.cursorrules', 'r') as f:
            return f.read()
    except Exception as e:
        print(f"❌ Error leyendo .cursorrules: {e}")
        sys.exit(1)

def extract_protected_modules(config):
    """Extrae módulos protegidos de axiom.config.yaml"""
    modules = []
    if 'protection_zones' in config:
        for level, zones in config['protection_zones'].items():
            for zone in zones:
                modules.append(zone['path'])
    return modules

def extract_validators(config):
    """Extrae validadores habilitados de axiom.yaml"""
    validators = []
    if 'validators' in config:
        for validator_name, validator_config in config['validators'].items():
            if validator_config.get('enabled', True):
                validators.append(validator_name)
    return validators

def check_protected_modules_in_rules(cursorrules, modules):
    """Verifica que los módulos protegidos estén mencionados en .cursorrules"""
    issues = []
    for module in modules:
        if module not in cursorrules:
            issues.append(f"Ruta protegida '{module}' no mencionada en .cursorrules")
    return issues

def check_validators_in_rules(cursorrules, validators):
    """Verifica que los validadores estén mencionados en .cursorrules"""
    issues = []
    validator_mapping = {
        'blast_radius': 'Blast Radius',
        'security_validator': 'secretos expuestos',
        'lazy_code': 'Lazy Code',
        'dependency_validator': 'Arquitectura Hexagonal',
        'naming_validator': 'nomenclatura',
        'boundary_validator': 'Fronteras Hexagonales',
        'state_machine_validator': 'transiciones de estado',
        'semgrep_validator': 'seguridad'
    }
    
    for validator in validators:
        search_term = validator_mapping.get(validator, validator)
        if search_term not in cursorrules:
            issues.append(f"Validador '{validator}' no mencionado en .cursorrules")
    return issues

def check_blast_radius_limits(config, cursorrules):
    """Verifica que los límites de blast radius coincidan"""
    issues = []
    if 'protection_zones' in config:
        for level, zones in config['protection_zones'].items():
            for zone in zones:
                path = zone['path']
                max_files = zone.get('max_files', 1)
                
                # Buscar el límite en .cursorrules (específico al formato de lista generado)
                pattern = rf"-\s+\*\*Ruta\*\*:\s*[`']?{re.escape(path)}[`']?.*?L[íi]mite\*\*:\s*(\d+)"
                match = re.search(pattern, cursorrules, re.IGNORECASE | re.DOTALL)
                
                if match:
                    cursorrules_limit = int(match.group(1))
                    if cursorrules_limit != max_files:
                        issues.append(
                            f"Límite de blast radius para '{path}' no coincide: "
                            f"axiom.yaml={max_files}, .cursorrules={cursorrules_limit}"
                        )
                else:
                    issues.append(f"Límite de blast radius para '{path}' no encontrado en .cursorrules")
    return issues

def check_version_sync(config, cursorrules):
    """Verifica que las versiones estén sincronizadas"""
    return [] # axiom.config.yaml actual no tiene versión

def main():
    print("Cargando configuración...")
    config = load_axiom_config()
    cursorrules = load_cursorrules()
    
    print("✓ Archivos cargados correctamente\n")
    
    all_issues = []
    
    # Verificar módulos protegidos
    print("🔒 Verificando módulos protegidos...")
    modules = extract_protected_modules(config)
    issues = check_protected_modules_in_rules(cursorrules, modules)
    all_issues.extend(issues)
    if not issues:
        print(f"  ✓ Todos los módulos protegidos están documentados ({len(modules)} módulos)")
    else:
        for issue in issues:
            print(f"  ⚠️  {issue}")
    
    # Verificar validadores
    print("\n🔍 Verificando validadores...")
    validators = extract_validators(config)
    issues = check_validators_in_rules(cursorrules, validators)
    all_issues.extend(issues)
    if not issues:
        print(f"  ✓ Todos los validadores están documentados ({len(validators)} validadores)")
    else:
        for issue in issues:
            print(f"  ⚠️  {issue}")
    
    # Verificar límites de blast radius
    print("\n💥 Verificando límites de blast radius...")
    issues = check_blast_radius_limits(config, cursorrules)
    all_issues.extend(issues)
    if not issues:
        print("  ✓ Todos los límites de blast radius coinciden")
    else:
        for issue in issues:
            print(f"  ⚠️  {issue}")
    
    # Verificar versiones
    print("\n📌 Verificando versiones...")
    issues = check_version_sync(config, cursorrules)
    all_issues.extend(issues)
    if not issues:
        print("  ✓ Versiones sincronizadas")
    else:
        for issue in issues:
            print(f"  ⚠️  {issue}")
    
    # Resumen final
    print("\n" + "="*50)
    if not all_issues:
        print("✅ VERIFICACIÓN EXITOSA")
        print("   .cursorrules está sincronizado con axiom.config.yaml")
        sys.exit(0)
    else:
        print(f"⚠️  ENCONTRADOS {len(all_issues)} PROBLEMAS")
        print("\n💡 Ejecuta: ./update_cursorrules.sh para sincronizar")
        sys.exit(1)

if __name__ == "__main__":
    main()
PYTHON_SCRIPT

# Ejecutar el script de verificación
python3 /tmp/verify_axiom_rules.py

# Limpiar
rm /tmp/verify_axiom_rules.py

echo ""
echo -e "${BLUE}================================================${NC}"
echo -e "${GREEN}✅ Verificación completada${NC}"
