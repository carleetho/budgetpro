# REGLAS DE NEGOCIO — MÓDULO PRESUPUESTO

## 1. Propósito del Módulo

El módulo PRESUPUESTO define el contrato digital que gobierna
la ejecución financiera de un Proyecto.

Un Presupuesto no es un documento de referencia.
Un Presupuesto aprobado es una ley ejecutable.

Este módulo es el corazón del sistema BudgetPro.

---

## 2. Existencia del Presupuesto

### 2.1 Creación

- Un Presupuesto solo puede crearse asociado a un Proyecto existente.
- Un Proyecto puede tener múltiples versiones de Presupuesto.
- Solo un Presupuesto puede estar ACTIVO (vigente) por Proyecto.

El estado inicial de todo Presupuesto es BORRADOR.

---

## 3. Estados del Presupuesto (Semántica Canónica)

### BORRADOR
- Presupuesto editable.
- Permite:
  - crear y modificar partidas,
  - definir APUs,
  - ajustar cantidades, precios y rendimientos.
- No habilita ejecución.
- No genera costos reales.

---

### CONGELADO
- Presupuesto aprobado formalmente.
- Se genera un Snapshot inmutable de la Línea Base.
- Habilita la ejecución del Proyecto.
- No permite modificación directa.

Un Presupuesto CONGELADO constituye un contrato digital.

---

### INVALIDADO
- Presupuesto reemplazado por una nueva versión.
- Permanece accesible solo para auditoría histórica.
- No habilita ejecución.

---

## 4. Congelamiento y Línea Base

### 4.1 Proceso de Congelamiento

Al pasar de BORRADOR a CONGELADO, el sistema DEBE:

- generar un Snapshot inmutable del Presupuesto,
- capturar:
  - partidas,
  - cantidades,
  - precios unitarios,
  - rendimientos,
  - duraciones asociadas,
- calcular el BAC (Budget at Completion),
- almacenar el Snapshot en formato inmutable (JSONB).

Este Snapshot nunca puede modificarse.

---

### 4.2 Inmutabilidad

- El Snapshot de Línea Base no se edita.
- Cualquier cambio posterior se gestiona exclusivamente mediante:
  - Órdenes de Cambio, o
  - Excepciones formales.

Modificar directamente la Línea Base constituye una violación grave del sistema.

---

## 5. Partidas y APU

### 5.1 Partidas

- Toda Partida pertenece a un Presupuesto específico.
- Toda Partida define:
  - unidad de medida,
  - cantidad contractual,
  - costo unitario contractual.

No existe ejecución sin Partida.

---

### 5.2 APU (Análisis de Precio Unitario)

- El APU descompone la Partida en:
  - materiales,
  - mano de obra,
  - equipos.
- El APU no es texto descriptivo, es estructura de datos.

El APU gobierna:
- topes máximos de compra,
- consumos permitidos,
- costos esperados.

---

## 6. Relación con la Ejecución

### 6.1 Compras

- Toda compra debe vincularse a una Partida válida del Presupuesto CONGELADO.
- Las cantidades compradas no pueden exceder los topes derivados del APU,
  salvo mediante Orden de Cambio o Excepción formal.

---

### 6.2 Inventario

- Todo movimiento de inventario debe:
  - originarse en una compra válida,
  - imputarse a una Partida específica.

Inventario sin Partida es ilegal.

---

### 6.3 Mano de Obra

- Toda planilla se imputa a una o más Partidas.
- La Mano de Obra es un Recurso de Partida, no un gasto administrativo.

---

## 7. Relación con Órdenes de Cambio

- Las Órdenes de Cambio crean capas aditivas o deductivas al Presupuesto original.
- No sobrescriben la Línea Base.
- Ajustan el BAC y las métricas de control.

El Presupuesto original siempre permanece visible para auditoría.

---

## 8. Límites Legales de Modificación Contractual (ENDURECIMIENTO LEGAL)

De conformidad con la Ley de Compras Públicas vigente,
las modificaciones contractuales están sujetas a límites legales.

Regla canónica del sistema:

- El monto acumulado de Órdenes de Cambio
  no puede exceder ±20 % del monto contractual original congelado.

El sistema DEBE:

- calcular el porcentaje acumulado de cambios,
- bloquear nuevas Órdenes de Cambio que superen el límite legal,
- exigir justificación y evidencia normativa si se intenta excederlo.

---

## 9. Órdenes de Cambio y Plazo Contractual (ENDURECIMIENTO)

Una Orden de Cambio puede modificar:

- cantidades,
- partidas,
- monto contractual,
- **plazo de ejecución**.

Reglas obligatorias:

- Toda Orden de Cambio que afecte plazo
  DEBE generar un ajuste formal del Cronograma contractual.
- El nuevo plazo forma parte de la Línea Base ajustada.
- El historial de plazos debe conservarse íntegro para auditoría.

---

## 10. Excesos de Consumo y Compras (REALIDAD OPERATIVA)

El sistema reconoce que el exceso de consumo puede ocurrir por:

- errores de estimación,
- rendimientos reales inferiores,
- desperdicio,
- robo,
- reprocesos o rehacer actividades.

Reglas del sistema:

- El exceso de consumo NO se oculta ni se normaliza.
- Debe registrarse como:
  - Excepción de consumo, o
  - Insumo asociado a Orden de Cambio.
- Todo exceso impacta directamente:
  - el Costo Real (AC),
  - la utilidad del Proyecto,
  - los indicadores de desempeño.

El sistema no impide la realidad;
la expone y la cuantifica.

---

## 11. Dependencia de Aprobación Externa

El sistema NO asume disponibilidad permanente de actores externos.

Reglas:

- Una Orden de Compra puede quedar en estado:
  - PENDIENTE DE APROBACIÓN,
  - EJECUTADA CON EVIDENCIA POSTERIOR.
- La responsabilidad queda registrada por rol y fecha.
- La falta de aprobación no elimina la trazabilidad del costo.

---

## 12. Reglas de Violación

Se considera violación grave del sistema si:

- Un Presupuesto CONGELADO es modificado directamente.
- Se ejecuta un Proyecto sin Presupuesto CONGELADO.
- Se imputan costos sin Partida válida.
- Se exceden límites legales sin Orden de Cambio válida.
- Se elimina o altera un Snapshot histórico.

Estas violaciones deben tratarse como errores de dominio,
no como excepciones técnicas.

## 13. Estructura Económica del Presupuesto (ENDURECIMIENTO)

El Presupuesto define la estructura económica completa del contrato,
no únicamente los costos directos de ejecución.

El Presupuesto se compone, como mínimo, de:

- Costos Directos (Partidas + APU)
- Costos Indirectos del Proyecto
- Gastos Generales / Administrativos
- Impuestos aplicables (IVA)
- Utilidad esperada

Todos estos componentes deben quedar explícitos
en el Presupuesto CONGELADO.

---

## 14. Costos Indirectos del Proyecto

### 14.1 Definición

Se consideran Costos Indirectos del Proyecto aquellos necesarios
para la ejecución de la obra, pero no imputables a una Partida específica.

Ejemplos:
- supervisión de obra,
- seguridad,
- bodegaje,
- energía temporal,
- alquiler de equipos comunes,
- señalización,
- seguros del proyecto.

---

### 14.2 Reglas de Gestión

- Los Costos Indirectos:
  - NO se descomponen en APU,
  - NO se imputan a Partidas,
  - SÍ impactan el BAC y el AC.

- Deben presupuestarse como partidas indirectas globales
  o como un bloque económico identificado.

---

### 14.3 Ejecución

- El consumo de costos indirectos:
  - impacta directamente el Costo Real (AC),
  - afecta CPI y utilidad,
  - no reduce avance físico (EV).

El sistema debe distinguir claramente:
- ejecución con avance,
- ejecución sin avance.

---

## 15. Gastos Generales y Administrativos

### 15.1 Definición

Los Gastos Generales corresponden a costos de estructura empresarial
asignados al Proyecto.

Ejemplos:
- administración central,
- contabilidad,
- dirección técnica,
- costos financieros,
- logística corporativa.

---

### 15.2 Reglas del Sistema

- Los Gastos Generales:
  - NO forman parte de las Partidas,
  - NO generan EV,
  - SÍ afectan la utilidad del Proyecto.

- Su imputación puede ser:
  - porcentual sobre el contrato, o
  - monto fijo definido al congelar el Presupuesto.

---

### 15.3 Control

- Los Gastos Generales deben:
  - estar definidos en el Presupuesto CONGELADO,
  - tener trazabilidad temporal,
  - ser visibles en el análisis de utilidad.

El sistema no los oculta ni los diluye.

---

## 16. Impuestos (IVA) — Tratamiento Canónico

### 16.1 Principio

El IVA NO es un costo del Proyecto.
Es un impuesto trasladable.

El sistema debe distinguir claramente entre:
- precio contractual,
- costo real,
- impuestos.

---

### 16.2 Reglas de Gestión

- El IVA:
  - NO impacta AC,
  - NO impacta CPI,
  - NO impacta EV,
  - SÍ impacta flujo de caja.

- El Presupuesto debe definir:
  - si el monto contractual es con o sin IVA,
  - la tasa aplicable,
  - el tratamiento fiscal.

---

### 16.3 Auditoría

El IVA debe ser:
- visible,
- separado,
- exportable a sistemas contables.

BudgetPro no liquida impuestos,
pero tampoco los ignora.

---

## 17. Utilidad del Proyecto (ENDURECIMIENTO CRÍTICO)

### 17.1 Definición

La Utilidad es un objetivo contractual explícito,
no un residuo contable.

Utilidad = Precio Contractual
           – Costos Directos
           – Costos Indirectos
           – Gastos Generales

---

### 17.2 Reglas del Sistema

- La Utilidad esperada debe:
  - declararse al congelar el Presupuesto,
  - expresarse como monto y/o porcentaje.

- Toda desviación en costos:
  - reduce utilidad,
  - debe ser visible en tiempo real.

---

### 17.3 Protección de Utilidad

BudgetPro existe para:

- exponer la erosión de la utilidad,
- mostrar su causa,
- advertir tempranamente su pérdida.

El sistema no promete utilidad.
Promete visibilidad brutal de su deterioro.

---

## 18. Relación con EVM y Control

- El EVM mide desempeño sobre costos directos y tiempo.
- La utilidad mide resultado económico final.

Un proyecto puede:
- tener SPI y CPI aceptables,
- y aun así perder utilidad
  por indirectos, gastos generales o impuestos mal gestionados.

El sistema debe mostrar esta diferencia sin ambigüedad.

