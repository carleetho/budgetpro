package com.budgetpro.infrastructure.rest.sobrecosto.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO de request para configurar los parámetros laborales (FSR).
 */
public record ConfigurarLaboralRequest(
        @Min(value = 0, message = "Los días de aguinaldo no pueden ser negativos")
        Integer diasAguinaldo,
        
        @Min(value = 0, message = "Los días de vacaciones no pueden ser negativos")
        Integer diasVacaciones,
        
        @DecimalMin(value = "0.0", message = "El porcentaje de seguridad social no puede ser negativo")
        @DecimalMax(value = "100.0", message = "El porcentaje de seguridad social no puede ser mayor a 100%")
        BigDecimal porcentajeSeguridadSocial,
        
        @Min(value = 0, message = "Los días no trabajados no pueden ser negativos")
        Integer diasNoTrabajados,
        
        @NotNull(message = "Los días laborables al año son obligatorios")
        @Min(value = 1, message = "Los días laborables al año deben ser positivos")
        Integer diasLaborablesAno
) {
}
