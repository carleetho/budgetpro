package com.budgetpro.domain.logistica.bodega.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado BODEGA.
 *
 * Representa un almacén físico por proyecto para el seguimiento de inventario
 * multi-bodega. Cada proyecto puede tener múltiples bodegas (relación 1:N).
 *
 * Invariantes: - Código único por proyecto (proyectoId + codigo) - codigo no
 * puede estar en blanco - proyectoId y responsable son obligatorios - Estado
 * activa/inactiva para ciclo de vida
 */
public final class Bodega {

    private final BodegaId id;
    private final UUID proyectoId;
    private final String codigo;
    private final String nombre;
    private final String ubicacionFisica;
    private final String responsable;
    // JUSTIFICACIÓN ARQUITECTÓNICA: Estado de lifecycle mutable.
    // - activa: estado del ciclo de vida (activar/desactivar bodega)
    // - version: optimistic locking para concurrencia
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private final boolean activa;
    private final LocalDateTime fechaCreacion;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private final Long version;

    /**
     * Constructor privado. Usar factory methods crear() y reconstruir().
     */
    private Bodega(BodegaId id, UUID proyectoId, String codigo, String nombre, String ubicacionFisica,
            String responsable, boolean activa, LocalDateTime fechaCreacion, Long version) {
        this.id = Objects.requireNonNull(id, "El ID de la bodega no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId es obligatorio");
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la bodega no puede estar en blanco");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre != null ? nombre.trim() : null;
        this.ubicacionFisica = ubicacionFisica != null ? ubicacionFisica.trim() : null;
        if (responsable == null || responsable.isBlank()) {
            throw new IllegalArgumentException("El responsable es obligatorio");
        }
        this.responsable = responsable.trim();
        this.activa = activa;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva bodega. La bodega se crea activa, con
     * fechaCreación actual y version 0.
     *
     * @param id              Identificador único de la bodega
     * @param proyectoId      ID del proyecto (obligatorio)
     * @param codigo          Código único por proyecto (no puede estar en blanco)
     * @param nombre          Nombre de la bodega
     * @param ubicacionFisica Ubicación física (opcional)
     * @param responsable     Responsable de la bodega (obligatorio)
     * @return Nueva Bodega activa
     */
    public static Bodega crear(BodegaId id, UUID proyectoId, String codigo, String nombre, String ubicacionFisica,
            String responsable) {
        return new Bodega(id, proyectoId, codigo, nombre, ubicacionFisica, responsable, true, LocalDateTime.now(), 0L);
    }

    /**
     * Factory method para reconstruir una bodega desde persistencia.
     */
    public static Bodega reconstruir(BodegaId id, UUID proyectoId, String codigo, String nombre, String ubicacionFisica,
            String responsable, boolean activa, LocalDateTime fechaCreacion, Long version) {
        return new Bodega(id, proyectoId, codigo, nombre, ubicacionFisica, responsable, activa, fechaCreacion, version);
    }

    /**
     * Activa la bodega (disponible para operaciones).
     */
    public Bodega activar() {
        if (this.activa) {
            return this;
        }
        return new Bodega(this.id, this.proyectoId, this.codigo, this.nombre, this.ubicacionFisica, this.responsable,
                true, this.fechaCreacion, this.version + 1);
    }

    /**
     * Desactiva la bodega (no disponible para nuevas operaciones).
     */
    public Bodega desactivar() {
        if (!this.activa) {
            return this;
        }
        return new Bodega(this.id, this.proyectoId, this.codigo, this.nombre, this.ubicacionFisica, this.responsable,
                false, this.fechaCreacion, this.version + 1);
    }

    // Getters

    public BodegaId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacionFisica() {
        return ubicacionFisica;
    }

    public String getResponsable() {
        return responsable;
    }

    public boolean isActiva() {
        return activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Bodega bodega = (Bodega) o;
        return Objects.equals(id, bodega.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Bodega{id=%s, proyectoId=%s, codigo='%s', nombre='%s', activa=%s}", id, proyectoId,
                codigo, nombre, activa);
    }
}
