package com.budgetpro.domain.logistica.requisicion.exception;

import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando un usuario intenta realizar una operación
 * para la cual no tiene autorización (ej. aprobar requisición sin ser Residente).
 */
public class UsuarioNoAutorizadoException extends RuntimeException {

    private final UUID usuarioId;
    private final String operacion;

    public UsuarioNoAutorizadoException(UUID usuarioId, String operacion) {
        super(String.format(
            "El usuario %s no está autorizado para realizar la operación: %s",
            usuarioId, operacion
        ));
        this.usuarioId = usuarioId;
        this.operacion = operacion;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getOperacion() {
        return operacion;
    }
}
