# Alcance: Crear proyecto y flujo de presupuestos (one-pager)

> **Tipo:** especificación de producto / decisión de alcance. **No** es canónico (`docs/canonical/`).  
> **Última actualización:** 2026-04-19  
> **Referencias:** `docs/PDF/S10-pag-9-16.pdf` (S10, inspiración de flujos); conversación de alineación BudgetPro.

## 1. Decisión de alcance — pantalla «Crear proyecto nuevo» (**c3**)

| ID | Decisión |
|----|----------|
| **c3** | En la pantalla **«Crear proyecto nuevo»** solo se gestiona el **proyecto**. Los **presupuestos** se dan de alta en **flujo aparte**. Una **especificación única** (este documento y enlaces que se agreguen) describe **ambos** flujos y cómo se enlazan (navegación, orden recomendado, identificadores). |

**Implicaciones**

- No se incluye en esa pantalla la **grilla de N presupuestos** (obra gris, eléctrico, plomería, etc.) como paso del mismo formulario de creación de proyecto.
- Tras crear el proyecto, el producto puede **recomendar** o **enlazar** explícitamente al usuario hacia el flujo de **registro de presupuesto(s)** (paridad conceptual con S10 «registro del nuevo presupuesto» en escenario distinto).

## 2. Cliente / propietario

| Decisión | Detalle |
|----------|---------|
| **Un cliente por proyecto** | Alineado con la lectura acordada del flujo S10 a nivel de **obra / propietario** centralizado en el proyecto. |

## 3. Hipótesis de mapeo S10 → capas BudgetPro (hasta análisis de subpresupuesto)

Esta tabla es **hipótesis de especificación** para implementación futura; no obliga migraciones ni DTOs hasta diseño de dominio y ADR correspondiente.

| Tema (S10 / referencia de contenido) | Capa BudgetPro (hipótesis) |
|--------------------------------------|----------------------------|
| Cliente / propietario | **Proyecto** |
| Logotipo, doble moneda, moneda base, datos adicionales de la ventana «Datos generales» del **presupuesto**, totales base/oferta de ese escenario | **Presupuesto** (por instancia / cabecera) |

**Nota:** El manual S10 asocia varios de esos elementos al escenario **PRESUPUESTO**; el cliente en BudgetPro queda en **Proyecto** por decisión de producto, sin negar que en UI de presupuesto se pueda **mostrar** el cliente del proyecto (lectura / herencia visual).

## 4. Pendientes explícitos (no cerrados en este documento)

| Tema | Acción recomendada |
|------|-------------------|
| **Subpresupuesto** | Spike: definición económica, relación con **presupuesto principal**, **órdenes de cambio** e integración al flujo. |
| **REGLA-110** (un presupuesto ACTIVO por proyecto vs. varios presupuestos por especialidad) | Análisis profundo; detectar conflicto canónico vs. negocio y documentar resolución antes de cambiar código o canónico. |
| **Catálogos externos** | Mantener separación: motor BudgetPro vs. catálogos consumidos externamente; revisar evolución del **WBS** frente a insumos/recursos externos. |

## 5. Enlaces útiles

- PDF S10 (fragmento analizado): `docs/PDF/S10-pag-9-16.pdf`
- UI — Presupuesto «Datos generales» (S10 §1.3, especificación de pantalla): [`docs/product/UI_PRESUPUESTO_DATOS_GENERALES_S10.md`](UI_PRESUPUESTO_DATOS_GENERALES_S10.md)
- Canónico Presupuesto (no editar desde aquí): `docs/canonical/modules/PRESUPUESTO_MODULE_CANONICAL.md`

## 6. Próximos pasos sugeridos

1. **Flujo UI** tras crear proyecto → registrar presupuesto: la cabecera **«Datos generales»** quedó especificada en [`UI_PRESUPUESTO_DATOS_GENERALES_S10.md`](UI_PRESUPUESTO_DATOS_GENERALES_S10.md); pendientes otros wireflows (p. ej. lista de presupuestos por proyecto, navegación global) si el producto los requiere.  
2. Ejecutar el **spike** de subpresupuesto + OC + REGLA-110 y volcar conclusiones en un ADR bajo `docs/product/` o herramienta de gestión acordada.  
3. Derivar **orden de trabajo** backend/frontend solo cuando el dominio y los contratos estén acotados (hexágono, sin persistir «a lo loco»).
