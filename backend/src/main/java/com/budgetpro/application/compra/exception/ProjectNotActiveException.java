package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación en un proyecto
 * que no está en estado ACTIVO (REGLA-150).
 */
public class ProjectNotActiveException extends RuntimeException {
    public ProjectNotActiveException(String message) {
        super(message);
    }
}
