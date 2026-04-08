package com.budgetpro.application.rrhh.exception;

/**
 * No existe configuración laboral efectiva (proyecto ni global) para la fecha indicada.
 */
public class ConfiguracionLaboralNotFoundException extends RuntimeException {

    public ConfiguracionLaboralNotFoundException(String message) {
        super(message);
    }
}
