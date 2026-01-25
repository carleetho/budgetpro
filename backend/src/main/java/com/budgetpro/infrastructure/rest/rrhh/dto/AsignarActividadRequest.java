package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.AsignarCuadrillaCommand;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AsignarActividadRequest(@NotNull(message = "El ID del proyecto es obligatorio") String proyectoId,

        @NotNull(message = "El ID de la partida es obligatorio") String partidaId,

        @NotNull(message = "La fecha de inicio es obligatoria") LocalDate fechaInicio,

        LocalDate fechaFin) {
    public AsignarCuadrillaCommand toCommand(String cuadrillaId) {
        return new AsignarCuadrillaCommand(UUID.fromString(cuadrillaId), UUID.fromString(proyectoId),
                UUID.fromString(partidaId), fechaInicio, fechaFin);
    }
}
