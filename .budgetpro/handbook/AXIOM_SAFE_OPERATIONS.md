# AXIOM OPERATIONS HANDBOOK & GOVERNANCE STD

**Versión:** 1.0 | **Estado:** MANDATORIO | **Proyecto:** BudgetPro

## 1. Definición del Problema y Alcance

### El Problema

Los agentes de IA tienden a la "Amnesia Operativa" y al "Bypass Pragmático". En proyectos complejos como BudgetPro, esto resulta en:

- Violaciones de la arquitectura hexagonal.
- Fugas de secretos (Leaks).
- Desorden en el historial de Git (Commits masivos o "Big Bangs").
- Desincronización entre el sistema de archivos y el índice de Git.

### Objetivo y Uso

Este Handbook es la **Memoria Externa de Largo Plazo** del proyecto. Su propósito es:

1. Estandarizar la interacción IA-Humano.
2. Prevenir la regresión de errores técnicos y de gobernanza.
3. Servir de Oráculo: Ante cualquier duda sobre "cómo proceder", la IA debe consultar este manual.

---

## 2. Protocolo de Operación por Modos (MODES)

El asistente debe declarar el modo antes de actuar. Cada modo redefine los límites del _Blast Radius_.

### 🔴 MODE_0: CONTENCIÓN (Emergency)

- **Cuándo:** El proyecto no compila, hay >50 errores, o existen "archivos fantasma" bloqueando el flujo.
- **Acción ante Bloqueo:** Se permite el bypass de reglas estéticas (Naming) **SOLO SI** la corrección es puramente mecánica para restaurar la compilación.
- **Prohibición:** Prohibido añadir nuevas features o lógica de negocio.
- **Unidad de Trabajo:** Atómica por "Causa Raíz", sin importar el número de archivos (Bypass documentado).

### 🟡 MODE_1: ESTABILIZACIÓN (Harden)

- **Cuándo:** El código compila pero AXIOM reporta violaciones de arquitectura o "Dirty Code".
- **Acción ante Bloqueo:** Se deben realizar commits por Batches Temáticos. Si AXIOM bloquea por Blast Radius, el asistente **DEBE** dividir el trabajo.
- **Prohibición:** Prohibido tocar `com.budgetpro.domain` a menos que sea para corregir un Leak o Purity violation.

### 🟢 MODE_2: OPERACIÓN NORMAL (Evolution)

- **Cuándo:** AXIOM Sentinel está en Verde (0 violaciones).
- **Acción ante Bloqueo:** **BLOQUEO ABSOLUTO**. No se permiten bypasses. Cada cambio debe ser perfecto.
- **Regla:** 1 Propósito = 1 Commit.

---

## 3. Gestión de Alertas y Bloqueos

| Nivel          | Significado       | Acción Requerida del Asistente                                                                                            |
| :------------- | :---------------- | :------------------------------------------------------------------------------------------------------------------------ |
| **[BLOCKING]** | Violación Crítica | **STOP**. Leer la "Sugerencia". No intentar bypass. Corregir y re-validar.                                                |
| **[WARNING]**  | Riesgo Potencial  | **ANALIZAR**. Si el warning dice "LEAK" o "SECRET", tratar como BLOCKING. Si es "Null-Safety", corregir antes del commit. |
| **[INFO]**     | Recomendación     | Registrar para futura refactorización en MODE_1.                                                                          |

---

## 4. Estándar de Naming (Branches & Commits)

Para mantener la trazabilidad, el asistente debe seguir este patrón estrictamente:

### A. Branches (Ramas)

`tipo/sistema-descripcion-corta`

- `fix/axiom-recovery-estimacion` (Correcciones de emergencia)
- `feat/domain-presupuesto-v0` (Nuevas funcionalidades)
- `refactor/naming-validator-logs` (Limpieza de código)
- `chore/governance-sync` (Cambios de reglas o configs)

### B. Commits (Mensajes)

`tipo: descripción breve [MODE_X][RISK:LEVEL]`

Ejemplos:

- `fix: resolve null-safety in JavaFileScanner [MODE_1][RISK:LOW]`
- `chore: purge 532 phantom files from index [MODE_0][RISK:MID]`
- `feat: implement budget validation logic [MODE_2][RISK:HIGH]`

---

## 5. Casos de Uso Específicos (Precedentes)

### Caso: "Fantasmas en el Índice"

- **Acción:** No basta con `rm`. Se debe usar `git rm --cached <path>` seguido de un commit de purga.
- **Regla:** Añadir la ruta al `.gitignore` inmediatamente.

### Caso: "Conflicto de Reglas"

- **Acción:** Si existen `.cursorrules` y `.cursorrules.md`, el asistente debe borrar el primero y quedarse solo con el `.md`.
