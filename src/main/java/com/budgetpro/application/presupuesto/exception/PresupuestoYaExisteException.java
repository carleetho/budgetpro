package com.budgetpro.application.presupuesto.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear un presupuesto para un proyecto que ya tiene uno.
 */
public class PresupuestoYaExisteException extends RuntimeException {

    public PresupuestoYaExisteException(UUID proyectoId) {
        super(String.format("El proyecto con ID '%s' ya tiene un presupuesto asociado", proyectoId));
    }
}
