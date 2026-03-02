package com.budgetpro.application.finanzas.evm.port.in;

import java.util.UUID;

/**
 * Excepción lanzada cuando el proyecto solicitado no existe.
 */
public class ProyectoNotFoundException extends RuntimeException {
    private final UUID proyectoId;

    public ProyectoNotFoundException(UUID proyectoId) {
        super(String.format("No se encontró un proyecto con ID '%s'", proyectoId));
        this.proyectoId = proyectoId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }
}
