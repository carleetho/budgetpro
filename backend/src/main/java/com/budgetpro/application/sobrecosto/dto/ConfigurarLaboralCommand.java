package com.budgetpro.application.sobrecosto.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO de comando para configurar los parámetros laborales (FSR).
 */
public record ConfigurarLaboralCommand(
        UUID proyectoId, // null para configuración global
        Integer diasAguinaldo,
        Integer diasVacaciones,
        BigDecimal porcentajeSeguridadSocial,
        Integer diasNoTrabajados,
        Integer diasLaborablesAno
) {
}
