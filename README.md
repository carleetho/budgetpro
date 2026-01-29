# 🏗️ BUDGETPRO - Sistema de Control Técnico-Financiero

Sistema completo de gestión de presupuestos y control de costos para proyectos de ingeniería civil.

## 📁 Estructura del Proyecto

```
budgetpro-backend/
├── backend/              # Backend Spring Boot
│   ├── src/             # Código fuente Java
│   ├── pom.xml          # Configuración Maven
│   └── compose.yaml     # Docker Compose para PostgreSQL
├── frontend/            # Frontend (por implementar)
│   └── README.md        # Documentación del frontend
├── docs/                # Documentación del proyecto
│   ├── ARQUITECTURA_VISUAL.md    # Diagramas y endpoints
│   ├── AUDITORIA_BACKEND.md      # Auditoría de código
│   └── audits/          # Reportes de implementación
└── README.md            # Este archivo
```

## 🚀 Inicio Rápido

### Backend

```bash
# Navegar al directorio backend
cd backend

# Levantar PostgreSQL con Docker
docker compose up -d

# Compilar y ejecutar
./mvnw spring-boot:run

# O construir JAR
./mvnw clean package
java -jar target/budgetpro-backend-1.0.0-SNAPSHOT.jar
```

El backend estará disponible en: `http://localhost:8080`

### Frontend

```bash
# Navegar al directorio frontend
cd frontend

# Instalar dependencias (cuando se configure)
npm install

# Iniciar servidor de desarrollo
npm run dev
```

## 📚 Documentación

- **Arquitectura Visual:** `docs/ARQUITECTURA_VISUAL.md` - Diagramas Mermaid, endpoints REST, diagramas ER
- **Auditoría Backend:** `docs/AUDITORIA_BACKEND.md` - Análisis de código, cobertura de tests
- **Reportes de Implementación:** `docs/audits/` - Reportes detallados de cada movimiento

## 🛠️ Tecnologías

### Backend

- Java 17
- Spring Boot 3.x
- PostgreSQL
- Flyway (migraciones)
- Maven

### Frontend

- (Por definir)

## 📖 API REST

Base URL: `http://localhost:8080/api/v1`

Ver documentación completa de endpoints en: `docs/ARQUITECTURA_VISUAL.md`

## 🧪 Testing

```bash
cd backend
./mvnw test
```

## 🛡️ AXIOM - Validación de Código

El proyecto utiliza **AXIOM** para garantizar la calidad del código y prevenir anti-patrones de IA (Lazy Code).

### Pre-commit Hook

Para instalar el hook de validación local:

```bash
./tools/axiom/install_hook.sh
```

### CI/CD Integration

AXIOM se ejecuta automáticamente en cada PR y Push hacia `main` y `develop` mediante GitHub Actions. El build fallará si se detectan:

- Métodos vacíos.
- Retornos `null` en capas de persistencia.
- `TODO/FIXME` en lógica de dominio crítica.

## 📝 Licencia

(Definir según corresponda)
