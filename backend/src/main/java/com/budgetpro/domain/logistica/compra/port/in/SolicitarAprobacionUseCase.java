package com.budgetpro.domain.logistica.compra.port.in;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para solicitar aprobación de una orden de compra.
 */
public interface SolicitarAprobacionUseCase {

    /**
     * Solicita la aprobación de una orden de compra (BORRADOR → SOLICITADA).
     * 
     * Valida:
     * - L-01: Presupuesto disponible
     * - L-04: Proveedor activo
     * - REGLA-153: Partidas leaf válidas
     * 
     * @param ordenCompraId ID de la orden de compra
     * @param userId ID del usuario que solicita la aprobación
     * @throws IllegalStateException si la orden no está en estado BORRADOR o las validaciones fallan
     * @throws IllegalArgumentException si la orden no existe
     */
    void solicitar(OrdenCompraId ordenCompraId, UUID userId);
}
