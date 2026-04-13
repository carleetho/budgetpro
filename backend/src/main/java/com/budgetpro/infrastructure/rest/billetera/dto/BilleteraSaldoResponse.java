package com.budgetpro.infrastructure.rest.billetera.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BilleteraSaldoResponse(
        UUID billeteraId,
        UUID proyectoId,
        String moneda,
        BigDecimal saldoActual
) {
}

