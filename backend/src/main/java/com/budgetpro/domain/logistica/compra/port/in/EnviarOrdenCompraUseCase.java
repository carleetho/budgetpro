package com.budgetpro.domain.logistica.compra.port.in;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para enviar una orden de compra al proveedor.
 */
public interface EnviarOrdenCompraUseCase {

    /**
     * Envía una orden de compra al proveedor (APROBADA → ENVIADA).
     * 
     * Publica el evento OrdenCompraEnviadaEvent.
     * 
     * @param ordenCompraId ID de la orden de compra
     * @param userId ID del usuario que envía la orden
     * @throws IllegalStateException si la orden no está en estado APROBADA
     * @throws IllegalArgumentException si la orden no existe
     */
    void enviar(OrdenCompraId ordenCompraId, UUID userId);
}
