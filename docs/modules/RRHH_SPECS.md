# REGLAS DE NEGOCIO — MÓDULO RECURSOS HUMANOS (RRHH)

## 1. Propósito del Módulo

El módulo RRHH gestiona la Mano de Obra como un recurso productivo,
temporal y multiproyecto, directamente imputable
a la ejecución del Presupuesto.

Un obrero no es un gasto administrativo por defecto.
Es un Recurso de Partida cuando ejecuta obra.

Toda Mano de Obra debe impactar el Costo Real (AC)
y, por ende, la utilidad del Proyecto.

---

## 2. Principio Rector

La Mano de Obra es costo real, no estimación.

Todo tiempo trabajado:
- cuesta,
- deja rastro,
- afecta utilidad,
aunque no haya pago inmediato.

---

## 3. Tipos de Personal (CLASIFICACIÓN OBLIGATORIA)

Todo recurso humano debe clasificarse como uno de los siguientes:

### 3.1 Personal de Campo (Productivo)
Ejemplos:
- obreros,
- ayudantes,
- maestros de obra,
- operadores.

Regla:
- Se imputa directamente a Partidas del Presupuesto.
- Ejecuta APUs.

---

### 3.2 Personal de Oficina de Proyecto
Ejemplos:
- residente,
- secretaria de obra,
- supervisión local.

Regla:
- Puede imputarse como:
  - Gasto General de Obra, o
  - Recurso indirecto de Partida (si así se define).
- Impacta AC del Proyecto.

---

### 3.3 Personal Administrativo Central
Ejemplos:
- contabilidad,
- RRHH,
- gerencia general.

Regla:
- Se clasifica como Gasto Administrativo.
- Puede prorratearse entre Proyectos.
- Impacta utilidad global.

---
## 3.4 Subcontratos y Destajos (EJECUCIÓN ALTERNATIVA DEL APU)

El sistema reconoce que una Partida puede ejecutarse mediante:

- Mano de Obra propia (jornal / horas),
- Mano de Obra por destajo,
- Subcontrato formal.

Un Subcontrato NO es un proveedor genérico.
Es un ejecutor directo del APU.

---

### 3.4.1 Definición de Subcontrato

Un Subcontrato representa la ejecución total o parcial
de una Partida específica por un tercero.

Características:

- Está vinculado obligatoriamente a:
  - un Proyecto ACTIVO,
  - una Partida del Presupuesto,
  - un APU contractual.
- Tiene:
  - alcance definido,
  - unidad de medición,
  - precio pactado,
  - plazo asociado.

---

### 3.4.2 Relación con el APU

Reglas:

- El Subcontrato ejecuta:
  - la totalidad del APU, o
  - un componente específico (mano de obra, equipo, rendimiento).
- El costo del Subcontrato:
  - reduce el saldo disponible de la Partida,
  - impacta directamente el AC.

No se permite Subcontrato sin Partida.

---

### 3.4.3 Modalidades Admitidas

El sistema debe soportar como mínimo:

- Subcontrato por unidad de obra,
- Subcontrato por precio global,
- Destajo por avance físico.

Cada modalidad debe registrar:

- unidad contractual,
- precio pactado,
- avance ejecutado,
- monto devengado.

---

### 3.4.4 Registro de Avance de Subcontrato

Reglas:

- El avance del Subcontrato:
  - se registra por unidad ejecutada,
  - se vincula a la Partida correspondiente.
- El costo devengado:
  - impacta AC al momento del avance,
  - independientemente del pago.

Pago ≠ Costo.

---

### 3.4.5 Control de Desviaciones en Subcontratos

Reglas:

- Si el costo del Subcontrato:
  - excede el costo contractual del APU,
  el sistema DEBE:
    - emitir alerta de sobrecosto,
    - reflejar pérdida de utilidad.

- Si el Subcontrato:
  - extiende plazo,
  - modifica alcance,
  debe gestionarse mediante:
    - Orden de Cambio formal.

El sistema no corrige negociaciones deficientes.
Las expone.

---

### 3.4.6 Subcontratos y Cronograma

Reglas:

- Todo Subcontrato debe:
  - tener ventana temporal definida,
  - estar alineado al cronograma contractual.
- Avances fuera de ventana:
  - no se bloquean,
  - generan alertas de desviación de tiempo.

---

### 3.4.7 Subcontrato vs Compra

Diferenciación canónica:

- Subcontrato:
  - ejecuta trabajo,
  - consume Partida,
  - ejecuta APU.
- Compra:
  - adquiere insumos,
  - no ejecuta obra.

Confundir ambos es violación de dominio.


## 4. Costo Empresa y Factor de Prestación (OBLIGATORIO)

El costo de Mano de Obra **NUNCA** se registra como salario neto.

Reglas:

- Todo trabajador debe tener definido:
  - salario base,
  - prestaciones previsionales,
  - cargas sociales,
  - otros beneficios obligatorios.
- El sistema calcula un **Costo Empresa Diario/Hora**.

El factor de prestación es parte del contrato interno del sistema.
Omitirlo constituye una falsificación del costo.

---

## 5. Asignación de Personal a Proyectos (MULTIPROYECTO)

Reglas:

- Un trabajador puede:
  - rotar entre proyectos,
  - ser reasignado,
  - alternarse en el tiempo.
- No puede estar asignado
  a dos Proyectos ACTIVO el mismo día
  en el mismo horario.

La asignación es:
- por Proyecto,
- por período,
- por rol.

---

## 6. Registro de Trabajo (TAREO)

### 6.1 Registro Diario

El tareo registra:

- trabajador,
- Proyecto,
- Partida (si aplica),
- fecha,
- cantidad:
  - horas,
  - jornal,
  - horas extra.

Registro sin clasificación es ilegal.

---

### 6.2 Validaciones

El sistema debe validar:

- Proyecto ACTIVO,
- trabajador asignado,
- coherencia de fechas,
- no duplicidad horaria.

Errores bloquean el registro.
Excesos generan alertas, no bloqueo.

---

## 7. Horas Extra y Jornadas Especiales

Reglas:

- Las horas extra deben clasificarse como:
  - diurnas,
  - nocturnas,
  - extraordinarias.
- Cada tipo tiene:
  - factor de recargo,
  - impacto distinto en AC.

Las horas extra:
- no son implícitas,
- no se asumen automáticamente,
- siempre deben justificarse.

---

## 8. Adelantos Salariales (REALIDAD OPERATIVA)

El sistema reconoce adelantos como práctica real.

Reglas:

- Un adelanto:
  - no elimina el costo,
  - no altera el AC,
  - afecta solo flujo de caja.
- Debe registrarse:
  - trabajador,
  - monto,
  - fecha,
  - período a descontar.

El adelanto no borra evidencia de costo ya incurrido.

---

## 9. Relación con el Presupuesto (EJECUCIÓN DE APU)

Reglas:

- La Mano de Obra ejecuta el APU contractual.
- El consumo de horas/jornales:
  - reduce el saldo disponible de la Partida.
- Sobreconsumo por:
  - atrasos,
  - clima,
  - bajo rendimiento,
  - reprocesos,
  debe registrarse como:
  - Excepción operativa,
  - pérdida de productividad.

El sistema no corrige el error.
Lo expone y lo cuantifica.

---

## 10. Relación con el Cronograma (CONTROL TEMPORAL)

Reglas:

- El tareo debe correlacionarse
  con la ventana temporal de la actividad.
- Si se registra trabajo en:
  - actividades finalizadas,
  - actividades no iniciadas,
el sistema DEBE:
  - emitir alerta,
  - advertir riesgo de desvío de tiempo y costo.

No bloquea.
Hace visible la incoherencia.

---

## 11. Planilla y Cierre de Costos

Reglas:

- La planilla se genera únicamente
  desde tareos registrados.
- El cierre de planilla:
  - impacta inmediatamente el AC,
  - independientemente del pago.
- El pago posterior
  afecta solo caja, no costo.

No existe planilla manual.

---

## 12. Auditoría y Evidencia

Toda imputación de RRHH debe registrar:

- trabajador,
- tipo de personal,
- Proyecto,
- Partida o clasificación,
- fecha,
- horas/jornales,
- tipo de jornada,
- usuario responsable.

No se permiten ajustes retroactivos
sin Excepción formal.

---

## 13. Principios de Diseño No Negociables

- RRHH no miente sobre costos.
- No suaviza la realidad laboral.
- No oculta ineficiencia.
- Prefiere alertar antes que maquillar.

---

## 14. Criterios de Violación

Se considera violación grave si:

- se omite el factor de prestación,
- se registra trabajo sin asignación válida,
- se ocultan horas extra,
- se falsea el impacto en AC,
- se duplica trabajo en proyectos simultáneos.

Estas violaciones destruyen
la credibilidad financiera del sistema.
