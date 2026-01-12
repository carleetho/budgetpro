package com.budgetpro.application.billetera.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta del saldo actual de una billetera.
 * 
 * Proyección de lectura para CQRS-Lite. NO contiene lógica de negocio.
 * 
 * Se usa exclusivamente para consultas READ (no para escrituras).
 */
public record SaldoResponse(
        UUID proyectoId,
        BigDecimal saldoActual
) {
}
