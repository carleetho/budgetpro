package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AsignarEmpleadoProyectoCommand(UUID empleadoId, UUID proyectoId, LocalDate fechaInicio,
        LocalDate fechaFin, BigDecimal tarifaHora, String rolProyecto) {
}
