package com.budgetpro.domain.finanzas.avance.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado VALUACION.
 * 
 * Representa un corte de cobro que agrupa avances en un periodo para cobrar al
 * cliente.
 * 
 * Invariantes: - El proyectoId es obligatorio - La fechaCorte es obligatoria -
 * El codigo no puede estar vacío - El estado no puede ser nulo
 * 
 * Contexto: Control de Producción Física y Cobros
 */
public final class Valuacion {

    private final ValuacionId id;
    private final UUID proyectoId;
    private final LocalDate fechaCorte;
    // Justificación: Código editable (VAL-01, VAL-02)
    // nosemgrep
    private String codigo;
    // Justificación: State machine BORRADOR → APROBADA
    // nosemgrep
    private EstadoValuacion estado;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Valuacion(ValuacionId id, UUID proyectoId, LocalDate fechaCorte, String codigo, EstadoValuacion estado,
            Long version) {
        validarInvariantes(proyectoId, fechaCorte, codigo, estado);

        this.id = Objects.requireNonNull(id, "El ID de la valuación no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.fechaCorte = Objects.requireNonNull(fechaCorte, "La fecha de corte no puede ser nula");
        this.codigo = normalizarCodigo(codigo);
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear una nueva Valuacion en estado BORRADOR.
     */
    public static Valuacion crear(ValuacionId id, UUID proyectoId, LocalDate fechaCorte, String codigo) {
        return new Valuacion(id, proyectoId, fechaCorte, codigo, EstadoValuacion.BORRADOR, 0L);
    }

    /**
     * Factory method para reconstruir una Valuacion desde persistencia.
     */
    public static Valuacion reconstruir(ValuacionId id, UUID proyectoId, LocalDate fechaCorte, String codigo,
            EstadoValuacion estado, Long version) {
        return new Valuacion(id, proyectoId, fechaCorte, codigo, estado, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, LocalDate fechaCorte, String codigo, EstadoValuacion estado) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (fechaCorte == null) {
            throw new IllegalArgumentException("La fecha de corte no puede ser nula");
        }
        if (codigo == null || codigo.isBlank()) {
            // REGLA-127
            throw new IllegalArgumentException("El código no puede estar vacío");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
    }

    /**
     * Normaliza el código.
     */
    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código no puede estar vacío");
        }
        return codigo.trim().toUpperCase();
    }

    /**
     * Actualiza el código.
     */
    public void actualizarCodigo(String nuevoCodigo) {
        this.codigo = normalizarCodigo(nuevoCodigo);
    }

    /**
     * Aprueba la valuación (cambia el estado a APROBADA).
     * 
     * Una vez aprobada, la valuación no debe modificarse.
     */
    public void aprobar() {
        if (this.estado == EstadoValuacion.APROBADA) {
            throw new IllegalStateException("La valuación ya está aprobada");
        }
        this.estado = EstadoValuacion.APROBADA;
    }

    // Getters

    public ValuacionId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public String getCodigo() {
        return codigo;
    }

    public EstadoValuacion getEstado() {
        return estado;
    }

    public Long getVersion() {
        return version;
    }

    public boolean isAprobada() {
        return estado == EstadoValuacion.APROBADA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Valuacion valuacion = (Valuacion) o;
        return Objects.equals(id, valuacion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Valuacion{id=%s, proyectoId=%s, fechaCorte=%s, codigo='%s', estado=%s}", id, proyectoId,
                fechaCorte, codigo, estado);
    }
}
