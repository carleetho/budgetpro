package com.budgetpro.domain.logistica.almacen.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Agregado que representa un almacén físico.
 * 
 * Refactorizado a inmutable para cumplir con AXIOM.
 */
public final class Almacen {

    private final AlmacenId id;
    private final UUID proyectoId;
    private final String codigo;
    private final String nombre;
    private final String ubicacion;
    private final UUID responsableId;
    private final boolean activo;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Almacen(AlmacenId id, UUID proyectoId, String codigo, String nombre, String ubicacion, UUID responsableId,
            boolean activo) {
        this.id = Objects.requireNonNull(id, "El ID del almacén no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.codigo = Objects.requireNonNull(codigo, "El código del almacén no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre del almacén no puede ser nulo");
        this.ubicacion = ubicacion;
        this.responsableId = responsableId;
        this.activo = activo;
    }

    /**
     * Factory method para crear un nuevo almacén.
     */
    public static Almacen crear(AlmacenId id, UUID proyectoId, String codigo, String nombre, String ubicacion,
            UUID responsableId) {
        return new Almacen(id, proyectoId, codigo, nombre, ubicacion, responsableId, true);
    }

    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static Almacen reconstruir(AlmacenId id, UUID proyectoId, String codigo, String nombre, String ubicacion,
            UUID responsableId, boolean activo) {
        return new Almacen(id, proyectoId, codigo, nombre, ubicacion, responsableId, activo);
    }

    /**
     * Actualiza la información básica del almacén.
     */
    public Almacen actualizarInformacion(String nuevoNombre, String nuevaUbicacion, UUID nuevoResponsableId) {
        return new Almacen(this.id, this.proyectoId, this.codigo, nuevoNombre != null ? nuevoNombre : this.nombre,
                nuevaUbicacion != null ? nuevaUbicacion : this.ubicacion,
                nuevoResponsableId != null ? nuevoResponsableId : this.responsableId, this.activo);
    }

    /**
     * Desactiva el almacén.
     */
    public Almacen desactivar() {
        return new Almacen(this.id, this.proyectoId, this.codigo, this.nombre, this.ubicacion, this.responsableId,
                false);
    }

    /**
     * Activa el almacén.
     */
    public Almacen activar() {
        return new Almacen(this.id, this.proyectoId, this.codigo, this.nombre, this.ubicacion, this.responsableId,
                true);
    }

    // Getters

    public AlmacenId getId() {
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

    public String getUbicacion() {
        return ubicacion;
    }

    public UUID getResponsableId() {
        return responsableId;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Almacen almacen = (Almacen) o;
        return Objects.equals(id, almacen.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Almacen{id=%s, codigo='%s', nombre='%s', activo=%b}", id, codigo, nombre, activo);
    }
}
