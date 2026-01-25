package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AsignacionProyectoResponse(UUID id, UUID empleadoId, UUID proyectoId, UUID recursoProxyId,
        LocalDate fechaInicio, LocalDate fechaFin, BigDecimal tarifaHora, String rolProyecto) {
}
