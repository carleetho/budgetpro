package com.budgetpro.application.rrhh.exception;

/**
 * Consulta de asistencias sin acotar por empleado ni por proyecto (GF-03 / contrato REST).
 */
public class FiltrosConsultaAsistenciaIncompletosException extends RuntimeException {

    public FiltrosConsultaAsistenciaIncompletosException(String message) {
        super(message);
    }
}
