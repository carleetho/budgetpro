#!/bin/bash
# install_cursorrules_tools.sh - Instala las herramientas de gestión de .cursorrules

set -e

echo "🔧 Instalando herramientas de gestión de .cursorrules..."
echo ""

# Carpeta scripts ya debería existir si este script está corriendo desde ahí o se llamó antes
mkdir -p scripts

# Los archivos ya fueron escritos directamente, este instalador se asegura de los permisos y symlinks
chmod +x scripts/verify_cursorrules.sh
chmod +x scripts/update_cursorrules.sh

# Crear enlaces simbólicos en la raíz
echo "🔗 Creando enlaces simbólicos..."
ln -sf scripts/verify_cursorrules.sh verify_cursorrules.sh
ln -sf scripts/update_cursorrules.sh update_cursorrules.sh
echo "✓ Enlaces creados"

# Instalar dependencia de PyYAML si no está
echo "📦 Verificando dependencias..."
if ! python3 -c "import yaml" 2>/dev/null; then
    echo "Instalando PyYAML..."
    pip3 install pyyaml
fi
echo "✓ Dependencias verificadas"

echo ""
echo "✅ Instalación completada"
echo ""
echo "📋 Comandos disponibles:"
echo "  ./verify_cursorrules.sh  - Verifica sincronización"
echo "  ./update_cursorrules.sh  - Actualiza .cursorrules desde axiom.yaml"
echo ""
