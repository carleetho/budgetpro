package com.budgetpro.application.avance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de comando para registrar un avance físico.
 */
public record RegistrarAvanceCommand(
        UUID partidaId,
        LocalDate fecha,
        BigDecimal metradoEjecutado,
        String observacion
) {
}
