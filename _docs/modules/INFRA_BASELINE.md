# 🏗️ INFRAESTRUCTURA - BASELINE TÉCNICO

> **Meta-Info:**
> - **Fecha de Generación:** 2026-01-10
> - **Fuente:** `pom.xml`, `compose.yaml`, `application.yml`, `application-test.yml`
> - **Estado:** Baseline Reconstruido (Forensic Analysis)
> - **Última Actualización:** 2026-01-10

---

## 🎯 PROPÓSITO

Este documento es el **Baseline Técnico de Infraestructura** del sistema BUDGETPRO, reconstruido mediante ingeniería inversa de los archivos de configuración existentes. Refleja el stack tecnológico real, versiones, puertos y variables de entorno tal como están configurados.

---

## 📦 STACK TECNOLÓGICO

### Runtime & Framework

| Componente | Versión | Fuente | Notas |
|------------|---------|--------|-------|
| **Java** | `17` | `pom.xml` (`<java.version>17</java.version>`) | JDK requerido |
| **Spring Boot** | `3.2.0` | `pom.xml` (parent `spring-boot-starter-parent`) | Framework principal |
| **Maven** | `4.0.0` | `pom.xml` (`<modelVersion>`) | Build tool |

### Dependencias Principales

| Dependencia | Versión | Grupo/Artifact | Scope | Uso |
|-------------|---------|----------------|-------|-----|
| **Spring Web** | `3.2.0` | `spring-boot-starter-web` | compile | REST API |
| **Spring Data JPA** | `3.2.0` | `spring-boot-starter-data-jpa` | compile | Persistencia |
| **Spring Validation** | `3.2.0` | `spring-boot-starter-validation` | compile | Validaciones Jakarta |
| **PostgreSQL Driver** | (heredado) | `postgresql` | runtime | Driver BD |
| **Flyway Core** | (heredado) | `flyway-core` | compile | Migraciones BD |
| **Flyway PostgreSQL** | `10.0.1` | `flyway-database-postgresql` | compile | Soporte PostgreSQL |
| **MapStruct** | `1.5.5.Final` | `mapstruct` | compile | Mapeo DTO ↔ Entity |
| **Lombok** | `1.18.30` | `lombok` | provided | Reducción de boilerplate |
| **Jakarta Validation** | (heredado) | `jakarta.validation-api` | compile | API de validación |
| **Spring Test** | `3.2.0` | `spring-boot-starter-test` | test | Testing |
| **Spring Testcontainers** | `3.2.0` | `spring-boot-testcontainers` | test | Integración con Testcontainers |
| **Testcontainers PostgreSQL** | `1.19.3` | `testcontainers/postgresql` | test | Tests de integración |
| **Testcontainers JUnit** | `1.19.3` | `testcontainers/junit-jupiter` | test | Tests con JUnit 5 |

**Nota:** Las versiones heredadas vienen del parent POM de Spring Boot 3.2.0.

---

## 🗄️ BASE DE DATOS

### PostgreSQL

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Versión** | `16-alpine` | `compose.yaml` (imagen) | Docker image |
| **Puerto** | `5432` | `compose.yaml` | Exposición host:container |
| **Base de Datos** | `budgetpro` | `compose.yaml` (env `POSTGRES_DB`) | Nombre de BD |
| **Usuario** | `postgres` | `compose.yaml` (env `POSTGRES_USER`) | Usuario de BD |
| **Contraseña** | `postgres` | `compose.yaml` (env `POSTGRES_PASSWORD`) | Contraseña de BD |
| **Volumen** | `./.docker/postgres-data:/var/lib/postgresql/data` | `compose.yaml` | Persistencia local |
| **Network** | `budgetpro-network` (bridge) | `compose.yaml` | Red Docker |

### Hibernate/JPA

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Dialect** | `org.hibernate.dialect.PostgreSQLDialect` | `application.yml` | Dialecto SQL |
| **DDL Auto** | `validate` | `application.yml` | NO genera esquema (Flyway lo hace) |
| **Show SQL** | `false` (prod), `true` (test) | `application.yml`, `application-test.yml` | Debug SQL |
| **Format SQL** | `true` | `application.yml` | Formato legible |
| **Use SQL Comments** | `true` (test) | `application-test.yml` | Comentarios en SQL |

**Nota:** Hibernate NO crea el esquema (`ddl-auto: validate`). Flyway se encarga de las migraciones.

---

## 🚀 MIGRACIONES (Flyway)

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Habilitado** | `true` | `application.yml` | Flyway activo |
| **Ubicación** | `classpath:db/migration` | `application.yml` | Carpeta de migraciones |
| **Baseline On Migrate** | `true` | `application.yml` | Crea baseline si BD no tiene historial |
| **Versión Actual** | `V2` | Migraciones detectadas | Última migración: `V2__create_billetera_and_movimiento_caja.sql` |

**Migraciones Detectadas:**
1. `V1__create_recurso_types_and_table.sql` - Enum types + tabla `recurso`
2. `V2__create_billetera_and_movimiento_caja.sql` - Tablas `proyecto`, `billetera`, `movimiento_caja`

---

## 🌐 SERVICIO WEB

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Puerto** | `8080` | `application.yml` (`server.port`) | Puerto HTTP |
| **Application Name** | `budgetpro-backend` | `application.yml` | Nombre de aplicación |

---

## 📊 LOGGING

### Configuración de Logs

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Nivel Root** | `INFO` | `application.yml` | Nivel global |
| **Nivel App** | `DEBUG` | `application.yml` (`com.budgetpro`) | Nivel para paquete de aplicación |
| **Nivel SQL (test)** | `DEBUG` | `application-test.yml` (`org.hibernate.SQL`) | SQL en tests |
| **Nivel Binder (test)** | `TRACE` | `application-test.yml` (`org.hibernate.type.descriptor.sql.BasicBinder`) | Valores de parámetros |
| **Patrón Console** | `"%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{correlationId}] - %msg%n"` | `application.yml` | Incluye correlationId del MDC |
| **Patrón File** | Mismo que console | `application.yml` | Mismo patrón para archivo |

**CorrelationId:**
- Se captura del MDC con clave `correlationId`
- Formato en logs: `[correlationId]`
- Implementado mediante `CorrelationIdFilter` (infraestructura de observabilidad)

---

## 🐳 DOCKER COMPOSE

### Servicio: `budgetpro-db`

| Parámetro | Valor | Fuente |
|-----------|-------|--------|
| **Imagen** | `postgres:16-alpine` | `compose.yaml` |
| **Container Name** | `budgetpro-db` | `compose.yaml` |
| **Puertos** | `5432:5432` | `compose.yaml` |
| **Variables de Entorno** | `POSTGRES_DB=budgetpro`, `POSTGRES_USER=postgres`, `POSTGRES_PASSWORD=postgres` | `compose.yaml` |
| **Volumen** | `./.docker/postgres-data:/var/lib/postgresql/data` | `compose.yaml` |
| **Healthcheck** | `pg_isready -U postgres` (intervalo 10s, timeout 5s, 5 retries) | `compose.yaml` |
| **Network** | `budgetpro-network` (bridge) | `compose.yaml` |

---

## 🧪 TESTING

### Testcontainers

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Habilitado** | `true` | Dependencias Maven | Spring Boot Testcontainers |
| **Contenedor** | `PostgreSQLContainer` | `AbstractIntegrationTest.java` | Contenedor estático |
| **Service Connection** | `@ServiceConnection` | `AbstractIntegrationTest.java` | Integración automática con Spring |
| **Profile** | `test` | `AbstractIntegrationTest.java` | Activa perfil `test` |

### Maven Surefire Plugin

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Versión** | `3.1.2` | `pom.xml` | Plugin de testing |
| **Variable de Entorno** | `DOCKER_HOST=unix:///var/run/docker.sock` | `pom.xml` | Permisos Docker para tests |

**Nota:** La variable `DOCKER_HOST` se configura para permitir que Maven se conecte a Docker durante los tests.

---

## 🛠️ BUILD & COMPILACIÓN

### Maven Compiler Plugin

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Versión** | `3.11.0` | `pom.xml` | Plugin de compilación |
| **Source** | `17` | `pom.xml` | Versión fuente Java |
| **Target** | `17` | `pom.xml` | Versión target Java |
| **Annotation Processors** | Lombok, MapStruct, Lombok-MapStruct-Binding | `pom.xml` | Procesadores de anotaciones |

### Spring Boot Maven Plugin

| Parámetro | Valor | Fuente | Notas |
|-----------|-------|--------|-------|
| **Excludes** | `lombok` | `pom.xml` | Excluye Lombok del ejecutable |

---

## 🔐 VARIABLES DE ENTORNO REQUERIDAS

### Desarrollo Local (Docker Compose)

| Variable | Valor | Fuente | Uso |
|----------|-------|--------|-----|
| `POSTGRES_DB` | `budgetpro` | `compose.yaml` | Nombre de BD |
| `POSTGRES_USER` | `postgres` | `compose.yaml` | Usuario de BD |
| `POSTGRES_PASSWORD` | `postgres` | `compose.yaml` | Contraseña de BD |

**Nota:** Estas variables se configuran automáticamente por Docker Compose. No se requieren variables de entorno explícitas en el host para desarrollo local.

### Tests (Testcontainers)

| Variable | Valor | Fuente | Uso |
|----------|-------|--------|-----|
| `DOCKER_HOST` | `unix:///var/run/docker.sock` | `maven-surefire-plugin` | Conexión a Docker para Testcontainers |

**Nota:** Esta variable se configura automáticamente por Maven Surefire Plugin.

---

## 📂 ESTRUCTURA DE DIRECTORIOS

### Configuración Detectada

```
src/
├── main/
│   ├── java/com/budgetpro/
│   │   ├── domain/          # Dominio (Pure Java)
│   │   ├── application/     # Casos de Uso (Spring Services)
│   │   └── infrastructure/  # Infraestructura (JPA, REST, etc.)
│   └── resources/
│       ├── db/migration/    # Migraciones Flyway
│       └── application.yml  # Configuración principal
└── test/
    ├── java/com/budgetpro/
    │   └── infrastructure/  # Tests de integración
    └── resources/
        └── application-test.yml  # Configuración de tests
```

---

## 🔍 OBSERVABILIDAD

### CorrelationIdFilter

**Ubicación:** `com.budgetpro.infrastructure.observability.CorrelationIdFilter`

**Funcionalidad:**
- Genera `X-Correlation-ID` si no viene en el header HTTP
- Establece `correlationId` en MDC (Mapped Diagnostic Context)
- Propaga `X-Correlation-ID` en header de respuesta
- Limpia MDC en `finally` para evitar memory leaks

**Orden:** `@Order(Ordered.HIGHEST_PRECEDENCE)` - Se ejecuta primero en el pipeline de filtros

**Integración:** Se incluye en los logs mediante `[%X{correlationId}]` en el patrón de logging.

---

## ⚠️ NOTAS CRÍTICAS

### 1. **Docker Permisos**

**Problema Conocido:** Los tests con Testcontainers requieren permisos de Docker.

**Solución Aplicada:**
- Variable `DOCKER_HOST` configurada en `maven-surefire-plugin`
- Usuario debe estar en grupo `docker` (configuración manual del sistema)

**Referencia:** Ver `SOLUCION_DOCKER_TESTS.md` (si existe) para más detalles.

---

### 2. **PostgreSQL Port Collision**

**Problema:** Puerto `5432` puede estar ocupado por otra instancia de PostgreSQL.

**Solución:** Docker Compose mapea `5432:5432`. Si hay conflicto, detener la instancia local antes de levantar el contenedor.

---

### 3. **Flyway Baseline**

**Configuración:** `baseline-on-migrate: true`

**Comportamiento:** Si la BD existe pero no tiene tabla `flyway_schema_history`, Flyway crea un baseline automáticamente en lugar de fallar.

---

### 4. **Hibernate vs Flyway**

**Estrategia:** Hibernate NO genera el esquema (`ddl-auto: validate`). Flyway es la única fuente de verdad para el esquema de BD.

**Beneficio:** Control total sobre el esquema mediante migraciones versionadas.

---

## ✅ VERIFICACIÓN DE CONFIGURACIÓN

**Última Verificación:** 2026-01-10

- ✅ Stack tecnológico documentado
- ✅ Versiones especificadas
- ✅ Puertos configurados
- ✅ Variables de entorno identificadas
- ✅ Configuración de logging documentada
- ✅ Docker Compose configurado
- ✅ Testcontainers configurado

---

**FIN DEL DOCUMENTO**
