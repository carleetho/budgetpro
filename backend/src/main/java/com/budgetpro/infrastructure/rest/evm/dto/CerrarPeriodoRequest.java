package com.budgetpro.infrastructure.rest.evm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO de request para cerrar un período de valuación (REQ-64, Invariante E-04).
 */
public record CerrarPeriodoRequest(
        @JsonProperty("fechaCorte")
        @NotNull(message = "La fecha de corte es obligatoria")
        LocalDate fechaCorte) {
}
