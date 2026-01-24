package com.budgetpro.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla bodega.
 *
 * Representa un almacén físico por proyecto. Relación 1:N (un proyecto, varias bodegas).
 * Unique constraint: (proyecto_id, codigo) — sin códigos duplicados por proyecto.
 *
 * CRÍTICO: El constructor acepta version = null. Hibernate inicializa la versión automáticamente.
 */
@Entity
@Table(name = "bodega",
        uniqueConstraints = @UniqueConstraint(name = "uq_bodega_proyecto_codigo", columnNames = {"proyecto_id", "codigo"}),
        indexes = @Index(name = "idx_bodega_proyecto", columnList = "proyecto_id"))
public class BodegaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "proyecto_id", nullable = false, updatable = false)
    private UUID proyectoId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", length = 200)
    private String nombre;

    @Column(name = "ubicacion_fisica", length = 300)
    private String ubicacionFisica;

    @Column(name = "responsable", nullable = false, length = 200)
    private String responsable;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * Constructor protegido para JPA.
     * CRÍTICO: Acepta version = null. Hibernate inicializará la versión automáticamente.
     */
    protected BodegaEntity() {
    }

    /**
     * Constructor para crear nuevas entidades.
     *
     * @param id              ID de la bodega
     * @param proyectoId      ID del proyecto
     * @param codigo          Código único por proyecto
     * @param nombre          Nombre de la bodega
     * @param ubicacionFisica Ubicación física (opcional)
     * @param responsable     Responsable
     * @param activa          Estado activa/inactiva
     * @param fechaCreacion   Fecha de creación
     * @param version         Versión (null para nuevas entidades)
     */
    public BodegaEntity(UUID id, UUID proyectoId, String codigo, String nombre,
                        String ubicacionFisica, String responsable, boolean activa,
                        LocalDateTime fechaCreacion, Integer version) {
        this.id = id;
        this.proyectoId = proyectoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.ubicacionFisica = ubicacionFisica;
        this.responsable = responsable;
        this.activa = activa;
        this.fechaCreacion = fechaCreacion;
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

    public String getUbicacionFisica() {
        return ubicacionFisica;
    }

    public void setUbicacionFisica(String ubicacionFisica) {
        this.ubicacionFisica = ubicacionFisica;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
