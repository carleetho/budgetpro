package com.budgetpro.application.rrhh.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AsignarCuadrillaCommand(UUID cuadrillaId, UUID proyectoId, UUID partidaId, LocalDate fechaInicio,
        LocalDate fechaFin) {
}
