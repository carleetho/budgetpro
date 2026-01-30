#!/bin/bash
# install_cursorrules_hook.sh - Instala el pre-commit hook para .cursorrules

set -e

echo "🪝 Instalando pre-commit hook para verificación de .cursorrules..."
echo ""

# Verificar que estamos en un repositorio git
if [ ! -d ".git" ]; then
    echo "❌ ERROR: No estás en un repositorio git"
    exit 1
fi

# Crear directorio de hooks si no existe
mkdir -p .git/hooks

# Verificar si ya existe un pre-commit hook
if [ -f ".git/hooks/pre-commit" ]; then
    echo "⚠️  Ya existe un pre-commit hook"
    echo ""
    
    # Verificar si ya incluye la verificación de cursorrules
    if grep -q "pre-commit-cursorrules.sh" .git/hooks/pre-commit; then
        echo "✓ El hook ya incluye verificación de .cursorrules"
        exit 0
    fi
    
    echo "Opciones:"
    echo "  1. Agregar verificación al hook existente"
    echo "  2. Crear backup y reemplazar"
    echo "  3. Cancelar"
    echo ""
    read -p "Selecciona una opción (1-3): " -n 1 -r
    echo ""
    
    case $REPLY in
        1)
            echo "📝 Agregando verificación al hook existente..."
            # Agregar llamada al script de verificación
            cat >> .git/hooks/pre-commit << 'EOF'

# AXIOM - Verificación de .cursorrules
if [ -f "./scripts/pre-commit-cursorrules.sh" ]; then
    ./scripts/pre-commit-cursorrules.sh || exit 1
fi
EOF
            echo "✓ Verificación agregada"
            ;;
        2)
            echo "📦 Creando backup del hook existente..."
            cp .git/hooks/pre-commit .git/hooks/pre-commit.backup.$(date +%Y%m%d_%H%M%S)
            echo "✓ Backup creado"
            ;;
        3)
            echo "❌ Instalación cancelada"
            exit 0
            ;;
        *)
            echo "❌ Opción inválida"
            exit 1
            ;;
    esac
fi

# El script pre-commit-cursorrules.sh ya fue creado por el agente coding assistant.
# Solo nos aseguramos de que tenga permisos de ejecución.
chmod +x scripts/pre-commit-cursorrules.sh
echo "✓ Script de verificación configurado"

# Crear o actualizar el pre-commit hook principal si no existe o si se eligió reemplazar (opción 2)
if [ ! -f ".git/hooks/pre-commit" ] || [ "$REPLY" == "2" ]; then
    echo "🪝 Configurando pre-commit hook..."
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash
# Pre-commit hook - Ejecuta validaciones antes del commit

# AXIOM - Verificación de .cursorrules
if [ -f "./scripts/pre-commit-cursorrules.sh" ]; then
    ./scripts/pre-commit-cursorrules.sh || exit 1
fi

# Aquí puedes agregar otras validaciones pre-commit
# Por ejemplo, ejecutar AXIOM validation:
# ./axiom.sh --dry-run || exit 1

exit 0
EOF
    chmod +x .git/hooks/pre-commit
    echo "✓ Pre-commit hook configurado"
fi

echo ""
echo "✅ Instalación completada"
echo ""
echo "📋 El hook verificará automáticamente:"
echo "  - Sincronización entre axiom.yaml e .cursorrules"
echo "  - Ofrecerá actualizar .cursorrules automáticamente"
echo "  - Bloqueará commits si hay desincronización"
echo ""
