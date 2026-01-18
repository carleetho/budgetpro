# 🔴 RESET NUCLEAR DE BASE DE DATOS - INSTRUCCIONES

## 📋 ANÁLISIS DEL PROBLEMA

**Error:** `org.flywaydb.core.api.exception.FlywayValidateException: Migration checksum mismatch for migration version 2`

**Causa:** El archivo `compose.yaml` usa un **Bind Mount local** (no un Named Volume):
```yaml
volumes:
  - ./.docker/postgres-data:/var/lib/postgresql/data
```

**Problema:** `docker compose down -v` NO elimina bind mounts, solo named volumes. Los datos persisten en `.docker/postgres-data/`.

---

## 🚀 SOLUCIÓN: SCRIPT DE LIMPIEZA

### Opción 1: Script Automático (Recomendado)

```bash
cd backend
./reset-db.sh
```

### Opción 2: Comandos Manuales

```bash
cd backend

# 1. Detener contenedores
docker compose down -v

# 2. Eliminar directorio de datos (requiere sudo por permisos)
sudo rm -rf .docker/postgres-data

# 3. Crear directorio vacío
mkdir -p .docker/postgres-data
chmod 700 .docker/postgres-data

# 4. Levantar contenedor limpio
docker compose up -d budgetpro-db

# 5. Esperar a que PostgreSQL esté listo
sleep 5
```

---

## ✅ VERIFICACIÓN: BD VACÍA

Antes de ejecutar Spring Boot, verifica que la base de datos está vacía:

```bash
cd backend
docker compose exec budgetpro-db psql -U postgres -d budgetpro -c "\dt"
```

**Resultado esperado:** `No relations found.` (0 tablas)

---

## 🎯 OBJETIVO FINAL

Después del reset, al ejecutar:
```bash
./mvnw spring-boot:run
```

Flyway encontrará una base de datos vacía y recreará todas las tablas desde cero sin errores de checksum.

---

## 📝 NOTAS TÉCNICAS

- **Ubicación de datos:** `backend/.docker/postgres-data/`
- **Tipo de volumen:** Bind Mount (carpeta local)
- **Permisos:** El directorio tiene permisos restrictivos (`drwx------`) por seguridad de PostgreSQL
- **Por qué sudo:** PostgreSQL crea archivos con UID 70 (usuario `postgres` dentro del contenedor), por lo que puede requerir `sudo` para eliminarlos
