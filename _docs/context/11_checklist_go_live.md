# CHECKLIST DE VALIDACIÓN PRE-DESPLIEGUE (GO-LIVE) para BUDGETPRO

```markdown
# 🚀 Checklist de Validación Pre-Despliegue (GO-LIVE) — **BUDGETPRO**

> **🏷️ META-INFO**
> * **📅 Fecha:** 2026-01-09
> * **🧊 Estado:** CONGELADO (Auditado)
> * **👮 Auditoría:** SRE / UX / Arquitectura — **APROBADO (GO-LIVE AUTHORIZED)**
> * **🏷️ Versión:** v2.0 — *Golden Source of Truth*
> * **📂 Clasificación:** 04. Experiencia & Riesgo
> * **📎 Archivo Origen:** Checklist SRE v2.0 — Auditoría de Resiliencia en Campo

---

## 🎯 Propósito del Checklist

Este checklist **NO es opcional**.
Es el **protocolo mínimo de supervivencia** para desplegar BUDGETPRO en entornos de obra:

- Conectividad inestable
- Dispositivos móviles expuestos
- Usuarios no técnicos
- Riesgo financiero real

👉 **Todas las casillas deben marcarse como “SÍ” antes de producción.**

---

## 1️⃣ Infraestructura & Datos (Resiliencia Técnica)

| # | Validación | Sí / No |
|---|-----------|---------|
| 1 | ¿Existe **backup automático horario** de la base de datos? | ⬜ |
| 2 | ¿Existe **backup diario externo** (off-site)? | ⬜ |
| 3 | ¿Se probó **restauración real** desde backup (no solo configuración)? | ⬜ |
| 4 | ¿Existen **scripts de rollback** de migraciones SQL en producción? | ⬜ |
| 5 | ¿Las migraciones están versionadas y bloqueadas contra ejecución doble? | ⬜ |
| 6 | ¿Todos los **índices críticos auditados** están aplicados en PROD? | ⬜ |
| 7 | ¿Las constraints financieras (`saldo >= 0`) están activas en BD? | ⬜ |
| 8 | ¿La BD está protegida contra conexiones externas no autorizadas? | ⬜ |

⚠️ **ALERTA:**
> Sin restore probado, **NO existe backup**, solo esperanza.

---

## 2️⃣ Continuidad Operativa (Entorno de Obra / Offline)

| # | Validación | Sí / No |
|---|-----------|---------|
| 9 | ¿La UI bloquea acciones durante transacciones (UI LOCK)? | ⬜ |
| 10 | ¿Las operaciones críticas son **idempotentes** ante reintentos? | ⬜ |
| 11 | ¿El frontend maneja correctamente **timeouts y pérdida de red**? | ⬜ |
| 12 | ¿Los errores de concurrencia muestran mensajes comprensibles al usuario? | ⬜ |
| 13 | ¿No existe ningún “Undo” que haga DELETE de datos financieros? | ⬜ |
| 14 | ¿Las correcciones se hacen solo vía **transacciones compensatorias**? | ⬜ |
| 15 | ¿Los formularios no permiten envío doble por latencia o doble tap? | ⬜ |

ℹ️ **Nota:**
> En obra, el doble clic **NO es un bug de usuario**, es una realidad operativa.

---

## 3️⃣ Soporte de Primer Nivel (Operación Humana)

| # | Validación | Sí / No |
|---|-----------|---------|
| 16 | ¿Cada transacción genera un **Correlation ID** visible en logs? | ⬜ |
| 17 | ¿Los logs son accesibles **sin llamar a un desarrollador**? | ⬜ |
| 18 | ¿Existe documentación básica para soporte (errores comunes)? | ⬜ |
| 19 | ¿Los errores financieros se distinguen de errores técnicos? | ⬜ |
| 20 | ¿Se puede rastrear “quién, cuándo y desde qué proyecto” una acción? | ⬜ |

🛑 **RIESGO:**
> Sin trazabilidad, cada incidente termina en discusiones sin evidencia.

---

## 4️⃣ Observabilidad & Seguridad Ofensiva (Bloqueante)

> 🔥 **Sección añadida tras Auditoría SRE — BLOQUEANTE PARA GO-LIVE**

| # | Validación | Sí / No |
|---|-----------|---------|
| 21 | ¿Existen **alertas automáticas** por errores 5xx / latencia? | ⬜ |
| 22 | ¿El equipo recibe alertas **sin intervención humana** (email/Slack)? | ⬜ |
| 23 | ¿Existe **monitor sintético** (healthcheck real de Compra Directa)? | ⬜ |
| 24 | ¿El sistema fuerza **HTTPS + TLS 1.2+ + HSTS**? | ⬜ |
| 25 | ¿Existe **kill-switch de sesión** para revocar accesos robados? | ⬜ |
| 26 | ¿Existe **Feature Flag** para apagar Compra Directa sin redeploy? | ⬜ |
| 27 | ¿El backend rechaza tokens inválidos o expirados inmediatamente? | ⬜ |

🛑 **NO-GO ABSOLUTO:**
> Si no hay alertas, el sistema puede morir en silencio mientras la obra se detiene.

---

## 🧾 Declaración de Go-Live

> **DECLARACIÓN FORMAL**

Al marcar **TODOS** los puntos como **SÍ**, el equipo declara que:

- El sistema es **operable en entornos hostiles**
- El riesgo técnico está controlado
- El riesgo residual es **administrativo**, no tecnológico

---

## 🏁 Estado Final del Artefacto

🟢 **APROBADO — GOLDEN SOURCE OF TRUTH**
📌 Cualquier despliegue que ignore este checklist se considera **NO AUTORIZADO**.

---

### 📎 Próximo Paso Natural
- Vincular este checklist al **Runbook de Producción**
- Usarlo como **gate obligatorio** en cada release

```