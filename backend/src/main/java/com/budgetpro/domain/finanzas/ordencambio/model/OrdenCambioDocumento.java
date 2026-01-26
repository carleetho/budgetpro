package com.budgetpro.domain.finanzas.ordencambio.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un documento adjunto a una Orden de Cambio.
 * 
 * Invariantes: - El ordenCambioId no puede ser nulo - El tipo no puede ser nulo
 * - El nombre de archivo no puede estar vacío - La ruta de archivo no puede
 * estar vacía - La fecha de carga no puede ser nula - El usuarioId no puede ser
 * nulo
 */
public class OrdenCambioDocumento {

    private final UUID id;
    private final OrdenCambioId ordenCambioId;
    private final TipoDocumentoOrdenCambio tipo;
    private final String nombreArchivo;
    private final String rutaArchivo;
    private final LocalDateTime fechaCarga;
    private final UUID usuarioId;

    private OrdenCambioDocumento(UUID id, OrdenCambioId ordenCambioId, TipoDocumentoOrdenCambio tipo,
            String nombreArchivo, String rutaArchivo, LocalDateTime fechaCarga, UUID usuarioId) {

        validarInvariantes(ordenCambioId, tipo, nombreArchivo, rutaArchivo, fechaCarga, usuarioId);

        this.id = Objects.requireNonNull(id, "El ID del documento no puede ser nulo");
        this.ordenCambioId = ordenCambioId;
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo.trim();
        this.rutaArchivo = rutaArchivo.trim();
        this.fechaCarga = fechaCarga;
        this.usuarioId = usuarioId;
    }

    public static OrdenCambioDocumento crear(OrdenCambioId ordenCambioId, TipoDocumentoOrdenCambio tipo,
            String nombreArchivo, String rutaArchivo, UUID usuarioId) {
        return new OrdenCambioDocumento(UUID.randomUUID(), ordenCambioId, tipo, nombreArchivo, rutaArchivo,
                LocalDateTime.now(), usuarioId);
    }

    public static OrdenCambioDocumento reconstruir(UUID id, OrdenCambioId ordenCambioId, TipoDocumentoOrdenCambio tipo,
            String nombreArchivo, String rutaArchivo, LocalDateTime fechaCarga, UUID usuarioId) {
        return new OrdenCambioDocumento(id, ordenCambioId, tipo, nombreArchivo, rutaArchivo, fechaCarga, usuarioId);
    }

    private void validarInvariantes(OrdenCambioId ordenCambioId, TipoDocumentoOrdenCambio tipo, String nombreArchivo,
            String rutaArchivo, LocalDateTime fechaCarga, UUID usuarioId) {
        if (ordenCambioId == null)
            throw new IllegalArgumentException("El ordenCambioId no puede ser nulo");
        if (tipo == null)
            throw new IllegalArgumentException("El tipo de documento no puede ser nulo");
        if (nombreArchivo == null || nombreArchivo.isBlank())
            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío");
        if (rutaArchivo == null || rutaArchivo.isBlank())
            throw new IllegalArgumentException("La ruta del archivo no puede estar vacía");
        if (fechaCarga == null)
            throw new IllegalArgumentException("La fecha de carga no puede ser nula");
        if (usuarioId == null)
            throw new IllegalArgumentException("El usuarioId no puede ser nulo");
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public OrdenCambioId getOrdenCambioId() {
        return ordenCambioId;
    }

    public TipoDocumentoOrdenCambio getTipo() {
        return tipo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrdenCambioDocumento that = (OrdenCambioDocumento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
