package com.budgetpro.infrastructure.persistence.entity.presupuesto;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla presupuesto_integrity_audit.
 */
@Entity
@Table(name = "presupuesto_integrity_audit",
       indexes = {
           @Index(name = "idx_integrity_audit_presupuesto", columnList = "presupuesto_id"),
           @Index(name = "idx_integrity_audit_event_type", columnList = "event_type"),
           @Index(name = "idx_integrity_audit_validated_at", columnList = "validated_at")
       })
public class IntegrityAuditEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "presupuesto_id", nullable = false, updatable = false)
    private UUID presupuestoId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "hash_approval", length = 64)
    private String hashApproval;

    @Column(name = "hash_execution", length = 64)
    private String hashExecution;

    @Column(name = "validated_by")
    private UUID validatedBy;

    @Column(name = "validated_at", nullable = false)
    private LocalDateTime validatedAt;

    @Column(name = "validation_result", length = 20, nullable = false)
    private String validationResult;

    @Column(name = "violation_details", columnDefinition = "TEXT")
    private String violationDetails;

    @Column(name = "algorithm_version", length = 20)
    private String algorithmVersion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected IntegrityAuditEntity() {
        // Hibernate requiere constructor protegido sin argumentos
    }

    public IntegrityAuditEntity(UUID id, UUID presupuestoId, String eventType,
                                String hashApproval, String hashExecution,
                                UUID validatedBy, LocalDateTime validatedAt,
                                String validationResult, String violationDetails,
                                String algorithmVersion) {
        this.id = id;
        this.presupuestoId = presupuestoId;
        this.eventType = eventType;
        this.hashApproval = hashApproval;
        this.hashExecution = hashExecution;
        this.validatedBy = validatedBy;
        this.validatedAt = validatedAt;
        this.validationResult = validationResult;
        this.violationDetails = violationDetails;
        this.algorithmVersion = algorithmVersion;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public UUID getPresupuestoId() {
        return presupuestoId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getHashApproval() {
        return hashApproval;
    }

    public String getHashExecution() {
        return hashExecution;
    }

    public UUID getValidatedBy() {
        return validatedBy;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public String getViolationDetails() {
        return violationDetails;
    }

    public String getAlgorithmVersion() {
        return algorithmVersion;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
