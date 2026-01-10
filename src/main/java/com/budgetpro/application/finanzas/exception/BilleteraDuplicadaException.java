package com.budgetpro.application.finanzas.exception;

import java.util.UUID;

/**
 * Excepción de aplicación que se lanza cuando se intenta crear una billetera
 * para un proyecto que ya tiene una billetera asociada.
 * 
 * REGLA: Cada proyecto tiene UNA sola billetera (relación 1:1 con UNIQUE constraint).
 */
public class BilleteraDuplicadaException extends RuntimeException {

    private final UUID proyectoId;

    public BilleteraDuplicadaException(UUID proyectoId) {
        super("Ya existe una billetera para el proyecto: " + proyectoId);
        this.proyectoId = proyectoId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }
}
