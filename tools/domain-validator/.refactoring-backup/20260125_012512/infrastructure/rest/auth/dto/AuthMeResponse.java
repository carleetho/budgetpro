package com.budgetpro.infrastructure.rest.auth.dto;

/**
 * DTO de respuesta para perfil autenticado.
 */
public record AuthMeResponse(
        String usuarioId,
        String nombreCompleto,
        String email,
        String rol
) {
}
