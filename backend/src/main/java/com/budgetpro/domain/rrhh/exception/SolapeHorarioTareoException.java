package com.budgetpro.domain.rrhh.exception;

/**
 * REGLA-125: no duplicidad horaria de tareos para el mismo trabajador en el intervalo considerado.
 */
public final class SolapeHorarioTareoException extends RuntimeException {

    public SolapeHorarioTareoException(String message) {
        super(message);
    }
}
