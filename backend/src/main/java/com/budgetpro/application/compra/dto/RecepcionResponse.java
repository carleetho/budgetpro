package com.budgetpro.application.compra.dto;

import com.budgetpro.domain.logistica.compra.model.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para una recepción de compra.
 * 
 * Representa la confirmación de recepción de una compra con todos sus detalles.
 */
public record RecepcionResponse(
        UUID recepcionId,
        UUID compraId,
        EstadoCompra estadoCompra,
        LocalDate fechaRecepcion,
        String guiaRemision,
        List<DetalleResponse> detalles,
        UUID creadoPor,
        LocalDateTime fechaCreacion
) {
    /**
     * DTO de respuesta para un detalle de recepción.
     * 
     * Representa la información de recepción de un recurso específico.
     */
    public record DetalleResponse(
            UUID recursoId,
            BigDecimal cantidadRecibida,
            BigDecimal cantidadPendiente,
            UUID almacenId,
            UUID movimientoAlmacenId
    ) {
        /**
         * Valida que la cantidad recibida no sea negativa.
         * 
         * @throws IllegalArgumentException si cantidadRecibida es negativa
         */
        public DetalleResponse {
            if (cantidadRecibida != null && cantidadRecibida.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("La cantidad recibida no puede ser negativa");
            }
        }
    }
}
