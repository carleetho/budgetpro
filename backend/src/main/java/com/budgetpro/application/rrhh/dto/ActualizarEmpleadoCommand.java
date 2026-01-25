package com.budgetpro.application.rrhh.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command to update an existing employee. All fields are optional except ID
 * (implied by path in controller, but explicit here for use case).
 */
public record ActualizarEmpleadoCommand(String id, String nombre, String apellido, String email, String telefono,
        String direccion, BigDecimal nuevoSalario, String nuevoPuesto, LocalDate fechaEfectiva) {
}
