package com.budgetpro.infrastructure.persistence.entity.requisicion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla requisicion_item.
 * 
 * Representa un ítem solicitado en una requisición.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "requisicion_item",
       indexes = {
           @Index(name = "idx_requisicion_item_requisicion", columnList = "requisicion_id"),
           @Index(name = "idx_requisicion_item_recurso_external", columnList = "recurso_external_id"),
           @Index(name = "idx_requisicion_item_partida", columnList = "partida_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_requisicion_item_recurso", columnNames = {"requisicion_id", "recurso_external_id"})
       })
public class RequisicionItemEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisicion_id", nullable = false, updatable = false)
    private RequisicionEntity requisicion;

    @Column(name = "recurso_external_id", nullable = false, length = 255, updatable = false)
    private String recursoExternalId; // Referencia externa al recurso (ej. "MAT-001")

    @Column(name = "partida_id", updatable = false)
    private UUID partidaId; // Partida presupuestal (imputación)

    @Column(name = "cantidad_solicitada", nullable = false, precision = 19, scale = 6, updatable = false)
    private BigDecimal cantidadSolicitada; // Inmutable después de creación

    @Column(name = "cantidad_despachada", nullable = false, precision = 19, scale = 6)
    private BigDecimal cantidadDespachada; // Acumulativa

    @Column(name = "unidad_medida", nullable = false, length = 20, updatable = false)
    private String unidadMedida; // Inmutable después de creación

    @Column(name = "justificacion", length = 500)
    private String justificacion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected RequisicionItemEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID del ítem
     * @param requisicion RequisicionEntity asociada
     * @param recursoExternalId ID externo del recurso (ej. "MAT-001")
     * @param partidaId ID de la partida (imputación presupuestal)
     * @param cantidadSolicitada Cantidad solicitada (inmutable)
     * @param cantidadDespachada Cantidad despachada (acumulativa)
     * @param unidadMedida Unidad de medida
     * @param justificacion Justificación de la solicitud
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public RequisicionItemEntity(UUID id, RequisicionEntity requisicion, String recursoExternalId, UUID partidaId,
                                BigDecimal cantidadSolicitada, BigDecimal cantidadDespachada,
                                String unidadMedida, String justificacion, Integer version) {
        this.id = id;
        this.requisicion = requisicion;
        this.recursoExternalId = recursoExternalId;
        this.partidaId = partidaId;
        this.cantidadSolicitada = cantidadSolicitada;
        this.cantidadDespachada = cantidadDespachada;
        this.unidadMedida = unidadMedida;
        this.justificacion = justificacion;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public RequisicionEntity getRequisicion() {
        return requisicion;
    }

    public void setRequisicion(RequisicionEntity requisicion) {
        this.requisicion = requisicion;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public void setRecursoExternalId(String recursoExternalId) {
        this.recursoExternalId = recursoExternalId;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public BigDecimal getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public void setCantidadSolicitada(BigDecimal cantidadSolicitada) {
        this.cantidadSolicitada = cantidadSolicitada;
    }

    public BigDecimal getCantidadDespachada() {
        return cantidadDespachada;
    }

    public void setCantidadDespachada(BigDecimal cantidadDespachada) {
        this.cantidadDespachada = cantidadDespachada;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
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
