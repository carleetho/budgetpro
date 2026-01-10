# Definición de MVP v1.0

```markdown

> **🏷️ META-INFO**
> * **📅 Fecha:** 09/01/2026
> * **🧊 Estado:** CONGELADO (Auditado)
> * **📂 Clasificación:** Dominio & Negocio
> * **📎 Archivo Origen:** BUDGETPRO — MVP REAL v1.0 (Auditado)

# 🎯 BUDGETPRO — MVP REAL v1.0

## Resumen Ejecutivo

El **MVP REAL v1.0 de BUDGETPRO** define el alcance mínimo, viable y **operativamente crítico** para ingenieros independientes y MIPES del sector construcción que necesitan **control técnico-financiero real** sin recurrir a Excel, WhatsApp y carpetas dispersas.

Este documento es la **fuente de verdad** sobre:
- Qué **sí** entra al MVP.
- Qué **queda explícitamente fuera**.
- Qué reglas son **bloqueantes** para evitar pérdida de dinero y errores operativos.
- Qué KPIs son **obligatorios** para considerar el MVP exitoso.

---

## 🎯 Objetivo del MVP

Permitir que un **ingeniero independiente o MIPE de construcción** pueda:

- Presupuestar con **APU real instanciado**
- Controlar **costos reales** (material + mano de obra)
- Mantener **trazabilidad por proyecto y partida**
- Gestionar **caja real por proyecto**
- **Cobrar con respaldo documental** (evidencia + aprobación)
- Evitar volver a Excel, WhatsApp y carpetas sueltas

**Sin exigir disciplina artificial al cliente.**

---

## ✅ Criterios de Inclusión (Congelados)

Un componente entra al MVP **solo si cumple al menos uno**:

| Criterio | Justificación |
|--------|---------------|
| Evita pérdida de dinero | Control financiero real |
| Es bloqueante para cobrar | Protege flujo de caja |
| Captura realidad operativa | Refleja cómo se trabaja en obra |
| Sin él el usuario vuelve a Excel | MVP pierde sentido |

🛑 **Todo lo que no cumpla esto queda fuera del MVP.**

---

## 🧩 Contextos Incluidos en MVP v1.0 (CORE)

### 1️⃣ Oportunidades / Pre-Proyecto (Ligero)

**Incluido**
- Creación de proyecto desde:
  - Invitación
  - Cotización
- Cliente:
  - Persona Natural
  - Empresa
- Estados:
  - Oportunidad
  - En ejecución
  - Cerrado

**Excluido**
- CRM avanzado
- Embudos y probabilidades

ℹ️ **Propósito:**
> El proyecto existe desde el primer contacto, no desde el contrato.

---

### 2️⃣ Gestión de Proyectos (CORE)

**Incluido**
- Proyecto (Aggregate Root)
- ProyectoAdministrativo (System Default)
- Tipo de cliente
- Condiciones básicas:
  - Forma de pago
  - Anticipo
  - Retenciones
- Estado del proyecto

**Excluido**
- Cronogramas complejos tipo MS Project

---

### 3️⃣ Presupuestos & APUs (CORE ABSOLUTO)

**Incluido**
- Presupuesto versionable
- Línea Base Contractual (congelada)
- Partidas:
  - Código
  - Descripción
  - Unidad
  - Cantidad
  - Precio unitario
- APU instanciado:
  - Materiales
  - Mano de obra (rendimiento diario)
  - Subcontratos simples
- Precio de venta

**Excluido**
- Multi-moneda
- Depreciación contable avanzada

🛑 **Regla Clave:**
> El APU importado al proyecto es independiente de la base maestra.

---

### 4️⃣ Control de Ejecución (CORE)

**Incluido**
- Avance por partida
- Comparación:
  - Contratado vs ejecutado
- Órdenes de cambio simples
- Alertas básicas de desviación

**Excluido**
- Control diario hiper detallado
- Programación por recursos

---

### 5️⃣ Logística & Costos (CORE)

**Incluido**
- Compras
- DTE (carga manual)
- Bodega / Inventario central
- Transferencias:
  - Bodega → Proyecto
- **Compra Directa a Proyecto (Switch UX)**
  - Bodega (stock)
  - Proyecto Directo (gasto inmediato)
- Distinción:
  - Consumible
  - Activo (sin depreciación en MVP)

🛑 **Regla Crítica:**
> El costo nace en el consumo, no en la compra.

---

### 6️⃣ Fiscalidad – DTE (CORE LOCAL)

**Incluido**
- Bandeja de DTE
- Lectura de:
  - Totales
  - IVA
  - Retenciones
- Asociación obligatoria a proyecto

**Excluido**
- Integración directa con Ministerio de Hacienda
- Automatización fiscal completa

---

### 7️⃣ Mano de Obra (Simplificado MVP)

⚠️ **No RRHH completo**

**Incluido**
- Planilla Semanal:
  - Proyecto
  - Periodo
  - Monto total pagado
- Distribución de planilla:
  - Por partidas
  - Manual o prorrateada
- Impacto directo en:
  - Costos reales
  - Caja del proyecto

**Excluido**
- Empleados individuales
- ISSS / AFP / vacaciones
- Nómina formal

🛑 **Regla:**
> La planilla no es proveedor, pero sí es costo real.

---

### 8️⃣ Finanzas Operativas (CORE)

**Incluido**
- Billetera por proyecto
- Movimientos de caja
- Flujo de caja real
- Préstamos inter-proyecto
  - Con cuentas internas por cobrar / pagar

🛑 **Regla:**
> El dinero puede moverse, la deuda no se oculta.

**Excluido**
- Conciliación bancaria automática
- Proyecciones complejas

---

### 🔟 Comunicación, Evidencia & Aprobaciones (CORE)

**Incluido**
- Evidencia de avance:
  - Foto
  - PDF
  - Acta
- Estimación de pago (valuación)
- Aprobación de cliente:
  - Directa (link sin login)
  - Delegada (WhatsApp, PDF, foto)

🛑 **Reglas Bloqueantes**
1. No hay cobro sin evidencia.
2. El cliente **no es usuario del sistema**.
3. WhatsApp y PDF son válidos si se documentan.

---

## 🚫 Contextos Explícitamente Fuera del MVP

- RRHH avanzado
- Depreciación contable real
- Contabilidad formal
- BI / IA
- Multi-empresa SaaS
- Portal cliente completo

---

## 📊 KPIs Mínimos Obligatorios (MVP)

El MVP **debe mostrar**:

| KPI | Motivo |
|---|---|
| Avance físico vs financiero | Control de obra |
| Costo real vs presupuesto por partida | Desviaciones |
| Caja disponible por proyecto | Liquidez |
| Margen bruto proyectado vs real | Rentabilidad |
| Proyectos en riesgo (semáforo) | Decisión temprana |

🛑 **Sin estos KPIs, el MVP falla.**

---

## 👤 Perfil de Usuario Objetivo

- Ingeniero independiente
- MIPE (1–5 proyectos activos)
- Usa Excel hoy
- No quiere aprender un ERP
- Necesita control, no burocracia

---

## 🧾 Definición Final del MVP

> **BUDGETPRO v1.0** es un sistema de control técnico-financiero para proyectos de ingeniería civil que permite presupuestar con APU, registrar costos reales de materiales y mano de obra, gestionar caja por proyecto y cobrar con respaldo documental, manteniendo trazabilidad completa sin exigir fricción al cliente.

---

## 📜 Registro de Auditoría y Cambios

✔️ Dominio cerrado
✔️ MVP completo y usable
✔️ Trazabilidad matemática validada
✔️ UX alineada a la realidad operativa
✔️ Listo para arquitectura e implementación

---

**🧊 DOCUMENTO CONGELADO — GOLDEN SOURCE OF TRUTH**

```