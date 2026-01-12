package com.budgetpro.application.presupuesto.dto;

import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta de un presupuesto.
 * 
 * Proyección de lectura para CQRS-Lite. NO contiene lógica de negocio.
 * 
 * Se usa exclusivamente para consultas READ (no para escrituras).
 */
public record PresupuestoResponse(
        UUID id,
        UUID proyectoId,
        Boolean esContractual,
        Integer version
) {
}
