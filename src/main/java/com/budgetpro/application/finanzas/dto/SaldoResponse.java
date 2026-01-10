package com.budgetpro.application.finanzas.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta al consultar el saldo de una billetera.
 * 
 * Usado como salida del caso de uso ConsultarSaldoUseCase (S1-07).
 */
public record SaldoResponse(
        UUID proyectoId,
        BigDecimal saldoActual,
        String moneda
) {
    public SaldoResponse {
        // Establecer moneda por defecto si no se proporciona
        if (moneda == null || moneda.isBlank()) {
            moneda = "USD"; // Por defecto USD, puede ser parametrizado según el sistema
        }
    }
}
