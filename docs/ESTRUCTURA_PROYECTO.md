# 📁 ESTRUCTURA DEL PROYECTO BUDGETPRO

**Fecha:** 2026-01-13  
**Versión:** 1.0

---

## 🗂️ Estructura de Directorios

```
budgetpro-backend/
├── backend/                    # Backend Spring Boot
│   ├── src/                    # Código fuente Java
│   │   ├── main/
│   │   │   ├── java/           # Código fuente principal
│   │   │   └── resources/      # Configuración y migraciones
│   │   └── test/               # Tests
│   ├── pom.xml                 # Configuración Maven
│   ├── mvnw                    # Maven Wrapper
│   └── compose.yaml            # Docker Compose para PostgreSQL
│
├── frontend/                   # Frontend (por implementar)
│   ├── README.md               # Documentación del frontend
│   ├── package.json            # Configuración Node.js (placeholder)
│   └── .gitignore              # Archivos a ignorar en Git
│
├── docs/                       # Documentación del proyecto
│   ├── ARQUITECTURA_VISUAL.md  # Diagramas Mermaid y endpoints
│   ├── AUDITORIA_BACKEND.md    # Auditoría de código
│   ├── audits/                 # Reportes de implementación
│   └── context/                # Documentación de contexto
│
├── knowledge_base/             # Recursos de conocimiento
│   └── COSTO Y TIEMPO EN EDIFICACION - Suarez Salazar.pdf
│
├── README.md                   # Documentación principal
└── .gitignore                 # Configuración Git
```

---

## 🚀 Comandos Rápidos

### Backend

```bash
# Navegar al backend
cd backend

# Compilar
./mvnw clean compile

# Ejecutar tests
./mvnw test

# Ejecutar aplicación
./mvnw spring-boot:run

# Levantar PostgreSQL
docker compose up -d
```

### Frontend

```bash
# Navegar al frontend
cd frontend

# (Por implementar)
# npm install
# npm run dev
```

---

## 📋 Estado Actual

- ✅ **Backend:** Compilando correctamente (BUILD SUCCESS)
- ✅ **Estructura:** Organizada (backend/, frontend/, docs/)
- ⏳ **Frontend:** Listo para implementación
- ✅ **Documentación:** Completa y actualizada

---

## 🔗 Enlaces Útiles

- **Backend API:** `http://localhost:8080/api/v1`
- **Documentación Visual:** `docs/ARQUITECTURA_VISUAL.md`
- **Auditoría:** `docs/AUDITORIA_BACKEND.md`
