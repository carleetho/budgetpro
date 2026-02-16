package com.budgetpro.domain.logistica.compra.port.in;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para confirmar la recepción de una orden de compra.
 */
public interface ConfirmarRecepcionUseCase {

    /**
     * Confirma la recepción de una orden de compra (ENVIADA → RECIBIDA).
     * 
     * Actualiza el inventario para partidas de tipo Material.
     * Publica el evento OrdenCompraRecibidaEvent.
     * 
     * @param ordenCompraId ID de la orden de compra
     * @param userId ID del usuario que confirma la recepción
     * @throws IllegalStateException si la orden no está en estado ENVIADA
     * @throws IllegalArgumentException si la orden no existe
     * @throws IllegalStateException si hay un error al actualizar el inventario (rollback de transacción)
     */
    void confirmarRecepcion(OrdenCompraId ordenCompraId, UUID userId);
}
