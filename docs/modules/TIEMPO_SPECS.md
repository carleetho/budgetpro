# REGLAS DE NEGOCIO — MÓDULO TIEMPO / CRONOGRAMA

## 1. Propósito del Módulo

El módulo TIEMPO define el marco temporal contractual
en el que la ejecución del Proyecto es válida.

El tiempo no es decorativo.
Es parte del contrato.

Un costo fuera de tiempo
es una desviación,
aunque esté presupuestado.

Sin tiempo congelado,
no existe control real.

---

## 2. Principio Rector

El Presupuesto y el Cronograma
constituyen juntos la Línea Base del Proyecto.

No existe Presupuesto congelado
sin Cronograma congelado.

---

## 3. Entidades Fundamentales

### 3.1 Actividad

Una Actividad representa un bloque de trabajo
asociado a una o más Partidas del Presupuesto.

Cada Actividad debe definir como mínimo:

- fecha de inicio,
- fecha de fin,
- duración,
- Partidas asociadas,
- tipo de calendario aplicable.

No existen Actividades sin Partidas.
No existen Partidas sin contexto temporal.

---

### 3.2 Ventana Temporal

La Ventana Temporal es el intervalo autorizado
para ejecutar una Actividad.

Toda ejecución fuera de esta ventana
constituye una desviación de tiempo,
aunque el trabajo sea real.

---

## 4. Congelamiento del Cronograma (LÍNEA BASE)

### 4.1 Congelamiento Automático

Al momento en que un Presupuesto
pasa a estado CONGELADO,
el sistema DEBE automáticamente:

- congelar el Cronograma asociado,
- generar un Snapshot temporal inmutable,
- almacenar:
  - fechas (fechaInicio, fechaFinEstimada, fechas de actividades),
  - duraciones (duracionTotalDias, duraciones de actividades),
  - secuencia (orden y dependencias entre actividades),
  - calendarios (estructura preparada para futuras extensiones).

**Acoplamiento Temporal:**
El congelamiento del Presupuesto y del Cronograma
es una operación atómica. Si el Cronograma no puede congelarse,
el Presupuesto NO se aprueba (rollback transaccional).

**Prerrequisitos para Congelamiento:**
- El ProgramaObra debe existir para el proyecto
- El ProgramaObra debe tener fechaInicio y fechaFinEstimada definidas
- El ProgramaObra no debe estar ya congelado

El Cronograma congelado
forma parte de la Línea Base contractual.

---

### 4.2 Mecanismo de Freeze

**Implementación Técnica:**

El mecanismo de freeze se implementa mediante:

1. **Campo de Estado:**
   - `congelado` (Boolean): Indica si el cronograma está congelado
   - `congeladoAt` (LocalDateTime): Timestamp del congelamiento
   - `congeladoBy` (UUID): Usuario que aprobó el congelamiento
   - `snapshotAlgorithm` (String): Versión del algoritmo de snapshot ("v1")

2. **Método de Congelamiento:**
   - `ProgramaObra.congelar(UUID approvedBy)`: Congela el cronograma
   - Valida que existan fechaInicio y fechaFinEstimada
   - Captura metadata del congelamiento
   - Operación irreversible (no hay método de descongelamiento)

3. **Guards de Freeze:**
   - `actualizarFechas()`: Lanza `CronogramaCongeladoException` si está congelado
   - `actualizarFechaFinDesdeActividades()`: Lanza `CronogramaCongeladoException` si está congelado
   - Todos los métodos de mutación verifican el estado de congelamiento

4. **Generación de Snapshot:**
   - `CronogramaService.congelarPorPresupuesto()`: Orquesta el congelamiento
   - `SnapshotGeneratorService`: Serializa datos temporales a JSON
   - `CronogramaSnapshot`: Entidad inmutable que preserva el baseline

---

### 4.3 Inmutabilidad

- El Cronograma congelado NO se edita.
- Cualquier cambio posterior
  solo puede gestionarse mediante:
  - Orden de Cambio,
  - Prórroga formal.

Modificar tiempos sin trazabilidad
es violación grave del sistema.

**Excepción de Dominio:**
- `CronogramaCongeladoException`: Se lanza cuando se intenta modificar
  un cronograma congelado. Incluye el ID del programa de obra y
  la operación intentada.

---

## 5. Relación con el APU (GOBIERNO DE DURACIÓN Y RENDIMIENTO)

La relación entre el Cronograma y el APU
puede operar bajo dos enfoques metodológicos válidos,
los cuales deben definirse explícitamente
antes del congelamiento de la Línea Base.

---

### 5.1 Enfoque A — Cronograma gobierna al APU (Top-Down)

Bajo este enfoque:

- La duración de la Actividad
  se define en el Cronograma.
- El APU debe ajustar:
  - cantidad de personal,
  - rendimientos,
  - turnos,
para cumplir el plazo contractual.

Reglas:

- El APU NO puede proponer
  una duración mayor a la definida en el Cronograma.
- Si los rendimientos calculados
  no permiten cumplir el plazo,
  el sistema DEBE emitir alerta técnica.

Ejemplo:
> Actividad: 10 días  
> El APU debe incrementar recursos,
> no extender el tiempo.

---

### 5.2 Enfoque B — Rendimiento del APU define la duración (Bottom-Up)

Bajo este enfoque:

- El APU define:
  - rendimiento por recurso,
  - cantidad de obra a ejecutar.
- El sistema puede calcular
  la duración de la Actividad
  en función de:
  - rendimiento,
  - cantidad,
  - número de recursos asignados.

Ejemplo:
> Rendimiento: 8 m²/día/obrero  
> Cantidad: 16 m²  
> 1 obrero → 2 días  
> 2 obreros → 1 día  

El Cronograma se construye
a partir de esta lógica productiva.

---

### 5.3 Selección de Enfoque (DECISIÓN EXPLÍCITA)

Reglas:

- El Proyecto DEBE definir
  el enfoque de planificación:
  - A (Cronograma → APU), o
  - B (APU → Cronograma).
- Esta decisión:
  - queda registrada,
  - forma parte de la Línea Base,
  - NO puede cambiarse
    después del congelamiento.

---

### 5.4 Congelamiento y Disciplina

Una vez congelados:

- Presupuesto,
- Cronograma,

el enfoque seleccionado se vuelve contractual.

Cualquier intento de:

- extender duración,
- alterar rendimientos,
- modificar recursos,

debe gestionarse mediante:

- Orden de Cambio, o
- Prórroga formal.

---

### 5.5 Alertas de Incoherencia Técnica

El sistema DEBE emitir alertas si:

- los rendimientos no soportan la duración,
- la duración no soporta la cantidad de obra,
- se intenta alterar el enfoque congelado.

Estas alertas:
- NO bloquean,
- NO corrigen,
- exponen riesgo técnico y financiero.

---

## 6. Calendarios y Días Computables

Toda Actividad debe indicar:

- tipo de día:
  - calendario,
  - hábil,
  - mixto.
- exclusión de:
  - feriados,
  - asuetos,
  - días no laborables.

Estos calendarios afectan:

- duración del Proyecto,
- control de atraso,
- factor de prestación en RRHH.

El calendario es parte del contrato.

---

## 7. Prórrogas Contractuales (ENDURECIMIENTO LEGAL)

El sistema reconoce la figura de la PRÓRROGA
conforme a la Ley de Compras Públicas.

Reglas:

- Una Prórroga:
  - modifica el plazo contractual,
  - NO es automática,
  - requiere justificación legal.
- Debe registrarse:
  - causa,
  - respaldo normativo,
  - autoridad que aprueba,
  - nuevo plazo.

Toda Prórroga genera
una nueva capa temporal,
sin borrar la original.

---

## 8. Atrasos y Consecuencias Económicas

El atraso es una desviación contractual
con impacto económico.

Reglas:

- El sistema debe calcular:
  - días de atraso,
  - causas atribuibles.
- El atraso puede generar:
  - multas,
  - penalidades,
  - pérdida directa de utilidad.

Las multas deben registrarse como:

- Costo Real (AC),
- impacto negativo en utilidad.

El atraso no se maquilla.
Se cuantifica.

---

## 9. Relación con la Ejecución

### 9.1 Mano de Obra

- El tareo fuera de ventana:
  - NO se bloquea,
  - genera alerta de desviación de tiempo y costo.

---

### 9.2 Compras e Inventarios

- Compras o consumos fuera de ventana:
  - NO se bloquean,
  - generan alerta de riesgo operativo,
  - afectan análisis de desempeño.

---

## 10. Avance Físico

### 10.1 Registro

- El avance se registra en unidades reales.
- El avance fuera de ventana
  es válido,
  pero sospechoso.

---

### 10.2 Validaciones

- El avance no puede exceder
  la cantidad contractual.
- Avance sin tiempo coherente
  deteriora métricas EVM.

---

## 11. Relación con EVM

El módulo TIEMPO define:

- Valor Planificado (PV),
- contexto del Valor Ganado (EV).

EV sin tiempo coherente
distorsiona SPI y TCPI.

---

## 12. Auditoría y Evidencia

Toda modificación temporal
debe registrar:

- usuario,
- fecha,
- motivo,
- respaldo legal (si aplica).

El historial temporal es permanente.

---

## 13. Principios de Diseño No Negociables

- El tiempo gobierna al costo.
- El cronograma es contractual.
- La prórroga no es un favor.
- El atraso tiene precio.
- El sistema alerta, no justifica.

---

## 14. Criterios de Violación

Se considera violación grave si:

- se ejecuta sin cronograma congelado,
- se modifica el plazo sin prórroga formal,
- se ignoran feriados en cálculos,
- se ocultan atrasos o multas,
- el APU define plazos por encima del cronograma.

Estas violaciones invalidan
la credibilidad del control del Proyecto.
