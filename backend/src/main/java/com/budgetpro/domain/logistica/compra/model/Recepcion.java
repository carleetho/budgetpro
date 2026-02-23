package com.budgetpro.domain.logistica.compra.model;

import com.budgetpro.application.compra.exception.BusinessRuleException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado RECEPCION.
 * 
 * Representa un evento de recepción de una orden de compra con cumplimiento legal
 * (guía de remisión) y trazabilidad de auditoría (REGLA-167).
 * 
 * Invariantes:
 * - El compraId es obligatorio
 * - La fechaRecepcion no puede ser nula
 * - La guiaRemision es obligatoria (requisito legal)
 * - La lista de detalles no puede ser nula ni vacía
 * - El creadoPorUsuarioId es obligatorio (REGLA-167)
 * - La fechaCreacion es obligatoria (REGLA-167)
 * 
 * Contexto: Logística & Compras - Recepción de Bienes
 */
public final class Recepcion {

    private final RecepcionId id;
    private final CompraId compraId;
    private final LocalDate fechaRecepcion;
    private final String guiaRemision;
    private final List<RecepcionDetalle> detalles;
    private final Long version;
    private final UUID creadoPorUsuarioId;
    private final LocalDateTime fechaCreacion;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Recepcion(RecepcionId id, CompraId compraId, LocalDate fechaRecepcion, String guiaRemision,
            List<RecepcionDetalle> detalles, Long version, UUID creadoPorUsuarioId, LocalDateTime fechaCreacion) {
        this.id = Objects.requireNonNull(id, "El ID de la recepción no puede ser nulo");
        this.compraId = Objects.requireNonNull(compraId, "El compraId no puede ser nulo");
        this.fechaRecepcion = Objects.requireNonNull(fechaRecepcion, "La fecha de recepción no puede ser nula");
        this.guiaRemision = validarGuiaRemision(guiaRemision);
        this.detalles = validarDetalles(detalles);
        this.version = version != null ? version : 0L;
        this.creadoPorUsuarioId = Objects.requireNonNull(creadoPorUsuarioId, "El creadoPorUsuarioId no puede ser nulo");
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : LocalDateTime.now();
    }

    /**
     * Factory method para crear una nueva Recepcion.
     * 
     * @param id el identificador único de la recepción
     * @param compraId el identificador de la compra asociada
     * @param fechaRecepcion la fecha en que se recibió la mercancía
     * @param guiaRemision el número de guía de remisión (requisito legal)
     * @param detalles la lista de detalles de recepción
     * @param creadoPorUsuarioId el ID del usuario que crea la recepción (RESIDENTE)
     * @return una nueva instancia de Recepcion
     */
    public static Recepcion crear(RecepcionId id, CompraId compraId, LocalDate fechaRecepcion, String guiaRemision,
            List<RecepcionDetalle> detalles, UUID creadoPorUsuarioId) {
        return new Recepcion(id, compraId, fechaRecepcion, guiaRemision, detalles, 0L, creadoPorUsuarioId, null);
    }

    /**
     * Valida que la guía de remisión no sea nula ni vacía.
     * 
     * @param guiaRemision la guía de remisión a validar
     * @return la guía de remisión normalizada (trimmed)
     * @throws BusinessRuleException si la guía de remisión es nula o vacía
     */
    private String validarGuiaRemision(String guiaRemision) {
        if (guiaRemision == null || guiaRemision.isBlank()) {
            throw new BusinessRuleException("La Guía de Remisión es obligatoria para recepción de bienes físicos");
        }
        return guiaRemision.trim();
    }

    /**
     * Valida que la lista de detalles no sea nula ni vacía.
     * 
     * @param detalles la lista de detalles a validar
     * @return una copia defensiva de la lista de detalles
     * @throws IllegalArgumentException si la lista es nula o vacía
     */
    private List<RecepcionDetalle> validarDetalles(List<RecepcionDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La recepción debe tener al menos un detalle");
        }
        return new ArrayList<>(detalles);
    }

    // Getters

    public RecepcionId getId() {
        return id;
    }

    public CompraId getCompraId() {
        return compraId;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public String getGuiaRemision() {
        return guiaRemision;
    }

    /**
     * Obtiene la lista de detalles (inmutable).
     */
    public List<RecepcionDetalle> getDetalles() {
        return List.copyOf(detalles);
    }

    public Long getVersion() {
        return version;
    }

    public UUID getCreadoPorUsuarioId() {
        return creadoPorUsuarioId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Recepcion recepcion = (Recepcion) o;
        return Objects.equals(id, recepcion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                "Recepcion{id=%s, compraId=%s, fechaRecepcion=%s, guiaRemision='%s', detalles=%d, version=%d, creadoPorUsuarioId=%s, fechaCreacion=%s}",
                id, compraId, fechaRecepcion, guiaRemision, detalles.size(), version, creadoPorUsuarioId, fechaCreacion);
    }
}
