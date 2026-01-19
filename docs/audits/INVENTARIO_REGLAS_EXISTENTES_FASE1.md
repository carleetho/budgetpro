# INVENTARIO DE REGLAS EXISTENTES — FASE 1

## REGLA-001
- Descripción exacta de la regla (lenguaje declarativo)
  - Un reporte aprobado o rechazado es inmutable; no se puede editar ni eliminar.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/validation/ProduccionValidator.java`
  - clase: `ProduccionValidator`
  - método o sección: `validarEditable`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (reporte.getEstado() == EstadoReporteProduccion.APROBADO || reporte.getEstado() == EstadoReporteProduccion.RECHAZADO) { throw new BusinessRuleException("Un reporte aprobado es inmutable. Debe crear una Nota de Crédito o un Reporte Deductivo para corregir."); }`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - Mensaje de negocio explícito en excepción.

## REGLA-002
- Descripción exacta de la regla (lenguaje declarativo)
  - La fecha del reporte es obligatoria y no puede ser futura.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/validation/ProduccionValidator.java`
  - clase: `ProduccionValidator`
  - método o sección: `validarFechaNoFutura`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (fechaReporte == null) { throw new BusinessRuleException("La fecha del reporte es obligatoria."); }`
    - `if (fechaReporte.isAfter(LocalDate.now())) { throw new BusinessRuleException("La fecha del reporte no puede ser futura."); }`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - N/A

## REGLA-003
- Descripción exacta de la regla (lenguaje declarativo)
  - Solo se puede reportar avance en proyectos en estado EJECUCION.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Producción (RPC)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/validation/ProduccionValidator.java`
  - clase: `ProduccionValidator`
  - método o sección: `validarProyectoEnEjecucion`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (proyecto.getEstado() != EstadoProyecto.EJECUCION) { throw new BusinessRuleException("No se puede reportar avance en un proyecto que no está en EJECUCION."); }`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - N/A

## REGLA-004
- Descripción exacta de la regla (lenguaje declarativo)
  - La cantidad reportada es obligatoria y el reporte no puede exceder el metrado vigente de la partida.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Producción (RPC)
  - Presupuesto/Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/validation/ProduccionValidator.java`
  - clase: `ProduccionValidator`
  - método o sección: `validarNoExcesoMetrado`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (cantidadNueva == null) { throw new BusinessRuleException("La cantidad reportada es obligatoria."); }`
    - `if (avanceTotal.compareTo(metradoVigente) > 0) { throw new BusinessRuleException("La cantidad reportada excede el saldo disponible de la partida. Requiere Orden de Cambio."); }`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - N/A

## REGLA-005
- Descripción exacta de la regla (lenguaje declarativo)
  - Un reporte de producción debe contener al menos un detalle.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/service/ProduccionServiceImpl.java`
  - clase: `ProduccionServiceImpl`
  - método o sección: `validarDetalles`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (detalles == null || detalles.isEmpty()) { throw new BusinessRuleException("El reporte debe contener al menos un detalle."); }`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - N/A

## REGLA-006
- Descripción exacta de la regla (lenguaje declarativo)
  - Solo se puede aprobar un reporte en estado PENDIENTE.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/service/ProduccionServiceImpl.java`
  - clase: `ProduccionServiceImpl`
  - método o sección: `aprobarReporte`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (reporte.getEstado() != EstadoReporteProduccion.PENDIENTE) { throw new BusinessRuleException("Solo se puede aprobar un reporte en estado PENDIENTE."); }`
- Estado:
  - Explícita

## REGLA-007
- Descripción exacta de la regla (lenguaje declarativo)
  - Solo se puede rechazar un reporte en estado PENDIENTE y el motivo es obligatorio.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/service/ProduccionServiceImpl.java`
  - clase: `ProduccionServiceImpl`
  - método o sección: `rechazarReporte`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (reporte.getEstado() != EstadoReporteProduccion.PENDIENTE) { throw new BusinessRuleException("Solo se puede rechazar un reporte en estado PENDIENTE."); }`
    - `if (motivo == null || motivo.isBlank()) { throw new BusinessRuleException("El motivo de rechazo es obligatorio."); }`
- Estado:
  - Explícita

## REGLA-008
- Descripción exacta de la regla (lenguaje declarativo)
  - Si el estado del reporte es nulo, se asigna PENDIENTE.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/service/ProduccionServiceImpl.java`
  - clase: `ProduccionServiceImpl`
  - método o sección: `crearReporte`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (reporte.getEstado() == null) { reporte.setEstado(EstadoReporteProduccion.PENDIENTE); }`
- Estado:
  - Explícita

## REGLA-009
- Descripción exacta de la regla (lenguaje declarativo)
  - El reporte de producción no puede ser nulo.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/produccion/service/ProduccionServiceImpl.java`
  - clase: `ProduccionServiceImpl`
  - método o sección: `crearReporte`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (reporte == null) { throw new BusinessRuleException("El reporte no puede ser nulo."); }`
- Estado:
  - Explícita

## REGLA-010
- Descripción exacta de la regla (lenguaje declarativo)
  - La estimación solo puede pasar de BORRADOR a APROBADA y de APROBADA a PAGADA.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
  - clase: `Estimacion`
  - método o sección: `aprobar`, `marcarComoPagada`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (this.estado != EstadoEstimacion.BORRADOR) { throw new IllegalStateException("Solo se pueden aprobar estimaciones en estado BORRADOR"); }`
    - `if (this.estado != EstadoEstimacion.APROBADA) { throw new IllegalStateException("Solo se pueden marcar como pagadas estimaciones en estado APROBADA"); }`
- Estado:
  - Explícita

## REGLA-011
- Descripción exacta de la regla (lenguaje declarativo)
  - El monto neto a pagar de una estimación es: montoBruto - amortizacionAnticipo - retencionFondoGarantia.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
  - clase: `Estimacion`
  - método o sección: `calcularMontoNeto`
- Evidencia:
  - fragmento de código o referencia precisa
    - `return this.montoBruto.subtract(this.amortizacionAnticipo).subtract(this.retencionFondoGarantia)`
- Estado:
  - Explícita

## REGLA-012
- Descripción exacta de la regla (lenguaje declarativo)
  - El número de estimación debe ser positivo cuando se define.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
  - clase: `Estimacion`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (numeroEstimacion != null && numeroEstimacion <= 0) { throw new IllegalArgumentException("El número de estimación debe ser positivo"); }`
- Estado:
  - Explícita

## REGLA-013
- Descripción exacta de la regla (lenguaje declarativo)
  - El periodo de fin no puede ser menor al periodo de inicio.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
  - clase: `Estimacion`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (periodoFin.isBefore(periodoInicio)) { throw new IllegalArgumentException("El periodo de fin no puede ser menor al periodo de inicio"); }`
- Estado:
  - Explícita

## REGLA-014
- Descripción exacta de la regla (lenguaje declarativo)
  - La amortización de anticipo y la retención de fondo de garantía no pueden ser negativas.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/Estimacion.java`
  - clase: `Estimacion`
  - método o sección: `actualizarAmortizacionAnticipo`, `actualizarRetencionFondoGarantia`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (nuevaAmortizacion.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("La amortización de anticipo no puede ser negativa"); }`
    - `if (nuevaRetencion.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("La retención de fondo de garantía no puede ser negativa"); }`
- Estado:
  - Explícita

## REGLA-015
- Descripción exacta de la regla (lenguaje declarativo)
  - La cantidad de avance y el precio unitario en un detalle de estimación no pueden ser negativos.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/model/DetalleEstimacion.java`
  - clase: `DetalleEstimacion`
  - método o sección: `validarInvariantes`, `actualizarCantidadAvance`, `actualizarPrecioUnitario`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (cantidadAvance != null && cantidadAvance.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("La cantidad de avance no puede ser negativa"); }`
    - `if (precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("El precio unitario no puede ser negativo"); }`
- Estado:
  - Explícita

## REGLA-016
- Descripción exacta de la regla (lenguaje declarativo)
  - El volumen estimado no puede exceder el volumen contratado.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Estimaciones
  - Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/service/GeneradorEstimacionService.java`
  - clase: `GeneradorEstimacionService`
  - método o sección: `validarVolumenEstimado`
  - archivo: `backend/src/main/java/com/budgetpro/application/estimacion/usecase/GenerarEstimacionUseCaseImpl.java`
  - clase: `GenerarEstimacionUseCaseImpl`
  - método o sección: `generar`
- Evidencia:
  - fragmento de código o referencia precisa
    - `return acumuladoTotal.compareTo(volumenContratado) <= 0;`
    - `if (!generadorEstimacionService.validarVolumenEstimado(...)) { throw new IllegalArgumentException("La cantidad estimada excede el volumen contratado..."); }`
- Estado:
  - Explícita

## REGLA-017
- Descripción exacta de la regla (lenguaje declarativo)
  - La amortización de anticipo calculada no puede exceder el saldo pendiente.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/estimacion/service/GeneradorEstimacionService.java`
  - clase: `GeneradorEstimacionService`
  - método o sección: `calcularAmortizacionAnticipo`
- Evidencia:
  - fragmento de código o referencia precisa
    - `return amortizacionTeorica.min(saldoAnticipoPendiente);`
- Estado:
  - Explícita

## REGLA-018
- Descripción exacta de la regla (lenguaje declarativo)
  - La fecha de fin de una actividad no puede ser menor que la fecha de inicio.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/cronograma/model/ActividadProgramada.java`
  - clase: `ActividadProgramada`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (fechaFin.isBefore(fechaInicio)) { throw new IllegalArgumentException("La fecha de fin no puede ser menor a la fecha de inicio"); }`
- Estado:
  - Explícita

## REGLA-019
- Descripción exacta de la regla (lenguaje declarativo)
  - Una actividad no puede ser predecesora de sí misma.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/cronograma/model/ActividadProgramada.java`
  - clase: `ActividadProgramada`
  - método o sección: `agregarPredecesora`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (actividadPredecesoraId.equals(this.id.getValue())) { throw new IllegalArgumentException("Una actividad no puede ser predecesora de sí misma"); }`
- Estado:
  - Explícita

## REGLA-020
- Descripción exacta de la regla (lenguaje declarativo)
  - En una dependencia Fin-Inicio, la fecha de inicio de B debe ser >= la fecha de fin de A.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/cronograma/service/CalculoCronogramaService.java`
  - clase: `CalculoCronogramaService`
  - método o sección: `validarDependencias`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (fechaInicioActividad.isBefore(fechaFinPredecesora)) { return false; }`
- Estado:
  - Explícita

## REGLA-021
- Descripción exacta de la regla (lenguaje declarativo)
  - La fecha de fin estimada del programa no puede ser menor a la fecha de inicio.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/cronograma/model/ProgramaObra.java`
  - clase: `ProgramaObra`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (fechaFinEstimada.isBefore(fechaInicio)) { throw new IllegalArgumentException("La fecha de fin estimada no puede ser menor a la fecha de inicio"); }`
- Estado:
  - Explícita

## REGLA-022
- Descripción exacta de la regla (lenguaje declarativo)
  - No se puede actualizar la fecha de fin desde actividades si no hay fecha de inicio.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/cronograma/model/ProgramaObra.java`
  - clase: `ProgramaObra`
  - método o sección: `actualizarFechaFinDesdeActividades`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (this.fechaInicio == null) { throw new IllegalStateException("No se puede actualizar la fecha de fin sin fecha de inicio"); }`
- Estado:
  - Explícita

## REGLA-023
- Descripción exacta de la regla (lenguaje declarativo)
  - Los porcentajes de indirectos, financiamiento, utilidad, fianzas e impuestos reflejables no pueden ser negativos ni mayores a 100%.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Sobrecostos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/AnalisisSobrecosto.java`
  - clase: `AnalisisSobrecosto`
  - método o sección: `validarPorcentaje`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (porcentaje.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("El porcentaje de %s no puede ser negativo"); }`
    - `if (porcentaje.compareTo(new BigDecimal("100")) > 0) { throw new IllegalArgumentException("El porcentaje de %s no puede ser mayor a 100%%"); }`
- Estado:
  - Explícita

## REGLA-024
- Descripción exacta de la regla (lenguaje declarativo)
  - Los días de aguinaldo, vacaciones y no trabajados no pueden ser negativos; los días laborables al año deben ser positivos; el porcentaje de seguridad social debe estar entre 0 y 100.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - RRHH / Sobrecostos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java`
  - clase: `ConfiguracionLaboral`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (diasAguinaldo != null && diasAguinaldo < 0) { throw new IllegalArgumentException("Los días de aguinaldo no pueden ser negativos"); }`
    - `if (porcentajeSeguridadSocial.compareTo(new BigDecimal("100")) > 0) { throw new IllegalArgumentException("El porcentaje de seguridad social no puede ser mayor a 100%"); }`
- Estado:
  - Explícita

## REGLA-025
- Descripción exacta de la regla (lenguaje declarativo)
  - El salario base debe ser positivo para calcular salario real.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - RRHH / Sobrecostos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/model/ConfiguracionLaboral.java`
  - clase: `ConfiguracionLaboral`
  - método o sección: `calcularSalarioReal`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (salarioBase == null || salarioBase.compareTo(BigDecimal.ZERO) <= 0) { throw new IllegalArgumentException("El salario base debe ser positivo"); }`
- Estado:
  - Explícita

## REGLA-026
- Descripción exacta de la regla (lenguaje declarativo)
  - Si un recurso EQUIPO tiene costo_horario = 0, se genera ALERTA CRÍTICA.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Alertas Paramétricas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/alertas/service/AnalizadorParametricoService.java`
  - clase: `AnalizadorParametricoService`
  - método o sección: `analizarMaquinaria`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (costoHorario == null || costoHorario.compareTo(BigDecimal.ZERO) == 0) { AlertaParametrica.crear(... NivelAlerta.CRITICA, ...) }`
- Estado:
  - Explícita

## REGLA-027
- Descripción exacta de la regla (lenguaje declarativo)
  - El ratio Kg Acero / m3 Concreto debe estar entre 80 y 150; fuera de rango genera WARNING.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Alertas Paramétricas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/alertas/service/AnalizadorParametricoService.java`
  - clase: `AnalizadorParametricoService`
  - método o sección: `analizarAceroConcreto`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (ratio.compareTo(RATIO_ACERO_MIN) < 0 || ratio.compareTo(RATIO_ACERO_MAX) > 0) { ... NivelAlerta.WARNING ... }`
- Estado:
  - Explícita

## REGLA-028
- Descripción exacta de la regla (lenguaje declarativo)
  - El tamaño del agregado no debe exceder 1/5 del ancho del elemento; si excede, genera ALERTA TÉCNICA.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Alertas Paramétricas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/alertas/service/AnalizadorParametricoService.java`
  - clase: `AnalizadorParametricoService`
  - método o sección: `analizarTamanoAgregado`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (factor.compareTo(FACTOR_AGREGADO_MAX) > 0) { ... NivelAlerta.WARNING ... }`
- Estado:
  - Explícita

## REGLA-029
- Descripción exacta de la regla (lenguaje declarativo)
  - Si un insumo tiene precio unitario 0, se genera alerta de descapitalización de maquinaria.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Inteligencia Maquinaria
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/service/InteligenciaMaquinariaService.java`
  - clase: `InteligenciaMaquinariaService`
  - método o sección: `analizarAPU`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (insumo.getPrecioUnitario().compareTo(BigDecimal.ZERO) == 0) { alertas.add(new AlertaInteligencia(...)) }`
- Estado:
  - Explícita

## REGLA-030
- Descripción exacta de la regla (lenguaje declarativo)
  - Valor inicial, vida útil y horas anuales de uso deben ser positivos para calcular costo horario.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Inteligencia Maquinaria
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/sobrecosto/service/InteligenciaMaquinariaService.java`
  - clase: `InteligenciaMaquinariaService`
  - método o sección: `calcularCostoHorarioPosesion`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (valorInicial == null || valorInicial.compareTo(BigDecimal.ZERO) <= 0) { throw new IllegalArgumentException("El valor inicial debe ser positivo"); }`
    - `if (vidaUtilAnos == null || vidaUtilAnos <= 0) { throw new IllegalArgumentException("La vida útil debe ser positiva"); }`
    - `if (horasAnualesUso == null || horasAnualesUso <= 0) { throw new IllegalArgumentException("Las horas anuales de uso deben ser positivas"); }`
- Estado:
  - Explícita

## REGLA-031
- Descripción exacta de la regla (lenguaje declarativo)
  - El proyectoId, la fecha, el proveedor y los detalles son obligatorios en una compra.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Compras
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/Compra.java`
  - clase: `Compra`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (proyectoId == null) { throw new IllegalArgumentException("El proyectoId no puede ser nulo"); }`
    - `if (detalles == null || detalles.isEmpty()) { throw new IllegalArgumentException("La compra debe tener al menos un detalle"); }`
- Estado:
  - Explícita

## REGLA-032
- Descripción exacta de la regla (lenguaje declarativo)
  - El subtotal de un detalle de compra es cantidad * precio unitario; cantidad y precio unitario no pueden ser negativos.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Compras
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/logistica/compra/model/CompraDetalle.java`
  - clase: `CompraDetalle`
  - método o sección: `validarInvariantes`, `calcularSubtotal`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("La cantidad no puede ser negativa"); }`
    - `private BigDecimal calcularSubtotal() { return this.cantidad.multiply(this.precioUnitario); }`
- Estado:
  - Explícita

## REGLA-033
- Descripción exacta de la regla (lenguaje declarativo)
  - Para procesar una compra, las partidas deben existir; se descuenta de billetera, se aprueba la compra y se registra entrada a inventario.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Compras
  - Billetera
  - Inventario
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/logistica/compra/service/ProcesarCompraService.java`
  - clase: `ProcesarCompraService`
  - método o sección: `procesar`
- Evidencia:
  - fragmento de código o referencia precisa
    - `Partida partida = partidaRepository.findById(...).orElseThrow(...);`
    - `billetera.egresar(...)`
    - `compra.aprobar();`
    - `gestionInventarioService.registrarEntradaPorCompra(compra);`
- Estado:
  - Explícita

## REGLA-034
- Descripción exacta de la regla (lenguaje declarativo)
  - El consumo de partida requiere partidaId, fecha y tipo; el monto no puede ser negativo.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Consumos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/consumo/model/ConsumoPartida.java`
  - clase: `ConsumoPartida`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (monto != null && monto.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("El monto no puede ser negativo"); }`
    - `if (fecha == null) { throw new IllegalArgumentException("La fecha no puede ser nula"); }`
- Estado:
  - Explícita

## REGLA-035
- Descripción exacta de la regla (lenguaje declarativo)
  - En APU, el partidaId es obligatorio y la lista de insumos no puede ser nula.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - APU
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/APU.java`
  - clase: `APU`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (partidaId == null) { throw new IllegalArgumentException("El partidaId no puede ser nulo"); }`
    - `if (insumos == null) { throw new IllegalArgumentException("La lista de insumos no puede ser nula"); }`
- Estado:
  - Explícita

## REGLA-036
- Descripción exacta de la regla (lenguaje declarativo)
  - En APU, el subtotal de insumo es cantidad * precio unitario; cantidad y precio unitario no pueden ser negativos.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - APU
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/apu/model/ApuInsumo.java`
  - clase: `ApuInsumo`
  - método o sección: `validarInvariantes`, `calcularSubtotal`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (cantidad != null && cantidad.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("La cantidad no puede ser negativa"); }`
    - `private BigDecimal calcularSubtotal() { return this.cantidad.multiply(this.precioUnitario); }`
- Estado:
  - Explícita

## REGLA-037
- Descripción exacta de la regla (lenguaje declarativo)
  - En Partida: presupuestoId obligatorio, item no vacío, descripción no vacía, metrado no negativo y nivel >= 1.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java`
  - clase: `Partida`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (item == null || item.isBlank()) { throw new IllegalArgumentException("El item (código WBS) no puede estar vacío"); }`
    - `if (nivel == null || nivel < 1) { throw new IllegalArgumentException("El nivel debe ser >= 1"); }`
- Estado:
  - Explícita

## REGLA-038
- Descripción exacta de la regla (lenguaje declarativo)
  - Si una partida tiene padreId, debe pertenecer al mismo presupuestoId (validado a nivel de aplicación).
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/partida/model/Partida.java`
  - clase: `Partida`
  - método o sección: comentario de invariantes
- Evidencia:
  - fragmento de código o referencia precisa
    - `// Si tiene padreId, debe pertenecer al mismo presupuestoId (validado a nivel de aplicación)`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - Regla documentada como validación a nivel de aplicación.

## REGLA-039
- Descripción exacta de la regla (lenguaje declarativo)
  - En Recurso: nombre no vacío, tipo no nulo y unidadBase no vacía.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/recurso/model/Recurso.java`
  - clase: `Recurso`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (nombre == null || nombre.isBlank()) { throw new IllegalArgumentException("El nombre del recurso no puede estar vacío"); }`
    - `if (tipo == null) { throw new IllegalArgumentException("El tipo del recurso no puede ser nulo"); }`
- Estado:
  - Explícita

## REGLA-040
- Descripción exacta de la regla (lenguaje declarativo)
  - El nombre del recurso se normaliza con trim, uppercase y espacios simples.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/recurso/model/Recurso.java`
  - clase: `Recurso`
  - método o sección: `normalizarNombre`
  - archivo: `backend/src/main/java/com/budgetpro/application/recurso/usecase/CrearRecursoUseCaseImpl.java`
  - clase: `CrearRecursoUseCaseImpl`
  - método o sección: `normalizarNombre`
- Evidencia:
  - fragmento de código o referencia precisa
    - `return nombre.trim().toUpperCase().replaceAll("\\s+", " ");`
- Estado:
  - Explícita

## REGLA-041
- Descripción exacta de la regla (lenguaje declarativo)
  - Un recurso provisional se crea con estado EN_REVISION.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/recurso/model/Recurso.java`
  - clase: `Recurso`
  - método o sección: `crearProvisional`
- Evidencia:
  - fragmento de código o referencia precisa
    - `return new Recurso(..., EstadoRecurso.EN_REVISION);`
- Estado:
  - Explícita

## REGLA-042
- Descripción exacta de la regla (lenguaje declarativo)
  - En MovimientoCaja: monto > 0, referencia no vacía, billeteraId y tipo no nulos.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Billetera
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/model/MovimientoCaja.java`
  - clase: `MovimientoCaja`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (monto.compareTo(BigDecimal.ZERO) <= 0) { throw new IllegalArgumentException("El monto debe ser positivo (mayor que cero)"); }`
    - `if (referencia == null || referencia.isBlank()) { throw new IllegalArgumentException("La referencia no puede ser nula ni vacía"); }`
- Estado:
  - Explícita

## REGLA-043
- Descripción exacta de la regla (lenguaje declarativo)
  - El nombre del proyecto no puede estar vacío; el estado del proyecto no puede ser nulo.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/proyecto/model/Proyecto.java`
  - clase: `Proyecto`
  - método o sección: `validarInvariantes`, `normalizarNombre`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (nombre == null || nombre.isBlank()) { throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío"); }`
    - `if (estado == null) { throw new IllegalArgumentException("El estado del proyecto no puede ser nulo"); }`
- Estado:
  - Explícita

## REGLA-044
- Descripción exacta de la regla (lenguaje declarativo)
  - El nombre del presupuesto no puede estar vacío; el proyectoId y el estado son obligatorios.
- Tipo:
  - Dominio
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/model/Presupuesto.java`
  - clase: `Presupuesto`
  - método o sección: `validarInvariantes`, `normalizarNombre`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (proyectoId == null) { throw new IllegalArgumentException("El proyectoId no puede ser nulo"); }`
    - `if (nombre == null || nombre.isBlank()) { throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío"); }`
- Estado:
  - Explícita

## REGLA-045
- Descripción exacta de la regla (lenguaje declarativo)
  - Al aprobar presupuesto, el estado cambia a APROBADO y esContractual se marca true.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/presupuesto/model/Presupuesto.java`
  - clase: `Presupuesto`
  - método o sección: `aprobar`
- Evidencia:
  - fragmento de código o referencia precisa
    - `this.estado = EstadoPresupuesto.APROBADO;`
    - `this.esContractual = true;`
- Estado:
  - Explícita

## REGLA-046
- Descripción exacta de la regla (lenguaje declarativo)
  - El presupuesto APROBADO es de solo lectura.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/PresupuestoEntity.java`
  - clase: `PresupuestoEntity`
  - método o sección: `preUpdate`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (estadoOriginal == EstadoPresupuesto.APROBADO) { throw new IllegalStateException("Presupuesto APROBADO es de solo lectura."); }`
- Estado:
  - Explícita

## REGLA-047
- Descripción exacta de la regla (lenguaje declarativo)
  - El metradoOriginal de partida es inmutable si el presupuesto está APROBADO.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Partidas
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/PartidaEntity.java`
  - clase: `PartidaEntity`
  - método o sección: `preUpdate`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (presupuesto.getEstado() == EstadoPresupuesto.APROBADO && metradoOriginalSnapshot != null && metradoOriginal.compareTo(metradoOriginalSnapshot) != 0) { throw new IllegalStateException("metradoOriginal es inmutable tras la aprobación del presupuesto."); }`
- Estado:
  - Explícita

## REGLA-048
- Descripción exacta de la regla (lenguaje declarativo)
  - Si metradoVigente es nulo al persistir una partida, se iguala a metradoOriginal.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/PartidaEntity.java`
  - clase: `PartidaEntity`
  - método o sección: `prePersist`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (metradoVigente == null) { metradoVigente = metradoOriginal; }`
- Estado:
  - Explícita

## REGLA-049
- Descripción exacta de la regla (lenguaje declarativo)
  - El movimiento de almacén de tipo SALIDA requiere partidaId.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/almacen/MovimientoAlmacenEntity.java`
  - clase: `MovimientoAlmacenEntity`
  - método o sección: `isPartidaValidaParaSalida`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (tipo == TipoMovimientoAlmacen.SALIDA) { return partidaId != null; }`
- Estado:
  - Explícita

## REGLA-050
- Descripción exacta de la regla (lenguaje declarativo)
  - Si tipo es nulo en movimiento de almacén, se asigna tipoMovimiento.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/almacen/MovimientoAlmacenEntity.java`
  - clase: `MovimientoAlmacenEntity`
  - método o sección: `prePersist`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (tipo == null) { tipo = tipoMovimiento; }`
- Estado:
  - Explícita

## REGLA-051
- Descripción exacta de la regla (lenguaje declarativo)
  - JWT_SECRET es obligatorio y debe tener al menos 32 caracteres.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/security/jwt/JwtService.java`
  - clase: `JwtService`
  - método o sección: constructor
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (secret == null || secret.isBlank()) { throw new IllegalStateException("JWT_SECRET es obligatorio."); }`
    - `if (secret.length() < 32) { throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres."); }`
- Estado:
  - Explícita

## REGLA-052
- Descripción exacta de la regla (lenguaje declarativo)
  - Las rutas `/api/public/**` y `/api/v1/auth/**` son públicas; el resto requiere autenticación.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/config/SecurityConfig.java`
  - clase: `SecurityConfig`
  - método o sección: `securityFilterChain`
- Evidencia:
  - fragmento de código o referencia precisa
    - `.requestMatchers("/api/public/**", "/api/v1/auth/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()`
    - `.anyRequest().authenticated()`
- Estado:
  - Explícita

## REGLA-053
- Descripción exacta de la regla (lenguaje declarativo)
  - CORS permite origen `http://localhost:3000` y métodos GET, POST, PUT, DELETE, PATCH, OPTIONS.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/config/SecurityConfig.java`
  - clase: `SecurityConfig`
  - método o sección: `corsConfigurationSource`
- Evidencia:
  - fragmento de código o referencia precisa
    - `config.setAllowedOrigins(List.of("http://localhost:3000"));`
    - `config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));`
- Estado:
  - Explícita

## REGLA-054
- Descripción exacta de la regla (lenguaje declarativo)
  - Las variables RESEND_API_KEY, ADMIN_EMAIL y JWT_SECRET se requieren por configuración.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Configuración
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/application.yml`
  - clase: N/A
  - método o sección: configuración `resend.api.key`, `app.notification.email`, `jwt.secret`
- Evidencia:
  - fragmento de código o referencia precisa
    - `resend.api.key: ${RESEND_API_KEY}`
    - `app.notification.email: ${ADMIN_EMAIL}`
    - `jwt.secret: ${JWT_SECRET}`
- Estado:
  - Explícita

## REGLA-055
- Descripción exacta de la regla (lenguaje declarativo)
  - La entidad Usuario exige email único.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/seguridad/UsuarioEntity.java`
  - clase: `UsuarioEntity`
  - método o sección: anotación `@Table`
  - archivo: `backend/src/main/resources/db/migration/V19__create_usuarios_schema.sql`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@UniqueConstraint(name = "uq_usuarios_email", columnNames = "email")`
    - `CREATE UNIQUE INDEX IF NOT EXISTS uq_usuarios_email ON usuarios(email);`
- Estado:
  - Explícita

## REGLA-056
- Descripción exacta de la regla (lenguaje declarativo)
  - El rol de usuario debe estar en {ADMIN, RESIDENTE, GERENTE, AUDITOR}.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V19__create_usuarios_schema.sql`
  - clase: N/A
  - método o sección: CHECK
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (rol IN ('ADMIN', 'RESIDENTE', 'GERENTE', 'AUDITOR'));`
- Estado:
  - Explícita

## REGLA-057
- Descripción exacta de la regla (lenguaje declarativo)
  - El estado de Lead debe estar en {NUEVO, CONTACTADO, CONVERTIDO}.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Marketing
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V18__create_marketing_lead.sql`
  - clase: N/A
  - método o sección: CHECK
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (estado IN ('NUEVO', 'CONTACTADO', 'CONVERTIDO'));`
- Estado:
  - Explícita

## REGLA-058
- Descripción exacta de la regla (lenguaje declarativo)
  - El estado de reporte de producción debe estar en {PENDIENTE, APROBADO, RECHAZADO}.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - clase: N/A
  - método o sección: CHECK de `reporte_produccion.estado`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))`
- Estado:
  - Explícita

## REGLA-059
- Descripción exacta de la regla (lenguaje declarativo)
  - En detalle RPC, cantidad_reportada debe ser >= 0.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - clase: N/A
  - método o sección: CHECK de `detalle_rpc.cantidad_reportada`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (cantidad_reportada >= 0)`
- Estado:
  - Explícita

## REGLA-060
- Descripción exacta de la regla (lenguaje declarativo)
  - En proyecto, el estado está restringido por CHECK en migraciones.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`
  - método o sección: CHECK `estado IN ('BORRADOR', 'ACTIVO', 'SUSPENDIDO', 'CERRADO')`
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK `estado IN ('BORRADOR', 'PAUSADO', 'EJECUCION', 'FINALIZADO')`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (estado IN ('BORRADOR', 'ACTIVO', 'SUSPENDIDO', 'CERRADO'))`
    - `CHECK (estado IN ('BORRADOR', 'PAUSADO', 'EJECUCION', 'FINALIZADO'))`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - Existen dos CHECK distintos en migraciones V2 y V16.

## REGLA-061
- Descripción exacta de la regla (lenguaje declarativo)
  - En presupuesto, el estado está restringido por CHECK en migraciones.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V2__create_proyecto_presupuesto_schema.sql`
  - método o sección: CHECK `estado IN ('EN_EDICION', 'APROBADO')`
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK `estado IN ('BORRADOR', 'APROBADO', 'ANULADO')`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (estado IN ('EN_EDICION', 'APROBADO'))`
    - `CHECK (estado IN ('BORRADOR', 'APROBADO', 'ANULADO'))`
- Estado:
  - Explícita
- Observaciones (solo si existen en el código)
  - Existen dos CHECK distintos en migraciones V2 y V16.

## REGLA-062
- Descripción exacta de la regla (lenguaje declarativo)
  - En partida, metrado_original, metrado_vigente y precio_unitario deben ser >= 0.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK `metrado_original`, `metrado_vigente`, `precio_unitario`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (metrado_original >= 0);`
    - `CHECK (metrado_vigente >= 0);`
    - `CHECK (precio_unitario >= 0);`
- Estado:
  - Explícita

## REGLA-063
- Descripción exacta de la regla (lenguaje declarativo)
  - En recurso, el tipo debe estar en {MATERIAL, MANO_OBRA, EQUIPO, SUBCONTRATO}; el estado en {ACTIVO, EN_REVISION, DEPRECADO}; costo_referencia >= 0.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK de `recurso.tipo`, `recurso.estado`, `recurso.costo_referencia`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (tipo IN ('MATERIAL', 'MANO_OBRA', 'EQUIPO', 'SUBCONTRATO'));`
    - `CHECK (estado IN ('ACTIVO', 'EN_REVISION', 'DEPRECADO'));`
    - `CHECK (costo_referencia >= 0);`
- Estado:
  - Explícita

## REGLA-064
- Descripción exacta de la regla (lenguaje declarativo)
  - En movimiento_almacen: cantidad > 0, precio_unitario >= 0, importe_total >= 0.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V14__create_almacen_inventarios_schema.sql`
  - método o sección: CHECK en `movimiento_almacen`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (cantidad > 0)`
    - `CHECK (precio_unitario >= 0)`
    - `CHECK (importe_total >= 0)`
- Estado:
  - Explícita

## REGLA-065
- Descripción exacta de la regla (lenguaje declarativo)
  - En stock_movimiento: cantidad_entrada, cantidad_salida, saldo_cantidad, saldo_valor y costo_promedio_ponderado no pueden ser negativos.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V14__create_almacen_inventarios_schema.sql`
  - método o sección: CHECK en `stock_movimiento`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (cantidad_entrada >= 0)`
    - `CHECK (cantidad_salida >= 0)`
    - `CHECK (saldo_cantidad >= 0)`
    - `CHECK (saldo_valor >= 0)`
    - `CHECK (costo_promedio_ponderado >= 0)`
- Estado:
  - Explícita

## REGLA-066
- Descripción exacta de la regla (lenguaje declarativo)
  - En estimación: periodo_fin >= periodo_inicio; montos y acumulados no negativos; estado en {BORRADOR, APROBADA, PAGADA}.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V11__create_estimacion_schema.sql`
  - método o sección: CHECK de `estimacion` y `detalle_estimacion`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (periodo_fin >= periodo_inicio)`
    - `CHECK (monto_bruto >= 0)`
    - `CHECK (estado IN ('BORRADOR', 'APROBADA', 'PAGADA'))`
    - `CHECK (cantidad_avance >= 0)`
- Estado:
  - Explícita

## REGLA-067
- Descripción exacta de la regla (lenguaje declarativo)
  - En cronograma: fecha_fin_estimada >= fecha_inicio; duracion_total_dias > 0 si existe; actividad con fecha_fin >= fecha_inicio y duracion_dias > 0 si existe; actividad_id != actividad_predecesora_id.
- Tipo:
  - Temporal
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V10__create_cronograma_schema.sql`
  - método o sección: CHECK de `programa_obra`, `actividad_programada`, `actividad_predecesora`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (fecha_fin_estimada IS NULL OR fecha_inicio IS NULL OR fecha_fin_estimada >= fecha_inicio)`
    - `CHECK (actividad_id != actividad_predecesora_id)`
- Estado:
  - Explícita

## REGLA-068
- Descripción exacta de la regla (lenguaje declarativo)
  - En avance_fisico: metrado_ejecutado >= 0; valuación con (proyecto_id, codigo) único.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Avance / Valuación
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V8__create_avance_valuacion_schema.sql`
  - método o sección: CHECK y UNIQUE
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (metrado_ejecutado >= 0)`
    - `UNIQUE (proyecto_id, codigo)`
- Estado:
  - Explícita

## REGLA-069
- Descripción exacta de la regla (lenguaje declarativo)
  - En configuracion_laboral: días no negativos; porcentaje_seguridad_social entre 0 y 100; dias_laborables_ano > 0.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - RRHH / Sobrecostos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V9__create_sobrecosto_laboral_schema.sql`
  - método o sección: CHECK `configuracion_laboral`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (dias_aguinaldo >= 0)`
    - `CHECK (porcentaje_seguridad_social >= 0 AND porcentaje_seguridad_social <= 100)`
- Estado:
  - Explícita

## REGLA-070
- Descripción exacta de la regla (lenguaje declarativo)
  - En analisis_sobrecosto: porcentajes entre 0 y 100.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Sobrecostos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V9__create_sobrecosto_laboral_schema.sql`
  - método o sección: CHECK `analisis_sobrecosto`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (porcentaje_utilidad >= 0 AND porcentaje_utilidad <= 100)`
- Estado:
  - Explícita

## REGLA-071
- Descripción exacta de la regla (lenguaje declarativo)
  - El proyecto tiene moneda obligatoria de longitud 3 y presupuesto_total no nulo.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/ProyectoEntity.java`
  - clase: `ProyectoEntity`
  - método o sección: anotaciones `@Size`, `@NotNull` y `prePersist`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@Size(min = 3, max = 3)`
    - `if (presupuestoTotal == null) { presupuestoTotal = BigDecimal.ZERO; }`
- Estado:
  - Explícita

## REGLA-072
- Descripción exacta de la regla (lenguaje declarativo)
  - La orden de cambio requiere proyecto, código y tipo/estado; impacto_plazo no puede ser negativo.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/cambio/OrdenCambioEntity.java`
  - clase: `OrdenCambioEntity`
  - método o sección: anotaciones `@NotNull`, `@NotBlank`
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK `impacto_plazo >= 0`, UNIQUE `(proyecto_id, codigo)`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull @Column(name = "impacto_plazo", nullable = false)`
    - `CHECK (impacto_plazo >= 0)`
- Estado:
  - Explícita

## REGLA-073
- Descripción exacta de la regla (lenguaje declarativo)
  - El created_by es obligatorio en entidades auditables.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Auditoría
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/AuditEntity.java`
  - clase: `AuditEntity`
  - método o sección: atributo `createdBy`
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: ALTER COLUMN created_by SET NOT NULL
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull @CreatedBy @Column(name = "created_by", nullable = false, updatable = false)`
    - `ALTER COLUMN created_by SET NOT NULL;`
- Estado:
  - Explícita

## REGLA-074
- Descripción exacta de la regla (lenguaje declarativo)
  - En Lead: nombre_contacto obligatorio; email debe ser válido; fecha_solicitud no nula.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Marketing
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/marketing/LeadEntity.java`
  - clase: `LeadEntity`
  - método o sección: anotaciones `@NotBlank`, `@Email`, `@CreationTimestamp`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank @Column(name = "nombre_contacto", nullable = false)`
    - `@Email @Column(name = "email", length = 200)`
    - `@CreationTimestamp @Column(name = "fecha_solicitud", nullable = false, updatable = false)`
- Estado:
  - Explícita

## REGLA-075
- Descripción exacta de la regla (lenguaje declarativo)
  - En usuario: nombre, email y password no pueden estar vacíos; email debe ser válido; rol y activo no nulos.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/seguridad/UsuarioEntity.java`
  - clase: `UsuarioEntity`
  - método o sección: anotaciones `@NotBlank`, `@Email`, `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank @Email @Column(name = "email", nullable = false)`
    - `@NotNull @Enumerated(EnumType.STRING) @Column(name = "rol", nullable = false)`
- Estado:
  - Explícita

## REGLA-076
- Descripción exacta de la regla (lenguaje declarativo)
  - En ReporteProduccion: fecha_reporte, responsable_id y estado son obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/produccion/ReporteProduccionEntity.java`
  - clase: `ReporteProduccionEntity`
  - método o sección: anotaciones `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull @Column(name = "fecha_reporte", nullable = false)`
    - `@NotNull @Column(name = "responsable_id", nullable = false)`
- Estado:
  - Explícita

## REGLA-077
- Descripción exacta de la regla (lenguaje declarativo)
  - En DetalleRPC: reporte_id, partida_id y cantidad_reportada son obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/persistence/entity/produccion/DetalleRPCEntity.java`
  - clase: `DetalleRPCEntity`
  - método o sección: anotaciones `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull @JoinColumn(name = "reporte_id", nullable = false)`
    - `@NotNull @Column(name = "cantidad_reportada", nullable = false)`
- Estado:
  - Explícita

## REGLA-078
- Descripción exacta de la regla (lenguaje declarativo)
  - Para login: email debe ser válido y no vacío; password es obligatoria.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/auth/dto/LoginRequest.java`
  - clase: `LoginRequest`
  - método o sección: anotaciones `@Email`, `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@Email(message = "El email debe ser válido")`
    - `@NotBlank(message = "La contraseña es obligatoria")`
- Estado:
  - Explícita

## REGLA-079
- Descripción exacta de la regla (lenguaje declarativo)
  - Para registro: nombreCompleto obligatorio; email válido y obligatorio; password obligatoria con tamaño 6-72.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/auth/dto/RegisterRequest.java`
  - clase: `RegisterRequest`
  - método o sección: anotaciones `@NotBlank`, `@Email`, `@Size`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@Size(min = 6, max = 72)`
- Estado:
  - Explícita

## REGLA-080
- Descripción exacta de la regla (lenguaje declarativo)
  - Para crear lead público: nombreContacto obligatorio; email válido si se provee; límites de tamaño.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Marketing
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/publico/dto/CrearLeadRequest.java`
  - clase: `CrearLeadRequest`
  - método o sección: anotaciones `@NotBlank`, `@Email`, `@Size`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "El nombre de contacto es obligatorio")`
- Estado:
  - Explícita

## REGLA-081
- Descripción exacta de la regla (lenguaje declarativo)
  - En RPC request: fechaReporte y responsableId obligatorios; debe incluir al menos un detalle.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/produccion/dto/CrearReporteProduccionRequest.java`
  - clase: `CrearReporteProduccionRequest`
  - método o sección: anotaciones `@NotNull`, `@NotEmpty`
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/produccion/dto/ActualizarReporteProduccionRequest.java`
  - clase: `ActualizarReporteProduccionRequest`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull(message = "La fecha del reporte es obligatoria")`
    - `@NotEmpty(message = "Debe incluir al menos un detalle")`
- Estado:
  - Explícita

## REGLA-082
- Descripción exacta de la regla (lenguaje declarativo)
  - En detalle RPC request: partidaId y cantidad reportada obligatorias; cantidad positiva.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/produccion/dto/DetalleRPCRequest.java`
  - clase: `DetalleRPCRequest`
  - método o sección: anotaciones `@NotNull`, `@Positive`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@Positive(message = "La cantidad reportada debe ser positiva")`
- Estado:
  - Explícita

## REGLA-083
- Descripción exacta de la regla (lenguaje declarativo)
  - En aprobación RPC request: aprobadorId es obligatorio.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/produccion/dto/AprobarReporteRequest.java`
  - clase: `AprobarReporteRequest`
  - método o sección: anotación `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull(message = "El aprobadorId es obligatorio")`
- Estado:
  - Explícita

## REGLA-084
- Descripción exacta de la regla (lenguaje declarativo)
  - En rechazo RPC request: aprobadorId y motivo son obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/produccion/dto/RechazarReporteRequest.java`
  - clase: `RechazarReporteRequest`
  - método o sección: anotaciones `@NotNull`, `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "El motivo de rechazo es obligatorio")`
- Estado:
  - Explícita

## REGLA-085
- Descripción exacta de la regla (lenguaje declarativo)
  - Para registrar movimiento de almacén: almacenId, recursoId, tipoMovimiento, fechaMovimiento, cantidad y precioUnitario son obligatorios; cantidad > 0; precioUnitario >= 0.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/almacen/dto/RegistrarMovimientoAlmacenRequest.java`
  - clase: `RegistrarMovimientoAlmacenRequest`
  - método o sección: anotaciones `@NotNull`, `@DecimalMin`, `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@DecimalMin(value = "0.000001", message = "La cantidad debe ser mayor a cero")`
    - `@DecimalMin(value = "0.0", message = "El precio unitario debe ser mayor o igual a cero")`
- Estado:
  - Explícita

## REGLA-086
- Descripción exacta de la regla (lenguaje declarativo)
  - Para calcular reajuste: proyectoId, presupuestoId, fechaCorte, códigos y fechas de índice base/actual son obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Reajuste
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/reajuste/dto/CalcularReajusteRequest.java`
  - clase: `CalcularReajusteRequest`
  - método o sección: anotaciones `@NotNull`, `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "El código del índice base es obligatorio")`
- Estado:
  - Explícita

## REGLA-087
- Descripción exacta de la regla (lenguaje declarativo)
  - Para generar estimación: fechas de corte/inicio/fin y detalles son obligatorios; porcentajes no negativos.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/estimacion/dto/GenerarEstimacionRequest.java`
  - clase: `GenerarEstimacionRequest`
  - método o sección: anotaciones `@NotNull`, `@DecimalMin`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull(message = "Los detalles son obligatorios")`
    - `@DecimalMin(value = "0.0", message = "El porcentaje de anticipo no puede ser negativo")`
- Estado:
  - Explícita

## REGLA-088
- Descripción exacta de la regla (lenguaje declarativo)
  - En detalle de estimación request: partidaId, cantidadAvance y precioUnitario obligatorios; no negativos.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/estimacion/dto/GenerarEstimacionRequest.java`
  - clase: `GenerarEstimacionRequest.DetalleEstimacionItem`
  - método o sección: anotaciones `@NotNull`, `@DecimalMin`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@DecimalMin(value = "0.0", message = "La cantidad de avance no puede ser negativa")`
- Estado:
  - Explícita

## REGLA-089
- Descripción exacta de la regla (lenguaje declarativo)
  - Para programar actividad: partidaId, fechaInicio y fechaFin son obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/cronograma/dto/ProgramarActividadRequest.java`
  - clase: `ProgramarActividadRequest`
  - método o sección: anotaciones `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull(message = "La fecha de inicio es obligatoria")`
- Estado:
  - Explícita

## REGLA-090
- Descripción exacta de la regla (lenguaje declarativo)
  - En configuración laboral request: días no negativos; porcentaje seguridad social entre 0 y 100; días laborables obligatorios y positivos.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - RRHH / Sobrecostos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/sobrecosto/dto/ConfigurarLaboralRequest.java`
  - clase: `ConfigurarLaboralRequest`
  - método o sección: anotaciones `@Min`, `@DecimalMin`, `@DecimalMax`, `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@DecimalMax(value = "100.0", message = "El porcentaje de seguridad social no puede ser mayor a 100%")`
- Estado:
  - Explícita

## REGLA-091
- Descripción exacta de la regla (lenguaje declarativo)
  - Para registrar avance físico: fecha y metradoEjecutado obligatorios; metradoEjecutado positivo.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Avance
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/avance/dto/RegistrarAvanceRequest.java`
  - clase: `RegistrarAvanceRequest`
  - método o sección: anotaciones `@NotNull`, `@Positive`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@Positive(message = "El metrado ejecutado debe ser positivo")`
- Estado:
  - Explícita

## REGLA-092
- Descripción exacta de la regla (lenguaje declarativo)
  - Para registrar compra: proyectoId, fecha, proveedor y detalles obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Compras
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/compra/dto/RegistrarCompraRequest.java`
  - clase: `RegistrarCompraRequest`
  - método o sección: anotaciones `@NotNull`, `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "El proveedor es obligatorio")`
- Estado:
  - Explícita

## REGLA-093
- Descripción exacta de la regla (lenguaje declarativo)
  - En detalle de compra request: recursoId, partidaId, cantidad y precioUnitario obligatorios; cantidad y precioUnitario no negativos.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Compras
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/compra/dto/CompraDetalleRequest.java`
  - clase: `CompraDetalleRequest`
  - método o sección: anotaciones `@NotNull`, `@DecimalMin`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@DecimalMin(value = "0.0", message = "La cantidad no puede ser negativa")`
- Estado:
  - Explícita

## REGLA-094
- Descripción exacta de la regla (lenguaje declarativo)
  - Para crear APU: lista de insumos obligatoria.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - APU
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/apu/dto/CrearApuRequest.java`
  - clase: `CrearApuRequest`
  - método o sección: anotación `@NotNull`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull(message = "La lista de insumos es obligatoria")`
- Estado:
  - Explícita

## REGLA-095
- Descripción exacta de la regla (lenguaje declarativo)
  - En insumo APU request: recursoId, cantidad y precioUnitario obligatorios; cantidad y precioUnitario no negativos.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - APU
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/apu/dto/ApuInsumoRequest.java`
  - clase: `ApuInsumoRequest`
  - método o sección: anotaciones `@NotNull`, `@DecimalMin`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")`
- Estado:
  - Explícita

## REGLA-096
- Descripción exacta de la regla (lenguaje declarativo)
  - Para crear partida: presupuestoId, item, descripcion y nivel obligatorios; metrado no negativo.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Partidas
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/partida/dto/CrearPartidaRequest.java`
  - clase: `CrearPartidaRequest`
  - método o sección: anotaciones `@NotNull`, `@NotBlank`, `@DecimalMin`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@DecimalMin(value = "0.0", message = "El metrado no puede ser negativo")`
- Estado:
  - Explícita

## REGLA-097
- Descripción exacta de la regla (lenguaje declarativo)
  - Para crear proyecto: nombre obligatorio.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/proyecto/dto/CrearProyectoRequest.java`
  - clase: `CrearProyectoRequest`
  - método o sección: anotación `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "El nombre del proyecto es obligatorio")`
- Estado:
  - Explícita

## REGLA-098
- Descripción exacta de la regla (lenguaje declarativo)
  - Para crear presupuesto: proyectoId y nombre obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/presupuesto/dto/CrearPresupuestoRequest.java`
  - clase: `CrearPresupuestoRequest`
  - método o sección: anotaciones `@NotNull`, `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotNull(message = "El ID del proyecto es obligatorio")`
- Estado:
  - Explícita

## REGLA-099
- Descripción exacta de la regla (lenguaje declarativo)
  - Para crear recurso: nombre, tipo y unidadBase obligatorios.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/recurso/dto/CrearRecursoRequest.java`
  - clase: `CrearRecursoRequest`
  - método o sección: anotaciones `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "La unidad base del recurso es obligatoria")`
- Estado:
  - Explícita

## REGLA-100
- Descripción exacta de la regla (lenguaje declarativo)
  - El sistema opera exclusivamente en modalidad online.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Arquitectura
- Origen técnico EXACTO:
  - archivo: `docs/context/BUSINESS_MANIFESTO.md`
  - clase: N/A
  - método o sección: "Decisión Arquitectónica No Negociable"
- Evidencia:
  - fragmento de código o referencia precisa
    - `BudgetPro opera exclusivamente en modalidad online.`
- Estado:
  - Explícita

## REGLA-101
- Descripción exacta de la regla (lenguaje declarativo)
  - Un presupuesto aprobado constituye un contrato digital inmutable.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `docs/context/BUSINESS_MANIFESTO.md`
  - método o sección: "Principio Supremo: El Presupuesto es Ley"
- Evidencia:
  - fragmento de código o referencia precisa
    - `El presupuesto aprobado constituye un contrato digital inmutable.`
- Estado:
  - Explícita

## REGLA-102
- Descripción exacta de la regla (lenguaje declarativo)
  - Ningún proceso operativo puede existir fuera del presupuesto (compras, inventarios, mano de obra, avances físicos, pagos).
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
  - Compras
  - Inventarios
  - RRHH
  - Producción
- Origen técnico EXACTO:
  - archivo: `docs/context/BUSINESS_MANIFESTO.md`
  - método o sección: "Principio Supremo: El Presupuesto es Ley"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Ningún proceso operativo puede existir fuera del presupuesto: ...`
- Estado:
  - Explícita

## REGLA-103
- Descripción exacta de la regla (lenguaje declarativo)
  - Los datos históricos no se corrigen; se explican mediante eventos formales.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Auditoría
- Origen técnico EXACTO:
  - archivo: `docs/context/BUSINESS_MANIFESTO.md`
  - método o sección: "Principio de Verdad No Retroactiva"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Los datos históricos no se corrigen, se explican mediante eventos formales...`
- Estado:
  - Explícita

## REGLA-104
- Descripción exacta de la regla (lenguaje declarativo)
  - Toda excepción debe clasificarse, requerir autorización explícita y quedar registrada de forma permanente.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/context/BUSINESS_MANIFESTO.md`
  - método o sección: "Principio de Excepción Formal"
- Evidencia:
  - fragmento de código o referencia precisa
    - `toda excepción debe clasificarse, debe requerir autorización explícita, debe quedar registrada de forma permanente.`
- Estado:
  - Explícita

## REGLA-105
- Descripción exacta de la regla (lenguaje declarativo)
  - Un Proyecto en BORRADOR no puede ejecutar compras, inventarios, mano de obra ni avances físicos.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "2.1 Creación"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Un Proyecto en BORRADOR no puede: ejecutar compras, registrar inventarios, registrar mano de obra, registrar avances físicos...`
- Estado:
  - Explícita

## REGLA-106
- Descripción exacta de la regla (lenguaje declarativo)
  - Un Proyecto solo puede activarse si existe Presupuesto congelado y Snapshot inmutable.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "2.2 Activación Contractual"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Un Proyecto solo puede pasar a estado ACTIVO si: existe al menos un Presupuesto asociado, dicho Presupuesto se encuentra en estado CONGELADO, existe un Snapshot inmutable de la Línea Base.`
- Estado:
  - Explícita

## REGLA-107
- Descripción exacta de la regla (lenguaje declarativo)
  - La Línea Base requiere Presupuesto CONGELADO y Cronograma CONGELADO; la ausencia invalida ejecución.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Presupuesto
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "2.3 Línea Base Contractual"
- Evidencia:
  - fragmento de código o referencia precisa
    - `La Línea Base del Proyecto está compuesta obligatoriamente por: Presupuesto CONGELADO, Cronograma CONGELADO.`
- Estado:
  - Explícita

## REGLA-108
- Descripción exacta de la regla (lenguaje declarativo)
  - Estados del Proyecto: BORRADOR, ACTIVO, SUSPENDIDO, CERRADO con semántica definida.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "3. Estados del Proyecto"
- Evidencia:
  - fragmento de código o referencia precisa
    - `### BORRADOR ... ### ACTIVO ... ### SUSPENDIDO ... ### CERRADO ...`
- Estado:
  - Explícita

## REGLA-109
- Descripción exacta de la regla (lenguaje declarativo)
  - Transiciones permitidas: BORRADOR→ACTIVO, ACTIVO→SUSPENDIDO, ACTIVO→CERRADO, SUSPENDIDO→ACTIVO; prohibidas: BORRADOR→CERRADO, CERRADO→otros.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "4. Reglas de Transición de Estado"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Las transiciones permitidas son: ... Transiciones prohibidas: ...`
- Estado:
  - Explícita

## REGLA-110
- Descripción exacta de la regla (lenguaje declarativo)
  - Un Presupuesto solo puede crearse asociado a un Proyecto existente y solo uno puede estar ACTIVO por Proyecto.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "2.1 Creación"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Un Presupuesto solo puede crearse asociado a un Proyecto existente.`
    - `Solo un Presupuesto puede estar ACTIVO (vigente) por Proyecto.`
- Estado:
  - Explícita

## REGLA-111
- Descripción exacta de la regla (lenguaje declarativo)
  - Estados del Presupuesto: BORRADOR, CONGELADO, INVALIDADO con semántica definida.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "3. Estados del Presupuesto"
- Evidencia:
  - fragmento de código o referencia precisa
    - `### BORRADOR ... ### CONGELADO ... ### INVALIDADO ...`
- Estado:
  - Explícita

## REGLA-112
- Descripción exacta de la regla (lenguaje declarativo)
  - Al congelar presupuesto se genera Snapshot inmutable con partidas, cantidades, precios, rendimientos, duraciones y BAC.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "4.1 Proceso de Congelamiento"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Al pasar de BORRADOR a CONGELADO, el sistema DEBE: generar un Snapshot inmutable...`
- Estado:
  - Explícita

## REGLA-113
- Descripción exacta de la regla (lenguaje declarativo)
  - Las Órdenes de Cambio no sobrescriben la Línea Base; ajustan el BAC y mantienen el Presupuesto original visible.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "7. Relación con Órdenes de Cambio"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Las Órdenes de Cambio crean capas aditivas o deductivas... No sobrescriben la Línea Base.`
- Estado:
  - Explícita

## REGLA-114
- Descripción exacta de la regla (lenguaje declarativo)
  - El monto acumulado de Órdenes de Cambio no puede exceder ±20% del monto contractual original congelado.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "8. Límites Legales"
  - archivo: `docs/modules/CAMBIOS_SPECS.md`
  - método o sección: "11. Límites Legales"
- Evidencia:
  - fragmento de código o referencia precisa
    - `no puede exceder ±20 % del monto contractual original congelado.`
- Estado:
  - Explícita

## REGLA-115
- Descripción exacta de la regla (lenguaje declarativo)
  - Compra sin clasificación es ilegal; compra sin Partida es válida.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Compras
- Origen técnico EXACTO:
  - archivo: `docs/modules/COMPRAS_SPECS.md`
  - método o sección: "3. Clasificación Obligatoria de Compras" y "4.2 Compras NO Asociadas a Partida"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Compra sin Partida es válida. Compra sin clasificación es ilegal.`
- Estado:
  - Explícita

## REGLA-116
- Descripción exacta de la regla (lenguaje declarativo)
  - Superar el tope del APU no bloquea la compra; se emite alerta y se exige motivo clasificado.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Compras
- Origen técnico EXACTO:
  - archivo: `docs/modules/COMPRAS_SPECS.md`
  - método o sección: "5. Purchasing Caps"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Superar el tope: NO bloquea la compra... el sistema DEBE emitir alerta técnica... exigir motivo clasificado.`
- Estado:
  - Explícita

## REGLA-117
- Descripción exacta de la regla (lenguaje declarativo)
  - Toda compra de bienes físicos genera entrada a inventario; inventario sin compra es ilegal.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Compras
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `docs/modules/COMPRAS_SPECS.md`
  - método o sección: "9. Relación con Inventarios"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Toda compra de bienes físicos genera entrada a inventario.`
    - `Inventario sin compra es ilegal.`
- Estado:
  - Explícita

## REGLA-118
- Descripción exacta de la regla (lenguaje declarativo)
  - Un movimiento de inventario solo puede existir si proyecto ACTIVO, presupuesto CONGELADO, compra válida y salida imputada a Partida.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `docs/modules/INVENTARIOS_SPECS.md`
  - método o sección: "2. Condiciones de Existencia"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Un movimiento de inventario solo puede existir si: el Proyecto está en estado ACTIVO, existe un Presupuesto CONGELADO vigente, el material proviene de una Compra válida, la salida se imputa a una Partida específica.`
- Estado:
  - Explícita

## REGLA-119
- Descripción exacta de la regla (lenguaje declarativo)
  - Salida sin Partida es ilegal; entrada sin compra es ilegal.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `docs/modules/INVENTARIOS_SPECS.md`
  - método o sección: "3.2 Salida de Inventario" y "7. Relación con Compras"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Salida sin Partida es ilegal.`
    - `Inventario sin compra es ilegal.`
- Estado:
  - Explícita

## REGLA-120
- Descripción exacta de la regla (lenguaje declarativo)
  - La salida de inventario reduce saldo disponible del APU; exceso debe registrarse como Excepción formal.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
  - APU
- Origen técnico EXACTO:
  - archivo: `docs/modules/INVENTARIOS_SPECS.md`
  - método o sección: "4. Relación con Partidas y APU"
- Evidencia:
  - fragmento de código o referencia precisa
    - `El sistema debe validar que la cantidad solicitada no exceda los límites contractuales, salvo Excepción formal registrada.`
- Estado:
  - Explícita

## REGLA-121
- Descripción exacta de la regla (lenguaje declarativo)
  - Diferencias entre inventario físico y sistema deben registrarse como Excepción.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/INVENTARIOS_SPECS.md`
  - método o sección: "8. Control de Pérdidas"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Diferencias entre inventario físico y sistema deben registrarse como Excepción.`
- Estado:
  - Explícita

## REGLA-122
- Descripción exacta de la regla (lenguaje declarativo)
  - Mano de obra es costo real; todo tiempo trabajado cuesta y deja rastro.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - RRHH
- Origen técnico EXACTO:
  - archivo: `docs/modules/RRHH_SPECS.md`
  - método o sección: "2. Principio Rector"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Todo tiempo trabajado: cuesta, deja rastro, afecta utilidad, aunque no haya pago inmediato.`
- Estado:
  - Explícita

## REGLA-123
- Descripción exacta de la regla (lenguaje declarativo)
  - El costo de mano de obra nunca se registra como salario neto; se calcula costo empresa con prestaciones.
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - RRHH
- Origen técnico EXACTO:
  - archivo: `docs/modules/RRHH_SPECS.md`
  - método o sección: "4. Costo Empresa y Factor de Prestación"
- Evidencia:
  - fragmento de código o referencia precisa
    - `El costo de Mano de Obra NUNCA se registra como salario neto.`
- Estado:
  - Explícita

## REGLA-124
- Descripción exacta de la regla (lenguaje declarativo)
  - No se permite que un trabajador esté asignado a dos proyectos ACTIVO el mismo día y horario.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - RRHH
- Origen técnico EXACTO:
  - archivo: `docs/modules/RRHH_SPECS.md`
  - método o sección: "5. Asignación de Personal a Proyectos"
- Evidencia:
  - fragmento de código o referencia precisa
    - `No puede estar asignado a dos Proyectos ACTIVO el mismo día en el mismo horario.`
- Estado:
  - Explícita

## REGLA-125
- Descripción exacta de la regla (lenguaje declarativo)
  - El tareo debe validar Proyecto ACTIVO, trabajador asignado, coherencia de fechas y no duplicidad horaria.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - RRHH
- Origen técnico EXACTO:
  - archivo: `docs/modules/RRHH_SPECS.md`
  - método o sección: "6.2 Validaciones"
- Evidencia:
  - fragmento de código o referencia precisa
    - `El sistema debe validar: Proyecto ACTIVO, trabajador asignado, coherencia de fechas, no duplicidad horaria.`
- Estado:
  - Explícita

## REGLA-126
- Descripción exacta de la regla (lenguaje declarativo)
  - El avance físico: partidaId obligatorio, fecha obligatoria, metradoEjecutado no negativo.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Avance
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/avance/model/AvanceFisico.java`
  - clase: `AvanceFisico`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (metradoEjecutado != null && metradoEjecutado.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException("El metrado ejecutado no puede ser negativo"); }`
- Estado:
  - Explícita

## REGLA-127
- Descripción exacta de la regla (lenguaje declarativo)
  - La valuación requiere proyectoId, fechaCorte, código no vacío y estado no nulo.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Valuación
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/avance/model/Valuacion.java`
  - clase: `Valuacion`
  - método o sección: `validarInvariantes`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (codigo == null || codigo.isBlank()) { throw new IllegalArgumentException("El código no puede estar vacío"); }`
- Estado:
  - Explícita

## REGLA-128
- Descripción exacta de la regla (lenguaje declarativo)
  - No se puede crear un recurso con el mismo nombre normalizado si ya existe.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/recurso/usecase/CrearRecursoUseCaseImpl.java`
  - clase: `CrearRecursoUseCaseImpl`
  - método o sección: `ejecutar`
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (recursoRepository.existsByNombre(nombreNormalizado)) { throw new RecursoDuplicadoException(nombreNormalizado); }`
- Estado:
  - Explícita

## REGLA-129
- Descripción exacta de la regla (lenguaje declarativo)
  - El tipo de recurso debe ser un valor válido del enum TipoRecurso.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/application/recurso/usecase/CrearRecursoUseCaseImpl.java`
  - clase: `CrearRecursoUseCaseImpl`
  - método o sección: `parsearTipoRecurso`
- Evidencia:
  - fragmento de código o referencia precisa
    - `return TipoRecurso.valueOf(tipoStr.toUpperCase());`
- Estado:
  - Explícita

## REGLA-130
- Descripción exacta de la regla (lenguaje declarativo)
  - La estimación es única por proyecto y número de estimación.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V11__create_estimacion_schema.sql`
  - método o sección: UNIQUE `(proyecto_id, numero_estimacion)`
- Evidencia:
  - fragmento de código o referencia precisa
    - `UNIQUE (proyecto_id, numero_estimacion)`
- Estado:
  - Explícita

## REGLA-131
- Descripción exacta de la regla (lenguaje declarativo)
  - El detalle de estimación es único por (estimacion_id, partida_id).
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Estimaciones
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V11__create_estimacion_schema.sql`
  - método o sección: UNIQUE `(estimacion_id, partida_id)`
- Evidencia:
  - fragmento de código o referencia precisa
    - `UNIQUE (estimacion_id, partida_id)`
- Estado:
  - Explícita

## REGLA-132
- Descripción exacta de la regla (lenguaje declarativo)
  - Un programa de obra es único por proyecto (proyecto_id UNIQUE).
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V10__create_cronograma_schema.sql`
  - método o sección: `programa_obra` con `proyecto_id UNIQUE`
- Evidencia:
  - fragmento de código o referencia precisa
    - `proyecto_id UUID NOT NULL UNIQUE`
- Estado:
  - Explícita

## REGLA-133
- Descripción exacta de la regla (lenguaje declarativo)
  - Una actividad programada es única por partida (partida_id UNIQUE).
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V10__create_cronograma_schema.sql`
  - método o sección: `actividad_programada` con `partida_id UNIQUE`
- Evidencia:
  - fragmento de código o referencia precisa
    - `partida_id UUID NOT NULL UNIQUE`
- Estado:
  - Explícita

## REGLA-134
- Descripción exacta de la regla (lenguaje declarativo)
  - En movimiento_almacen, el tipo y tipo_movimiento deben estar en {ENTRADA, SALIDA, DEVOLUCION}.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK `tipo`, CHECK `tipo_movimiento`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (tipo IN ('ENTRADA', 'SALIDA', 'DEVOLUCION'));`
    - `CHECK (tipo_movimiento IN ('ENTRADA', 'SALIDA', 'DEVOLUCION'));`
- Estado:
  - Explícita

## REGLA-135
- Descripción exacta de la regla (lenguaje declarativo)
  - En orden_cambio, tipo debe estar en {ADICIONAL, DEDUCTIVO, PRECIO, PLAZO} y estado en {SOLICITADO, APROBADO, RECHAZADO}.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: CHECK `tipo`, CHECK `estado`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CHECK (tipo IN ('ADICIONAL', 'DEDUCTIVO', 'PRECIO', 'PLAZO'))`
    - `CHECK (estado IN ('SOLICITADO', 'APROBADO', 'RECHAZADO'))`
- Estado:
  - Explícita

## REGLA-136
- Descripción exacta de la regla (lenguaje declarativo)
  - En almacén, el código es único por proyecto.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V14__create_almacen_inventarios_schema.sql`
  - método o sección: UNIQUE `(proyecto_id, codigo)`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CONSTRAINT uq_almacen_codigo_proyecto UNIQUE (proyecto_id, codigo)`
- Estado:
  - Explícita

## REGLA-137
- Descripción exacta de la regla (lenguaje declarativo)
  - En stock_actual, la combinación (almacen_id, recurso_id) es única.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V14__create_almacen_inventarios_schema.sql`
  - método o sección: índice único `idx_stock_actual_almacen_recurso`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CREATE UNIQUE INDEX idx_stock_actual_almacen_recurso ON stock_actual(almacen_id, recurso_id);`
- Estado:
  - Explícita

## REGLA-138
- Descripción exacta de la regla (lenguaje declarativo)
  - JWT expira en 24 horas por defecto (jwt.expiration-hours: 24).
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Seguridad
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/application.yml`
  - método o sección: `jwt.expiration-hours`
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/security/jwt/JwtService.java`
  - método o sección: constructor
- Evidencia:
  - fragmento de código o referencia precisa
    - `jwt: expiration-hours: 24`
    - `this.expiration = Duration.ofHours(expirationHours);`
- Estado:
  - Explícita

## REGLA-139
- Descripción exacta de la regla (lenguaje declarativo)
  - En RPC legacy request: fechaReporte obligatoria y al menos un item.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/dto/produccion/CrearReporteRequest.java`
  - clase: `CrearReporteRequest`
  - método o sección: anotaciones `@NotNull`, `@NotEmpty`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotEmpty(message = "Debe incluir al menos un item")`
- Estado:
  - Explícita

## REGLA-140
- Descripción exacta de la regla (lenguaje declarativo)
  - En item RPC legacy: partidaId y cantidad obligatorias; cantidad positiva.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/dto/produccion/DetalleItemRequest.java`
  - clase: `DetalleItemRequest`
  - método o sección: anotaciones `@NotNull`, `@Positive`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@Positive(message = "La cantidad debe ser positiva")`
- Estado:
  - Explícita

## REGLA-141
- Descripción exacta de la regla (lenguaje declarativo)
  - En rechazo RPC legacy: motivo obligatorio.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Producción (RPC)
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/dto/produccion/RechazarReporteRequest.java`
  - clase: `RechazarReporteRequest`
  - método o sección: anotación `@NotBlank`
- Evidencia:
  - fragmento de código o referencia precisa
    - `@NotBlank(message = "El motivo es obligatorio")`
- Estado:
  - Explícita

## REGLA-142
- Descripción exacta de la regla (lenguaje declarativo)
  - El acumulado no debería superar el metrado total de la partida (alertar, no bloquear).
- Tipo:
  - Financiera
- Módulo(s) afectado(s)
  - Avance
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/domain/finanzas/avance/model/AvanceFisico.java`
  - clase: `AvanceFisico`
  - método o sección: comentario de invariantes
- Evidencia:
  - fragmento de código o referencia precisa
    - `- (Opcional MVP) El acumulado no debería superar el metrado total de la partida (Alertar, no bloquear)`
- Estado:
  - Implícita

## REGLA-143
- Descripción exacta de la regla (lenguaje declarativo)
  - El presupuesto de línea base es único cuando es_linea_base = true.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `backend/src/main/resources/db/migration/V16__core_immutable_schema.sql`
  - método o sección: índice único `idx_presupuesto_linea_base`
- Evidencia:
  - fragmento de código o referencia precisa
    - `CREATE UNIQUE INDEX IF NOT EXISTS idx_presupuesto_linea_base ON presupuesto(proyecto_id) WHERE es_linea_base = TRUE;`
- Estado:
  - Explícita

## REGLA-144
- Descripción exacta de la regla (lenguaje declarativo)
  - En DTO de recurso, atributos se inicializan como mapa vacío si es nulo.
- Tipo:
  - Técnica
- Módulo(s) afectado(s)
  - Recursos
- Origen técnico EXACTO:
  - archivo: `backend/src/main/java/com/budgetpro/infrastructure/rest/recurso/dto/CrearRecursoRequest.java`
  - clase: `CrearRecursoRequest`
  - método o sección: constructor compacto
- Evidencia:
  - fragmento de código o referencia precisa
    - `if (atributos == null) { atributos = new HashMap<>(); }`
- Estado:
  - Explícita

## REGLA-145
- Descripción exacta de la regla (lenguaje declarativo)
  - El Proyecto es una entidad contractual que habilita o bloquea la ejecución según el estado del presupuesto asociado.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "1. Propósito del Módulo"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Un Proyecto es una entidad contractual que habilita o bloquea la ejecución según el estado del presupuesto asociado.`
- Estado:
  - Explícita

## REGLA-146
- Descripción exacta de la regla (lenguaje declarativo)
  - Si no hay Presupuesto congelado, la activación del Proyecto debe bloquearse con el mensaje "Este proyecto no puede activarse sin un presupuesto congelado."
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "2.2 Activación Contractual"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Mensaje obligatorio al usuario: "Este proyecto no puede activarse sin un presupuesto congelado."`
- Estado:
  - Explícita

## REGLA-147
- Descripción exacta de la regla (lenguaje declarativo)
  - Un Proyecto solo puede activarse si existe Cronograma congelado del mismo Proyecto; si no, se bloquea con el mensaje "Este proyecto no puede activarse sin un cronograma congelado."
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "2.4 Condición Temporal de Activación"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Mensaje obligatorio al usuario: "Este proyecto no puede activarse sin un cronograma congelado."`
- Estado:
  - Explícita

## REGLA-148
- Descripción exacta de la regla (lenguaje declarativo)
  - Un Snapshot de Presupuesto sin Cronograma no constituye una Línea Base válida.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Presupuesto
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "2.3 Línea Base Contractual"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Un Snapshot de Presupuesto sin Cronograma NO constituye una Línea Base válida.`
- Estado:
  - Explícita

## REGLA-149
- Descripción exacta de la regla (lenguaje declarativo)
  - Si el Presupuesto principal se invalida, el Proyecto debe pasar a SUSPENDIDO automáticamente.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Presupuesto
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "5.1 Presupuesto"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Si el Presupuesto principal se invalida, el Proyecto debe pasar a SUSPENDIDO automáticamente.`
- Estado:
  - Explícita

## REGLA-150
- Descripción exacta de la regla (lenguaje declarativo)
  - Ningún módulo operativo puede ejecutar acciones si el Proyecto no está en estado ACTIVO.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Compras
  - Inventarios
  - RRHH
  - Producción
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "5.3 Compras, Inventarios y RRHH"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Ningún módulo operativo puede ejecutar acciones si el Proyecto no está en estado ACTIVO.`
- Estado:
  - Explícita

## REGLA-151
- Descripción exacta de la regla (lenguaje declarativo)
  - Todo cambio de estado del Proyecto debe registrar estado anterior, nuevo, usuario, fecha/hora y motivo.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Proyectos
  - Auditoría
- Origen técnico EXACTO:
  - archivo: `docs/modules/PROYECTO_SPECS.md`
  - método o sección: "5.4 Auditoría"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Todo cambio de estado del Proyecto debe quedar registrado: estado anterior, estado nuevo, usuario responsable, fecha y hora, motivo.`
- Estado:
  - Explícita

## REGLA-152
- Descripción exacta de la regla (lenguaje declarativo)
  - Un Presupuesto CONGELADO no permite modificación directa; cambios solo mediante Órdenes de Cambio o Excepciones formales.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "4.2 Inmutabilidad"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Cualquier cambio posterior se gestiona exclusivamente mediante: Órdenes de Cambio, o Excepciones formales.`
- Estado:
  - Explícita

## REGLA-153
- Descripción exacta de la regla (lenguaje declarativo)
  - Toda compra debe vincularse a una Partida válida del Presupuesto CONGELADO.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
  - Compras
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "6.1 Compras"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Toda compra debe vincularse a una Partida válida del Presupuesto CONGELADO.`
- Estado:
  - Explícita

## REGLA-154
- Descripción exacta de la regla (lenguaje declarativo)
  - Inventario sin Partida es ilegal.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
  - Inventarios
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "6.2 Inventario"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Inventario sin Partida es ilegal.`
- Estado:
  - Explícita

## REGLA-155
- Descripción exacta de la regla (lenguaje declarativo)
  - Las Órdenes de Cambio ajustan el BAC y las métricas de control; el Presupuesto original permanece visible.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "7. Relación con Órdenes de Cambio"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Ajustan el BAC y las métricas de control. El Presupuesto original siempre permanece visible para auditoría.`
- Estado:
  - Explícita

## REGLA-156
- Descripción exacta de la regla (lenguaje declarativo)
  - Toda Orden de Cambio que afecte plazo debe generar ajuste formal del Cronograma contractual.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
  - Cronograma
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "9. Órdenes de Cambio y Plazo Contractual"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Toda Orden de Cambio que afecte plazo DEBE generar un ajuste formal del Cronograma contractual.`
- Estado:
  - Explícita

## REGLA-157
- Descripción exacta de la regla (lenguaje declarativo)
  - El exceso de consumo debe registrarse como Excepción de consumo o Insumo asociado a Orden de Cambio.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Presupuesto
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/PRESUPUESTO_SPECS.md`
  - método o sección: "10. Excesos de Consumo y Compras"
- Evidencia:
  - fragmento de código o referencia precisa
    - `El exceso de consumo NO se oculta... Debe registrarse como: Excepción de consumo, o Insumo asociado a Orden de Cambio.`
- Estado:
  - Explícita

## REGLA-158
- Descripción exacta de la regla (lenguaje declarativo)
  - Una Orden de Cambio o Excepción debe registrar tipo, partida afectada, monto, motivo, usuario, fecha y autorización; si falta, se rechaza.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/CAMBIOS_SPECS.md`
  - método o sección: "4.1 Requisitos Mínimos"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Si alguno de estos datos falta, el sistema DEBE rechazar el registro.`
- Estado:
  - Explícita

## REGLA-159
- Descripción exacta de la regla (lenguaje declarativo)
  - Las Excepciones no modifican la Línea Base ni ajustan el BAC; incrementan AC.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/CAMBIOS_SPECS.md`
  - método o sección: "5.2 Excepciones"
- Evidencia:
  - fragmento de código o referencia precisa
    - `No modifica la Línea Base. No ajusta el BAC. Impacta el Costo Real (AC).`
- Estado:
  - Explícita

## REGLA-160
- Descripción exacta de la regla (lenguaje declarativo)
  - Una desviación es permanente; no puede eliminarse ni editarse, solo anularse mediante evento correctivo trazado.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/CAMBIOS_SPECS.md`
  - método o sección: "9. Auditoría y No Repudio"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Toda desviación es permanente. No puede eliminarse ni editarse. Solo puede anularse mediante un evento correctivo trazado.`
- Estado:
  - Explícita

## REGLA-161
- Descripción exacta de la regla (lenguaje declarativo)
  - Las Órdenes de Cambio pueden registrarse en estado PROPUESTA, APROBADA CON EVIDENCIA DIFERIDA o RECHAZADA.
- Tipo:
  - Gobierno
- Módulo(s) afectado(s)
  - Cambios
- Origen técnico EXACTO:
  - archivo: `docs/modules/CAMBIOS_SPECS.md`
  - método o sección: "12. Aprobación Asíncrona y Evidencia Posterior"
- Evidencia:
  - fragmento de código o referencia precisa
    - `Una Orden de Cambio puede registrarse en estado: PROPUESTA, APROBADA CON EVIDENCIA DIFERIDA, RECHAZADA.`
- Estado:
  - Explícita
