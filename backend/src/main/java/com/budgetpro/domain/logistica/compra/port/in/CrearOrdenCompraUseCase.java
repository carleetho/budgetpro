package com.budgetpro.domain.logistica.compra.port.in;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para crear una nueva orden de compra.
 */
public interface CrearOrdenCompraUseCase {

    /**
     * Crea una nueva orden de compra en estado BORRADOR.
     * 
     * @param command Comando con los datos de la orden de compra
     * @return ID de la orden de compra creada
     * @throws IllegalArgumentException si el proveedor no existe o no está ACTIVO
     */
    OrdenCompraId crear(CrearOrdenCompraCommand command);

    /**
     * Comando para crear una orden de compra.
     */
    record CrearOrdenCompraCommand(
            UUID proyectoId,
            UUID proveedorId,
            LocalDate fecha,
            String condicionesPago,
            String observaciones,
            List<DetalleCommand> detalles,
            UUID createdBy
    ) {
    }

    /**
     * Comando para un detalle de orden de compra.
     */
    record DetalleCommand(
            UUID partidaId,
            String descripcion,
            BigDecimal cantidad,
            String unidad,
            BigDecimal precioUnitario
    ) {
    }
}
