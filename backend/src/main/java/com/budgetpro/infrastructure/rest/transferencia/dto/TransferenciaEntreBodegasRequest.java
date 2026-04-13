package com.budgetpro.infrastructure.rest.transferencia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferenciaEntreBodegasRequest(
        @NotNull UUID inventarioOrigenId,
        @NotNull UUID bodegaDestinoId,
        @NotNull BigDecimal cantidad,
        @NotBlank String referencia
) {
}

