package com.budgetpro.application.sobrecosto.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de respuesta para ConfiguracionLaboral.
 */
public record ConfiguracionLaboralResponse(
        UUID id,
        UUID proyectoId, // null si es global
        Integer diasAguinaldo,
        Integer diasVacaciones,
        BigDecimal porcentajeSeguridadSocial,
        Integer diasNoTrabajados,
        Integer diasLaborablesAno,
        BigDecimal factorSalarioReal, // FSR calculado
        Integer version
) {
}
