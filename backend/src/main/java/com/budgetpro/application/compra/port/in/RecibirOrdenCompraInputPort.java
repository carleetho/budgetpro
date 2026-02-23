package com.budgetpro.application.compra.port.in;

import com.budgetpro.application.compra.command.RecibirOrdenCompraCommand;
import com.budgetpro.domain.logistica.compra.model.RecepcionId;

/**
 * Puerto de entrada (Inbound Port) para recibir una orden de compra.
 * 
 * Define el contrato que deben cumplir las implementaciones del caso de uso
 * de recepción de órdenes de compra. Sigue el patrón de Arquitectura Hexagonal.
 */
public interface RecibirOrdenCompraInputPort {

    /**
     * Ejecuta el caso de uso para recibir una orden de compra.
     * 
     * Este método procesa el workflow de recepción de productos, que incluye:
     * - Validación de idempotencia (guía de remisión única por compra)
     * - Creación del agregado Recepcion con sus detalles
     * - Actualización de cantidad_recibida en CompraDetalle
     * - Persistencia de la recepción
     * 
     * @param command Comando con los datos de la recepción, incluyendo guía de remisión y detalles
     * @return RecepcionId Identificador de la recepción creada
     * @throws com.budgetpro.application.compra.exception.BusinessRuleException si la guía de remisión ya existe para esta compra (idempotencia)
     * @throws IllegalArgumentException si el comando es inválido o la compra no existe
     * @throws IllegalStateException si la compra no está en un estado válido para recepción (debe estar ENVIADA o PARCIAL)
     */
    RecepcionId ejecutar(RecibirOrdenCompraCommand command);
}
