package com.budgetpro.infrastructure.persistence.entity.outbox;

/**
 * Enum que representa el estado de un evento en el Outbox.
 * 
 * Estados:
 * - PENDING: Evento pendiente de procesamiento
 * - PROCESSED: Evento procesado exitosamente
 * - FAILED: Evento que falló al procesarse
 */
public enum OutboxEventStatus {
    PENDING,
    PROCESSED,
    FAILED
}
