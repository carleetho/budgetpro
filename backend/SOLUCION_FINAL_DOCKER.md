# 🔧 Solución Final para Problema de Docker

## ⚠️ PROBLEMA IDENTIFICADO

La configuración `api-version` en `/etc/docker/daemon.json` **NO ES VÁLIDA** y está causando que Docker falle.

Docker 29.1.4 **NO soporta** la opción `api-version` en `daemon.json` de esa manera.

## ✅ PASO 1: REVERTIR CONFIGURACIÓN (Ejecutar PRIMERO)

Ejecuta estos comandos para restaurar Docker:

```bash
# 1. Eliminar configuración inválida
sudo rm /etc/docker/daemon.json

# 2. Reiniciar Docker
sudo systemctl restart docker

# 3. Esperar unos segundos
sleep 3

# 4. Verificar que Docker funciona
docker ps
```

Si Docker funciona correctamente, continúa con el Paso 2.

## ✅ PASO 2: SOLUCIÓN ALTERNATIVA - Usar Docker Compose

Como Testcontainers no es compatible con Docker 29.1.4, usaremos Docker Compose:

### 2.1. Iniciar base de datos con Docker Compose

```bash
cd /home/wazoox/Desktop/budgetpro-backend/backend

# Iniciar PostgreSQL
docker-compose -f compose.yaml up -d budgetpro-db

# Verificar que está corriendo
docker ps | grep budgetpro-db
```

### 2.2. Configurar tests para usar Docker Compose

Los tests de integración pueden usar la base de datos de Docker Compose en lugar de Testcontainers.

## ✅ PASO 3: Ejecutar Tests

### Opción A: Solo tests unitarios (NO requieren Docker)

```bash
cd /home/wazoox/Desktop/budgetpro-backend/backend
./mvnw test -Dtest="ProveedorTest,OrdenCompraTest"
```

### Opción B: Tests de integración con Docker Compose

Primero asegúrate de que la base de datos esté corriendo:

```bash
docker-compose -f compose.yaml up -d budgetpro-db
```

Luego ejecuta los tests (pueden requerir configuración adicional).

## 📊 Estado Actual

- ✅ **Tests unitarios**: 26 tests pasando (sin Docker)
- ⚠️ **Tests de integración**: Requieren configuración alternativa
- ✅ **Reporte de cobertura**: Generado correctamente

## 🔄 Solución Permanente

Cuando Testcontainers lance una versión compatible con Docker 29.1.4, actualizar en `pom.xml`:

```xml
<testcontainers.version>1.21.0</testcontainers.version>
```
