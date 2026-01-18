package com.budgetpro.application.inventario.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para un item de inventario.
 */
public record InventarioItemResponse(
        UUID id,
        UUID proyectoId,
        UUID recursoId,
        BigDecimal cantidadFisica,
        BigDecimal costoPromedio,
        String ubicacion,
        LocalDateTime ultimaActualizacion,
        Long version
) {
}
