package com.budgetpro.domain.logistica.backlog.model;

import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un requerimiento de compra generado automáticamente
 * cuando una requisición no puede ser despachada por falta de stock.
 * 
 * Refactorizado a inmutable para cumplir con AXIOM.
 */
public final class RequerimientoCompra {

    private final RequerimientoCompraId id;
    private final UUID proyectoId;
    private final RequisicionId requisicionId;
    private final String recursoExternalId;
    private final BigDecimal cantidadNecesaria;
    private final String unidadMedida;
    private final PrioridadCompra prioridad;
    private final EstadoRequerimiento estado;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaActualizacion;
    private final Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private RequerimientoCompra(RequerimientoCompraId id, UUID proyectoId, RequisicionId requisicionId,
            String recursoExternalId, BigDecimal cantidadNecesaria, String unidadMedida, PrioridadCompra prioridad,
            EstadoRequerimiento estado, LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, Long version) {
        validarInvariantes(proyectoId, requisicionId, recursoExternalId, cantidadNecesaria, prioridad, estado);

        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.requisicionId = Objects.requireNonNull(requisicionId, "El requisicionId no puede ser nulo");
        this.recursoExternalId = Objects.requireNonNull(recursoExternalId, "El recursoExternalId no puede ser nulo")
                .trim();
        this.cantidadNecesaria = cantidadNecesaria;
        this.unidadMedida = Objects.requireNonNull(unidadMedida, "La unidadMedida no puede ser nula").trim();
        this.prioridad = Objects.requireNonNull(prioridad, "La prioridad no puede ser nula");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
        this.fechaActualizacion = fechaActualizacion != null ? fechaActualizacion : LocalDateTime.now();
        this.version = version != null ? version : 0L;
    }

    /**
     * Valida las invariantes del requerimiento.
     */
    private void validarInvariantes(UUID proyectoId, RequisicionId requisicionId, String recursoExternalId,
            BigDecimal cantidadNecesaria, PrioridadCompra prioridad, EstadoRequerimiento estado) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (requisicionId == null) {
            throw new IllegalArgumentException("El requisicionId no puede ser nulo");
        }
        if (recursoExternalId == null || recursoExternalId.isBlank()) {
            throw new IllegalArgumentException("El recursoExternalId no puede ser nulo o vacío");
        }
        if (cantidadNecesaria == null || cantidadNecesaria.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidadNecesaria debe ser positiva");
        }
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad no puede ser nula");
        }
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
    }

    /**
     * Factory method para crear un nuevo RequerimientoCompra.
     */
    public static RequerimientoCompra crear(RequerimientoCompraId id, UUID proyectoId, RequisicionId requisicionId,
            String recursoExternalId, BigDecimal cantidadNecesaria, String unidadMedida, PrioridadCompra prioridad) {
        return new RequerimientoCompra(id, proyectoId, requisicionId, recursoExternalId, cantidadNecesaria,
                unidadMedida, prioridad, EstadoRequerimiento.PENDIENTE, LocalDateTime.now(), LocalDateTime.now(), 0L);
    }

    /**
     * Factory method para reconstruir un RequerimientoCompra desde persistencia.
     */
    public static RequerimientoCompra reconstruir(RequerimientoCompraId id, UUID proyectoId,
            RequisicionId requisicionId, String recursoExternalId, BigDecimal cantidadNecesaria, String unidadMedida,
            PrioridadCompra prioridad, EstadoRequerimiento estado, LocalDateTime fechaCreacion,
            LocalDateTime fechaActualizacion, Long version) {
        return new RequerimientoCompra(id, proyectoId, requisicionId, recursoExternalId, cantidadNecesaria,
                unidadMedida, prioridad, estado, fechaCreacion, fechaActualizacion, version);
    }

    /**
     * Marca el requerimiento como recibido y retorna una nueva instancia.
     */
    public RequerimientoCompra marcarRecibido() {
        if (this.estado == EstadoRequerimiento.RECIBIDA) {
            return this;
        }
        if (this.estado == EstadoRequerimiento.CANCELADA) {
            throw new IllegalStateException("No se puede marcar como recibido un requerimiento cancelado");
        }
        return new RequerimientoCompra(this.id, this.proyectoId, this.requisicionId, this.recursoExternalId,
                this.cantidadNecesaria, this.unidadMedida, this.prioridad, EstadoRequerimiento.RECIBIDA,
                this.fechaCreacion, LocalDateTime.now(), this.version + 1);
    }

    /**
     * Cancela el requerimiento y retorna una nueva instancia.
     */
    public RequerimientoCompra cancelar() {
        if (this.estado == EstadoRequerimiento.CANCELADA) {
            return this;
        }
        if (this.estado == EstadoRequerimiento.RECIBIDA) {
            throw new IllegalStateException("No se puede cancelar un requerimiento ya recibido");
        }
        return new RequerimientoCompra(this.id, this.proyectoId, this.requisicionId, this.recursoExternalId,
                this.cantidadNecesaria, this.unidadMedida, this.prioridad, EstadoRequerimiento.CANCELADA,
                this.fechaCreacion, LocalDateTime.now(), this.version + 1);
    }

    // Getters

    public RequerimientoCompraId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public RequisicionId getRequisicionId() {
        return requisicionId;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public BigDecimal getCantidadNecesaria() {
        return cantidadNecesaria;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public PrioridadCompra getPrioridad() {
        return prioridad;
    }

    public EstadoRequerimiento getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
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
        RequerimientoCompra that = (RequerimientoCompra) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "RequerimientoCompra{id=%s, proyectoId=%s, requisicionId=%s, recursoExternalId='%s', cantidadNecesaria=%s, prioridad=%s, estado=%s}",
                id, proyectoId, requisicionId, recursoExternalId, cantidadNecesaria, prioridad, estado);
    }
}
