# 🔒 Corrección de Seguridad: Credenciales Expuestas

## 🚨 Problema Detectado

Se identificaron credenciales hardcodeadas en archivos commitados al repositorio:

1. **`backend/compose.yaml`**: Contiene credenciales de PostgreSQL (usuario y contraseña)
2. **`database.env`**: Aunque está en .gitignore, podría haber sido commitado previamente

## ✅ Solución Implementada

### 1. Actualización de `compose.yaml`
- ✅ Cambiado para usar variables de entorno
- ✅ Credenciales ya no están hardcodeadas
- ✅ Creado `compose.yaml.example` como referencia

### 2. Archivo `.env.example`
- ✅ Creado `backend/.env.example` con todas las variables necesarias
- ✅ Documenta todas las credenciales requeridas
- ✅ Incluye instrucciones claras

### 3. Actualización de `.gitignore`
- ✅ Verificado que `.env` y archivos relacionados estén ignorados
- ✅ Agregado patrón más específico para archivos de entorno
- ✅ Documentación mejorada

## 📋 Acciones Requeridas

### Para Desarrolladores

1. **Crear archivo `.env` local:**
   ```bash
   cd backend
   cp .env.example .env
   # Editar .env con tus credenciales locales
   ```

2. **Usar variables de entorno al ejecutar Docker Compose:**
   ```bash
   # Opción 1: Cargar desde .env automáticamente
   docker compose --env-file .env up -d
   
   # Opción 2: Exportar variables manualmente
   export POSTGRES_PASSWORD=tu-password-seguro
   docker compose up -d
   ```

3. **Verificar que `.env` no esté en git:**
   ```bash
   git status
   # No debe aparecer .env en los archivos tracked
   ```

### Para el Repositorio

1. **Rotar credenciales expuestas:**
   - Si las credenciales en `compose.yaml` fueron expuestas públicamente, deben rotarse
   - Cambiar contraseñas de bases de datos en todos los ambientes

2. **Revisar historial de Git:**
   ```bash
   # Verificar si database.env fue commitado previamente
   git log --all --full-history -- database.env
   git log --all --full-history -- backend/compose.yaml
   ```

3. **Si es necesario, limpiar historial:**
   - Considerar usar `git filter-branch` o `git filter-repo` si las credenciales fueron expuestas
   - **ADVERTENCIA**: Esto reescribe el historial de Git

## 🔍 Verificación de Seguridad

### Archivos que NO deben estar en Git:
- ✅ `backend/.env`
- ✅ `backend/.env.local`
- ✅ `database.env`
- ✅ `backend/RESEND_API_KEY.env`
- ✅ Cualquier archivo con credenciales

### Archivos que SÍ deben estar en Git:
- ✅ `backend/.env.example` (sin credenciales reales)
- ✅ `backend/compose.yaml.example` (sin credenciales reales)
- ✅ `backend/compose.yaml` (ahora usa variables de entorno)

## 📝 Variables de Entorno Requeridas

### Para Docker Compose:
- `POSTGRES_DB` (opcional, default: budgetpro)
- `POSTGRES_USER` (opcional, default: postgres)
- `POSTGRES_PASSWORD` (REQUERIDO)
- `DB_PORT` (opcional, default: 5432)

### Para Spring Boot:
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_HOURS`
- `RESEND_API_KEY`

## 🚨 Si las Credenciales Fueron Expuestas

1. **Inmediatamente:**
   - Rotar todas las contraseñas expuestas
   - Revisar logs de acceso a la base de datos
   - Verificar si hubo accesos no autorizados

2. **A corto plazo:**
   - Implementar monitoreo de accesos
   - Revisar permisos de base de datos
   - Considerar usar secretos gestionados (AWS Secrets Manager, HashiCorp Vault, etc.)

3. **A largo plazo:**
   - Implementar gestión de secretos centralizada
   - Usar variables de entorno en CI/CD
   - Implementar rotación automática de credenciales

## 📚 Referencias

- [OWASP: Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html)
- [GitHub: Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [Docker Compose: Environment variables](https://docs.docker.com/compose/environment-variables/)
