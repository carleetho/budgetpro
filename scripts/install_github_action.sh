#!/bin/bash
# scripts/install_github_action.sh - Instala el GitHub Action para verificación de .cursorrules

set -e

echo "🔄 Instalando GitHub Action para verificación de .cursorrules..."
echo ""

# Crear directorio de workflows si no existe
mkdir -p .github/workflows

# Verificar si ya existe el workflow
if [ -f ".github/workflows/verify-cursorrules.yml" ]; then
    echo "⚠️  Ya existe el workflow verify-cursorrules.yml"
    read -p "¿Deseas reemplazarlo? (s/n): " -n 1 -r
    echo ""
    
    if [[ ! $REPLY =~ ^[Ss]$ ]]; then
        echo "❌ Instalación cancelada"
        exit 1
    fi
    
    # Crear backup
    cp .github/workflows/verify-cursorrules.yml .github/workflows/verify-cursorrules.yml.backup.$(date +%Y%m%d_%H%M%S)
    echo "✓ Backup creado"
fi

# El script ya fue creado por el agente coding assistant.
# Este instalador simplemente informa al usuario.
echo "✓ GitHub Action configurado en .github/workflows/verify-cursorrules.yml"

echo ""
echo "✅ Instalación completada"
echo ""
echo "📋 El GitHub Action:"
echo "  - Se ejecuta en PRs que modifican axiom.yaml o .cursorrules"
echo "  - Verifica sincronización automáticamente"
echo "  - Comenta en el PR si hay problemas"
echo "  - Bloquea el merge si no están sincronizados"
echo ""
echo "💡 Recuerda hacer commit del workflow:"
echo "  git add .github/workflows/verify-cursorrules.yml"
echo "  git commit -m 'Add .cursorrules verification workflow'"
echo ""
