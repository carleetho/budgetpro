package com.budgetpro.infrastructure.persistence.entity.organizacion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla frente_trabajo.
 *
 * Representa un frente de trabajo dentro de un proyecto.
 * Relación 1:N (un proyecto, varios frentes de trabajo).
 * Unique constraint: (proyecto_id, codigo) — sin códigos duplicados por proyecto.
 */
@Entity
@Table(name = "frente_trabajo",
        uniqueConstraints = @UniqueConstraint(name = "uq_frente_trabajo_proyecto_codigo", columnNames = {"proyecto_id", "codigo"}),
        indexes = {
            @Index(name = "idx_frente_trabajo_proyecto", columnList = "proyecto_id"),
            @Index(name = "idx_frente_trabajo_codigo", columnList = "codigo")
        })
public class FrenteTrabajoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "responsable", nullable = false, length = 200)
    private String responsable; // Ingeniero responsable

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Constructor protegido para JPA.
     */
    protected FrenteTrabajoEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     *
     * @param id          ID del frente de trabajo
     * @param proyectoId  ID del proyecto
     * @param codigo      Código único por proyecto
     * @param nombre      Nombre del frente de trabajo
     * @param responsable Ingeniero responsable
     * @param activo      Estado activo/inactivo
     */
    public FrenteTrabajoEntity(UUID id, UUID proyectoId, String codigo, String nombre,
                               String responsable, boolean activo) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.responsable = responsable;
        this.activo = activo;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
