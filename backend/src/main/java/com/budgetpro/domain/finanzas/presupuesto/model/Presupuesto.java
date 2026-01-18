package com.budgetpro.domain.finanzas.presupuesto.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado PRESUPUESTO.
 * 
 * Representa un presupuesto asociado a un proyecto.
 * 
 * Invariantes:
 * - El nombre no puede estar vacío
 * - El proyectoId no puede ser nulo
 * - El estado no puede ser nulo
 * - Un proyecto solo tiene un presupuesto activo (validado a nivel de persistencia)
 * 
 * Contexto: Presupuestos & APUs
 */
public final class Presupuesto {

    private final PresupuestoId id;
    private final UUID proyectoId;
    private String nombre;
    private EstadoPresupuesto estado;
    private Boolean esContractual;
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Presupuesto(PresupuestoId id, UUID proyectoId, String nombre, 
                        EstadoPresupuesto estado, Boolean esContractual, Long version) {
        validarInvariantes(proyectoId, nombre, estado);
        
        this.id = Objects.requireNonNull(id, "El ID del presupuesto no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.nombre = normalizarNombre(nombre);
        this.estado = Objects.requireNonNull(estado, "El estado del presupuesto no puede ser nulo");
        this.esContractual = esContractual != null ? esContractual : false;
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear un nuevo Presupuesto en estado BORRADOR.
     */
    public static Presupuesto crear(PresupuestoId id, UUID proyectoId, String nombre) {
        return new Presupuesto(id, proyectoId, nombre, EstadoPresupuesto.BORRADOR, false, 0L);
    }

    /**
     * Factory method para reconstruir un Presupuesto desde persistencia.
     */
    public static Presupuesto reconstruir(PresupuestoId id, UUID proyectoId, String nombre,
                                          EstadoPresupuesto estado, Boolean esContractual, Long version) {
        return new Presupuesto(id, proyectoId, nombre, estado, esContractual, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, String nombre, EstadoPresupuesto estado) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado del presupuesto no puede ser nulo");
        }
    }

    /**
     * Normaliza el nombre del presupuesto (trim).
     */
    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío");
        }
        return nombre.trim();
    }

    /**
     * Actualiza el nombre del presupuesto.
     */
    public void actualizarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del presupuesto no puede estar vacío");
        }
        this.nombre = nuevoNombre.trim();
    }

    /**
     * Aprueba el presupuesto (cambia el estado a CONGELADO y lo marca como contractual).
     * 
     * Congelamiento lógico: El presupuesto no debe modificarse después de aprobarse.
     */
    public void aprobar() {
        this.estado = EstadoPresupuesto.CONGELADO;
        this.esContractual = true; // Congelamiento lógico
    }

    /**
     * Marca el presupuesto como contractual.
     */
    public void marcarComoContractual() {
        this.esContractual = true;
    }

    // Getters

    public PresupuestoId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoPresupuesto getEstado() {
        return estado;
    }

    public Boolean getEsContractual() {
        return esContractual;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isAprobado() {
        return estado == EstadoPresupuesto.CONGELADO;
    }

    public boolean isContractual() {
        return esContractual != null && esContractual;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Presupuesto that = (Presupuesto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Presupuesto{id=%s, proyectoId=%s, nombre='%s', estado=%s, esContractual=%s, version=%d}", 
                           id, proyectoId, nombre, estado, esContractual, version);
    }
}
