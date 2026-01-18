package com.budgetpro.application.recurso.exception;

/**
 * Excepción de negocio lanzada cuando se intenta crear un recurso
 * con un nombre normalizado que ya existe en el catálogo.
 * 
 * Sigue el principio de excepciones de dominio para reglas de negocio.
 */
public class RecursoDuplicadoException extends RuntimeException {

    private final String nombreNormalizado;

    public RecursoDuplicadoException(String nombreNormalizado) {
        super("Ya existe un recurso con el nombre normalizado: " + nombreNormalizado);
        this.nombreNormalizado = nombreNormalizado;
    }

    public RecursoDuplicadoException(String nombreNormalizado, Throwable cause) {
        super("Ya existe un recurso con el nombre normalizado: " + nombreNormalizado, cause);
        this.nombreNormalizado = nombreNormalizado;
    }

    public String getNombreNormalizado() {
        return nombreNormalizado;
    }
}
