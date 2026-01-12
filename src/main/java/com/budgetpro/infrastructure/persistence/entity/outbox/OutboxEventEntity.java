package com.budgetpro.infrastructure.persistence.entity.outbox;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla outbox_event.
 * 
 * Implementa el patrón Outbox para garantizar persistencia confiable de eventos de dominio
 * dentro de la misma transacción que los cambios de negocio.
 */
@Entity
@Table(name = "outbox_event", indexes = {
    @Index(name = "idx_outbox_event_status", columnList = "status"),
    @Index(name = "idx_outbox_event_created_at", columnList = "created_at"),
    @Index(name = "idx_outbox_event_aggregate", columnList = "aggregate_type,aggregate_id")
})
public class OutboxEventEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_payload", nullable = false, columnDefinition = "jsonb")
    private String eventPayload;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    /**
     * Constructor protegido sin argumentos requerido por JPA.
     */
    protected OutboxEventEntity() {
    }

    /**
     * Constructor público para crear instancias de OutboxEventEntity.
     * 
     * @param id Identificador único del evento
     * @param aggregateType Tipo del agregado (ej: "Compra")
     * @param aggregateId ID del agregado (ej: compraId)
     * @param eventType Tipo del evento (ej: "CompraRegistrada")
     * @param eventPayload Payload del evento serializado como JSON
     */
    public OutboxEventEntity(
            UUID id,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String eventPayload
    ) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventPayload = eventPayload;
        this.status = OutboxEventStatus.PENDING.name();
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OutboxEventStatus.PENDING.name();
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventPayload() {
        return eventPayload;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

}
