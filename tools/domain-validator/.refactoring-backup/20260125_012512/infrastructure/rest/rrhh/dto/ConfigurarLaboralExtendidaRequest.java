package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.ConfigurarLaboralExtendidaCommand;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ConfigurarLaboralExtendidaRequest(
        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,

        @PositiveOrZero(message = "Los días de aguinaldo no pueden ser negativos") Integer diasAguinaldo,

        @PositiveOrZero(message = "Los días de vacaciones no pueden ser negativos") Integer diasVacaciones,

        @PositiveOrZero(message = "El porcentaje de seguridad social no puede ser negativo") BigDecimal porcentajeSeguridadSocial,

        @PositiveOrZero(message = "Los días no trabajados no pueden ser negativos") Integer diasNoTrabajados,

        @Positive(message = "Los días laborables deben ser positivos") Integer diasLaborablesAno,

        @PositiveOrZero(message = "El factor de horas extras no puede ser negativo") BigDecimal factorHorasExtras,

        @PositiveOrZero(message = "El factor de turno nocturno no puede ser negativo") BigDecimal factorTurnoNocturno,

        @PositiveOrZero(message = "El factor de riesgo no puede ser negativo") BigDecimal factorRiesgo,

        @PositiveOrZero(message = "El factor regional no puede ser negativo") BigDecimal factorRegional) {
    public ConfigurarLaboralExtendidaCommand toCommand(String proyectoId) {
        return new ConfigurarLaboralExtendidaCommand(proyectoId != null ? ProyectoId.from(proyectoId) : null,
                fechaInicio, diasAguinaldo, diasVacaciones, porcentajeSeguridadSocial, diasNoTrabajados,
                diasLaborablesAno, factorHorasExtras, factorTurnoNocturno, factorRiesgo, factorRegional);
    }
}
