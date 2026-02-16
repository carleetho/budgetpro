package com.budgetpro.infrastructure.rest.compra.dto;

import com.budgetpro.domain.logistica.compra.model.OrdenCompraEstado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de respuesta REST para una orden de compra.
 */
@Schema(description = "Respuesta con los detalles completos de una orden de compra")
public record OrdenCompraResponse(
        @Schema(description = "ID único de la orden de compra", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        
        @Schema(description = "Número secuencial de la orden", example = "PO-2024-001")
        String numero,
        
        @Schema(description = "ID del proyecto", example = "660e8400-e29b-41d4-a716-446655440000")
        UUID proyectoId,
        
        @Schema(description = "Información del proveedor")
        ProveedorResponse proveedor,
        
        @Schema(description = "Fecha de la orden", example = "2024-02-15")
        LocalDate fecha,
        
        @Schema(description = "Estado actual de la orden", example = "BORRADOR")
        OrdenCompraEstado estado,
        
        @Schema(description = "Monto total calculado automáticamente", example = "1250.00")
        BigDecimal montoTotal,
        
        @Schema(description = "Condiciones de pago", example = "30 días crédito")
        String condicionesPago,
        
        @Schema(description = "Observaciones adicionales", example = "Entrega urgente")
        String observaciones,
        
        @Schema(description = "Versión para control de concurrencia optimista", example = "1")
        Long version,
        
        @Schema(description = "Lista de detalles de la orden")
        List<DetalleOrdenCompraResponse> detalles,
        
        @Schema(description = "Fecha y hora de creación")
        LocalDateTime createdAt,
        
        @Schema(description = "Fecha y hora de última actualización")
        LocalDateTime updatedAt,
        
        @Schema(description = "ID del usuario que creó la orden")
        UUID createdBy,
        
        @Schema(description = "ID del usuario que actualizó la orden por última vez")
        UUID updatedBy
) {
}
