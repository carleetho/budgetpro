package com.budgetpro.application.rrhh.dto;

import java.util.List;
import java.util.UUID;

public record CrearCuadrillaCommand(UUID proyectoId, String nombre, String tipo, UUID liderEmpleadoId,
        List<UUID> miembrosInicialesIds) {
}
