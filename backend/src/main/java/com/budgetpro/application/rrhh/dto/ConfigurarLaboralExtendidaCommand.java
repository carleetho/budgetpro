package com.budgetpro.application.rrhh.dto;

import com.budgetpro.domain.proyecto.model.ProyectoId;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command to configure extended labor parameters (FSR). All decimal fields are
 * optional; if null, defaults (0) will be used in the entity logic.
 */
public record ConfigurarLaboralExtendidaCommand(ProyectoId proyectoId, LocalDate fechaInicio, // Must be provided
        Integer diasAguinaldo, Integer diasVacaciones, BigDecimal porcentajeSeguridadSocial, Integer diasNoTrabajados,
        Integer diasLaborablesAno, BigDecimal factorHorasExtras, BigDecimal factorTurnoNocturno,
        BigDecimal factorRiesgo, BigDecimal factorRegional) {
}
