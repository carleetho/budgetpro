#!/bin/bash
# Wrapper script para ejecutar Maven con permisos de Docker
# Este script ejecuta Maven dentro del contexto del grupo docker
# para que Testcontainers pueda acceder a Docker

# Verificar si el usuario está en el grupo docker
if ! getent group docker | grep -q "\b$(whoami)\b"; then
    echo "ERROR: El usuario $(whoami) no está en el grupo docker."
    echo "Ejecuta: sudo usermod -aG docker $(whoami)"
    echo "Luego cierra y vuelve a abrir tu terminal."
    exit 1
fi

# Ejecutar Maven dentro del contexto del grupo docker
# Usamos exec para reemplazar el proceso actual
exec sg docker -c "mvn $@"
