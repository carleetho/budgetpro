package com.budgetpro.application.compra.dto;

import com.budgetpro.domain.logistica.compra.model.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta para una compra registrada.
 */
public record RegistrarCompraResponse(
        UUID id,
        UUID proyectoId,
        LocalDate fecha,
        String proveedor,
        EstadoCompra estado,
        BigDecimal total,
        Integer version,
        List<CompraDetalleResponse> detalles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
