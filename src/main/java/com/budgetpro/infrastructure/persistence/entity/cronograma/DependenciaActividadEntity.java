package com.budgetpro.infrastructure.persistence.entity.cronograma;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entidad JPA para la tabla dependencia_actividad.
 * 
 * Representa una dependencia Fin-Inicio entre dos actividades.
 * 
 * Relación: Una actividad puede tener múltiples predecesoras.
 */
@Entity
@Table(name = "dependencia_actividad",
       indexes = {
           @Index(name = "idx_dependencia_actividad", columnList = "actividad_id"),
           @Index(name = "idx_dependencia_predecesora", columnList = "actividad_predecesora_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_dependencia_actividad", columnNames = {"actividad_id", "actividad_predecesora_id"})
       })
public class DependenciaActividadEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id", nullable = false, updatable = false)
    private ActividadProgramadaEntity actividad;

    @Column(name = "actividad_predecesora_id", nullable = false, updatable = false)
    private UUID actividadPredecesoraId; // ID de la actividad predecesora (dependencia Fin-Inicio)

    /**
     * Constructor protegido para JPA.
     */
    protected DependenciaActividadEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     */
    public DependenciaActividadEntity(UUID id, ActividadProgramadaEntity actividad, UUID actividadPredecesoraId) {
        this.id = id;
        this.actividad = actividad;
        this.actividadPredecesoraId = actividadPredecesoraId;
    }

    // Getters y Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ActividadProgramadaEntity getActividad() {
        return actividad;
    }

    public void setActividad(ActividadProgramadaEntity actividad) {
        this.actividad = actividad;
    }

    public UUID getActividadPredecesoraId() {
        return actividadPredecesoraId;
    }

    public void setActividadPredecesoraId(UUID actividadPredecesoraId) {
        this.actividadPredecesoraId = actividadPredecesoraId;
    }
}
