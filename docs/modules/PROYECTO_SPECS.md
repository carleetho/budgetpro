# REGLAS DE NEGOCIO — MÓDULO PROYECTO

## 1. Propósito del Módulo

El módulo PROYECTO define el contenedor contractual donde se ejecuta una obra.

Un Proyecto NO es un contenedor administrativo.
Un Proyecto es una entidad contractual que habilita o bloquea la ejecución
según el estado del presupuesto asociado.

Este módulo actúa como el primer filtro disciplinario del sistema.

---

## 2. Existencia del Proyecto

### 2.1 Creación

- El sistema permite crear un Proyecto en estado BORRADOR sin presupuesto.
- Un Proyecto en BORRADOR no puede:
  - ejecutar compras,
  - registrar inventarios,
  - registrar mano de obra,
  - registrar avances físicos,
  - generar costos reales (AC).

La creación de un Proyecto no implica autorización de ejecución.

---

### 2.2 Activación Contractual

- Un Proyecto solo puede pasar a estado ACTIVO si:
  - existe al menos un Presupuesto asociado,
  - dicho Presupuesto se encuentra en estado CONGELADO,
  - existe un Snapshot inmutable de la Línea Base.

Si estas condiciones no se cumplen, el sistema DEBE bloquear la activación.

Mensaje obligatorio al usuario:
> "Este proyecto no puede activarse sin un presupuesto congelado."

---

### 2.3 Línea Base Contractual (ENDURECIMIENTO CANÓNICO)

La Línea Base del Proyecto está compuesta obligatoriamente por:

- Presupuesto CONGELADO
- Cronograma CONGELADO

Ambos elementos son indivisibles.
La ausencia de cualquiera de ellos invalida la ejecución del Proyecto.

Un Snapshot de Presupuesto sin Cronograma
NO constituye una Línea Base válida.

---

### 2.4 Condición Temporal de Activación (ENDURECIMIENTO)

Además del Presupuesto congelado, un Proyecto solo puede pasar a estado ACTIVO si:

- existe un Cronograma asociado,
- dicho Cronograma se encuentra en estado CONGELADO,
- el Cronograma pertenece al mismo Proyecto.

Si no existe Cronograma congelado,
el sistema DEBE bloquear la activación del Proyecto.

Mensaje obligatorio al usuario:
> "Este proyecto no puede activarse sin un cronograma congelado."

---

## 3. Estados del Proyecto (Semántica Canónica)

El módulo PROYECTO maneja los siguientes estados con significado contractual:

### BORRADOR
- Proyecto sin contrato digital.
- Uso permitido: preparación administrativa y carga de presupuesto.
- No habilita ejecución.

### ACTIVO
- Proyecto con Línea Base congelada (Presupuesto + Cronograma).
- Habilita ejecución completa del sistema.
- Todo costo imputado a este proyecto se considera contractual y temporalmente válido.

### SUSPENDIDO
- Proyecto con ejecución detenida.
- No permite nuevas compras, planillas ni movimientos de inventario.
- Se utiliza por causas legales, financieras o administrativas formales.

### CERRADO
- Proyecto finalizado.
- No permite ninguna modificación operativa.
- Solo admite consultas, reportes y auditoría histórica.

---

## 4. Reglas de Transición de Estado

Las transiciones permitidas son:

- BORRADOR → ACTIVO  
  (solo si existe Presupuesto y Cronograma congelados)

- ACTIVO → SUSPENDIDO  
  (decisión administrativa formal)

- ACTIVO → CERRADO  
  (cuando toda ejecución ha finalizado)

- SUSPENDIDO → ACTIVO  
  (solo si Presupuesto y Cronograma siguen vigentes)

Transiciones prohibidas:
- BORRADOR → CERRADO
- CERRADO → cualquier otro estado

El sistema debe rechazar cualquier transición no autorizada.

---

## 5. Relación con Otros Módulos

### 5.1 Presupuesto

- Todo Proyecto ACTIVO debe tener un Presupuesto congelado vigente.
- Si el Presupuesto principal se invalida, el Proyecto debe pasar a SUSPENDIDO automáticamente.

---

### 5.2 Cronograma (DEPENDENCIA EXPLÍCITA)

- Todo Proyecto ACTIVO debe tener un Cronograma congelado vigente.
- El Cronograma define:
  - ventanas temporales válidas de ejecución,
  - contexto temporal del costo,
  - base del Valor Planificado (PV).

Sin Cronograma congelado:
- no existe PV válido,
- no existe SPI válido,
- no existe ejecución temporalmente controlada.

---

### 5.3 Compras, Inventarios y RRHH

- Ningún módulo operativo puede ejecutar acciones si el Proyecto no está en estado ACTIVO.
- Toda ejecución debe ocurrir dentro del marco temporal definido por el Cronograma.
- Todo costo generado debe referenciar un Proyecto ACTIVO.

---

### 5.4 Auditoría

- Todo cambio de estado del Proyecto debe quedar registrado:
  - estado anterior,
  - estado nuevo,
  - usuario responsable,
  - fecha y hora,
  - motivo.

Los estados no son reversibles sin evidencia formal.

---

## 6. Principios de Diseño No Negociables

- El módulo PROYECTO introduce fricción temprana.
- La facilidad de creación nunca debe comprometer la disciplina contractual.
- Un Proyecto ACTIVO implica responsabilidad financiera inmediata.
- Un Proyecto ACTIVO sin Cronograma es inválido.
- El sistema prefiere bloquear antes que permitir ejecución sin Línea Base completa.

---

## 7. Criterios de Violación

Se considera una violación grave del sistema si:

- Un Proyecto ACTIVO no tiene Presupuesto congelado.
- Un Proyecto ACTIVO no tiene Cronograma congelado.
- Un Proyecto en BORRADOR permite ejecución operativa.
- Se permite modificar estados históricos sin registro.
- Otros módulos ignoran el estado del Proyecto o su Línea Base.

Estas violaciones deben tratarse como errores de dominio, no como fallos técnicos.
