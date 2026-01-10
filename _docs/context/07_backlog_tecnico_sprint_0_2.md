# BACKLOG TÉCNICO EJECUTABLE — Sprint 0–2

Perfecto. Con la **arquitectura blindada** y el **GO-LIVE autorizado**, corresponde ahora cerrar el último frente crítico: **el plan de ejecución**.

A continuación entrego el **BACKLOG TÉCNICO RECTIFICADO (FINAL)**, alineado con **todo lo validado** (Dominio, ERD, UX, SRE) y corrigiendo **explícitamente** los hallazgos del Engineering Manager.

---

# 🧭 BACKLOG TÉCNICO RECTIFICADO — BUDGETPRO

**Alcance:** MVP funcional — Caso de Uso *Compra Directa*

**Horizonte:** 4 semanas

**Equipo:** 2 desarrolladores (Full-Stack / Backend-heavy)

**Estado:** 🟢 **PLAN VIABLE / EJECUTABLE**

---

## 🧱 PRINCIPIOS DE REPLANIFICACIÓN (OBLIGATORIOS)

1. **Read antes de Write**
    
    Ninguna pantalla de escritura se implementa sin sus queries de lectura.
    
2. **Vertical Slice temprano**
    
    Cada sprint deja algo **ejecutable**, aunque sea feo.
    
3. **Infraestructura atomizada**
    
    Ningún ticket bloquea más de un agregado.
    
4. **Una sola fuente de verdad**
    
    Este backlog reemplaza cualquier versión anterior.
    

---

## 🟦 SPRINT 0 — FUNDACIÓN (Semana 0–1)

**Objetivo:** Que el equipo pueda trabajar sin fricción ni deuda invisible.

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S0-01 | Repo mono + estructura Hexagonal base | Chore | 🔴 Alta | — |
| S0-02 | Pipeline CI básico (build + test) | Chore | 🔴 Alta | S0-01 |
| S0-03 | Configuración DB local + migraciones | Chore | 🔴 Alta | S0-01 |
| S0-04 | Observabilidad mínima (logs + correlationId) | Chore | 🔴 Alta | S0-01 |
| S0-05 | Feature Flags base (Compra Directa OFF) | Chore | 🟠 Media | S0-01 |

✅ **Definición de terminado Sprint 0:**

El proyecto **compila**, **migra BD**, **loggea transacciones** y puede **desactivar Compra Directa** sin redeploy.

---

## 🟩 SPRINT 1 — READ MODEL + DOMINIO (Semana 1–2)

**Objetivo:** Desbloquear frontend y cerrar el Vertical Slice de lectura.

### 🔹 Dominio y Persistencia (sin UI aún)

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S1-01 | Agregado RECURSO (Dominio + VO) | Feat | 🔴 Alta | S0 |
| S1-02 | Repo Recurso (JPA + Adapter) | Feat | 🔴 Alta | S1-01 |
| S1-03 | Agregado BILLETERA (Dominio) | Feat | 🔴 Alta | S0 |
| S1-04 | Repo Billetera (JPA + Adapter) | Feat | 🔴 Alta | S1-03 |
| S1-05 | Agregado PARTIDA + Presupuesto | Feat | 🔴 Alta | S0 |
| S1-06 | Repo Partida / Presupuesto | Feat | 🔴 Alta | S1-05 |

### 🔹 Read Model (CRÍTICO – movido desde Sprint 2)

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S1-07 | Query: Saldo actual por proyecto | Feat | 🔴 Alta | S1-04 |
| S1-08 | Query: Partidas disponibles por proyecto | Feat | 🔴 Alta | S1-06 |
| S1-09 | API GET /saldo + /partidas | Feat | 🔴 Alta | S1-07, S1-08 |

🛑 **Este bloque corrige el ERROR 1 del backlog anterior.**

### 🔹 Vertical Slice temprano (UI mínima)

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S1-10 | UI básica: ver saldo y partidas (read-only) | Feat | 🟠 Media | S1-09 |

✅ **Definición de terminado Sprint 1:**

Un usuario puede **entrar al sistema y ver** saldo y partidas reales desde la BD.

---

## 🟨 SPRINT 2 — WRITE MODEL + UX DEFENSIVA (Semana 3–4)

**Objetivo:** Compra Directa funcionando end-to-end.

### 🔹 Dominio + Use Case (Vertical Slice “La Ley”)

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S2-01 | Agregado COMPRA (crear vs reconstruir) | Feat | 🔴 Alta | S1 |
| S2-02 | ProcesarCompraDirectaService (Dominio puro) | Feat | 🔴 Alta | S2-01 |
| S2-03 | UseCase RegistrarCompraDirecta | Feat | 🔴 Alta | S2-02 |
| S2-04 | Persistencia Compra + Detalle | Feat | 🔴 Alta | S2-01 |
| S2-05 | Transacción atómica (Compra + Billetera + Inventario) | Feat | 🔴 Alta | S2-03, S2-04 |

### 🔹 UX Defensiva (Write)

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S2-06 | UI Compra Directa (formulario real) | Feat | 🔴 Alta | S1-10 |
| S2-07 | UI Lock + estados Pendiente/Confirmado/Error | Feat | 🔴 Alta | S2-06 |
| S2-08 | Manejo de Sad Paths (saldo cambió, error red) | Feat | 🔴 Alta | S2-07 |

### 🔹 Operación y Seguridad

| ID | Título | Tipo | Prioridad | Dependencia |
| --- | --- | --- | --- | --- |
| S2-09 | Alertas SRE (5xx, latencia, caída) | Chore | 🔴 Alta | S0-04 |
| S2-10 | Kill-Switch de sesión (revocar tokens) | Feat | 🟠 Media | S0 |
| S2-11 | Activar Feature Flag Compra Directa | Chore | 🟢 Baja | S2 completo |

---

## 🎯 CRITERIOS DE ÉXITO DEL PLAN

✔️ Frontend **no se bloquea** esperando datos

✔️ Infraestructura **no es monolítica**

✔️ Compra Directa funciona **en entorno hostil**

✔️ Rollback lógico y técnico disponibles

✔️ Arquitectura respetada **en código real**

---

## 🏁 CONCLUSIÓN EJECUTIVA

Este backlog:

- Corrige **todos** los hallazgos del Engineering Manager
- Está alineado con la **Golden Source of Truth**
- Permite tener **Compra Directa en producción en 4 semanas**
- Reduce el riesgo técnico a **casi cero**

📌 **ESTADO DEL PLAN:**

🟢 **APROBADO — AUTORIZADO PARA EJECUCIÓN**

Cuando quieras, el siguiente paso natural es:

- 📄 convertir esto en **épicas y tickets Jira/Notion**, o
- 👥 asignar tareas día a día para un equipo real.