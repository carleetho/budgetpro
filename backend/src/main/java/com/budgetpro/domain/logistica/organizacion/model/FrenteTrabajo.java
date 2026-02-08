package com.budgetpro.domain.logistica.organizacion.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Entidad del dominio que representa un Frente de Trabajo.
 * 
 * Un FrenteTrabajo agrupa cuadrillas y actividades dentro de un proyecto.
 * Pertenece al contexto del Proyecto (no es aggregate root).
 * 
 * Invariantes: - Código único por proyecto (proyectoId + codigo) - codigo no
 * puede estar en blanco - proyectoId y responsable son obligatorios - Estado
 * activo/inactivo para ciclo de vida
 */
public final class FrenteTrabajo {

    private final FrenteTrabajoId id;
    private final UUID proyectoId;
    private final String codigo;
    private final String nombre;
    private final String responsable; // Ingeniero responsable
    // JUSTIFICACIÓN ARQUITECTÓNICA: Estado de lifecycle mutable.
    // - activo: ciclo de vida del frente de trabajo (activar/desactivar)
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    private boolean activo;

    /**
     * Constructor privado. Usar factory methods crear() y reconstruir().
     */
    private FrenteTrabajo(FrenteTrabajoId id, UUID proyectoId, String codigo, String nombre, String responsable,
            boolean activo) {
        this.id = Objects.requireNonNull(id, "El ID del frente de trabajo no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId es obligatorio");
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del frente de trabajo no puede estar en blanco");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre != null ? nombre.trim() : null;
        if (responsable == null || responsable.isBlank()) {
            throw new IllegalArgumentException("El responsable es obligatorio");
        }
        this.responsable = responsable.trim();
        this.activo = activo;
    }

    /**
     * Factory method para crear un nuevo frente de trabajo. El frente se crea
     * activo.
     *
     * @param id          Identificador único del frente
     * @param proyectoId  ID del proyecto (obligatorio)
     * @param codigo      Código único por proyecto (no puede estar en blanco)
     * @param nombre      Nombre del frente de trabajo
     * @param responsable Ingeniero responsable (obligatorio)
     * @return Nuevo FrenteTrabajo activo
     */
    public static FrenteTrabajo crear(FrenteTrabajoId id, UUID proyectoId, String codigo, String nombre,
            String responsable) {
        return new FrenteTrabajo(id, proyectoId, codigo, nombre, responsable, true);
    }

    /**
     * Factory method para reconstruir un frente de trabajo desde persistencia.
     */
    public static FrenteTrabajo reconstruir(FrenteTrabajoId id, UUID proyectoId, String codigo, String nombre,
            String responsable, boolean activo) {
        return new FrenteTrabajo(id, proyectoId, codigo, nombre, responsable, activo);
    }

    /**
     * Activa el frente de trabajo (disponible para operaciones).
     */
    public void activar() {
        this.activo = true;
    }

    /**
     * Desactiva el frente de trabajo (no disponible para nuevas operaciones).
     */
    public void desactivar() {
        this.activo = false;
    }

    // Getters

    public FrenteTrabajoId getId() {
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

    public String getResponsable() {
        return responsable;
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
        FrenteTrabajo that = (FrenteTrabajo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("FrenteTrabajo{id=%s, proyectoId=%s, codigo='%s', nombre='%s', activo=%s}", id, proyectoId,
                codigo, nombre, activo);
    }
}
