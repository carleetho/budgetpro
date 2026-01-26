package com.budgetpro.domain.finanzas.ordencambio.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que registra el historial de cambios de estado de una Orden de
 * Cambio.
 * 
 * Invariantes: - El ordenCambioId no puede ser nulo - El estado nuevo no puede
 * ser nulo - La fecha no puede ser nula - El usuarioId no puede ser nulo
 */
public class OrdenCambioHistorial {

    private final UUID id;
    private final OrdenCambioId ordenCambioId;
    private final EstadoOrdenCambio estadoAnterior; // Nullable (caso inicial)
    private final EstadoOrdenCambio estadoNuevo;
    private final UUID usuarioId;
    private final LocalDateTime fecha;
    private final String comentario;

    private OrdenCambioHistorial(UUID id, OrdenCambioId ordenCambioId, EstadoOrdenCambio estadoAnterior,
            EstadoOrdenCambio estadoNuevo, UUID usuarioId, LocalDateTime fecha, String comentario) {

        validarInvariantes(ordenCambioId, estadoNuevo, usuarioId, fecha);

        this.id = Objects.requireNonNull(id, "El ID del historial no puede ser nulo");
        this.ordenCambioId = ordenCambioId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuarioId = usuarioId;
        this.fecha = fecha;
        this.comentario = comentario != null ? comentario.trim() : null;
    }

    public static OrdenCambioHistorial registrarCambio(OrdenCambioId ordenCambioId, EstadoOrdenCambio estadoAnterior,
            EstadoOrdenCambio estadoNuevo, UUID usuarioId, String comentario) {
        return new OrdenCambioHistorial(UUID.randomUUID(), ordenCambioId, estadoAnterior, estadoNuevo, usuarioId,
                LocalDateTime.now(), comentario);
    }

    public static OrdenCambioHistorial reconstruir(UUID id, OrdenCambioId ordenCambioId,
            EstadoOrdenCambio estadoAnterior, EstadoOrdenCambio estadoNuevo, UUID usuarioId, LocalDateTime fecha,
            String comentario) {
        return new OrdenCambioHistorial(id, ordenCambioId, estadoAnterior, estadoNuevo, usuarioId, fecha, comentario);
    }

    private void validarInvariantes(OrdenCambioId ordenCambioId, EstadoOrdenCambio estadoNuevo, UUID usuarioId,
            LocalDateTime fecha) {
        if (ordenCambioId == null)
            throw new IllegalArgumentException("El ordenCambioId no puede ser nulo");
        if (estadoNuevo == null)
            throw new IllegalArgumentException("El estado nuevo no puede ser nulo");
        if (usuarioId == null)
            throw new IllegalArgumentException("El usuarioId no puede ser nulo");
        if (fecha == null)
            throw new IllegalArgumentException("La fecha no puede ser nula");
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public OrdenCambioId getOrdenCambioId() {
        return ordenCambioId;
    }

    public EstadoOrdenCambio getEstadoAnterior() {
        return estadoAnterior;
    }

    public EstadoOrdenCambio getEstadoNuevo() {
        return estadoNuevo;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getComentario() {
        return comentario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrdenCambioHistorial that = (OrdenCambioHistorial) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
