# BUDGETPRO Backend

Sistema de control técnico-financiero para proyectos de ingeniería civil.

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Levantar la Infraestructura

```bash
# Levantar PostgreSQL con Docker Compose
docker compose up -d

# Verificar que el contenedor está corriendo
docker compose ps

# Ver logs del contenedor
docker compose logs -f budgetpro-db
```

### Ejecutar la Aplicación

```bash
# Compilar y ejecutar
mvn spring-boot:run

# O construir y ejecutar el JAR
mvn clean package
java -jar target/budgetpro-backend-1.0.0-SNAPSHOT.jar
```

La aplicación estará disponible en: `http://localhost:8080`

### Configuración

La configuración de la base de datos está en `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/budgetpro
    username: postgres
    password: ${DB_PASSWORD}
```

### Migraciones de Base de Datos

Flyway ejecuta automáticamente las migraciones al iniciar la aplicación desde `src/main/resources/db/migration/`.

### Comandos Útiles

```bash
# Levantar infraestructura
docker compose up -d

# Detener infraestructura
docker compose down

# Ver logs
docker compose logs -f budgetpro-db

# Ejecutar tests
mvn test

# Limpiar y reconstruir
mvn clean install
```

## 📁 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/budgetpro/
│   │   ├── domain/          # Capa de Dominio (DDD)
│   │   ├── application/     # Capa de Aplicación (Use Cases)
│   │   └── infrastructure/  # Capa de Infraestructura (REST, JPA)
│   └── resources/
│       ├── application.yml  # Configuración principal
│       └── db/migration/    # Migraciones Flyway
└── test/
    └── java/                # Tests
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests de integración específicos
mvn test -Dtest=RecursoControllerIT

# Ejecutar con cobertura (si está configurado)
mvn test jacoco:report
```
