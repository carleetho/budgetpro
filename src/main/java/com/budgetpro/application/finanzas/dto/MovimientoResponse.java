package com.budgetpro.application.finanzas.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de un movimiento de fondos.
 * 
 * Usado como salida de los casos de uso IngresarFondosUseCase y EgresarFondosUseCase.
 */
public record MovimientoResponse(
        UUID id,
        UUID billeteraId,
        BigDecimal monto,
        String tipo,
        LocalDateTime fecha,
        String referencia,
        String evidenciaUrl
) {
}
