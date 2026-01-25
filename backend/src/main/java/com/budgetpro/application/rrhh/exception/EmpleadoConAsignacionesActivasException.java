package com.budgetpro.application.rrhh.exception;

public class EmpleadoConAsignacionesActivasException extends RuntimeException {
    public EmpleadoConAsignacionesActivasException(String id) {
        super("No se puede inactivar el empleado " + id + " porque tiene asignaciones de proyecto activas.");
    }
}
