package com.budgetpro.infrastructure.persistence.entity.cronograma;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA para la tabla actividad_programada.
 * 
 * CRÍTICO: El constructor acepta version = null. NUNCA se fuerza version = 0.
 * @PrePersist solo se usa para fechas, NO para version.
 */
@Entity
@Table(name = "actividad_programada",
       indexes = {
           @Index(name = "idx_actividad_programada_programa", columnList = "programa_obra_id"),
           @Index(name = "idx_actividad_programada_partida", columnList = "partida_id", unique = true)
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_actividad_programada_partida", columnNames = "partida_id")
       })
public class ActividadProgramadaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "partida_id", nullable = false, updatable = false, unique = true)
    private UUID partidaId;

    @Column(name = "programa_obra_id", nullable = false, updatable = false)
    private UUID programaObraId;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

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
     * Relación con dependencias (tabla dependencia_actividad).
     * Una actividad puede tener múltiples predecesoras.
     */
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DependenciaActividadEntity> dependencias = new ArrayList<>();

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected ActividadProgramadaEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public ActividadProgramadaEntity(UUID id, UUID partidaId, UUID programaObraId,
                                    LocalDate fechaInicio, LocalDate fechaFin,
                                    Integer duracionDias, Integer version) {
        this.id = id;
        this.partidaId = partidaId;
        this.programaObraId = programaObraId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.duracionDias = duracionDias;
        this.version = version; // CRÍTICO: Acepta null, Hibernate lo manejará
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(UUID partidaId) {
        this.partidaId = partidaId;
    }

    public UUID getProgramaObraId() {
        return programaObraId;
    }

    public void setProgramaObraId(UUID programaObraId) {
        this.programaObraId = programaObraId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getDuracionDias() {
        return duracionDias;
    }

    public void setDuracionDias(Integer duracionDias) {
        this.duracionDias = duracionDias;
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

    public List<DependenciaActividadEntity> getDependencias() {
        return dependencias;
    }

    public void setDependencias(List<DependenciaActividadEntity> dependencias) {
        this.dependencias = dependencias;
    }
}
