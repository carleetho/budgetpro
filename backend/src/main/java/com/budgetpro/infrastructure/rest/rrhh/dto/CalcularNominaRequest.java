package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.CalcularNominaCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CalcularNominaRequest(@NotNull(message = "El ID del proyecto es obligatorio") String proyectoId,

        @NotNull(message = "El inicio del periodo es obligatorio") LocalDate periodoInicio,

        @NotNull(message = "El fin del periodo es obligatorio") LocalDate periodoFin,

        List<String> empleadoIds) {
    public CalcularNominaCommand toCommand() {
        return new CalcularNominaCommand(UUID.fromString(proyectoId), periodoInicio, periodoFin,
                empleadoIds != null ? empleadoIds.stream().map(UUID::fromString).collect(Collectors.toList()) : null);
    }
}
