package com.budgetpro.application.avance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de respuesta para AvanceFisico.
 */
public record AvanceFisicoResponse(
        UUID id,
        UUID partidaId,
        LocalDate fecha,
        BigDecimal metradoEjecutado,
        String observacion,
        BigDecimal porcentajeAvance, // Porcentaje de avance de la partida después de este registro
        Integer version
) {
}
