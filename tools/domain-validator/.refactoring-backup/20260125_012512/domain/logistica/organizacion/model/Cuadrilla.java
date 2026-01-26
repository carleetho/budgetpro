package com.budgetpro.domain.logistica.organizacion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del dominio que representa una Cuadrilla de trabajo.
 * 
 * Una Cuadrilla está vinculada a un FrenteTrabajo y tiene un capataz (foreman).
 * Pertenece al contexto del Proyecto (no es aggregate root).
 * 
 * Invariantes:
 * - Código único por proyecto (proyectoId + codigo)
 * - codigo no puede estar en blanco
 * - proyectoId, capataz y frenteTrabajo son obligatorios
 * - Estado activa/inactiva para ciclo de vida
 */
public final class Cuadrilla {

    private final CuadrillaId id;
    private final UUID proyectoId;
    private final String codigo;
    private final String nombre;
    private final String capataz; // Capataz (foreman) de la cuadrilla
    private final FrenteTrabajoId frenteTrabajoId; // FK al frente de trabajo
    private boolean activa;

    /**
     * Constructor privado. Usar factory methods crear() y reconstruir().
     */
    private Cuadrilla(CuadrillaId id, UUID proyectoId, String codigo, String nombre,
                     String capataz, FrenteTrabajoId frenteTrabajoId, boolean activa) {
        this.id = Objects.requireNonNull(id, "El ID de la cuadrilla no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId es obligatorio");
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la cuadrilla no puede estar en blanco");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre != null ? nombre.trim() : null;
        if (capataz == null || capataz.isBlank()) {
            throw new IllegalArgumentException("El capataz es obligatorio");
        }
        this.capataz = capataz.trim();
        this.frenteTrabajoId = Objects.requireNonNull(frenteTrabajoId, "El frenteTrabajoId es obligatorio");
        this.activa = activa;
    }

    /**
     * Factory method para crear una nueva cuadrilla.
     * La cuadrilla se crea activa.
     *
     * @param id              Identificador único de la cuadrilla
     * @param proyectoId      ID del proyecto (obligatorio)
     * @param codigo          Código único por proyecto (no puede estar en blanco)
     * @param nombre          Nombre de la cuadrilla
     * @param capataz         Capataz (foreman) de la cuadrilla (obligatorio)
     * @param frenteTrabajoId ID del frente de trabajo (obligatorio)
     * @return Nueva Cuadrilla activa
     */
    public static Cuadrilla crear(CuadrillaId id, UUID proyectoId, String codigo, String nombre,
                                 String capataz, FrenteTrabajoId frenteTrabajoId) {
        return new Cuadrilla(id, proyectoId, codigo, nombre, capataz, frenteTrabajoId, true);
    }

    /**
     * Factory method para reconstruir una cuadrilla desde persistencia.
     */
    public static Cuadrilla reconstruir(CuadrillaId id, UUID proyectoId, String codigo, String nombre,
                                        String capataz, FrenteTrabajoId frenteTrabajoId, boolean activa) {
        return new Cuadrilla(id, proyectoId, codigo, nombre, capataz, frenteTrabajoId, activa);
    }

    /**
     * Activa la cuadrilla (disponible para operaciones).
     */
    public void activar() {
        this.activa = true;
    }

    /**
     * Desactiva la cuadrilla (no disponible para nuevas operaciones).
     */
    public void desactivar() {
        this.activa = false;
    }

    // Getters

    public CuadrillaId getId() {
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

    public String getCapataz() {
        return capataz;
    }

    public FrenteTrabajoId getFrenteTrabajoId() {
        return frenteTrabajoId;
    }

    public boolean isActiva() {
        return activa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuadrilla that = (Cuadrilla) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Cuadrilla{id=%s, proyectoId=%s, codigo='%s', nombre='%s', frenteTrabajoId=%s, activa=%s}",
                id, proyectoId, codigo, nombre, frenteTrabajoId, activa);
    }
}
