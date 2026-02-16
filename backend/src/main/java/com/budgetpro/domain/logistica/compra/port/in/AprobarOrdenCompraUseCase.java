package com.budgetpro.domain.logistica.compra.port.in;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para aprobar una orden de compra.
 */
public interface AprobarOrdenCompraUseCase {

    /**
     * Aprueba una orden de compra (SOLICITADA → APROBADA).
     * 
     * @param ordenCompraId ID de la orden de compra
     * @param userId ID del usuario que aprueba la orden
     * @throws IllegalStateException si la orden no está en estado SOLICITADA
     * @throws IllegalArgumentException si la orden no existe
     */
    void aprobar(OrdenCompraId ordenCompraId, UUID userId);
}
