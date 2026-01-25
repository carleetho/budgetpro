package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.CrearCuadrillaCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record CrearCuadrillaRequest(@NotNull(message = "El ID del proyecto es obligatorio") String proyectoId,

        @NotBlank(message = "El nombre es obligatorio") String nombre,

        String tipo,

        @NotNull(message = "El ID del líder es obligatorio") String liderEmpleadoId,

        List<String> miembrosInicialesIds) {
    public CrearCuadrillaCommand toCommand() {
        return new CrearCuadrillaCommand(UUID.fromString(proyectoId), nombre, tipo, UUID.fromString(liderEmpleadoId),
                miembrosInicialesIds != null
                        ? miembrosInicialesIds.stream().map(UUID::fromString).collect(Collectors.toList())
                        : null);
    }
}
