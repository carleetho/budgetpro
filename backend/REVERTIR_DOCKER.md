# ⚠️ REVERTIR Configuración de Docker

## Problema

La configuración `api-version` en `/etc/docker/daemon.json` no es válida y está causando que Docker falle.

## Solución Inmediata: Revertir Cambios

Ejecuta estos comandos para restaurar Docker:

```bash
# 1. Eliminar o restaurar daemon.json
sudo rm /etc/docker/daemon.json

# O si existe un backup, restaurarlo:
# sudo cp /etc/docker/daemon.json.backup.* /etc/docker/daemon.json

# 2. Reiniciar Docker
sudo systemctl restart docker

# 3. Verificar que funciona
docker ps
```

## Solución Alternativa: Usar Docker Compose

Como Docker 29.1.4 no puede configurarse para aceptar API 1.32, usaremos Docker Compose:

```bash
cd /home/wazoox/Desktop/budgetpro-backend/backend
docker-compose -f compose.yaml up -d budgetpro-db
```

Luego configurar los tests para usar esta base de datos en lugar de Testcontainers.
