# ERD Físico (SQL + Mermaid)

---

> 🏷️ META-INFO
> 
> - **📅 Fecha:** 09/01/2026
> - **🧊 Estado:** CONGELADO (Auditado)
> - **📂 Clasificación:** Datos & Persistencia
> - **📎 Archivo Origen:** ERD TEXTUAL — BUDGETPRO MVP v1.0

# 🏗️ ERD FÍSICO — BUDGETPRO MVP v1.0

---

## 📌 Resumen Ejecutivo

Este documento describe el **Modelo Entidad–Relación (ERD) textual definitivo** de **BUDGETPRO MVP v1.0**, diseñado para garantizar:

- **Trazabilidad matemática completa**
- **Auditabilidad financiera**
- **Separación clara de costos, caja y control**
- **Soporte directo a los wireflows críticos validados**

El modelo está organizado por **núcleos funcionales**, alineados con los *Bounded Contexts* del dominio y optimizados para PostgreSQL.

---

## 📐 Convenciones Utilizadas

| Convención | Significado |
| --- | --- |
| `PK` | Primary Key |
| `FK` | Foreign Key |
| `1—1`, `1—N`, `N—M` | Cardinalidad |
| `enum` | Campo de dominio controlado |
| UUID | Identificador global |
| NUMERIC | Precisión financiera |

---

## 🧱 Núcleos del Modelo de Datos

[NÚCLEO 0 — Identidad del Proyecto y Línea Base](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%200%20%E2%80%94%20Identidad%20del%20Proyecto%20y%20L%C3%ADnea%20Base%202e46e70607218077a644cb2023538c18.md)

**Contextos:** Gestión de Proyectos + Presupuesto

**Objetivo:** Definir proyectos, versiones y línea base contractual.

- Proyecto
- Cliente
- Presupuesto
- PresupuestoVersión
- PresupuestoSnapshot (JSONB)

ℹ️ *Este núcleo habilita congelación contractual y performance de lectura.*

---

[NÚCLEO 1 — APU Instanciado y Meta de Costo](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%201%20%E2%80%94%20APU%20Instanciado%20y%20Meta%20de%20Costo%202e46e70607218001a84cf172b8f9c366.md)

**Contexto:** Presupuesto (Ingeniería)

- Partida
- APU (PK compartida con Partida)
- APU_Recurso → FK a **RECURSO (Shared Kernel)**

⚠️ **Regla crítica:**

> El APU es composición fuerte de Partida (@MapsId).
> 

---

[NÚCLEO 2 — Operación de Materiales](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%202%20%E2%80%94%20Operaci%C3%B3n%20de%20Materiales%202e46e70607218099a640cf65237f03c8.md)

**Contexto:** Logística & Costos

**Flujo:** Compra → Inventario → Consumo → Partida

- Compra
- CompraDetalle
- InventarioItem
- MovimientoInventario
- ConsumoPartida

ℹ️ **Modelo de costo:** Promedio ponderado (MVP).

---

[NÚCLEO 3 — DTE y Bandeja de Costos](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%203%20%E2%80%94%20DTE%20y%20Bandeja%20de%20Costos%202e46e7060721806cb11dd5904a0768c1.md)

**Contextos:** Fiscal + Costos

- DTE
- DTELínea
- BandejaCosto

🛑 *Nada entra a costos sin clasificación explícita.*

---

[NÚCLEO 4 — Finanzas Operativas](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%204%20%E2%80%94%20Finanzas%20Operativas%202e46e706072180d1a48bf84bb94ded47.md)

**Contexto:** Caja & Liquidez

- BilleteraProyecto
- MovimientoCaja
- PrestamoInterProyecto

⚠️ **Regla inquebrantable:**

> Todo dinero vive en MovimientoCaja.
> 

---

[NÚCLEO 5 — Ejecución y Cambios](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%205%20%E2%80%94%20Ejecuci%C3%B3n%20y%20Cambios%202e46e706072180a18c2bee44fc551400.md)

**Contexto:** Control de Obra

- AvancePartida
- OrdenCambio
- OrdenCambioDetalle
- PartidaAdicional

ℹ️ *La línea base nunca se edita: solo se compara.*

---

[NÚCLEO 6 — Mano de Obra (MVP Simplificado)](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%206%20%E2%80%94%20Mano%20de%20Obra%20(MVP%20Simplificado)%202e46e70607218013a4dbdc253ee39dfa.md)

**Contexto:** RRHH (Light)

- PlanillaSemanal
- BolsaManoObraNoAsignada
- Distribución a ConsumoPartida

⚠️ **Riesgo controlado:** bolsa no distribuida afecta KPIs.

---

[NÚCLEO 7 — Estimación y Cobro](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%207%20%E2%80%94%20Estimaci%C3%B3n%20y%20Cobro%202e46e706072180d781e8c8edc2d71b9b.md)

**Contexto:** Gestión de Ingresos

- EstimacionPago
- AprobacionCliente
- Evidencia
- EvidenciaVinculo (polimórfico)

🛑 **No hay cobro sin evidencia.**

---

[NÚCLEO 8 — KPIs y Derivaciones](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%208%20%E2%80%94%20KPIs%20y%20Derivaciones%202e46e70607218021ad85c4935311dae8.md)

**Contexto:** Reportes

- Vistas y consultas derivadas
- No tablas base
- Depende 100% de imputación correcta

---

[NÚCLEO 9 — Activos (MVP)](ERD%20F%C3%ADsico%20(SQL%20+%20Mermaid)/N%C3%9ACLEO%209%20%E2%80%94%20Activos%20(MVP)%202e46e706072180d38c78d5ab27667903.md)

**Contexto:** Activos

- Activo
- Diferenciación vs Consumible

ℹ️ *No hay depreciación contable en MVP.*

---

## 📊 Checklist de Trazabilidad Matemática (MVP)

| # | Regla |
| --- | --- |
| 1 | Todo ingreso/egreso está en `MovimientoCaja` |
| 2 | Todo costo real nace en `ConsumoPartida` |
| 3 | Toda compra tiene respaldo documental |
| 4 | Toda planilla cuadra (pagado = distribuido + bolsa) |
| 5 | No hay cobro sin evidencia |
| 6 | Aprobaciones delegadas exigen prueba |
| 7 | Préstamos inter-proyecto generan deuda trazable |

---

## 💡 Observaciones de Ingeniería de Valor

- Diseño **audit-first**: lo derivable no se edita.
- Compra Directa resuelta sin romper integridad.
- Evidencia pasa de “foto” a **prueba legal de cobro**.
- Shared Kernel `RECURSO` evita colapso semántico del sistema.

---

## 🧾 Registro de Auditoría

| Fecha | Cambio |
| --- | --- |
| 09/01/2026 | Unificación de catálogos en RECURSO |
| 09/01/2026 | Validación forense de núcleos ERD |
| 09/01/2026 | Congelación del modelo físico MVP |

---

**🧊 Documento congelado.
Cualquier cambio requiere RFC arquitectónica.**