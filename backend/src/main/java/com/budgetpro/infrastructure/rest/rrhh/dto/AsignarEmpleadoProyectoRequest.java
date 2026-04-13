package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.AsignarEmpleadoProyectoCommand;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AsignarEmpleadoProyectoRequest(
        @NotNull(message = "El proyecto es obligatorio") UUID proyectoId,
        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal tarifaHora,
        String rolProyecto) {

    public AsignarEmpleadoProyectoCommand toCommand(UUID empleadoId) {
        return new AsignarEmpleadoProyectoCommand(empleadoId, proyectoId, fechaInicio, fechaFin, tarifaHora,
                rolProyecto);
    }
}
