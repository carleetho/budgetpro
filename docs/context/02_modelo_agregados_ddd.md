# MODELO DE AGREGADOS E INVARIANTES (DDD) v1.0

> 🏷️ META-INFO
> 
> - **📅 Fecha:** 09/01/2026
> - **🧊 Estado:** CONGELADO (Auditado)
> - **📂 Clasificación:** Dominio & Negocio
> - **📎 Archivo Origen:** *BUDGETPRO — MODELO DE AGREGADOS E INVARIANTES (DDD) v1.0*

---

# 📦 Modelo de Agregados e Invariantes (DDD) — BUDGETPRO

## 1. Resumen Ejecutivo

Este documento define **el corazón del dominio de BUDGETPRO** bajo principios de **Domain-Driven Design (DDD)**.

Aquí se establecen de forma **no negociable**:

- Los **Bounded Contexts** del sistema.
- Los **Agregados**, sus **Aggregate Roots** y **Value Objects**.
- Las **Invariantes de negocio** que garantizan coherencia económica, contable y contractual.

⚠️ **Alcance:**

Este artefacto **NO** define base de datos, ORM, APIs ni UX. Es la **fuente de verdad semántica** que gobierna todas las capas posteriores.

---

## 2. Principios Rectores (Inmutables)

| # | Principio |
| --- | --- |
| 1 | El **Dominio** es la fuente de verdad. |
| 2 | Cada **Agregado** es un **límite transaccional**. |
| 3 | Las **invariantes se validan antes de persistir**. |
| 4 | La orquestación entre agregados ocurre vía **Domain / Application Services**. |
| 5 | **Lectura ≠ Escritura** (Write Model separado del Read Model). |

🛑 Cualquier violación a estos principios requiere RFC formal.

---

## 3. Bounded Contexts Confirmados

| # | Contexto |
| --- | --- |
| 1 | Gestión de Proyectos |
| 2 | Presupuestos & APUs |
| 3 | Logística & Costos |
| 4 | Finanzas Operativas |
| 5 | Ejecución de Obra |
| 6 | Comunicación, Evidencia & Cobros |
| 7 | **Shared Kernel — Catálogo de Recursos** |

ℹ️ Cada contexto mantiene su **lenguaje ubicuo** y reglas internas.

---

## 4. Shared Kernel — Catálogo de Recursos

### 🟪 Agregado: `CatalogoRecurso`

**Propósito:**

Proveer una **identidad económica canónica** para comparar costos meta vs reales en todo el sistema.

**Entidad Principal:** `Recurso`

| Campo | Descripción |
| --- | --- |
| recursoId | Identificador único |
| nombre | Nombre normalizado |
| tipo | MATERIAL | MANO_OBRA | SUBCONTRATO | ACTIVO |
| unidadBase | Unidad económica base |

**Invariante Crítica:**

> Un Recurso representa un único concepto económico global en todo BUDGETPRO.
> 

---

## 5. Agregados del Dominio

---

### 🟦 A. Proyecto

**Contexto:** Gestión de Proyectos

**Aggregate Root:** `Proyecto`

**Value Objects:**

TipoCliente, FormaPago, PorcentajeAnticipo, PorcentajeRetencion

**Invariantes:**

1. Un proyecto tiene **un solo estado activo**.
2. Máximo **una Línea Base Contractual**.
3. Proyectos administrativos **no generan ingresos**.

---

### 🟦 B. Presupuesto

**Contexto:** Presupuestos & APUs

**Aggregate Root:** `Presupuesto`

**Entidades:** Partida, APU, APURecurso

**Invariantes Clave:**

- Presupuesto contractual es **inmutable**.
- Cada Partida tiene **exactamente un APU**.
- `CostoMetaAPU = Σ Subtotales APURecurso`.
- Referencia exclusiva a `CatalogoRecurso`.

---

### 🟦 C. Inventario

**Contexto:** Logística & Costos

**Aggregate Root:** `InventarioItem`

**Invariantes:**

- Stock **nunca negativo**.
- Toda salida tiene **origen válido**.
- El inventario **no conoce proyectos ni partidas**.

⚠️ Separación estricta para evitar acoplamiento financiero.

---

### 🟦 D. Compra

**Contexto:** Logística & Costos

**Aggregate Root:** `Compra`

**Invariantes:**

- La compra **no genera costo por sí sola**.
- El destino es obligatorio.
- Cada detalle referencia a `CatalogoRecurso`.

---

### 🟦 E. Consumo de Costo

**Contexto:** Logística & Costos

**Aggregate Root:** `ConsumoPartida`

**Invariantes:**

- Todo consumo pertenece a una partida.
- El **costo real nace aquí**, no en la compra.
- No puede exceder disponibilidad válida.

---

### 🟦 F. Planilla Semanal

**Contexto:** Logística & Costos

**Aggregate Root:** `PlanillaSemanal`

**Estados:** REGISTRADA → DISTRIBUIDA

**Invariantes:**

- Distribuido + NoAsignado = TotalPagado
- Toda planilla genera **egreso de caja**
- La planilla **no es proveedor**

---

### 🟦 G. Billetera de Proyecto

**Contexto:** Finanzas Operativas

**Aggregate Root:** `BilleteraProyecto`

**Invariantes Críticas:**

- El saldo **no se edita manualmente**
- Todo cambio genera `MovimientoCaja`
- **No existe dinero sin movimiento**

🛑 Este agregado es **ultra-auditable**.

---

### 🟦 H. Préstamo Inter-Proyecto

**Contexto:** Finanzas Operativas

**Aggregate Root:** `PrestamoInterProyecto`

**Invariantes:**

- Todo préstamo crea:
    - Egreso origen
    - Ingreso destino
- La deuda pendiente **no puede ser negativa**

---

### 🟦 I. Ejecución de Obra

**Contexto:** Ejecución

**Aggregate Root:** `AvancePartida`

**Invariante:**

> Ejecutado acumulado ≤ Contratado + Variaciones aprobadas
> 

---

### 🟦 J. Estimación de Pago

**Contexto:** Comunicación & Cobros

**Aggregate Root:** `EstimacionPago`

**Invariantes:**

- No hay estimación sin partidas
- Estimado ≤ Ejecutado acumulado

---

### 🟦 K. Aprobación de Cliente

**Contexto:** Comunicación & Cobros

**Aggregate Root:** `AprobacionCliente`

**Estados:** PENDIENTE, APROBADA

**Invariantes:**

- Evidencia obligatoria en aprobación delegada
- Una estimación se aprueba **una sola vez**

---

### 🟦 L. Evidencia

**Contexto:** Comunicación & Cobros

**Aggregate Root:** `Evidencia`

**Estados:** CARGANDO → DISPONIBLE → ERROR

**Invariantes:**

- Toda evidencia debe vincularse al dominio
- Solo evidencias **DISPONIBLE** habilitan cobros

---

## 6. Domain Services Críticos

### 🔹 ProcesarCompraDirectaService

- Orquesta: Compra + Inventario + Consumo + Billetera
- **Transacción ACID única**
- Punto único de consistencia financiera

### 🔹 CongelarLineaBaseService

- Garantiza inmutabilidad contractual
- Genera `PresupuestoSnapshot` (Read Model)

---

## 7. Estado del Artefacto

| Estado | Resultado |
| --- | --- |
| Agregados | ✔️ Definidos |
| Invariantes | ✔️ Validadas |
| Riesgos | ✔️ Mitigados |
| Dominio | ✔️ Listo para implementación |

> 🧊 CONGELAMIENTO FORMAL
> 
> 
> A partir de este punto:
> 
> - ❌ No se agregan invariantes sin RFC
> - ❌ No se redefinen límites de agregados
> - ✅ Todo desarrollo debe alinearse a este modelo

---

## 8. Registro de Auditoría

**Auditoría Técnica:** Arquitectura Senior

**Resultado:** APROBADO

**Mejoras Aplicadas:**

- Clarificación de invariantes financieras
- Separación estricta Inventario vs Costos
- Refuerzo de Shared Kernel

---

### ➡️ Siguiente Documento Relacionado

**📄 🏰 Arquitectura Hexagonal (Capas y Reglas)**

(Traduce este dominio en puertos, adaptadores y casos de uso).