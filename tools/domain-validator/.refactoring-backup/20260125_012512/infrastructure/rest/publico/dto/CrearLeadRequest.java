package com.budgetpro.infrastructure.rest.publico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registrar una solicitud de demo pública.
 */
public record CrearLeadRequest(
        @NotBlank(message = "El nombre de contacto es obligatorio")
        @Size(max = 150)
        String nombreContacto,

        @Email(message = "El email debe ser válido")
        @Size(max = 200)
        String email,

        @Size(max = 40)
        String telefono,

        @Size(max = 200)
        String nombreEmpresa,

        @Size(max = 120)
        String rol
) {
}
