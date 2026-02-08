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
 * Esta entidad pertenece al contexto de Compras y se usa para inyectar demanda
 * en el módulo de Compras cuando hay backlog de inventario.
 * 
 * Invariantes: - El proyectoId es obligatorio - El requisicionId (origen) es
 * obligatorio - El recursoExternalId no puede ser nulo o vacío - La
 * cantidadNecesaria debe ser positiva - La prioridad no puede ser nula - El
 * estado no puede ser nulo
 */
public final class RequerimientoCompra {

    private final RequerimientoCompraId id;
    private final UUID proyectoId;
    private final RequisicionId requisicionId; // Requisición que originó este requerimiento
    private final String recursoExternalId; // ID externo del recurso (ej. "MAT-001")
    private final BigDecimal cantidadNecesaria; // Cantidad que se necesita comprar
    private final String unidadMedida; // Unidad de medida (debe coincidir con RequisicionItem)
    private final PrioridadCompra prioridad; // Prioridad del requerimiento
    // JUSTIFICACIÓN ARQUITECTÓNICA: Aggregate Root con estado de workflow mutable.
    // Campos de lifecycle y auditoría que DEBEN ser mutables:
    // - estado: transiciones de workflow (PENDIENTE → RECIBIDA/CANCELADA)
    // - fechaCreacion/fechaActualizacion: timestamps de auditoría
    // - version: optimistic locking
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    private EstadoRequerimiento estado; // Estado en el workflow de compras
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    private LocalDateTime fechaCreacion;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    private LocalDateTime fechaActualizacion;
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields
    private Long version;

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
     * Factory method para crear un nuevo RequerimientoCompra. Estado inicial:
     * PENDIENTE.
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
     * Marca el requerimiento como recibido (cuando la compra llega y se registra en
     * inventario).
     */
    public void marcarRecibido() {
        if (this.estado == EstadoRequerimiento.RECIBIDA) {
            return; // Ya está recibido
        }
        if (this.estado == EstadoRequerimiento.CANCELADA) {
            throw new IllegalStateException("No se puede marcar como recibido un requerimiento cancelado");
        }
        this.estado = EstadoRequerimiento.RECIBIDA;
        this.fechaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;
    }

    /**
     * Cancela el requerimiento.
     */
    public void cancelar() {
        if (this.estado == EstadoRequerimiento.CANCELADA) {
            return; // Ya está cancelado
        }
        if (this.estado == EstadoRequerimiento.RECIBIDA) {
            throw new IllegalStateException("No se puede cancelar un requerimiento ya recibido");
        }
        this.estado = EstadoRequerimiento.CANCELADA;
        this.fechaActualizacion = LocalDateTime.now();
        this.version = this.version + 1;
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
