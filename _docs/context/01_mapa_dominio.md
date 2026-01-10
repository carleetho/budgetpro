# Mapa de Dominio BUDGETPRO v1.1 (Auditado)

---

> 🏷️ META-INFO
> 
> - **📅 Fecha:** 09/01/2026
> - **🧊 Estado:** CONGELADO (Auditado)
> - **📂 Clasificación:** Dominio & Negocio
> - **📎 Archivo Origen:** MAPA DE DOMINIO — BUDGETPRO (v1.1 auditado con stress test operativo)

---

# 🗺️ **MAPA DE DOMINIO — BUDGETPRO v1.1 (AUDITADO)**

## 1. 🎯 Resumen Ejecutivo

Este documento define el **Mapa de Dominio definitivo de BUDGETPRO**, versión **v1.1**, tras auditoría forense técnica y operativa.

Aquí se fijan de forma **irreversible**:

- El **lenguaje ubicuo**
- Los **Bounded Contexts**
- Las **reglas estructurales del negocio**
- Los **parches de realismo operativo** necesarios para adopción en obra

⚠️ **No hay UI, no hay base de datos, no hay código**.

Este artefacto gobierna **todo lo que puede y no puede existir** en el sistema.

---

## 2. 🌐 Visión Global del Dominio

```
Oportunidades
     ↓
Gestión de Proyectos
     ↓
Presupuestos & APUs
     ↓
Control de Ejecución
     ↓
Logística & Costos
     ↓
Fiscalidad (DTE)
     ↓
Recursos Humanos
     ↓
Finanzas Operativas
     ↓
Reportes & Decisión

```

Este flujo **no es técnico**, es **operacional y realista**, validado contra la práctica de ingeniería civil.

---

## 3. 📦 Contextos Delimitados Definitivos (v1.1)

### 1️⃣ Oportunidades / Pre-Proyecto

**Propósito:** Capturar obras antes de ser proyectos formales.

**Estado:** Sin cambios.

---

### 2️⃣ Gestión de Proyectos

**Propósito:** Contenedor principal del sistema.

**Entidades Clave:**

- **Proyecto**
- Cliente
- CondicionesContractuales
- CronogramaBase

🆕 **Nueva Entidad Crítica**

- **ProyectoAdministrativo** (System Default)

**Reglas:**

- Proyecto nace desde Oportunidad.
- Línea Base Contractual se congela al adjudicar.
- ProyectoAdministrativo absorbe gastos indirectos (overhead).

---

### 3️⃣ Presupuestos & APUs

**Propósito:** Definir costo y precio de la obra.

**Agregados:**

- **Presupuesto**
- **Partida**
- **APU (Instanciado)**

**Reglas Inquebrantables:**

- Presupuesto es versionable.
- Existe una versión contractual inmutable.
- APU instanciado **no se recalcula automáticamente**.

---

### 4️⃣ Control de Ejecución

**Propósito:** Medir la realidad de obra.

**Entidades:**

- **AvancePartida**
- Medición
- OrdenCambio
- Adendo

**Reglas:**

- Lo contratado no se edita.
- Toda desviación es trazable.

---

### 5️⃣ 🆕 Logística & Costos

*(Antes: Costos & Compras)*

**Propósito:** Registrar costos reales sin mentirle al sistema.

**Entidades Clave:**

- **Bodega**
- **InventarioItem**
- **TransferenciaMaterial**
- **ActivoFijo**
- Compra
- AsignaciónCosto

**Reglas de Oro:**

- **Comprar ≠ Consumir**
- El costo nace cuando el recurso entra a la obra.
- El inventario es un **Proyecto Pasivo del Sistema**.
- Activo ≠ Consumible.

⚠️ **Nota Crítica:**

Un activo **nunca** se consume en un proyecto.

El APU solo ve “cuota de uso” o “alquiler interno”.

---

### 6️⃣ Fiscalidad (DTE)

**Propósito:** Conectar realidad fiscal con costos.

**Entidades:**

- **DTE**
- LíneaDTE
- Emisor / Receptor

**Reglas:**

- DTE entra a Bandeja de Costos.
- Sin asignación → error de control.

---

### 7️⃣ Recursos Humanos por Proyecto

**Propósito:** Controlar costo humano real.

**Entidades:**

- **Empleado**
- Tareo
- NóminaProyecto

**Reglas:**

- El costo se prorratea.
- Prestaciones afectan costo real.

---

### 8️⃣ Finanzas Operativas

**Propósito:** Gobernar el dinero real.

**Entidades:**

- **BilleteraProyecto**
- **MovimientoCaja**
- 🆕 **PréstamoInterProyecto**
- CuentaInterna

**Regla de Oro:**

> El dinero puede moverse, la deuda no puede ocultarse.
> 

**Comportamiento:**

- No se bloquea la operación.
- Toda excepción queda registrada como deuda interna.

---

### 9️⃣ Reportes & Decisión

**Propósito:** Convertir datos en acción temprana.

**KPIs No Negociables:**

- Avance físico vs financiero
- Margen bruto
- Caja disponible
- Desviación APU
- Resultado operativo mensual

---

## 4. 🧮 Checklist de Trazabilidad Matemática (MVP v1.0)

| # | Condición |
| --- | --- |
| 1 | Todo ingreso/egreso está en `MovimientoCaja` |
| 2 | Todo costo material proviene de `Consumo/Transferencia` |
| 3 | Toda compra está respaldada por DTE o documento |
| 4 | Toda planilla genera egreso y distribución |
| 5 | No existe cobro sin evidencia |
| 6 | No hay aprobación sin conformidad |
| 7 | Préstamos inter-proyecto generan deuda visible |

---

## 5. ⚠️ Alertas de Ingeniería de Valor

- **Inventario no es costo**, es estado intermedio del dinero.
- **Overhead sin proyecto administrativo destruye el margen real.**
- **Billetera rígida mata la operación diaria.**

🛑 **Advertencia:**

Eliminar cualquiera de estos parches **rompe la adopción real del sistema**.

---

## 6. 📜 Registro de Cambios / Auditoría

### Cambios introducidos en v1.1

| Área | Ajuste |
| --- | --- |
| Costos | Integración de Inventario/Bodega |
| Activos | Distinción formal Activo vs Consumible |
| Finanzas | Préstamos inter-proyecto |
| Proyectos | ProyectoAdministrativo default |
| Dominio | Eliminación de costos falsos y utilidades ficticias |

✅ **Auditoría:** Aprobado tras stress test operativo y financiero.

🧊 **Estado:** Congelado como base de todo desarrollo posterior.

---

## 🧾 Veredicto Final

> BUDGETPRO deja de ser software de presupuestos.
> 
> 
> Se convierte en un **sistema de gobierno financiero-operativo para ingeniería civil**,
> 
> realista, auditable y adoptable en obra.
> 

---

**FIN DEL DOCUMENTO — GOLDEN SOURCE OF TRUTH (MAPA DE DOMINIO v1.1)**