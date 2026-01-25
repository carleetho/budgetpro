package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;

/**
 * Response DTO for Employee data.
 */
public record EmpleadoResponse(String id, String nombre, String apellido, String numeroIdentificacion, String email,
        String telefono, String direccion, String estado, BigDecimal salarioActual, String puestoActual, String tipo,
        String fechaContratacion) {
}
