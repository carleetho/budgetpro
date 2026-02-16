# Solución para Problema de Docker en Tests de Integración

## Problema

Testcontainers está usando un cliente Docker con API versión 1.32, pero Docker 29.1.4 requiere API 1.44+.

```
ERROR: client version 1.32 is too old. Minimum supported API version is 1.44
```

## Solución Temporal: Configurar Docker para Aceptar API Antigua

**⚠️ ADVERTENCIA**: Esta es una solución temporal. Lo ideal es actualizar Testcontainers cuando haya una versión compatible.

### Opción 1: ⚠️ NO FUNCIONA - Docker 29.1.4 no soporta api-version en daemon.json

**Esta opción NO es válida** porque Docker 29.1.4 no permite configurar `api-version` en `daemon.json` de esa manera.

Si intentaste esta opción y Docker falló, revierte los cambios:

```bash
# Eliminar configuración inválida
sudo rm /etc/docker/daemon.json

# Reiniciar Docker
sudo systemctl restart docker

# Verificar
docker ps
```

### Opción 1: Usar Docker Compose para tests (RECOMENDADO)

En lugar de Testcontainers, usar un contenedor Docker Compose:

```bash
cd backend
docker-compose -f compose.yaml up -d budgetpro-db
```

Y configurar los tests para usar esta base de datos en lugar de Testcontainers.

### Opción 2: Ejecutar solo tests unitarios

Los tests unitarios no requieren Docker:

```bash
./mvnw test -Dtest="*Test" -DexcludeTests="*IntegrationTest,*E2ETest"
```

## Solución Permanente: Actualizar Testcontainers

Cuando Testcontainers lance una versión compatible con Docker 29.1.4, actualizar en `pom.xml`:

```xml
<testcontainers.version>1.21.0</testcontainers.version> <!-- o versión más reciente -->
```

## Estado Actual

- ✅ Tests unitarios: Funcionan correctamente (26 tests pasando)
- ⚠️ Tests de integración: Requieren Docker con API 1.32 o configuración alternativa
- ✅ Reporte de cobertura: Generado correctamente con JaCoCo

## Verificación

Para verificar la versión de API de Docker:

```bash
docker version --format '{{.Server.APIVersion}}'
```

Si muestra `1.52` o superior, necesitas usar una de las soluciones anteriores.
