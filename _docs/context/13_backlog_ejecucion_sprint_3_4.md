BACKLOG DE EJECUCIÓN TÉCNICA (SPRINT 3 & 4)

    🏷️ META-INFO

        📅 Fecha: 2026-01-11

        🧊 Estado: ACTIVO

        🎯 Objetivo: Frontend Reactivo & DevOps Básico

        🔗 Dependencia: ESTADO_PROYECTO.md (Fases 1, 2 y 3 Completas)

🟥 SPRINT 3: FRONTEND (READ MODEL)

Objetivo: Que el usuario pueda VER lo que el Backend ya procesa correctamente. Sin escribir datos aún.
ID	Tarea Técnica	Definición de Terminado (DoD)	Prioridad
F-01	Infraestructura Frontend	

- Proyecto React/Next.js inicializado.

- Configuración de Cliente HTTP (Axios/TanStack Query) con interceptor para JWT.

- Estructura de carpetas Hexagonal en Frontend (UI / Infra / Domain).
	🔴 BLOQUEANTE
F-02	Store & Auth UI	

- Contexto de Autenticación (Login/Logout).

- Almacenamiento seguro del Token.

- Redirección automática si token expira (401).
	🔴 BLOQUEANTE
F-03	UI: Dashboard Saldos	

- Consumo de GET /api/v1/proyectos/{id}/saldo.

- Visualización de saldo formateado (Moneda).

- Manejo de estados de carga (Loading/Error).
	🟡 ALTA
F-04	UI: Árbol de Partidas	

- Consumo de GET /api/v1/presupuestos/{id}/partidas.

- Renderizado recursivo del WBS (Padre e Hijos).

- Visualización de "Costo Meta" vs "Ejecutado".
	🟡 ALTA
🟨 SPRINT 4: FRONTEND (WRITE MODEL) & UX DEFENSIVA

Objetivo: Permitir operaciones financieras seguras desde la UI.
ID	Tarea Técnica	Definición de Terminado (DoD)	Prioridad
F-05	Formulario Compra Directa	

- Pantalla de ingreso de compra.

- Selección de Recursos (Dropdown/Search).

- Validación de cliente (montos positivos, campos obligatorios).
	🔴 BLOQUEANTE
F-06	UX Defensiva (Transaction Lock)	

- Botón "Confirmar" se deshabilita al enviar.

- Spinner de carga bloqueante.

- Feedback visual claro (Éxito/Error) basado en respuesta del Backend.
	🔴 BLOQUEANTE
F-07	Integración End-to-End	

- Conexión real: Formulario -> API POST /compras.

- Refresco automático de Saldo tras compra exitosa (Invalidación de caché).
	🔴 BLOQUEANTE
F-08	Manejo de Errores Financieros	

- Captura de errores 409 (Saldo Insuficiente / Stock Insuficiente).

- Mostrar mensaje de negocio amigable (no "Error 500").
	🟡 ALTA
🟩 SPRINT 5: DEVOPS & PROD-READINESS

Objetivo: Sacar el sistema de "localhost" de forma segura.
ID	Tarea Técnica	Definición de Terminado (DoD)	Prioridad
OPS-01	Dockerización	

- Dockerfile optimizado (Multi-stage build) para Backend.

- Dockerfile para Frontend (Nginx/Node).

- docker-compose.prod.yml final.
	🔴 BLOQUEANTE
OPS-02	CI Pipeline (GitHub Actions)	

- Workflow que ejecuta: mvn test + mvn verify en cada Push.

- Bloqueo de Merge si los tests fallan.
	🟡 ALTA
OPS-03	Scripts de Operación	- Scripts .sh para: Backup de DB, Restauración y Rollback de versión.	🟡 ALTA
🧪 PROTOCOLO DE VALIDACIÓN (REGLA DE ORO)

Para marcar CUALQUIER ticket de arriba como "DONE", Cursor debe ejecutar la siguiente rutina:

    IMPLEMENTAR: Código funcional.

    TESTEAR:

        Backend: Test de Integración nuevo o actualizado.

        Frontend: Test de Componente (Jest/Vitest) que verifique renderizado y llamadas a API (Mock).

    AUDITAR: Verificar que no rompe reglas de Seguridad (ej. no exponer tokens en localStorage inseguro).

    LOGUEAR: Entrada en _logs/dev_journal.md.


    Dile a Cursor:

    "Analiza el nuevo backlog 13_backlog_ejecucion_sprint_3_4.md. Actúa como Frontend Lead. Inicia la tarea F-01 (Infraestructura Frontend). Inicializa el proyecto en una carpeta frontend/ dentro de la raíz, usando Vite + React + TypeScript. Configura la arquitectura hexagonal de carpetas para el cliente."