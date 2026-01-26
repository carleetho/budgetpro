package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.ActualizarEmpleadoCommand;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActualizarEmpleadoRequest(String nombre, String apellido, String email, String telefono, String direccion,
        @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a 0") BigDecimal nuevoSalario,
        String nuevoPuesto, LocalDate fechaEfectiva) {
    public ActualizarEmpleadoCommand toCommand(String id) {
        return new ActualizarEmpleadoCommand(id, nombre, apellido, email, telefono, direccion, nuevoSalario,
                nuevoPuesto, fechaEfectiva);
    }
}
