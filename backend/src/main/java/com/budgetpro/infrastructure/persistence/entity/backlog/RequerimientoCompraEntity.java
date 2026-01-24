package com.budgetpro.infrastructure.persistence.entity.backlog;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla requerimiento_compra.
 * 
 * Representa un requerimiento de compra generado automáticamente cuando hay backlog.
 */
@Entity
@Table(name = "requerimiento_compra",
       indexes = {
           @Index(name = "idx_requerimiento_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_requerimiento_requisicion", columnList = "requisicion_id"),
           @Index(name = "idx_requerimiento_recurso", columnList = "recurso_external_id"),
           @Index(name = "idx_requerimiento_estado", columnList = "estado"),
           @Index(name = "idx_requerimiento_prioridad", columnList = "prioridad")
       })
public class RequerimientoCompraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "requisicion_id", nullable = false, updatable = false)
    private UUID requisicionId;

    @Column(name = "recurso_external_id", nullable = false, length = 255, updatable = false)
    private String recursoExternalId;

    @Column(name = "cantidad_necesaria", nullable = false, precision = 19, scale = 6, updatable = false)
    private BigDecimal cantidadNecesaria;

    @Column(name = "unidad_medida", nullable = false, length = 20, updatable = false)
    private String unidadMedida;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20, updatable = false)
    private com.budgetpro.domain.logistica.backlog.model.PrioridadCompra prioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento estado;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RequerimientoCompraEntity() {
    }

    public RequerimientoCompraEntity(UUID id, UUID proyectoId, UUID requisicionId, String recursoExternalId,
                                     BigDecimal cantidadNecesaria, String unidadMedida,
                                     com.budgetpro.domain.logistica.backlog.model.PrioridadCompra prioridad,
                                     com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento estado,
                                     Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.requisicionId = requisicionId;
        this.recursoExternalId = recursoExternalId;
        this.cantidadNecesaria = cantidadNecesaria;
        this.unidadMedida = unidadMedida;
        this.prioridad = prioridad;
        this.estado = estado;
        this.version = version;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(UUID proyectoId) {
        this.proyectoId = proyectoId;
    }

    public UUID getRequisicionId() {
        return requisicionId;
    }

    public void setRequisicionId(UUID requisicionId) {
        this.requisicionId = requisicionId;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public void setRecursoExternalId(String recursoExternalId) {
        this.recursoExternalId = recursoExternalId;
    }

    public BigDecimal getCantidadNecesaria() {
        return cantidadNecesaria;
    }

    public void setCantidadNecesaria(BigDecimal cantidadNecesaria) {
        this.cantidadNecesaria = cantidadNecesaria;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public com.budgetpro.domain.logistica.backlog.model.PrioridadCompra getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(com.budgetpro.domain.logistica.backlog.model.PrioridadCompra prioridad) {
        this.prioridad = prioridad;
    }

    public com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento estado) {
        this.estado = estado;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
