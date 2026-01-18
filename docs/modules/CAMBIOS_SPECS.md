# REGLAS DE NEGOCIO — ÓRDENES DE CAMBIO Y EXCEPCIONES

## 1. Propósito del Módulo

El módulo CAMBIOS gobierna las desviaciones respecto a la Línea Base
sin permitir la pérdida de trazabilidad ni la reescritura de la historia.

Este módulo existe para aceptar que la realidad cambia,
pero exige que cada cambio quede explicado, autorizado y medido.

---

## 2. Principio Rector

La Línea Base nunca se modifica.

Toda desviación se gestiona mediante capas formales que:
- se suman o restan,
- quedan trazadas,
- impactan métricas,
- preservan el contrato original.

---

## 3. Tipos de Desviación

### 3.1 Orden de Cambio (Contractual)

Una Orden de Cambio representa una modificación formal del alcance,
precio o cantidad contractual del Proyecto.

#### Características:
- Requiere aprobación explícita.
- Impacta el Presupuesto contractual.
- Ajusta el BAC (Budget at Completion).
- Modifica los denominadores de control (EVM).

#### Usos típicos:
- aumento de cantidades por rediseño,
- trabajos adicionales solicitados por el cliente,
- cambios de especificación técnica.

---

### 3.2 Excepción (Operativa)

Una Excepción representa un gasto no previsto
que no modifica el contrato, pero sí la ejecución.

#### Características:
- No modifica la Línea Base.
- No ajusta el BAC.
- Impacta el Costo Real (AC).
- Afecta negativamente métricas de desempeño (CPI).

#### Usos típicos:
- desperdicio por error humano,
- reprocesos,
- compras de emergencia,
- fallas logísticas.

---

## 4. Proceso de Registro

### 4.1 Requisitos Mínimos (Obligatorios)

Toda Orden de Cambio o Excepción debe registrar:

- tipo de desviación,
- partida afectada (si aplica),
- monto económico,
- motivo clasificado,
- usuario responsable,
- fecha y hora,
- autorización correspondiente.

Si alguno de estos datos falta,
el sistema DEBE rechazar el registro.

---

## 5. Impacto en el Presupuesto

### 5.1 Órdenes de Cambio

- Se agregan como capas al Presupuesto original.
- Ajustan el BAC vigente.
- El Presupuesto original permanece visible e intacto.

---

### 5.2 Excepciones

- No alteran el Presupuesto.
- Se acumulan contra el Costo Real.
- Son visibles como pérdida operativa.

---

## 6. Impacto en Métricas (EVM)

- Las Órdenes de Cambio recalculan:
  - BAC,
  - EAC,
  - TCPI.

- Las Excepciones:
  - incrementan AC,
  - deterioran CPI,
  - no ajustan SPI ni BAC.

El sistema debe reflejar claramente esta diferencia.

---

## 7. Taxonomía del Error (Clasificación)

Toda Excepción debe clasificarse en al menos una categoría:

- error de planificación,
- error de ejecución,
- desperdicio,
- reproceso,
- emergencia operativa,
- causa externa.

Esta clasificación construye
un historial reputacional del Proyecto y del equipo.

---

## 8. Relación con Otros Módulos

### Presupuesto
- Ninguna desviación puede sobrescribir la Línea Base.

### Compras
- Excesos de compra solo se permiten vía:
  - Orden de Cambio, o
  - Excepción formal.

### Proyecto
- Un volumen alto de Excepciones puede gatillar alertas de salud crítica.

---

## 9. Auditoría y No Repudio

- Toda desviación es permanente.
- No puede eliminarse ni editarse.
- Solo puede anularse mediante un evento correctivo trazado.

El sistema debe ser capaz de responder:
> “¿Quién autorizó esta pérdida y por qué?”

---

## 10. Criterios de Violación

Se considera violación grave del sistema si:

- Se modifica un Presupuesto CONGELADO sin Orden de Cambio.
- Se oculta una Excepción dentro de otro proceso.
- Se permite ejecutar excesos sin registro formal.
- Se elimina o altera una desviación histórica.

Estas violaciones invalidan la credibilidad del sistema.

## 11. Límites Legales de las Órdenes de Cambio (ENDURECIMIENTO LEGAL)

Las Órdenes de Cambio están sujetas a los límites establecidos
por la Ley de Compras Públicas vigente.

Regla canónica del sistema:

- El monto acumulado de Órdenes de Cambio
  no puede exceder ±20 % del monto contractual original congelado.

El sistema DEBE:

- calcular el porcentaje acumulado de modificaciones,
- bloquear Órdenes de Cambio que excedan el límite legal,
- exigir referencia normativa y justificación formal
  para cualquier intento de exceso.

El límite legal es acumulativo y permanente.

---

## 12. Aprobación Asíncrona y Evidencia Posterior

El sistema reconoce que los actores con autoridad contractual
no siempre operan dentro de BudgetPro.

Reglas del sistema:

- Una Orden de Cambio puede registrarse en estado:
  - PROPUESTA,
  - APROBADA CON EVIDENCIA DIFERIDA,
  - RECHAZADA.

- La aprobación puede sustentarse mediante:
  - documento firmado,
  - resolución administrativa,
  - acta,
  - comunicación oficial adjunta.

La ausencia de aprobación en sistema
no elimina la trazabilidad ni la responsabilidad.

---

## 13. Impacto en Alcance del Proyecto

Una Orden de Cambio puede:

- agregar nuevas Partidas,
- modificar cantidades existentes,
- eliminar Partidas previamente definidas.

Reglas:

- Toda Partida agregada por Orden de Cambio:
  - debe tener APU propio,
  - queda vinculada a la Orden de Cambio,
  - impacta BAC y ejecución futura.

- Las Partidas eliminadas:
  - no se borran,
  - se marcan como desactivadas,
  - permanecen visibles para auditoría.

---

## 14. Impacto en Plazo Contractual (PRÓRROGAS)

Las Órdenes de Cambio pueden modificar el plazo contractual.

Además, el sistema reconoce la figura de PRÓRROGA DE TIEMPO,
aun cuando no implique modificación económica.

Reglas:

- Toda modificación de plazo:
  - debe registrarse formalmente,
  - debe quedar vinculada a una Orden de Cambio
    o a una Prórroga independiente.

- La Prórroga:
  - ajusta el Cronograma congelado,
  - redefine la Línea Base temporal,
  - impacta PV, SPI y TCPI.

No existe prórroga informal en el sistema.

---

## 15. Relación con Compras e Inventarios

Las Órdenes de Cambio pueden generar:

- compras adicionales,
- nuevos insumos,
- aumento o reducción de inventarios,
- desactivación de materiales ya adquiridos.

Reglas del sistema:

- Toda compra derivada de una Orden de Cambio
  debe vincularse explícitamente a dicha Orden.
- El inventario generado conserva trazabilidad
  respecto a la desviación que lo originó.

Inventario sin contexto contractual es ilegal.

---

## 16. Excesos de Compra y Consumo (REALIDAD AMPLIADA)

El exceso de compra o consumo puede ocurrir por múltiples causas,
independientes de la intención contractual.

Reglas:

- El exceso puede registrarse como:
  - Excepción operativa, o
  - Insumo asociado a Orden de Cambio posterior.

- Todo exceso:
  - incrementa AC,
  - reduce utilidad,
  - deteriora indicadores de desempeño.

El sistema no impide el exceso.
Obliga a explicarlo y medirlo.

---

## 17. Relación con el Cronograma y EVM

Toda Orden de Cambio o Prórroga:

- altera el contexto temporal del Proyecto,
- redefine el Valor Planificado (PV),
- afecta el cálculo de SPI y TCPI.

El sistema DEBE recalcular métricas
a partir de la Línea Base ajustada,
manteniendo historial de estados anteriores.

---

## 18. Principio de No Compensación

Las Órdenes de Cambio y Excepciones
no pueden compensarse entre sí para ocultar pérdidas.

Ejemplo prohibido:
- justificar una Excepción con una ODC posterior
  sin dejar trazabilidad explícita.

Cada evento conserva identidad propia.

---

## 19. Violaciones Específicas del Módulo CAMBIOS

Se considera violación grave del sistema si:

- se exceden límites legales sin respaldo normativo,
- se modifica plazo sin registro formal,
- se ejecutan compras derivadas de cambios no registrados,
- se eliminan partidas sin dejar evidencia histórica,
- se oculta una Excepción como Orden de Cambio.

Estas violaciones invalidan la confiabilidad contractual del sistema.
