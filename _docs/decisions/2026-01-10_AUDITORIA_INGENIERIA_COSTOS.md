# 🔍 INFORME DE AUDITORÍA TÉCNICA: INGENIERÍA DE COSTOS

**Fecha:** 2026-01-10  
**Auditor:** Ingeniero Civil Senior (AACE Internacional, PMI-CP)  
**Alcance:** Validación de dominio BudgetPro vs. Realidad de Obra  
**Estado:** 🟡 **PARCIALMENTE VIABLE CON RIESGOS CRÍTICOS**

---

## 📊 ESTADO DE COBERTURA (SEMÁFORO)

### 🟢 CUBIERTO — Módulos Robustos

| Módulo | Estado | Justificación |
|--------|--------|---------------|
| **RECURSO (Catálogo Maestro)** | 🟢 Cubierto | Agregado bien definido, tipos claros (MATERIAL, MANO_OBRA, SUBCONTRATO, ACTIVO), normalización adecuada. Base sólida para APUs futuros. |
| **BILLETERA (Movimientos Financieros)** | 🟢 Cubierto | MovimientoCaja bien estructurado, tipos de movimiento claros. Soporta ingresos/egresos básicos. |
| **COMPRA (Transacciones de Compra)** | 🟢 Cubierto | Agregado Compra con estados transaccionales, detalles, persistencia atómica. Cubre el flujo básico de "comprar material". |
| **INVENTARIO (Stock de Materiales)** | 🟢 Cubierto | InventarioItem con control de stock, invariantes no-negativos. Base para control de almacén. |

---

### 🟡 PARCIAL/RIESGOSO — Definido pero Insuficiente

| Módulo | Estado | Hallazgos |
|--------|--------|-----------|
| **PRESUPUESTO + PARTIDA** | 🟡 Parcial | **CRÍTICO:** La estructura actual no soporta WBS jerárquico. `Partida` es plana (solo `codigo`, `descripcion`). En obra real necesitas: Títulos → Subtítulos → Partidas → Subpartidas. **FALTA:** Campo `nivel`, `partida_padre_id`, estructura jerárquica. |
| **CONTROL DE CONSUMO** | 🟡 Parcial | Existe `ConsumoPartida`, pero **NO hay trazabilidad completa** de: Requisición → Cotización → Orden Compra → Entrada Almacén → Salida a Partida. Falta el flujo completo de aprovisionamiento. |
| **BILLETERA (Pasivos/Plazos)** | 🟡 Parcial | **CRÍTICO:** La Billetera asume que el dinero sale/in-gresa al instante. En construcción pagamos a 30/60 días (Cuentas por Pagar) y cobramos con retenciones (10-15%). **FALTA:** Módulo de Cuentas por Pagar, Fechas de Vencimiento, Retenciones por Cliente. |

---

### 🔴 CRÍTICO/FALTANTE — No Funcionará en Obra

| Módulo | Estado | Impacto |
|--------|--------|---------|
| **ANÁLISIS DE PRECIOS UNITARIOS (APU)** | 🔴 Faltante | **DEAL BREAKER #1:** No existe el concepto de "Análisis" que componga: Rendimientos (m²/día), Cuadrillas (operarios + herramientas), Aportes Unitarios (materiales con desperdicio %), Gastos Generales (%). Sin APUs, el presupuesto es solo "números planos" sin sustento técnico. |
| **METRADOS/CUBICACIÓN** | 🔴 Faltante | **DEAL BREAKER #2:** No hay dónde almacenar el sustento del metrado (largo x ancho x alto, fórmulas, áreas). Solo existe un "monto presupuestado" sin descomposición. En obra, cuando el cliente pide "¿de dónde sale este precio?", no hay respuesta. |
| **GASTOS GENERALES** | 🔴 Faltante | **DEAL BREAKER #3:** No distingue entre Costos Directos (mano de obra, materiales) vs. Costos Indirectos (staff, oficina técnica, herramientas). Los Gastos Generales (15-25% típico) deben aplicarse como porcentaje o importe fijo sobre costos directos. |
| **VALUACIONES/ESTIMACIONES** | 🔴 Faltante | **DEAL BREAKER #4:** No existe soporte para cobrar al cliente por avance físico (%). En construcción, cobras por "valuación mensual" basada en % de avance. Falta: EstimacionPago, AprobacionCliente, control de avance físico vs. financiero. |
| **CONTROL DE CAMBIOS (Adicionales/Deductivos)** | 🔴 Faltante | **DEAL BREAKER #5:** No hay mecanismo para "Adicionales de Obra" o "Deductivos" que modifiquen el presupuesto original. En obra, el presupuesto base se ajusta constantemente. Falta: Versionamiento de Presupuesto, Control de Cambios, Aprobaciones. |
| **FONDOS DE GARANTÍA/RETENCIONES** | 🔴 Faltante | **DEAL BREAKER #6:** La Billetera no contempla que el cliente retiene 10-15% hasta recepción final. El dinero "cobrado" no está realmente disponible. Falta: Módulo de Retenciones, Fondo de Garantía, Desembolsos Condicionados. |
| **LÍNEA BASE vs. REAL (Valor Ganado)** | 🔴 Faltante | **DEAL BREAKER #7:** No distingue entre "Presupuesto Venta" (contrato con cliente), "Presupuesto Meta" (interno optimizado) y "Ejecutado Real". Sin esto, no hay control de desviaciones ni análisis de Valor Ganado (Earned Value Management). |

---

## 💣 HALLAZGOS DE "ROMPE-OBRA" (DEAL BREAKERS)

### 1. **PRESUPUESTO ES PLANO, NO WBS JERÁRQUICO**

**Problema:**
- La estructura actual de `Partida` solo tiene `codigo`, `descripcion`.
- No existe jerarquía (Títulos → Subtítulos → Partidas).
- No hay relación padre-hijo entre partidas.

**Impacto en Obra:**
- Imposible estructurar un presupuesto real (ej: "01. EXCAVACIONES" → "01.01. Excavación Manual" → "01.01.01. Excavación en Material Común").
- No se puede agrupar partidas para reportes de nivel.
- Los presupuestos en construcción TIENEN niveles (típicamente 3-4 niveles).

**Ejemplo Real:**
```
01. EXCAVACIONES (Título - Nivel 1)
  ├─ 01.01. Excavación Manual (Subtítulo - Nivel 2)
  │   ├─ 01.01.01. Excavación en Material Común (Partida - Nivel 3)
  │   └─ 01.01.02. Excavación en Material Rocoso (Partida - Nivel 3)
  └─ 01.02. Excavación Mecánica (Subtítulo - Nivel 2)
      └─ 01.02.01. Excavación con Retroexcavadora (Partida - Nivel 3)
```

**Solución Requerida:**
- Agregar `nivel` (INT) y `partida_padre_id` (UUID, FK a partida) a `Partida`.
- Modificar agregado `Partida` para soportar jerarquía.
- Migration SQL para agregar campos.

---

### 2. **NO EXISTE ANÁLISIS DE PRECIOS UNITARIOS (APU)**

**Problema:**
- El presupuesto solo tiene "monto presupuestado" por partida.
- No hay descomposición en: Rendimientos, Cuadrillas, Aportes Unitarios.
- No existe la entidad "Análisis" que sustente el precio unitario.

**Impacto en Obra:**
- Imposible justificar un precio al cliente ("¿de dónde sale $50/m²?").
- No se puede calcular precio unitario desde insumos (materiales + mano de obra + desperdicios).
- En construcción, TODO precio unitario tiene un "Análisis" que lo sustenta.

**Ejemplo Real (APU de "Muro de Albañilería 0.15m"):**
```
APU: Muro de Albañilería 0.15m
Precio Unitario: $45.50/m²

Componentes:
1. Materiales (Aportes Unitarios):
   - Ladrillo: 45 unidades/m² × $0.80 = $36.00
   - Mortero: 0.05 m³/m² × $150.00 = $7.50
   - Desperdicio 5%: +$2.18
   Subtotal Materiales: $45.68

2. Mano de Obra (Cuadrilla):
   - Albañil: 0.15 hh/m² × $25/hora = $3.75
   - Ayudante: 0.10 hh/m² × $15/hora = $1.50
   Subtotal MO: $5.25

3. Herramientas: $0.50/m²

4. Gastos Generales (15%): $7.72

TOTAL: $59.15/m²
```

**Solución Requerida:**
- Crear agregado `AnalisisPrecioUnitario` (APU).
- Entidades: `AporteUnitario` (material + cantidad + desperdicio %), `Cuadrilla` (operarios + horas), `Rendimiento`.
- Relación: `Partida` → `AnalisisPrecioUnitario` (1:1 o 1:N si hay variantes).

---

### 3. **NO HAY METRADOS/CUBICACIÓN (Sustento del Presupuesto)**

**Problema:**
- El presupuesto tiene "monto total" pero no el sustento del metrado.
- No existe dónde almacenar: largo × ancho × alto, áreas, fórmulas.
- No hay relación entre "cantidad presupuestada" y "sustento geométrico".

**Impacto en Obra:**
- Imposible revisar metrados cuando el cliente cuestiona cantidades.
- No se puede recalcular presupuesto si cambia el diseño (ej: más área de muros).
- En construcción, TODO presupuesto tiene un "metrado" que lo sustenta.

**Ejemplo Real:**
```
Partida: "Muro de Albañilería 0.15m"
Monto Presupuestado: $50,000

Sustento (Metrado):
- Largo: 120 m
- Alto: 3.5 m
- Área: 120 × 3.5 = 420 m²
- Precio Unitario: $119.05/m² (del APU)
- Total: 420 × $119.05 = $50,000
```

**Solución Requerida:**
- Crear agregado `Metrado` o extender `Partida` con campos: `cantidad_presupuestada`, `unidad_medida`, `sustento` (JSON o texto).
- O crear entidad separada `MetradoPartida` con fórmulas/medidas.

---

### 4. **BILLETERA NO SOPORTA CUENTAS POR PAGAR (Plazos de Pago)**

**Problema:**
- La Billetera asume que el dinero sale/ingresa al instante.
- No hay concepto de "Cuentas por Pagar" (pagar a 30/60 días).
- No hay fechas de vencimiento, proveedores pendientes.

**Impacto en Obra:**
- Imposible reflejar la realidad financiera: "Compré material hoy, lo pago en 45 días".
- El flujo de caja proyectado es incorrecto (no considera plazos).
- En construcción, TODO pago tiene un plazo (factoring, crédito comercial).

**Ejemplo Real:**
```
Fecha: 2026-01-10
Compra Material: $100,000
Plazo de Pago: 45 días
Fecha Vencimiento: 2026-02-24

Estado Actual del Sistema:
- Billetera: -$100,000 (INCORRECTO, el dinero no sale hoy)

Estado Correcto:
- Billetera: $0 (dinero disponible no cambia)
- Cuentas por Pagar: +$100,000 (obligación futura)
- Fecha Vencimiento: 2026-02-24
```

**Solución Requerida:**
- Crear agregado `CuentaPorPagar` con: `proveedor_id`, `monto`, `fecha_emision`, `fecha_vencimiento`, `estado` (PENDIENTE, PAGADA).
- Modificar `Billetera` para distinguir entre "Saldo Disponible" vs. "Obligaciones Futuras".
- O crear módulo `PasivosFinancieros` separado.

---

### 5. **NO EXISTE MÓDULO DE RETENCIONES/FONDOS DE GARANTÍA**

**Problema:**
- La Billetera no contempla que el cliente retiene 10-15% hasta recepción final.
- El dinero "cobrado" no está realmente disponible.
- No hay control de desembolsos condicionados.

**Impacto en Obra:**
- El flujo de caja disponible es incorrecto: "Cobré $1,000,000 pero solo tengo $850,000 disponibles (15% retenido)".
- Imposible planificar pagos si parte del dinero está "retenido".
- En construcción, las retenciones son estándar (10-15% hasta recepción final).

**Ejemplo Real:**
```
Valuación Mensual: $1,000,000
Retención Cliente: 15% ($150,000)
Cobro Neto: $850,000

Estado Actual del Sistema:
- Billetera: +$1,000,000 (INCORRECTO, no tienes todo el dinero)

Estado Correcto:
- Billetera: +$850,000 (dinero disponible)
- Fondo de Garantía: +$150,000 (retenido)
- Total Cobrado: $1,000,000
```

**Solución Requerida:**
- Crear agregado `FondoGarantia` o `RetencionCliente` con: `monto_retenido`, `fecha_recepcion_prevista`, `estado` (RETENIDO, DESEMBOLSADO).
- Modificar `MovimientoCaja` para distinguir entre "Cobro Bruto" vs. "Cobro Neto" vs. "Retención".

---

### 6. **NO HAY VALUACIONES/ESTIMACIONES (Cobro por Avance)**

**Problema:**
- No existe soporte para cobrar al cliente por avance físico (%).
- No hay relación entre "avance físico" (ej: 45% de muros completados) y "cobro financiero".
- Falta el módulo de "Estimaciones de Pago" mencionado en el ERD pero no implementado.

**Impacto en Obra:**
- Imposible facturar al cliente por avance de obra.
- No hay control de "lo que cobré" vs. "lo que ejecuté".
- En construcción, cobras mensualmente por "valuación" basada en % de avance físico.

**Ejemplo Real:**
```
Mes: Enero 2026
Avance Físico: 25% de la obra
Presupuesto Total: $10,000,000
Valuación: 25% × $10,000,000 = $2,500,000
Retención: 15% × $2,500,000 = $375,000
Cobro Neto: $2,125,000
```

**Solución Requerida:**
- Crear agregado `EstimacionPago` o `Valuacion` con: `periodo`, `avance_fisico_%`, `monto_valuado`, `retencion_%`, `monto_cobrado`, `aprobacion_cliente` (estado).
- Relación: `Presupuesto` → `EstimacionPago` (1:N).
- Mencionado en ERD Núcleo 7 pero no implementado.

---

### 7. **NO HAY CONTROL DE CAMBIOS (Adicionales/Deductivos)**

**Problema:**
- No existe mecanismo para "Adicionales de Obra" o "Deductivos" que modifiquen el presupuesto original.
- El presupuesto es inmutable una vez creado.
- No hay versionamiento ni control de cambios aprobados.

**Impacto en Obra:**
- Imposible ajustar presupuesto cuando el cliente solicita cambios.
- No se puede rastrear "Presupuesto Original" vs. "Presupuesto Ajustado".
- En construcción, el presupuesto base se ajusta constantemente (adicionales típicamente 10-20% del original).

**Ejemplo Real:**
```
Presupuesto Original: $10,000,000
Adicional #1: "Muro Adicional": +$500,000 (Aprobado)
Adicional #2: "Ampliación de Área": +$300,000 (Pendiente Aprobación)
Deductivo #1: "Eliminación de Terrazas": -$200,000 (Aprobado)

Presupuesto Ajustado: $10,600,000 (solo aprobados)
Presupuesto Final (si se aprueba #2): $10,900,000
```

**Solución Requerida:**
- Crear agregado `AdicionalObra` o `VariacionPresupuesto` con: `tipo` (ADICIONAL, DEDUCTIVO), `monto`, `justificacion`, `estado_aprobacion` (PENDIENTE, APROBADO, RECHAZADO), `fecha_aprobacion`.
- Relación: `Presupuesto` → `VariacionPresupuesto` (1:N).
- Modificar `Presupuesto` para tener `presupuesto_base` vs. `presupuesto_ajustado`.

---

### 8. **NO HAY GASTOS GENERALES (Costos Indirectos)**

**Problema:**
- No distingue entre Costos Directos (mano de obra, materiales) vs. Costos Indirectos (staff, oficina técnica).
- Los Gastos Generales (15-25% típico) no se aplican sobre costos directos.
- No hay estructura para distribuir gastos generales por partida.

**Impacto en Obra:**
- El presupuesto no refleja costos reales (falta el overhead de oficina técnica, herramientas, transporte).
- Imposible calcular margen real (utilidad = precio venta - costos directos - gastos generales).
- En construcción, los Gastos Generales son críticos (15-25% del costo directo).

**Ejemplo Real:**
```
Costos Directos (Partidas): $8,000,000
Gastos Generales (20%): $1,600,000
Total Costos: $9,600,000
Precio Venta: $12,000,000
Utilidad: $2,400,000 (20% sobre costos)
```

**Solución Requerida:**
- Crear agregado `GastoGeneral` o extender `Presupuesto` con: `porcentaje_gg`, `monto_fijo_gg`, `tipo_aplicacion` (PORCENTAJE, FIJO).
- O crear entidad `PartidaGastoGeneral` que se distribuya sobre partidas directas.

---

### 9. **NO HAY LÍNEA BASE vs. REAL (Valor Ganado)**

**Problema:**
- No distingue entre "Presupuesto Venta" (contrato con cliente), "Presupuesto Meta" (interno optimizado) y "Ejecutado Real".
- No hay control de desviaciones ni análisis de Valor Ganado (Earned Value Management).

**Impacto en Obra:**
- Imposible responder: "¿Estamos ganando o perdiendo dinero en esta obra?"
- No se puede calcular: PV (Planned Value), EV (Earned Value), AC (Actual Cost), CPI (Cost Performance Index), SPI (Schedule Performance Index).
- En construcción, el control de desviaciones es crítico para rentabilidad.

**Ejemplo Real (EVM):**
```
Presupuesto Venta (Contrato): $10,000,000
Presupuesto Meta (Interno): $8,500,000
Ejecutado Real (a la fecha): $4,500,000
Avance Físico: 50%
Valor Ganado (EV): 50% × $10,000,000 = $5,000,000

CPI = EV / AC = $5,000,000 / $4,500,000 = 1.11 (Vamos bien, bajo costo)
SPI = EV / PV = $5,000,000 / $5,000,000 = 1.0 (A tiempo)
```

**Solución Requerida:**
- Modificar `Presupuesto` para tener: `presupuesto_venta` (contrato), `presupuesto_meta` (interno).
- Crear agregado `EjecucionReal` o extender `Partida` con: `monto_ejecutado_real`, `avance_fisico_%`.
- Crear módulo `ValorGanado` para calcular indicadores (CPI, SPI, desviaciones).

---

### 10. **TRAZABILIDAD INCOMPLETA (Requisición → Compra → Consumo)**

**Problema:**
- Existe `Compra` y `ConsumoPartida`, pero **NO hay flujo completo**:
  - Requisición de Campo → Cotización → Orden de Compra → Entrada de Almacén → Salida a Partida
- No existe la entidad "Requisición" que inicia el proceso de compra.
- No hay relación entre "qué se pidió" vs. "qué se compró" vs. "qué se consumió".

**Impacto en Obra:**
- Imposible rastrear el ciclo completo: "El jefe de obra pidió 100 sacos de cemento, compramos 95 (descuento de proveedor), consumimos 90 en la partida, 5 quedaron en almacén".
- No hay control de "pedidos pendientes" vs. "compras realizadas".
- En construcción, la trazabilidad es crítica para control de costos.

**Solución Requerida:**
- Crear agregado `Requisicion` con: `partida_id`, `recurso_id`, `cantidad_solicitada`, `estado` (PENDIENTE, COTIZADA, APROBADA, COMPRADA).
- Crear agregado `Cotizacion` con: `requisicion_id`, `proveedor_id`, `precio_unitario`, `plazo_entrega`, `validez`.
- Relación: `Requisicion` → `Cotizacion` (1:N) → `Compra` (1:1) → `ConsumoPartida` (1:N).

---

## 🏗️ DIAGRAMA DE MÓDULOS SUGERIDO

Basado en los hallazgos, estos son los **Agregados de Dominio** que DEBERÍAS agregar al Backlog:

### **FASE 1: FUNDACIÓN CRÍTICA (Bloqueante para Presupuestos Reales)**

1. **`AnalisisPrecioUnitario` (APU)**
   - Agregado Root
   - Entidades internas: `AporteUnitario` (material + cantidad + desperdicio %), `Cuadrilla` (operarios + horas), `Rendimiento`
   - Relación: `Partida` → `AnalisisPrecioUnitario` (1:1 o 1:N)

2. **`Metrado` (Cubicación/Sustento)**
   - Agregado Root (o entidad dentro de `Partida`)
   - Campos: `cantidad_presupuestada`, `unidad_medida`, `sustento` (JSON con fórmulas/medidas)
   - Relación: `Partida` → `Metrado` (1:1)

3. **Extensión `Partida` (WBS Jerárquico)**
   - Agregar: `nivel` (INT), `partida_padre_id` (UUID, FK)
   - Métodos: `obtenerPartidasHijas()`, `obtenerPartidasRaiz()`
   - Relación: `Partida` → `Partida` (auto-referencia, 1:N)

4. **`GastoGeneral`**
   - Agregado Root
   - Campos: `porcentaje_gg`, `monto_fijo_gg`, `tipo_aplicacion`
   - Relación: `Presupuesto` → `GastoGeneral` (1:1)

### **FASE 2: CONTROL FINANCIERO (Bloqueante para Flujo de Caja Real)**

5. **`CuentaPorPagar`**
   - Agregado Root
   - Campos: `proveedor_id`, `monto`, `fecha_emision`, `fecha_vencimiento`, `estado`
   - Relación: `Compra` → `CuentaPorPagar` (1:1, opcional si hay plazo)

6. **`FondoGarantia` / `RetencionCliente`**
   - Agregado Root
   - Campos: `monto_retenido`, `fecha_recepcion_prevista`, `estado`
   - Relación: `MovimientoCaja` (ingreso) → `FondoGarantia` (1:N)

7. **`EstimacionPago` / `Valuacion`**
   - Agregado Root (mencionado en ERD Núcleo 7, no implementado)
   - Campos: `periodo`, `avance_fisico_%`, `monto_valuado`, `retencion_%`, `monto_cobrado`, `aprobacion_cliente`
   - Relación: `Presupuesto` → `EstimacionPago` (1:N)

### **FASE 3: CONTROL DE CAMBIOS Y TRAZABILIDAD**

8. **`VariacionPresupuesto` / `AdicionalObra`**
   - Agregado Root
   - Campos: `tipo` (ADICIONAL, DEDUCTIVO), `monto`, `justificacion`, `estado_aprobacion`
   - Relación: `Presupuesto` → `VariacionPresupuesto` (1:N)

9. **`Requisicion`**
   - Agregado Root
   - Campos: `partida_id`, `recurso_id`, `cantidad_solicitada`, `estado`
   - Relación: `Requisicion` → `Cotizacion` (1:N) → `Compra` (1:1)

10. **`Cotizacion`**
    - Agregado Root
    - Campos: `requisicion_id`, `proveedor_id`, `precio_unitario`, `plazo_entrega`, `validez`
    - Relación: `Requisicion` → `Cotizacion` (1:N)

### **FASE 4: VALOR GANADO (Avanzado, Post-MVP)**

11. **`EjecucionReal` / Extensión `Partida`**
    - Agregar a `Partida`: `monto_ejecutado_real`, `avance_fisico_%`
    - O crear agregado separado `EjecucionReal` con relación a `Partida`

12. **`PresupuestoVersion`**
    - Extender `Presupuesto` para versionamiento
    - Campos: `version`, `presupuesto_base_id`, `tipo_version` (VENTA, META)

---

## 💡 VEREDICTO DEL EXPERTO

### ¿Es viable continuar con Sprint 2?

**RESPUESTA: 🟡 SÍ, PERO CON ADVERTENCIAS CRÍTICAS**

#### ✅ **PUEDES CONTINUAR SI:**
1. **Aceptas que el MVP será "incompleto" para obra real** pero funcional para demos/prototipos.
2. **Planeas un "Sprint 3: Fundación de Presupuestos"** que incluya APUs, Metrados y WBS jerárquico ANTES de producción.
3. **El objetivo actual es validar el flujo de Compra Directa**, no control de costos completo.

#### 🛑 **NO PUEDES CONTINUAR SI:**
1. **El objetivo es desplegar en obra real** sin las estructuras base (APUs, Metrados).
2. **Necesitas justificar precios al cliente** (requiere APUs).
3. **Necesitas control de flujo de caja real** (requiere Cuentas por Pagar, Retenciones).

---

### 🎯 RECOMENDACIÓN ESTRATÉGICA

**OPCIÓN A: Continuar Sprint 2 (Compra Directa) + Sprint 3 (Fundación)**
- ✅ Pros: Validas flujo de compra, luego agregas estructuras base.
- ⚠️ Contras: Presupuestos seguirán siendo "planos" hasta Sprint 3.

**OPCIÓN B: Pivotar a Sprint 2.5 (Fundación Crítica)**
- ✅ Pros: Tienes estructuras base (APUs, WBS) antes de compras.
- ⚠️ Contras: Retrasa validación de Compra Directa.

**RECOMENDACIÓN FINAL: OPCIÓN A**
- Continuar Sprint 2 (Compra Directa) como está.
- Agregar al backlog: **Sprint 3: Fundación de Presupuestos** (APUs, Metrados, WBS jerárquico) como PREREQUISITO para producción.
- Marcar Sprint 2 como "MVP Técnico" no apto para obra real sin Sprint 3.

---

## 📋 CHECKLIST DE VALIDACIÓN PRE-PRODUCCIÓN

Para que BudgetPro funcione en obra real, estos módulos DEBEN estar implementados:

- [ ] WBS Jerárquico (Partida con niveles)
- [ ] Análisis de Precios Unitarios (APU)
- [ ] Metrados/Cubicación
- [ ] Gastos Generales
- [ ] Cuentas por Pagar
- [ ] Fondos de Garantía/Retenciones
- [ ] Valuaciones/Estimaciones de Pago
- [ ] Control de Cambios (Adicionales/Deductivos)
- [ ] Trazabilidad completa (Requisición → Compra → Consumo)
- [ ] Valor Ganado (Línea Base vs. Real)

**Sin estos módulos, el sistema NO funcionará en obra real.**

---

## 🔗 REFERENCIAS

- **AACE International:** Recommended Practice 18R-97 (Cost Estimate Classification System)
- **PMI-CP:** Project Cost Management (Earned Value Management)
- **Sistemas de Referencia:** Presto, S10, Oracle Primavera, Procore
- **ERD Núcleo 7:** Menciona `EstimacionPago` pero no está implementado
- **ERD Núcleo 8:** Menciona "Vistas y consultas derivadas" pero no hay implementación

---

**🧊 DOCUMENTO CONGELADO.**  
**Cualquier cambio requiere validación de Experto en Ingeniería de Costos.**
