package com.budgetpro.infrastructure.persistence.entity.cronograma;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla programa_obra.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "programa_obra",
       indexes = {
           @Index(name = "idx_programa_obra_proyecto", columnList = "proyecto_id", unique = true)
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_programa_obra_proyecto", columnNames = "proyecto_id")
       })
public class ProgramaObraEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false, unique = true)
    private UUID proyectoId;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin_estimada")
    private LocalDate fechaFinEstimada;

    @Column(name = "duracion_total_dias")
    private Integer duracionTotalDias;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Freeze state fields
    /**
     * Indica si el cronograma ha sido congelado (baseline establecido).
     */
    @Column(name = "congelado", nullable = false)
    private Boolean congelado;

    /**
     * Timestamp de cuando se congeló el cronograma.
     */
    @Column(name = "congelado_at")
    private LocalDateTime congeladoAt;

    /**
     * ID del usuario que congeló el cronograma.
     */
    @Column(name = "congelado_by")
    private UUID congeladoBy;

    /**
     * Versión del algoritmo usado para generar el snapshot del cronograma.
     */
    @Column(name = "snapshot_algorithm", length = 50)
    private String snapshotAlgorithm;

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected ProgramaObraEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public ProgramaObraEntity(UUID id, UUID proyectoId, LocalDate fechaInicio,
                              LocalDate fechaFinEstimada, Integer duracionTotalDias, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.duracionTotalDias = duracionTotalDias;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
        this.congelado = false; // Por defecto no está congelado
        this.congeladoAt = null;
        this.congeladoBy = null;
        this.snapshotAlgorithm = null;
    }

    /**
     * Constructor completo con campos de freeze.
     */
    public ProgramaObraEntity(UUID id, UUID proyectoId, LocalDate fechaInicio,
                              LocalDate fechaFinEstimada, Integer duracionTotalDias, Integer version,
                              Boolean congelado, LocalDateTime congeladoAt, UUID congeladoBy, String snapshotAlgorithm) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.fechaInicio = fechaInicio;
        this.fechaFinEstimada = fechaFinEstimada;
        this.duracionTotalDias = duracionTotalDias;
        this.version = version;
        this.congelado = congelado != null ? congelado : false;
        this.congeladoAt = congeladoAt;
        this.congeladoBy = congeladoBy;
        this.snapshotAlgorithm = snapshotAlgorithm;
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

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFinEstimada() {
        return fechaFinEstimada;
    }

    public void setFechaFinEstimada(LocalDate fechaFinEstimada) {
        this.fechaFinEstimada = fechaFinEstimada;
    }

    public Integer getDuracionTotalDias() {
        return duracionTotalDias;
    }

    public void setDuracionTotalDias(Integer duracionTotalDias) {
        this.duracionTotalDias = duracionTotalDias;
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

    // Freeze state getters and setters

    public Boolean getCongelado() {
        return congelado;
    }

    public void setCongelado(Boolean congelado) {
        this.congelado = congelado;
    }

    public LocalDateTime getCongeladoAt() {
        return congeladoAt;
    }

    public void setCongeladoAt(LocalDateTime congeladoAt) {
        this.congeladoAt = congeladoAt;
    }

    public UUID getCongeladoBy() {
        return congeladoBy;
    }

    public void setCongeladoBy(UUID congeladoBy) {
        this.congeladoBy = congeladoBy;
    }

    public String getSnapshotAlgorithm() {
        return snapshotAlgorithm;
    }

    public void setSnapshotAlgorithm(String snapshotAlgorithm) {
        this.snapshotAlgorithm = snapshotAlgorithm;
    }
}
