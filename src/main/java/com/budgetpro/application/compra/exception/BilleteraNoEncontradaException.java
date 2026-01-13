package com.budgetpro.application.compra.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta registrar una compra y no se encuentra la billetera del proyecto.
 */
public class BilleteraNoEncontradaException extends RuntimeException {

    public BilleteraNoEncontradaException(UUID proyectoId) {
        super(String.format("No se encontró una billetera para el proyecto con ID '%s'", proyectoId));
    }
}
