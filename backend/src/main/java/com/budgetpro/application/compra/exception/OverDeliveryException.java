package com.budgetpro.application.compra.exception;

/**
 * Excepción lanzada cuando se intenta recibir una cantidad mayor
 * a la cantidad pendiente de un detalle de compra.
 */
public class OverDeliveryException extends RuntimeException {
    public OverDeliveryException(String message) {
        super(message);
    }
}
