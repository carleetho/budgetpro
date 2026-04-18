package com.budgetpro.domain.rrhh.exception;

/**
 * R-03 (régimen civil / decisión PO): el trabajador no puede tener dos asignaciones a proyecto cuyos
 * intervalos de fechas (inicio–fin inclusive; fin abierto = hasta infinito) se intersecten con la ventana candidata.
 */
public final class AsignacionSuperpuestaException extends RuntimeException {

    public AsignacionSuperpuestaException(String message) {
        super(message);
    }
}
