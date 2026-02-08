package com.budgetpro.domain.proyecto.model;

import java.util.Objects;

/**
 * Aggregate Root del agregado PROYECTO.
 * 
 * Representa un proyecto de construcción con su identidad, ubicación y estado.
 * 
 * Invariantes: - El nombre no puede estar vacío - El nombre debe ser único
 * (validado a nivel de persistencia) - El estado no puede ser nulo
 * 
 * Contexto: Gestión de Proyectos
 */
public final class Proyecto {

    private final ProyectoId id;
    private String nombre; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.proyecto -
                           // Project name editable via actualizarNombre()
    private String ubicacion; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.proyecto -
                              // Project location editable via actualizarUbicacion()
    private EstadoProyecto estado; // nosemgrep: budgetpro.domain.immutability.entity-final-fields.proyecto -
                                   // Project state machine (BORRADOR -> ACTIVO -> CERRADO)

    /**
     * Constructor privado. Usar factory methods.
     */
    private Proyecto(ProyectoId id, String nombre, String ubicacion, EstadoProyecto estado) {
        validarInvariantes(nombre, estado);

        this.id = Objects.requireNonNull(id, "El ID del proyecto no puede ser nulo");
        this.nombre = normalizarNombre(nombre);
        this.ubicacion = ubicacion != null ? ubicacion.trim() : null;
        this.estado = Objects.requireNonNull(estado, "El estado del proyecto no puede ser nulo");
    }

    /**
     * Factory method para crear un nuevo Proyecto en estado BORRADOR.
     */
    public static Proyecto crear(ProyectoId id, String nombre, String ubicacion) {
        return new Proyecto(id, nombre, ubicacion, EstadoProyecto.BORRADOR);
    }

    /**
     * Factory method para reconstruir un Proyecto desde persistencia.
     */
    public static Proyecto reconstruir(ProyectoId id, String nombre, String ubicacion, EstadoProyecto estado) {
        return new Proyecto(id, nombre, ubicacion, estado);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(String nombre, EstadoProyecto estado) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del proyecto no puede ser nulo");
        }
    }

    /**
     * Normaliza el nombre del proyecto (trim).
     */
    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío");
        }
        return nombre.trim();
    }

    /**
     * Actualiza el nombre del proyecto.
     */
    public void actualizarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del proyecto no puede estar vacío");
        }
        this.nombre = nuevoNombre.trim();
    }

    /**
     * Actualiza la ubicación del proyecto.
     */
    public void actualizarUbicacion(String nuevaUbicacion) {
        this.ubicacion = nuevaUbicacion != null ? nuevaUbicacion.trim() : null;
    }

    /**
     * Inicia el proyecto (cambia el estado a ACTIVO).
     */
    public void activar() {
        this.estado = EstadoProyecto.ACTIVO;
    }

    /**
     * Suspende el proyecto (cambia el estado a SUSPENDIDO).
     */
    public void suspender() {
        this.estado = EstadoProyecto.SUSPENDIDO;
    }

    /**
     * Finaliza el proyecto (cambia el estado a CERRADO).
     */
    public void cerrar() {
        this.estado = EstadoProyecto.CERRADO;
    }

    // Getters

    public ProyectoId getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public EstadoProyecto getEstado() {
        return estado;
    }

    public boolean isActivo() {
        return estado == EstadoProyecto.ACTIVO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Proyecto proyecto = (Proyecto) o;
        return Objects.equals(id, proyecto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Proyecto{id=%s, nombre='%s', ubicacion='%s', estado=%s}", id, nombre, ubicacion, estado);
    }
}
