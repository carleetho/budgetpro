package com.budgetpro.infrastructure.persistence.entity.requisicion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla requisicion.
 * 
 // REGLA-135
 * Representa una requisición de materiales con workflow de estados.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "requisicion",
       indexes = {
           @Index(name = "idx_requisicion_proyecto", columnList = "proyecto_id"),
           @Index(name = "idx_requisicion_fecha", columnList = "fecha_solicitud"),
           @Index(name = "idx_requisicion_estado", columnList = "estado"),
           @Index(name = "idx_requisicion_solicitante", columnList = "solicitante")
       })
public class RequisicionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "solicitante", nullable = false, length = 200)
    private String solicitante;

    @Column(name = "frente_trabajo", length = 200)
    private String frenteTrabajo;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDate fechaSolicitud;

    @Column(name = "aprobado_por")
    private UUID aprobadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion estado;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "requisicion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequisicionItemEntity> items = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected RequisicionEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     * 
     * @param id ID de la requisición
     * @param proyectoId ID del proyecto asociado
     * @param solicitante Nombre del solicitante
     * @param frenteTrabajo Frente de trabajo (opcional)
     * @param fechaSolicitud Fecha de solicitud
     * @param aprobadoPor ID del usuario que aprobó (null si no está aprobada)
     * @param estado Estado de la requisición
     * @param observaciones Observaciones generales
     * @param version Versión (puede ser null para nuevas entidades)
     */
    public RequisicionEntity(UUID id, UUID proyectoId, String solicitante, String frenteTrabajo,
                            LocalDate fechaSolicitud, UUID aprobadoPor,
                            com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion estado,
                            String observaciones, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.solicitante = solicitante;
        this.frenteTrabajo = frenteTrabajo;
        this.fechaSolicitud = fechaSolicitud;
        this.aprobadoPor = aprobadoPor;
        this.estado = estado;
        this.observaciones = observaciones;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
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

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getFrenteTrabajo() {
        return frenteTrabajo;
    }

    public void setFrenteTrabajo(String frenteTrabajo) {
        this.frenteTrabajo = frenteTrabajo;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public UUID getAprobadoPor() {
        return aprobadoPor;
    }

    public void setAprobadoPor(UUID aprobadoPor) {
        this.aprobadoPor = aprobadoPor;
    }

    public com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion getEstado() {
        return estado;
    }

    public void setEstado(com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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

    public List<RequisicionItemEntity> getItems() {
        return items;
    }

    public void setItems(List<RequisicionItemEntity> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
