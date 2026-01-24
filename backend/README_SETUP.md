# 🔧 Configuración del Entorno de Desarrollo

## ⚠️ Importante: Seguridad de Credenciales

**NUNCA** commitees archivos con credenciales reales. Este proyecto usa variables de entorno para proteger información sensible.

## 🚀 Configuración Inicial

### 1. Crear archivo de entorno local

```bash
cd backend
cp .env.example .env
```

### 2. Editar `.env` con tus credenciales

Edita el archivo `.env` y actualiza las siguientes variables:

```bash
# PostgreSQL Database Configuration
POSTGRES_DB=budgetpro
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu-password-seguro-aqui

# Database Connection (for Spring Boot application)
DB_URL=jdbc:postgresql://localhost:5432/budgetpro
DB_USERNAME=postgres
DB_PASSWORD=tu-password-seguro-aqui

# JWT Configuration
JWT_SECRET=tu-secret-jwt-minimo-32-caracteres
JWT_EXPIRATION_HOURS=24

# Resend API Key (for email notifications)
RESEND_API_KEY=tu-resend-api-key-aqui
```

### 3. Levantar base de datos con Docker Compose

```bash
# Docker Compose cargará automáticamente las variables de .env
docker compose --env-file .env up -d

# O si prefieres exportar las variables manualmente:
export POSTGRES_PASSWORD=tu-password-seguro
docker compose up -d
```

### 4. Verificar que la base de datos esté corriendo

```bash
docker compose ps
# Debe mostrar budgetpro-db como "Up"
```

### 5. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación cargará las variables de entorno desde:
- `backend/.env` (si existe)
- `database.env` (si existe en la raíz del proyecto)
- Variables de entorno del sistema

## 📋 Variables de Entorno Requeridas

### Para Docker Compose (`compose.yaml`)

| Variable | Descripción | Requerido | Default |
|----------|-------------|-----------|---------|
| `POSTGRES_DB` | Nombre de la base de datos | No | `budgetpro` |
| `POSTGRES_USER` | Usuario de PostgreSQL | No | `postgres` |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL | **Sí** | - |
| `DB_PORT` | Puerto de PostgreSQL | No | `5432` |

### Para Spring Boot (`application.yml`)

| Variable | Descripción | Requerido |
|----------|-------------|-----------|
| `DB_URL` | URL de conexión JDBC | **Sí** |
| `DB_USERNAME` | Usuario de la base de datos | **Sí** |
| `DB_PASSWORD` | Contraseña de la base de datos | **Sí** |
| `JWT_SECRET` | Secreto para firmar JWT (mínimo 32 caracteres) | **Sí** |
| `JWT_EXPIRATION_HOURS` | Horas de expiración del JWT | No (default: 24) |
| `RESEND_API_KEY` | API Key de Resend para emails | **Sí** |
| `ADMIN_EMAIL` | Email del administrador | No |
| `NOTIFICATION_SENDER` | Email remitente | No (default: onboarding@resend.dev) |

## 🔒 Verificación de Seguridad

### Antes de hacer commit:

```bash
# Verificar que .env no está en git
git status
# No debe aparecer .env en los archivos tracked

# Verificar que .env está en .gitignore
git check-ignore -v .env
# Debe mostrar que está ignorado
```

### Archivos que NO deben estar en Git:

- ❌ `backend/.env`
- ❌ `backend/.env.local`
- ❌ `database.env`
- ❌ `backend/RESEND_API_KEY.env`
- ❌ Cualquier archivo con credenciales reales

### Archivos que SÍ deben estar en Git:

- ✅ `backend/.env.example` (sin credenciales reales)
- ✅ `backend/compose.yaml.example` (sin credenciales reales)
- ✅ `backend/compose.yaml` (usa variables de entorno)

## 🐛 Solución de Problemas

### Error: "POSTGRES_PASSWORD is not set"

```bash
# Asegúrate de tener un archivo .env con POSTGRES_PASSWORD
cat backend/.env | grep POSTGRES_PASSWORD

# O exporta la variable manualmente
export POSTGRES_PASSWORD=tu-password
docker compose up -d
```

### Error: "Connection refused" al conectar a la base de datos

```bash
# Verificar que el contenedor está corriendo
docker compose ps

# Ver logs del contenedor
docker compose logs budgetpro-db

# Reiniciar el contenedor
docker compose restart budgetpro-db
```

### Error: "Variable DB_PASSWORD not found"

```bash
# Verificar que .env existe y tiene DB_PASSWORD
cat backend/.env | grep DB_PASSWORD

# O crear database.env en la raíz del proyecto
echo "DB_PASSWORD=tu-password" >> database.env
```

## 📚 Referencias

- [Docker Compose: Environment variables](https://docs.docker.com/compose/environment-variables/)
- [Spring Boot: Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- Ver `SECURITY_FIX.md` en la raíz del proyecto para más información sobre seguridad
