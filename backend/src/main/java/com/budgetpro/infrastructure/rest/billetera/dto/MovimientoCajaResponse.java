package com.budgetpro.infrastructure.rest.billetera.dto;

import com.budgetpro.domain.finanzas.model.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MovimientoCajaResponse(
        UUID id,
        BigDecimal monto,
        String moneda,
        TipoMovimiento tipo,
        LocalDateTime fecha,
        String referencia,
        String evidenciaUrl,
        String estado
) {
}

