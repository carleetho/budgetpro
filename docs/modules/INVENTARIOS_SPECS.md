# REGLAS DE NEGOCIO — MÓDULO INVENTARIOS (BODEGA)

## 1. Propósito del Módulo

El módulo INVENTARIOS controla la existencia física de los recursos
adquiridos para un Proyecto.

Su función es garantizar que:
- todo material tenga origen contractual,
- toda salida tenga destino técnico,
- ninguna pérdida sea silenciosa.

El inventario es un medio operativo, no un activo contable.

---

## 2. Condiciones de Existencia

Un movimiento de inventario solo puede existir si:

- el Proyecto está en estado ACTIVO,
- existe un Presupuesto CONGELADO vigente,
- el material proviene de una Compra válida,
- la salida se imputa a una Partida específica.

<!-- AJUSTE POR PROPAGACIÓN DESDE COMPRAS -->
- Si el material proviene de una Compra clasificada como:
  - Gasto General de Obra, o
  - Gasto Administrativo,
  la salida NO se imputa a Partida,
  pero debe mantener clasificación obligatoria.

Si alguna condición falla, el movimiento es ilegal.

---

## 3. Tipos de Movimiento

### 3.1 Entrada a Inventario

- Toda entrada debe originarse en una Compra válida.
- La entrada debe reflejar:
  - material,
  - cantidad,
  - unidad,
  - fecha,
  - referencia de compra.

No existe entrada manual sin compra.

<!-- AJUSTE POR PROPAGACIÓN DESDE COMPRAS -->
- La entrada hereda la clasificación de la Compra
  (naturaleza del gasto y rubro),
  habilitando análisis posterior.

---

### 3.2 Salida de Inventario

- Toda salida debe realizarse mediante una Requisición.
- La Requisición debe indicar:
  - Partida de destino,
  - cantidad solicitada,
  - responsable de la solicitud.

Salida sin Partida es ilegal.

<!-- AJUSTE POR PROPAGACIÓN DESDE TIEMPO -->
- La salida debe correlacionarse con:
  - la Actividad asociada,
  - su Ventana Temporal.
- Salidas fuera de ventana:
  - NO se bloquean,
  - generan alerta de riesgo operativo.

---

## 4. Relación con Partidas y APU

- Las salidas de inventario reducen el saldo disponible
  del APU correspondiente.
- El sistema debe validar que la cantidad solicitada
  no exceda los límites contractuales,
  salvo Excepción formal registrada.

<!-- AJUSTE POR PROPAGACIÓN DESDE COMPRAS -->
- Los límites contractuales operan como:
  - referencia técnica,
  - sistema de alerta,
  - no como bloqueo automático.

---

### 4.1 Naturaleza del “saldo disponible” (CLARIFICACIÓN CANÓNICA)

El término “saldo disponible del APU” se utiliza **exclusivamente como referencia técnica**.

Alcance explícito:
- INVENTARIOS **NO calcula** saldo disponible contractual.
- INVENTARIOS **NO mantiene** una cuenta de disponibilidad económica.
- INVENTARIOS **NO bloquea** salidas por sobreconsumo.

Uso permitido del concepto:
- Referencia para alertas técnicas.
- Comparación contra cantidades esperadas del APU.
- Detección de riesgos operativos.

Cualquier definición de saldo contractual o cuantificación económica de exceso:
- no pertenece a INVENTARIOS,
- requiere definición canónica en otro dominio.

Esta aclaración evita inferencias automáticas y cierra VD-02 / RS-03 / FG-03.


## 5. Multi-Bodega

- Un Proyecto puede manejar múltiples bodegas físicas.
- Todo movimiento debe indicar bodega de origen o destino.
- La existencia total del Proyecto es la suma de sus bodegas.

La trazabilidad por bodega es obligatoria.

<!-- AJUSTE POR PROPAGACIÓN DESDE MULTIPROYECTO -->
- No se permite mezclar inventario entre Proyectos
  sin movimiento explícito de transferencia.
- Transferencias entre Proyectos:
  - requieren Excepción formal,
  - impactan AC y utilidad de ambos Proyectos.

---

## 6. Impacto en Costos

- La salida de inventario es el momento de imputación
  del costo real (AC) a la Partida.
- El costo imputado corresponde al costo de compra,
  no al precio presupuestado.

El sistema no espera al consumo final para registrar el costo.

<!-- AJUSTE POR PROPAGACIÓN DESDE EVM -->
- Todo AC generado en inventarios
  alimenta directamente los cálculos EVM.
- Inventario sin salida no genera EV.

---

## 7. Relación con Compras

- Toda Compra válida debe generar inventario.
- No se permite consumo directo desde compra
  sin pasar por inventario,
  salvo servicios explícitamente definidos.

Compra sin inventario es ilegal.
Inventario sin compra es ilegal.

<!-- AJUSTE POR PROPAGACIÓN DESDE COMPRAS -->
- Compras por adelantamiento logístico:
  - son válidas,
  - permanecen en inventario,
  - afectan AC aunque no se consuman de inmediato.

---

## 8. Control de Pérdidas

- Diferencias entre inventario físico y sistema
  deben registrarse como Excepción.
- Las mermas no justificadas deterioran métricas
  y deben ser visibles.

<!-- AJUSTE POR PROPAGACIÓN DESDE EVM -->
- Las pérdidas impactan directamente:
  - AC,
  - CPI,
  - utilidad final.

El sistema no oculta pérdidas.
Las expone.

---

## 9. Auditoría y Evidencia

Todo movimiento de inventario debe registrar:

- tipo de movimiento,
- material,
- cantidad,
- bodega,
- Partida asociada (en salidas),
- usuario responsable,
- fecha y hora.

No se permiten ajustes anónimos.

---

## 10. Principios de Diseño No Negociables

- El inventario introduce fricción operativa intencional.
- La rapidez de salida no justifica la falta de trazabilidad.
- El sistema prefiere bloquear una requisición
  antes que permitir una pérdida silenciosa.

---

## 11. Criterios de Violación

Se considera violación grave del sistema si:

- se permite salida sin Partida,
- se permite entrada sin Compra,
- se imputa costo sin salida registrada,
- se ajusta inventario sin Excepción formal.

Estas violaciones invalidan la confiabilidad operativa del sistema.
