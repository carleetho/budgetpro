package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.RegistrarAsistenciaCommand;
import com.budgetpro.domain.rrhh.model.EmpleadoId;
import com.budgetpro.domain.proyecto.model.ProyectoId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record RegistrarAsistenciaRequest(@NotNull(message = "El ID del empleado es obligatorio") String empleadoId,

        @NotNull(message = "El ID del proyecto es obligatorio") String proyectoId,

        @NotNull(message = "La fecha es obligatoria") @PastOrPresent(message = "La fecha no puede ser futura") LocalDate fecha,

        @NotNull(message = "La hora de entrada es obligatoria") @PastOrPresent(message = "La hora de entrada no puede ser futura") LocalDateTime horaEntrada,

        @NotNull(message = "La hora de salida es obligatoria") LocalDateTime horaSalida,

        String ubicacion) {
    public RegistrarAsistenciaCommand toCommand() {
        return new RegistrarAsistenciaCommand(EmpleadoId.fromString(empleadoId), ProyectoId.from(proyectoId), fecha,
                horaEntrada, horaSalida, ubicacion);
    }
}
