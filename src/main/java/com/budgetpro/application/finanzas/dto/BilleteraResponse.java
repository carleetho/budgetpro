package com.budgetpro.application.finanzas.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta al crear o consultar una billetera.
 * 
 * Usado como salida de los casos de uso de Billetera.
 * Contiene solo los campos necesarios para la capa de presentación.
 */
public record BilleteraResponse(
        UUID id,
        UUID proyectoId,
        BigDecimal saldoActual,
        Long version
) {
}
