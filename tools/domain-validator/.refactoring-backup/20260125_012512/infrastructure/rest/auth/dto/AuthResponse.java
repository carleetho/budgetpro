package com.budgetpro.infrastructure.rest.auth.dto;

/**
 * DTO de respuesta para autenticación.
 */
public record AuthResponse(
        String token,
        String usuarioId,
        String email,
        String rol
) {
}
