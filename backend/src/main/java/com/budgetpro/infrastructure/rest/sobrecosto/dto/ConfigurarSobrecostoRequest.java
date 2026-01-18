package com.budgetpro.infrastructure.rest.sobrecosto.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/**
 * DTO de request para configurar el análisis de sobrecosto.
 */
public record ConfigurarSobrecostoRequest(
        @DecimalMin(value = "0.0", message = "El porcentaje de indirectos oficina central no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de indirectos oficina central no puede ser mayor a 100%")
        BigDecimal porcentajeIndirectosOficinaCentral,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de indirectos oficina campo no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de indirectos oficina campo no puede ser mayor a 100%")
        BigDecimal porcentajeIndirectosOficinaCampo,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de financiamiento no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de financiamiento no puede ser mayor a 100%")
        BigDecimal porcentajeFinanciamiento,
        
        Boolean financiamientoCalculado,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de utilidad no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de utilidad no puede ser mayor a 100%")
        BigDecimal porcentajeUtilidad,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de fianzas no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de fianzas no puede ser mayor a 100%")
        BigDecimal porcentajeFianzas,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de impuestos reflejables no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de impuestos reflejables no puede ser mayor a 100%")
        BigDecimal porcentajeImpuestosReflejables
) {
}
