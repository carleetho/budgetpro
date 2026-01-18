package com.budgetpro.application.presupuesto.dto;

import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para Presupuesto.
 */
public record PresupuestoResponse(
        UUID id,
        UUID proyectoId,
        String nombre,
        EstadoPresupuesto estado,
        Boolean esContractual,
        BigDecimal costoTotal, // Costo Directo (CD) - Lo que cuesta construir
        BigDecimal precioVenta, // Precio de Venta (PV) - Lo que se cobra al cliente
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
