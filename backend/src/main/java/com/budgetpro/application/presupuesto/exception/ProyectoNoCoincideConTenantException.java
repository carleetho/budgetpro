package com.budgetpro.application.presupuesto.exception;

import java.util.UUID;

/**
 * El proyecto no existe o no pertenece al tenant solicitado.
 */
public class ProyectoNoCoincideConTenantException extends RuntimeException {

    private final UUID proyectoId;
    private final UUID tenantId;

    public ProyectoNoCoincideConTenantException(UUID proyectoId, UUID tenantId) {
        super("El proyecto no existe o no pertenece al tenant indicado");
        this.proyectoId = proyectoId;
        this.tenantId = tenantId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public UUID getTenantId() {
        return tenantId;
    }
}
