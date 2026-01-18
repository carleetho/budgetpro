# REGLAS DE NEGOCIO — MÓDULO COMPRAS (PROCUREMENT)

## 1. Propósito del Módulo

El módulo COMPRAS registra la adquisición real de bienes y servicios
necesarios para la ejecución de un Proyecto.

No interpreta el Presupuesto.
No corrige errores de planificación.
No decide desviaciones contractuales.

Su función es capturar la realidad económica,
clasificarla correctamente
y exponer su impacto financiero y operativo.

---

## 2. Condiciones de Existencia

Una compra solo puede registrarse si:

- el Proyecto está en estado ACTIVO,
- existe un Presupuesto CONGELADO vigente.

La compra **NO se rechaza** por falta de Partida o APU.
La compra **SI debe clasificarse** obligatoriamente.

El sistema no borra realidad económica.

---

## 3. Clasificación Obligatoria de Compras (NÚCLEO DEL MÓDULO)

Toda compra DEBE clasificarse en los siguientes ejes:

### 3.1 Naturaleza del Gasto

- Gasto Directo de Partida
- Gasto General de Obra
- Gasto Administrativo

No se permiten compras sin naturaleza definida.

---

### 3.2 Relación Contractual

- Contractual (dentro de Presupuesto)
- Por Orden de Cambio
- Por Excepción Operativa

---

### 3.3 Rubro / Tipo de Insumo

Ejemplos:
- Material de construcción
- Mano de obra indirecta
- Equipo / herramienta
- Combustible
- EPP
- Servicios
- Logística

Esta clasificación habilita análisis estadístico y control gerencial.

---

## 4. Relación con Partidas y APU

### 4.1 Compras Asociadas a Partida

- Pueden vincularse a:
  - una Partida contractual,
  - su APU correspondiente.
- Impactan ejecución técnica directa.

---

### 4.2 Compras NO Asociadas a Partida

Ejemplos:
- EPP,
- agua potable,
- sanitarios,
- vigilancia,
- combustible,
- logística.

Estas compras:
- NO son ilegales,
- NO habilitan ejecución contractual,
- impactan directamente la utilidad.

Compra sin Partida es válida.
Compra sin clasificación es ilegal.

---

## 5. Purchasing Caps (LÍMITES COMO ALERTA, NO BLOQUEO)

El APU define consumos técnicos esperados.

Reglas:

- Superar el tope:
  - NO bloquea la compra,
  - NO genera Orden de Cambio automática.
- El sistema DEBE:
  - emitir alerta técnica,
  - cuantificar el exceso,
  - exigir motivo clasificado.

Ejemplo:
> “Consumo 150 % superior al esperado. Verifique desperdicio, reproceso o error.”

BudgetPro **no propone** Órdenes de Cambio.

---

## 5.1 Alcance del control de consumo (CLARIFICACIÓN CANÓNICA)

El módulo COMPRAS **NO calcula** saldo disponible por Partida ni por APU.

Alcance explícito:
- COMPRAS registra el **hecho económico real**.
- COMPRAS compara consumos **solo contra referencias técnicas** del APU.
- COMPRAS **NO posee** una fuente canónica de “saldo disponible contractual”.

Consecuencias:
- El sistema **NO bloquea** compras por exceso de consumo.
- El sistema **NO infiere** fórmulas de disponibilidad.
- El sistema **SÍ emite alertas** cuando el consumo supera referencias técnicas declaradas.

Cualquier cuantificación contractual de sobreconsumo:
- queda fuera del alcance de este módulo,
- requiere definición explícita en dominio PRESUPUESTO / CAMBIOS.

Esta aclaración cierra los vacíos VD-02 / RS-03 / FG-03 sin introducir reglas nuevas.

## 6. Flujo de Autorización

El sistema reconoce que:

- aprobaciones pueden ser posteriores,
- supervisores pueden no usar el sistema.

Reglas:

- La compra puede registrarse como:
  - PENDIENTE DE APROBACIÓN,
  - EJECUTADA CON EVIDENCIA POSTERIOR.
- La responsabilidad queda registrada por:
  - usuario,
  - rol,
  - fecha.

La trazabilidad no depende de la disciplina externa.

---

## 7. Impacto Financiero (CORREGIDO)

### 7.1 Precio Contractual vs Precio Real

- El APU congela precios contractuales estimados.
- El mercado impone precios reales.

Reglas:

- El Precio Contractual:
  - permanece inmutable,
  - sirve como referencia.
- El Precio Real:
  - se registra al momento de la compra,
  - incrementa el Costo Real (AC).

La diferencia representa:
- variación de mercado,
- pérdida o ganancia real,
- impacto directo en utilidad.

---

### 7.2 Momento del Costo

- El costo se reconoce cuando:
  - la compra ocurre,
  - el bien o servicio se recibe.

El pago posterior afecta flujo de caja,
no la existencia del costo.

---

## 8. Órdenes de Compra y Facturación Múltiple

Una Orden de Compra:

- puede ejecutarse con múltiples proveedores,
- puede generar múltiples facturas,
- puede completarse en fases.

Reglas:

- Cada factura:
  - registra proveedor, fecha y monto,
  - se asocia a la Orden de Compra.
- La Orden se considera cerrada solo cuando:
  - se completa,
  - se cancela formalmente.

---

## 9. Relación con Inventarios

- Toda compra de bienes físicos
  genera entrada a inventario.
- Servicios y gastos indirectos
  pueden no generar inventario.

Inventario sin compra es ilegal.
Compra sin inventario físico debe justificarse.

---

## 10. Relación con el Cronograma (CONTROL TEMPORAL)

El sistema debe advertir si:

- se compran insumos de actividades no iniciadas,
- se consumen insumos de actividades ya finalizadas.

Estas situaciones:
- NO bloquean la compra,
- generan alertas de riesgo operativo.

Comprar fuera de tiempo es visible, no oculto.

---

## 11. Auditoría y Evidencia

Toda compra debe registrar como mínimo:

- proveedor,
- fecha,
- monto,
- usuario responsable,
- clasificación completa,
- impacto estimado en utilidad,
- alertas generadas.

El sistema debe responder:
> “¿Qué se compró, por qué, cuándo y con qué impacto?”

---

## 12. Principios de Diseño No Negociables

- El módulo COMPRAS no miente.
- No bloquea la realidad.
- No automatiza decisiones humanas.
- Prefiere alertar antes que impedir.

---

## 13. Criterios de Violación

Se considera violación grave si:

- se oculta una compra real,
- se elimina evidencia de exceso,
- se fuerza Orden de Cambio automática,
- se permite comprar sin clasificación,
- se falsea impacto en utilidad.

Estas violaciones destruyen la credibilidad financiera del sistema.
