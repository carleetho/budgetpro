# Solución para Ejecutar Tests con Docker

## Problema Identificado

Los tests de integración SÍ se están ejecutando, pero fallan porque Testcontainers no puede acceder al socket de Docker:

```
Could not find a valid Docker environment
DOCKER_HOST unix:///var/run/docker.sock is not listening
```

## Diagnóstico

1. ✅ El usuario `wazoox` **SÍ está en el grupo docker** a nivel de sistema (`getent group docker`)
2. ❌ La sesión actual del shell **NO tiene el grupo docker cargado** (`id -Gn` no muestra docker)
3. ✅ El socket de Docker tiene permisos correctos: `srw-rw---- 1 root docker`

## Solución Permanente (Recomendada)

**Cierra y vuelve a abrir tu terminal** (o cierra sesión y vuelve a iniciar sesión).

Después de hacerlo, el grupo docker estará cargado automáticamente y podrás ejecutar:

```bash
mvn test
docker run hello-world
```

## Solución Temporal (Mientras tanto)

### Opción 1: Usar `newgrp` (crea nuevo shell)

```bash
newgrp docker
mvn test
```

**Nota:** `newgrp` crea un nuevo shell, así que tendrás que salir con `exit` después.

### Opción 2: Usar el script wrapper (requiere ajuste)

El script `mvn-docker.sh` está configurado pero requiere que el grupo esté cargado en la sesión actual.

### Opción 3: Cambiar permisos del socket (temporal, requiere sudo)

⚠️ **NO RECOMENDADO para producción**, solo para desarrollo:

```bash
sudo chmod 666 /var/run/docker.sock
mvn test
sudo chmod 660 /var/run/docker.sock  # Restaurar permisos originales
```

## Verificación

Para verificar que tienes los permisos correctos:

```bash
# Verificar grupo
groups | grep docker

# Verificar acceso directo a Docker
docker ps

# Si ambos funcionan, los tests deberían funcionar
mvn test
```

## Estado Actual

- ✅ Tests compilados correctamente
- ✅ Testcontainers configurado correctamente
- ✅ Script wrapper creado (`mvn-docker.sh`)
- ✅ Maven Surefire configurado con DOCKER_HOST
- ❌ **PROBLEMA IDENTIFICADO**: Los procesos hijos de Maven/Surefire (JVM) no heredan el grupo docker aunque usemos `sg docker`

## Análisis Técnico

El problema es que cuando Maven ejecuta los tests:
1. Maven crea un proceso hijo (Surefire/JVM) para ejecutar los tests
2. Este proceso hijo **NO hereda** el grupo docker del proceso padre, incluso cuando usamos `sg docker`
3. Testcontainers intenta acceder al socket `/var/run/docker.sock` pero falla por permisos
4. El error específico: `Could not find a valid Docker environment`

## Solución DEFINITIVA

**Debes cerrar y volver a abrir tu terminal** (o cerrar sesión y volver a iniciar).

Después de hacerlo:
1. El grupo docker estará cargado automáticamente en la sesión
2. Todos los procesos (incluyendo procesos hijos de Maven) tendrán acceso a Docker
3. Los tests funcionarán correctamente

## Verificación Post-Solución

Después de cerrar y volver a abrir tu terminal, verifica:

```bash
# 1. Verificar que el grupo docker está cargado
groups | grep docker

# 2. Verificar acceso directo a Docker
docker ps

# 3. Ejecutar los tests
mvn test

# 4. Verificar que los tests pasan
mvn test -Dtest=RecursoControllerIT
```

## Por Qué Las Soluciones Temporales No Funcionan

- `sg docker -c "mvn test"`: Funciona para Maven, pero los procesos hijos de Surefire no heredan el grupo
- `newgrp docker`: Crea un nuevo shell interactivo, pero los procesos hijos de Maven tampoco heredan el grupo correctamente
- Script wrapper: El mismo problema, los procesos hijos no heredan permisos

La única solución real es que la sesión del shell tenga el grupo docker cargado desde el inicio, lo cual solo se logra cerrando y volviendo a abrir la terminal.
