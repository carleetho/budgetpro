#!/bin/bash
# Script para revertir la configuración de Docker que está causando problemas

set -e

echo "🔧 Revirtiendo configuración de Docker..."

# Verificar si existe el archivo daemon.json
if [ -f /etc/docker/daemon.json ]; then
    echo "⚠️  Eliminando /etc/docker/daemon.json..."
    sudo rm /etc/docker/daemon.json
    echo "✅ Archivo eliminado"
else
    echo "ℹ️  /etc/docker/daemon.json no existe, no hay nada que revertir"
fi

# Reiniciar Docker
echo "🔄 Reiniciando Docker..."
sudo systemctl restart docker

# Esperar a que Docker se inicie
echo "⏳ Esperando a que Docker se inicie..."
sleep 3

# Verificar que Docker funciona
echo "✅ Verificando que Docker funciona..."
if docker ps > /dev/null 2>&1; then
    echo "✅ Docker está funcionando correctamente"
    docker ps
else
    echo "❌ Docker aún no está funcionando. Intenta ejecutar manualmente:"
    echo "   sudo systemctl status docker"
    exit 1
fi

echo ""
echo "✅ Reversión completada. Docker debería estar funcionando ahora."
