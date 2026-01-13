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
        BigDecimal costoTotal, // Costo total calculado
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
