package com.budgetpro.infrastructure.rest.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para registro de usuario.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 150)
        String nombreCompleto,

        @Email(message = "El email debe ser válido")
        @NotBlank(message = "El email es obligatorio")
        @Size(max = 200)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        // REGLA-079
        @Size(min = 6, max = 72)
        String password
) {
}
