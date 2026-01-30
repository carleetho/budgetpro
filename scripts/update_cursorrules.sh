#!/bin/bash
# update_cursorrules.sh - Actualiza .cursorrules desde axiom.yaml

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔄 AXIOM - Actualización de .cursorrules${NC}"
echo "================================================"
echo ""

# Verificar que existe axiom.yaml
if [ ! -f ".budgetpro/axiom.config.yaml" ]; then
    echo -e "${RED}❌ ERROR: .budgetpro/axiom.config.yaml no encontrado${NC}"
    exit 1
fi

# Backup de .cursorrules existente
if [ -f ".cursorrules" ]; then
    BACKUP_FILE=".cursorrules.backup.$(date +%Y%m%d_%H%M%S)"
    echo -e "${YELLOW}📦 Creando backup: ${BACKUP_FILE}${NC}"
    cp .cursorrules "$BACKUP_FILE"
    echo -e "${GREEN}✓${NC} Backup creado"
fi

echo ""
echo -e "${YELLOW}🔨 Generando nuevo .cursorrules...${NC}"

# Crear script Python para generar .cursorrules
cat > /tmp/generate_cursorrules.py << 'PYTHON_SCRIPT'
import yaml
import sys
from datetime import datetime

def load_axiom_config():
    """Carga la configuración de axiom.yaml"""
    try:
        with open('.budgetpro/axiom.config.yaml', 'r') as f:
            return yaml.safe_load(f)
    except Exception as e:
        print(f"❌ Error leyendo axiom.yaml: {e}")
        sys.exit(1)

def generate_cursorrules(config):
    """Generate .cursorrules from axiom.config.yaml"""
    
    protection_zones = config.get('protection_zones', {})
    validators = config.get('validators', {})
    
    # Template
    content = f"""# 🛡️ AXIOM - Architectural Integrity Guardian (BudgetPro)

## 🚨 REGLA SUPREMA: AXIOM ES LEY
AXIOM es el guardián arquitectónico de este proyecto. TODAS las sugerencias de código DEBEN pasar validación AXIOM antes de ser ejecutadas. NO realices cambios que violen las reglas arquitectónicas.

---

## 🧼 PROTOCOLO "MANOS LIMPIAS" (ANTIGRAVITY EXCLUSIVE)
En caso de fallos de compilación masivos (>50 errores):

1. **DIAGNÓSTICO SIN MUTACIÓN**: Extraer únicamente los **primeros 20 errores** para identificar la raíz. No editar archivos hasta que la raíz sea confirmada.
2. **PRIORIDAD DE CONSUMIDORES**: Si la falla es una referencia rota a una clase de Dominio, se corregirá el `import` o la llamada en `Application/Infrastructure`. **NUNCA** se corregirá el Dominio para satisfacer un error de capa superior.
3. **STOP EN ZONA ROJA**: Si la raíz está **realmente** en `com.budgetpro.domain`, detenerse, presentar un `implementation_plan.md` y esperar aprobación explícita.
4. **VALIDACIÓN ATÓMICA**: Cada cambio individual debe ser validado con `./axiom.sh --dry-run` antes de proceder al siguiente.

---

## 🎯 INICIALIZACIÓN AUTOMÁTICA
Antes de Escribir Cualquier Código:
1. **SIEMPRE ejecuta**: `./axiom.sh --dry-run` para validar el estado actual.
2. **VERIFICA** que no hay violaciones pendientes.
3. **CONSULTA** `.budgetpro/axiom.config.yaml` para entender las reglas.

---

## 🏛️ ARQUITECTURA HEXAGONAL - REGLAS INQUEBRANTABLES
```
Domain (Core) ← Application ← Infrastructure
     ↑              ↑              ↑
  NUNCA         NUNCA          PUEDE
 depende       depende        depender
   de            de            de
 nada          Infra         todo
```

---

## 🔒 PROTECCIÓN POR ZONAS (BLAST RADIUS)
"""
    
    # Dynamic zones
    for level, zones in protection_zones.items():
        content += f"### Zona {level.upper()}\n"
        for zone in zones:
            path = zone['path']
            max_files = zone.get('max_files', 1)
            content += f"- **Ruta**: `{path}` | **Límite**: {max_files} archivos/commit\n"
        content += "\n"
    
    content += """---

## 🤖 INSTRUCCIONES PARA AI ASSISTANTS
"""
    
    validator_mapping = {
        'blast_radius': '✅ Verifica Blast Radius del cambio',
        'security_validator': '✅ Confirma que no hay secretos expuestos',
        'lazy_code': '✅ Asegura que no hay Lazy Code',
        'dependency_validator': '✅ Verifica Arquitectura Hexagonal / Aislamiento de Dominio'
    }
    
    for v_id, text in validator_mapping.items():
        if validators.get(v_id, {}).get('enabled', True):
            content += f"{text}\n"
    
    content += f"""
### 🚀 WORKFLOW
1. `./axiom.sh --status`
2. `./axiom.sh --dry-run`

---

*Última actualización: {datetime.now().strftime('%Y-%m-%d')}*
*Gobernanza Generada desde: axiom.config.yaml*
"""
    return content

def main():
    print("Cargando configuración de axiom.yaml...")
    config = load_axiom_config()
    
    print("Generando contenido de .cursorrules...")
    content = generate_cursorrules(config)
    
    print("Escribiendo .cursorrules...")
    try:
        with open('.cursorrules', 'w') as f:
            f.write(content)
        print("✓ .cursorrules actualizado exitosamente")
    except Exception as e:
        print(f"❌ Error escribiendo .cursorrules: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
PYTHON_SCRIPT

# Ejecutar el generador
python3 /tmp/generate_cursorrules.py

# Limpiar
rm /tmp/generate_cursorrules.py

echo ""
echo -e "${GREEN}✅ .cursorrules actualizado exitosamente${NC}"
