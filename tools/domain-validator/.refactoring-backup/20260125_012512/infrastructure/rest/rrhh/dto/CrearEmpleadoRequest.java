package com.budgetpro.infrastructure.rest.rrhh.dto;

import com.budgetpro.application.rrhh.dto.CrearEmpleadoCommand;
import com.budgetpro.domain.rrhh.model.TipoEmpleado;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearEmpleadoRequest(@NotBlank(message = "El nombre es obligatorio") String nombre,

        @NotBlank(message = "El apellido es obligatorio") String apellido,

        @NotBlank(message = "El número de identificación es obligatorio") String numeroIdentificacion,

        @Email(message = "El email debe ser válido") String email,

        String telefono,

        String direccion,

        @NotNull(message = "La fecha de contratación es obligatoria") @PastOrPresent(message = "La fecha de contratación no puede ser futura") LocalDate fechaContratacion,

        @NotNull(message = "El salario inicial es obligatorio") @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a 0") BigDecimal salarioInicial,

        @NotBlank(message = "El puesto inicial es obligatorio") String puestoInicial,

        @NotNull(message = "El tipo de empleado es obligatorio") TipoEmpleado tipo) {
    public CrearEmpleadoCommand toCommand() {
        return new CrearEmpleadoCommand(nombre, apellido, numeroIdentificacion, email, telefono, direccion,
                fechaContratacion, salarioInicial, puestoInicial, tipo);
    }
}
