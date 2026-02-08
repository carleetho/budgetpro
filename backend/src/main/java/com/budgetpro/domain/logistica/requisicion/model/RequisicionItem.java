package com.budgetpro.domain.logistica.requisicion.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado Requisicion.
 * 
 * Representa un ítem solicitado en una requisición.
 * 
 * Invariantes: - El recursoExternalId no puede ser nulo o vacío - La
 * cantidadSolicitada debe ser positiva - La cantidadDespachada no puede ser
 * negativa - La cantidadDespachada no puede exceder cantidadSolicitada - La
 * unidadMedida no puede estar vacía
 */
public final class RequisicionItem {

    private final RequisicionItemId id;
    private final String recursoExternalId; // Referencia externa al recurso (ej. "MAT-001")
    private final UUID partidaId; // Partida presupuestal (imputación)
    private final BigDecimal cantidadSolicitada; // Cantidad solicitada (inmutable)
    // JUSTIFICACIÓN ARQUITECTÓNICA: Tracking acumulativo de despachos.
    // - cantidadDespachada: se incrementa con cada despacho parcial
    // (registrarDespacho)
    // Este patrón acumulativo es esencial para registrar entregas parciales de
    // inventario
    // nosemgrep: budgetpro.domain.immutability.entity-final-fields.logistica
    private BigDecimal cantidadDespachada; // Cantidad despachada (acumulativa)
    private final String unidadMedida; // Unidad de medida (ej. "SACOS", "TONELADAS")
    private final String justificacion; // Justificación de la solicitud

    /**
     * Constructor privado. Usar factory methods.
     */
    private RequisicionItem(RequisicionItemId id, String recursoExternalId, UUID partidaId,
            BigDecimal cantidadSolicitada, BigDecimal cantidadDespachada, String unidadMedida, String justificacion) {
        validarInvariantes(recursoExternalId, cantidadSolicitada, cantidadDespachada, unidadMedida);

        this.id = Objects.requireNonNull(id, "El ID del ítem no puede ser nulo");
        this.recursoExternalId = Objects.requireNonNull(recursoExternalId, "El recursoExternalId no puede ser nulo")
                .trim();
        this.partidaId = partidaId;
        this.cantidadSolicitada = cantidadSolicitada != null ? cantidadSolicitada : BigDecimal.ZERO;
        this.cantidadDespachada = cantidadDespachada != null ? cantidadDespachada : BigDecimal.ZERO;
        this.unidadMedida = unidadMedida != null ? unidadMedida.trim() : null;
        this.justificacion = justificacion != null ? justificacion.trim() : null;

        // Validar que cantidadDespachada no exceda cantidadSolicitada
        if (this.cantidadDespachada.compareTo(this.cantidadSolicitada) > 0) {
            throw new IllegalArgumentException(
                    String.format("La cantidad despachada (%s) no puede exceder la cantidad solicitada (%s)",
                            this.cantidadDespachada, this.cantidadSolicitada));
        }
    }

    /**
     * Valida las invariantes del ítem.
     */
    private void validarInvariantes(String recursoExternalId, BigDecimal cantidadSolicitada,
            BigDecimal cantidadDespachada, String unidadMedida) {
        if (recursoExternalId == null || recursoExternalId.isBlank()) {
            throw new IllegalArgumentException("El recursoExternalId no puede ser nulo o vacío");
        }
        if (cantidadSolicitada == null || cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser positiva");
        }
        if (cantidadDespachada != null && cantidadDespachada.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad despachada no puede ser negativa");
        }
        if (unidadMedida == null || unidadMedida.isBlank()) {
            throw new IllegalArgumentException("La unidad de medida no puede estar vacía");
        }
    }

    /**
     * Factory method para crear un nuevo RequisicionItem.
     */
    public static RequisicionItem crear(RequisicionItemId id, String recursoExternalId, UUID partidaId,
            BigDecimal cantidadSolicitada, String unidadMedida, String justificacion) {
        return new RequisicionItem(id, recursoExternalId, partidaId, cantidadSolicitada, BigDecimal.ZERO, unidadMedida,
                justificacion);
    }

    /**
     * Factory method para reconstruir un RequisicionItem desde persistencia.
     */
    public static RequisicionItem reconstruir(RequisicionItemId id, String recursoExternalId, UUID partidaId,
            BigDecimal cantidadSolicitada, BigDecimal cantidadDespachada, String unidadMedida, String justificacion) {
        return new RequisicionItem(id, recursoExternalId, partidaId, cantidadSolicitada, cantidadDespachada,
                unidadMedida, justificacion);
    }

    /**
     * Registra un despacho parcial o total del ítem.
     * 
     * @param cantidad Cantidad a despachar (debe ser positiva)
     * @throws IllegalArgumentException si la cantidad no es positiva o excedería
     *                                  cantidadSolicitada
     */
    public void registrarDespacho(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a despachar debe ser positiva");
        }

        BigDecimal nuevaCantidadDespachada = this.cantidadDespachada.add(cantidad);

        if (nuevaCantidadDespachada.compareTo(this.cantidadSolicitada) > 0) {
            throw new IllegalArgumentException(
                    String.format("No se puede despachar %s. Ya se despacharon %s de %s solicitados", cantidad,
                            this.cantidadDespachada, this.cantidadSolicitada));
        }

        this.cantidadDespachada = nuevaCantidadDespachada;
    }

    /**
     * Verifica si el ítem está completamente despachado.
     */
    public boolean estaCompletamenteDespachado() {
        return cantidadDespachada.compareTo(cantidadSolicitada) >= 0;
    }

    /**
     * Obtiene la cantidad pendiente de despachar.
     */
    public BigDecimal getCantidadPendiente() {
        return cantidadSolicitada.subtract(cantidadDespachada);
    }

    // Getters

    public RequisicionItemId getId() {
        return id;
    }

    public String getRecursoExternalId() {
        return recursoExternalId;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public BigDecimal getCantidadSolicitada() {
        return cantidadSolicitada;
    }

    public BigDecimal getCantidadDespachada() {
        return cantidadDespachada;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public String getJustificacion() {
        return justificacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RequisicionItem that = (RequisicionItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "RequisicionItem{id=%s, recursoExternalId='%s', cantidadSolicitada=%s, cantidadDespachada=%s}", id,
                recursoExternalId, cantidadSolicitada, cantidadDespachada);
    }
}
