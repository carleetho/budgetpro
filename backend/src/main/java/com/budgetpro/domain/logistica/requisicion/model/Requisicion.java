package com.budgetpro.domain.logistica.requisicion.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregate Root del agregado REQUISICION.
 * 
 * Refactorizado a inmutable para cumplir con AXIOM.
 */
public final class Requisicion {

    private final RequisicionId id;
    private final UUID proyectoId;
    private final String solicitante;
    private final String frenteTrabajo;
    private final LocalDate fechaSolicitud;
    private final UUID aprobadoPor;
    private final EstadoRequisicion estado;
    private final String observaciones;
    private final Long version;
    private final List<RequisicionItem> items;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Requisicion(RequisicionId id, UUID proyectoId, String solicitante, String frenteTrabajo,
            LocalDate fechaSolicitud, UUID aprobadoPor, EstadoRequisicion estado, String observaciones, Long version,
            List<RequisicionItem> items) {
        validarInvariantes(proyectoId, solicitante, items);

        this.id = Objects.requireNonNull(id, "El ID de la requisición no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.solicitante = normalizarSolicitante(solicitante);
        this.frenteTrabajo = frenteTrabajo != null ? frenteTrabajo.trim() : null;
        this.fechaSolicitud = fechaSolicitud != null ? fechaSolicitud : LocalDate.now();
        this.aprobadoPor = aprobadoPor;
        this.estado = estado != null ? estado : EstadoRequisicion.BORRADOR;
        this.observaciones = observaciones != null ? observaciones.trim() : null;
        this.version = version != null ? version : 0L;
        this.items = items != null ? List.copyOf(items) : List.of();
    }

    /**
     * Valida las invariantes del agregado.
     */
    private void validarInvariantes(UUID proyectoId, String solicitante, List<RequisicionItem> items) {
        if (proyectoId == null) {
            throw new IllegalArgumentException("El proyectoId no puede ser nulo");
        }
        if (solicitante == null || solicitante.isBlank()) {
            throw new IllegalArgumentException("El solicitante no puede estar vacío");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("La requisición debe tener al menos un ítem");
        }
    }

    /**
     * Normaliza el solicitante (trim).
     */
    private String normalizarSolicitante(String solicitante) {
        if (solicitante == null || solicitante.isBlank()) {
            throw new IllegalArgumentException("El solicitante no puede estar vacío");
        }
        return solicitante.trim();
    }

    /**
     * Factory method para crear una nueva Requisicion en estado BORRADOR.
     */
    public static Requisicion crear(RequisicionId id, UUID proyectoId, String solicitante, String frenteTrabajo,
            String observaciones, List<RequisicionItem> items) {
        return new Requisicion(id, proyectoId, solicitante, frenteTrabajo, LocalDate.now(), null,
                EstadoRequisicion.BORRADOR, observaciones, 0L, items);
    }

    /**
     * Factory method para reconstruir una Requisicion desde persistencia.
     */
    public static Requisicion reconstruir(RequisicionId id, UUID proyectoId, String solicitante, String frenteTrabajo,
            LocalDate fechaSolicitud, UUID aprobadoPor, EstadoRequisicion estado, String observaciones, Long version,
            List<RequisicionItem> items) {
        return new Requisicion(id, proyectoId, solicitante, frenteTrabajo, fechaSolicitud, aprobadoPor, estado,
                observaciones, version, items);
    }

    /**
     * Envía la requisición para aprobación y retorna una nueva instancia.
     */
    public Requisicion solicitar() {
        if (this.estado != EstadoRequisicion.BORRADOR) {
            throw new IllegalStateException(String.format(
                    "Solo se puede solicitar una requisición en estado BORRADOR. Estado actual: %s", this.estado));
        }
        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, LocalDate.now(),
                this.aprobadoPor, EstadoRequisicion.SOLICITADA, this.observaciones, this.version + 1, this.items);
    }

    /**
     * Aprueba la requisición y retorna una nueva instancia.
     */
    public Requisicion aprobar(UUID aprobadoPor, UUID residenteAsignadoId) {
        if (this.estado != EstadoRequisicion.SOLICITADA) {
            throw new IllegalStateException(String.format(
                    "Solo se puede aprobar una requisición en estado SOLICITADA. Estado actual: %s", this.estado));
        }

        if (residenteAsignadoId == null) {
            throw new IllegalArgumentException(
                    "El proyecto debe tener un Residente asignado para aprobar requisiciones");
        }
        if (!Objects.equals(aprobadoPor, residenteAsignadoId)) {
            throw new IllegalArgumentException(
                    String.format("Solo el Residente asignado al proyecto puede aprobar requisiciones. "
                            + "Aprobador: %s, Residente asignado: %s", aprobadoPor, residenteAsignadoId));
        }

        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                aprobadoPor, EstadoRequisicion.APROBADA, this.observaciones, this.version + 1, this.items);
    }

    /**
     * Rechaza la requisición y retorna una nueva instancia.
     */
    public Requisicion rechazar() {
        if (this.estado != EstadoRequisicion.SOLICITADA) {
            throw new IllegalStateException(String.format(
                    "Solo se puede rechazar una requisición en estado SOLICITADA. Estado actual: %s", this.estado));
        }
        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                this.aprobadoPor, EstadoRequisicion.RECHAZADA, this.observaciones, this.version + 1, this.items);
    }

    /**
     * Marca la requisición como pendiente de compra y retorna una nueva instancia.
     */
    public Requisicion marcarPendienteCompra() {
        if (this.estado != EstadoRequisicion.APROBADA && this.estado != EstadoRequisicion.DESPACHADA_PARCIAL) {
            throw new IllegalStateException(String.format(
                    "Solo se puede marcar como pendiente de compra una requisición en estado APROBADA o DESPACHADA_PARCIAL. Estado actual: %s",
                    this.estado));
        }
        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                this.aprobadoPor, EstadoRequisicion.PENDIENTE_COMPRA, this.observaciones, this.version + 1, this.items);
    }

    /**
     * Reactiva la requisición y retorna una nueva instancia.
     */
    public Requisicion reactivar() {
        if (this.estado != EstadoRequisicion.PENDIENTE_COMPRA) {
            throw new IllegalStateException(String.format(
                    "Solo se puede reactivar una requisición en estado PENDIENTE_COMPRA. Estado actual: %s",
                    this.estado));
        }
        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                this.aprobadoPor, EstadoRequisicion.APROBADA, this.observaciones, this.version + 1, this.items);
    }

    /**
     * Registra un despacho y retorna una nueva instancia.
     */
    public Requisicion registrarDespacho(RequisicionItemId itemId, BigDecimal cantidad) {
        if (this.estado != EstadoRequisicion.APROBADA && this.estado != EstadoRequisicion.DESPACHADA_PARCIAL
                && this.estado != EstadoRequisicion.PENDIENTE_COMPRA) {
            throw new IllegalStateException(String.format("No se puede despachar una requisición en estado %s. "
                    + "Debe estar en APROBADA, DESPACHADA_PARCIAL o PENDIENTE_COMPRA", this.estado));
        }

        List<RequisicionItem> nuevosItems = new ArrayList<>(items);
        for (RequisicionItem item : nuevosItems) {
            if (item.getId().equals(itemId)) {
                item.registrarDespacho(cantidad);
            }
        }

        // Calcular nuevo estado según despacho
        EstadoRequisicion nuevoEstado = calcularEstadoSegunDespacho(nuevosItems);

        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                this.aprobadoPor, nuevoEstado, this.observaciones, this.version + 1, nuevosItems);
    }

    /**
     * Calcula el estado de la requisición según el progreso del despacho de sus
     * ítems.
     */
    private EstadoRequisicion calcularEstadoSegunDespacho(List<RequisicionItem> items) {
        boolean todosCompletos = items.stream().allMatch(RequisicionItem::estaCompletamenteDespachado);
        boolean algunoDespachado = items.stream()
                .anyMatch(item -> item.getCantidadDespachada().compareTo(BigDecimal.ZERO) > 0);

        if (todosCompletos) {
            return EstadoRequisicion.DESPACHADA_TOTAL;
        } else if (algunoDespachado) {
            return EstadoRequisicion.DESPACHADA_PARCIAL;
        }
        return this.estado;
    }

    /**
     * Cierra la requisición y retorna una nueva instancia.
     */
    public Requisicion cerrar() {
        if (this.estado != EstadoRequisicion.DESPACHADA_TOTAL) {
            throw new IllegalStateException(String.format(
                    "Solo se puede cerrar una requisición en estado DESPACHADA_TOTAL. Estado actual: %s", this.estado));
        }
        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                this.aprobadoPor, EstadoRequisicion.CERRADA, this.observaciones, this.version + 1, this.items);
    }

    /**
     * Agrega un ítem y retorna una nueva instancia.
     */
    public Requisicion agregarItem(RequisicionItem item) {
        if (this.estado != EstadoRequisicion.BORRADOR) {
            throw new IllegalStateException(String.format(
                    "Solo se pueden agregar ítems a una requisición en estado BORRADOR. Estado actual: %s",
                    this.estado));
        }
        if (item == null) {
            throw new IllegalArgumentException("El ítem no puede ser nulo");
        }
        List<RequisicionItem> nuevosItems = new ArrayList<>(this.items);
        nuevosItems.add(item);
        return new Requisicion(this.id, this.proyectoId, this.solicitante, this.frenteTrabajo, this.fechaSolicitud,
                this.aprobadoPor, this.estado, this.observaciones, this.version + 1, nuevosItems);
    }

    // Getters

    public RequisicionId getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public String getFrenteTrabajo() {
        return frenteTrabajo;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public UUID getAprobadoPor() {
        return aprobadoPor;
    }

    public EstadoRequisicion getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Long getVersion() {
        return version;
    }

    public List<RequisicionItem> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Requisicion that = (Requisicion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Requisicion{id=%s, proyectoId=%s, solicitante='%s', estado=%s, items=%d}", id, proyectoId,
                solicitante, estado, items.size());
    }
}
