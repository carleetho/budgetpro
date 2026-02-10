package com.budgetpro.infrastructure.rest.estimacion.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO de request para generar una estimación.
 */
public record GenerarEstimacionRequest(
        @NotNull(message = "La fecha de corte es obligatoria")
        LocalDate fechaCorte,
        
        @NotNull(message = "El periodo de inicio es obligatorio")
        LocalDate periodoInicio,
        
        @NotNull(message = "El periodo de fin es obligatorio")
        LocalDate periodoFin,
        
        @Valid
        @NotNull(message = "Los detalles son obligatorios")
        List<DetalleEstimacionItem> detalles,
        
        // REGLA-087
        @DecimalMin(value = "0.0", message = "El porcentaje de anticipo no puede ser negativo")
        BigDecimal porcentajeAnticipo,

        String evidenciaUrl,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de retención de fondo de garantía no puede ser negativo")
        BigDecimal porcentajeRetencionFondoGarantia
) {
    /**
     * DTO interno para representar un detalle de estimación.
     */
    public record DetalleEstimacionItem(
            @NotNull(message = "El partidaId es obligatorio")
            UUID partidaId,
            
            @NotNull(message = "La cantidad de avance es obligatoria")
            // REGLA-088
            @DecimalMin(value = "0.0", message = "La cantidad de avance no puede ser negativa")
            BigDecimal cantidadAvance,
            
            @NotNull(message = "El precio unitario es obligatorio")
            @DecimalMin(value = "0.0", message = "El precio unitario no puede ser negativo")
            BigDecimal precioUnitario
    ) {
    }
}
