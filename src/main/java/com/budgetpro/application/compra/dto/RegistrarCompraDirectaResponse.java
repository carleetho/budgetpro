package com.budgetpro.application.compra.dto;

import com.budgetpro.domain.finanzas.compra.EstadoCompra;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta del caso de uso RegistrarCompraDirectaUseCase.
 * 
 * Contiene el identificador de la compra registrada, su estado transaccional actual
 * y un mensaje opcional para el usuario.
 */
public record RegistrarCompraDirectaResponse(
        UUID compraId,
        EstadoCompra estado,
        String mensajeUsuario
) {
    /**
     * Constructor auxiliar para crear respuesta sin mensaje de usuario.
     */
    public RegistrarCompraDirectaResponse(UUID compraId, EstadoCompra estado) {
        this(compraId, estado, null);
    }
}
