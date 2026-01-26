package com.budgetpro.infrastructure.rest.avance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de request para registrar un avance físico.
 */
public record RegistrarAvanceRequest(
        @NotNull(message = "La fecha es obligatoria")
        LocalDate fecha,
        
        @NotNull(message = "El metrado ejecutado es obligatorio")
        @Positive(message = "El metrado ejecutado debe ser positivo")
        BigDecimal metradoEjecutado,
        
        String observacion
) {
}
