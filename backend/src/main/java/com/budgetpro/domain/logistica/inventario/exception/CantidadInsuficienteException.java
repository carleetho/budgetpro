package com.budgetpro.domain.logistica.inventario.exception;

/**
 * Excepción de dominio lanzada cuando se intenta egresar más cantidad de la disponible en inventario.
 */
public class CantidadInsuficienteException extends RuntimeException {

    public CantidadInsuficienteException(String message) {
        super(message);
    }
}
