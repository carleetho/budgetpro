package com.budgetpro.infrastructure.persistence.entity.organizacion;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla cuadrilla.
 *
 * Representa una cuadrilla de trabajo vinculada a un frente de trabajo.
 * Relación 1:N (un frente de trabajo, varias cuadrillas).
 * Unique constraint: (proyecto_id, codigo) — sin códigos duplicados por proyecto.
 */
@Entity
@Table(name = "cuadrilla",
        uniqueConstraints = @UniqueConstraint(name = "uq_cuadrilla_proyecto_codigo", columnNames = {"proyecto_id", "codigo"}),
        indexes = {
            @Index(name = "idx_cuadrilla_proyecto", columnList = "proyecto_id"),
            @Index(name = "idx_cuadrilla_codigo", columnList = "codigo"),
            @Index(name = "idx_cuadrilla_frente_trabajo", columnList = "frente_trabajo_id")
        })
public class CuadrillaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "capataz", nullable = false, length = 200)
    private String capataz; // Capataz (foreman) de la cuadrilla

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "frente_trabajo_id", nullable = false, updatable = false)
    private FrenteTrabajoEntity frenteTrabajo;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Constructor protegido para JPA.
     */
    protected CuadrillaEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     *
     * @param id            ID de la cuadrilla
     * @param proyectoId    ID del proyecto
     * @param codigo        Código único por proyecto
     * @param nombre        Nombre de la cuadrilla
     * @param capataz       Capataz (foreman) de la cuadrilla
     * @param frenteTrabajo FrenteTrabajoEntity asociado
     * @param activa        Estado activa/inactiva
     */
    public CuadrillaEntity(UUID id, UUID proyectoId, String codigo, String nombre,
                          String capataz, FrenteTrabajoEntity frenteTrabajo, boolean activa) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.capataz = capataz;
        this.frenteTrabajo = frenteTrabajo;
        this.activa = activa;
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

    public String getCapataz() {
        return capataz;
    }

    public void setCapataz(String capataz) {
        this.capataz = capataz;
    }

    public FrenteTrabajoEntity getFrenteTrabajo() {
        return frenteTrabajo;
    }

    public void setFrenteTrabajo(FrenteTrabajoEntity frenteTrabajo) {
        this.frenteTrabajo = frenteTrabajo;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
