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
 * Representa una requisición de materiales con workflow de estados.
 * 
 * Invariantes:
 * - El proyectoId es obligatorio
 * - El solicitante no puede estar vacío
 * - La fechaSolicitud no puede ser nula
 * - La lista de ítems no puede ser nula ni vacía
 * - Solo el Residente asignado al proyecto puede aprobar
 * - El estado debe seguir la máquina de estados válida
 * 
 * Contexto: Logística & Control de Materiales
 */
public final class Requisicion {

    private final RequisicionId id;
    private final UUID proyectoId;
    private final String solicitante; // Nombre del solicitante
    private final String frenteTrabajo; // Frente de trabajo (opcional, puede ser null)
    private LocalDate fechaSolicitud;
    private UUID aprobadoPor; // ID del usuario que aprobó (null si no está aprobada)
    private EstadoRequisicion estado;
    private String observaciones; // Observaciones generales
    private Long version;
    
    // Lista de ítems (entidades internas del agregado)
    private final List<RequisicionItem> items;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Requisicion(RequisicionId id, UUID proyectoId, String solicitante, String frenteTrabajo,
                       LocalDate fechaSolicitud, UUID aprobadoPor, EstadoRequisicion estado,
                       String observaciones, Long version, List<RequisicionItem> items) {
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
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
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
                                         LocalDate fechaSolicitud, UUID aprobadoPor, EstadoRequisicion estado,
                                         String observaciones, Long version, List<RequisicionItem> items) {
        return new Requisicion(id, proyectoId, solicitante, frenteTrabajo, fechaSolicitud, aprobadoPor,
                             estado, observaciones, version, items);
    }

    /**
     * Envía la requisición para aprobación (cambia estado a SOLICITADA).
     * Solo válido desde BORRADOR.
     */
    public void solicitar() {
        if (this.estado != EstadoRequisicion.BORRADOR) {
            throw new IllegalStateException(
                String.format("Solo se puede solicitar una requisición en estado BORRADOR. Estado actual: %s", this.estado)
            );
        }
        this.estado = EstadoRequisicion.SOLICITADA;
        this.fechaSolicitud = LocalDate.now();
        this.version = this.version + 1;
    }

    /**
     * Aprueba la requisición (cambia estado a APROBADA).
     * Solo válido desde SOLICITADA.
     * 
     * @param aprobadoPor ID del usuario que aprueba (debe ser el Residente asignado al proyecto)
     * @param residenteAsignadoId ID del Residente asignado al proyecto (para validación RBAC)
     * @throws IllegalStateException si el estado no permite aprobación
     * @throws IllegalArgumentException si el aprobador no es el Residente asignado
     */
    public void aprobar(UUID aprobadoPor, UUID residenteAsignadoId) {
        if (this.estado != EstadoRequisicion.SOLICITADA) {
            throw new IllegalStateException(
                String.format("Solo se puede aprobar una requisición en estado SOLICITADA. Estado actual: %s", this.estado)
            );
        }
        
        // Validación RBAC: solo el Residente asignado puede aprobar
        if (residenteAsignadoId == null) {
            throw new IllegalArgumentException("El proyecto debe tener un Residente asignado para aprobar requisiciones");
        }
        if (!Objects.equals(aprobadoPor, residenteAsignadoId)) {
            throw new IllegalArgumentException(
                String.format("Solo el Residente asignado al proyecto puede aprobar requisiciones. " +
                            "Aprobador: %s, Residente asignado: %s", aprobadoPor, residenteAsignadoId)
            );
        }
        
        this.aprobadoPor = Objects.requireNonNull(aprobadoPor, "El aprobadoPor no puede ser nulo");
        this.estado = EstadoRequisicion.APROBADA;
        this.version = this.version + 1;
    }

    /**
     * Rechaza la requisición (cambia estado a RECHAZADA).
     * Solo válido desde SOLICITADA.
     */
    public void rechazar() {
        if (this.estado != EstadoRequisicion.SOLICITADA) {
            throw new IllegalStateException(
                String.format("Solo se puede rechazar una requisición en estado SOLICITADA. Estado actual: %s", this.estado)
            );
        }
        this.estado = EstadoRequisicion.RECHAZADA;
        this.version = this.version + 1;
    }

    /**
     * Marca la requisición como pendiente de compra (cambia estado a PENDIENTE_COMPRA).
     * Solo válido desde APROBADA o DESPACHADA_PARCIAL.
     */
    public void marcarPendienteCompra() {
        if (this.estado != EstadoRequisicion.APROBADA &&
            this.estado != EstadoRequisicion.DESPACHADA_PARCIAL) {
            throw new IllegalStateException(
                String.format("Solo se puede marcar como pendiente de compra una requisición en estado APROBADA o DESPACHADA_PARCIAL. Estado actual: %s", this.estado)
            );
        }
        this.estado = EstadoRequisicion.PENDIENTE_COMPRA;
        this.version = this.version + 1;
    }

    /**
     * Reactiva la requisición desde PENDIENTE_COMPRA a APROBADA cuando llega stock.
     * Solo válido desde PENDIENTE_COMPRA.
     */
    public void reactivar() {
        if (this.estado != EstadoRequisicion.PENDIENTE_COMPRA) {
            throw new IllegalStateException(
                String.format("Solo se puede reactivar una requisición en estado PENDIENTE_COMPRA. Estado actual: %s", this.estado)
            );
        }
        this.estado = EstadoRequisicion.APROBADA;
        this.version = this.version + 1;
    }

    /**
     * Registra un despacho parcial o total de un ítem.
     * Actualiza el estado de la requisición según el progreso del despacho.
     * 
     * @param itemId ID del ítem a despachar
     * @param cantidad Cantidad a despachar
     * @throws IllegalStateException si la requisición no está en estado válido para despacho
     * @throws IllegalArgumentException si el ítem no existe o la cantidad es inválida
     */
    public void registrarDespacho(RequisicionItemId itemId, BigDecimal cantidad) {
        // Validar que la requisición esté en estado válido para despacho
        if (this.estado != EstadoRequisicion.APROBADA && 
            this.estado != EstadoRequisicion.DESPACHADA_PARCIAL &&
            this.estado != EstadoRequisicion.PENDIENTE_COMPRA) {
            throw new IllegalStateException(
                String.format("No se puede despachar una requisición en estado %s. " +
                            "Debe estar en APROBADA, DESPACHADA_PARCIAL o PENDIENTE_COMPRA", this.estado)
            );
        }
        
        // Buscar el ítem
        RequisicionItem item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ítem no encontrado: " + itemId));
        
        // Registrar el despacho en el ítem
        item.registrarDespacho(cantidad);
        
        // Actualizar el estado de la requisición según el progreso
        actualizarEstadoSegunDespacho();
        
        this.version = this.version + 1;
    }

    /**
     * Actualiza el estado de la requisición según el progreso del despacho.
     * - Si todos los ítems están completamente despachados → DESPACHADA_TOTAL
     * - Si algunos ítems están despachados → DESPACHADA_PARCIAL
     * - Si ningún ítem está despachado → APROBADA (o PENDIENTE_COMPRA)
     */
    private void actualizarEstadoSegunDespacho() {
        boolean todosCompletos = items.stream()
                .allMatch(RequisicionItem::estaCompletamenteDespachado);
        
        boolean algunoDespachado = items.stream()
                .anyMatch(item -> item.getCantidadDespachada().compareTo(BigDecimal.ZERO) > 0);
        
        if (todosCompletos) {
            this.estado = EstadoRequisicion.DESPACHADA_TOTAL;
        } else if (algunoDespachado) {
            this.estado = EstadoRequisicion.DESPACHADA_PARCIAL;
        }
        // Si ningún ítem está despachado, mantener el estado actual (APROBADA o PENDIENTE_COMPRA)
    }

    /**
     * Cierra la requisición (cambia estado a CERRADA).
     * Solo válido desde DESPACHADA_TOTAL.
     */
    public void cerrar() {
        if (this.estado != EstadoRequisicion.DESPACHADA_TOTAL) {
            throw new IllegalStateException(
                String.format("Solo se puede cerrar una requisición en estado DESPACHADA_TOTAL. Estado actual: %s", this.estado)
            );
        }
        this.estado = EstadoRequisicion.CERRADA;
        this.version = this.version + 1;
    }

    /**
     * Agrega un ítem a la requisición.
     * Solo válido en estado BORRADOR.
     */
    public void agregarItem(RequisicionItem item) {
        if (this.estado != EstadoRequisicion.BORRADOR) {
            throw new IllegalStateException(
                String.format("Solo se pueden agregar ítems a una requisición en estado BORRADOR. Estado actual: %s", this.estado)
            );
        }
        if (item == null) {
            throw new IllegalArgumentException("El ítem no puede ser nulo");
        }
        this.items.add(item);
        this.version = this.version + 1;
    }

    /**
     * Obtiene la lista de ítems (inmutable).
     */
    public List<RequisicionItem> getItems() {
        return List.copyOf(items);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Requisicion that = (Requisicion) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Requisicion{id=%s, proyectoId=%s, solicitante='%s', estado=%s, items=%d}",
                           id, proyectoId, solicitante, estado, items.size());
    }
}
