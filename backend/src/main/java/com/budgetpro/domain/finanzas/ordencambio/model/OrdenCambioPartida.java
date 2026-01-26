package com.budgetpro.domain.finanzas.ordencambio.model;

import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa una partida afectada o nueva en una Orden de Cambio.
 * 
 * Invariantes: - El ordenCambioId no puede ser nulo - El item no puede estar
 * vacío - La descripción no puede estar vacía - El metrado debe ser positivo
 * (puede ser 0) - El precio unitario debe ser positivo (puede ser 0) - El
 * subtotal se calcula automáticamente (metrado * precioUnitario)
 */
public class OrdenCambioPartida {

    private final UUID id;
    private final OrdenCambioId ordenCambioId;
    private final PartidaId partidaId; // Nullable (si es una partida nueva que no existe en presupuesto)
    private final String item;
    private final String descripcion;
    private final String unidad;
    private final BigDecimal metrado;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;
    private final Long apuSnapshotId; // Referencia al análisis de precios unitarios congelado

    private OrdenCambioPartida(UUID id, OrdenCambioId ordenCambioId, PartidaId partidaId, String item,
            String descripcion, String unidad, BigDecimal metrado, BigDecimal precioUnitario, Long apuSnapshotId) {

        validarInvariantes(ordenCambioId, item, descripcion, metrado, precioUnitario);

        this.id = Objects.requireNonNull(id, "El ID de la partida no puede ser nulo");
        this.ordenCambioId = ordenCambioId;
        this.partidaId = partidaId;
        this.item = item.trim();
        this.descripcion = descripcion.trim();
        this.unidad = unidad != null ? unidad.trim() : "";
        this.metrado = metrado;
        this.precioUnitario = precioUnitario;
        this.subtotal = metrado.multiply(precioUnitario);
        this.apuSnapshotId = apuSnapshotId;
    }

    public static OrdenCambioPartida crear(OrdenCambioId ordenCambioId, PartidaId partidaId, String item,
            String descripcion, String unidad, BigDecimal metrado, BigDecimal precioUnitario, Long apuSnapshotId) {
        return new OrdenCambioPartida(UUID.randomUUID(), ordenCambioId, partidaId, item, descripcion, unidad, metrado,
                precioUnitario, apuSnapshotId);
    }

    public static OrdenCambioPartida reconstruir(UUID id, OrdenCambioId ordenCambioId, PartidaId partidaId, String item,
            String descripcion, String unidad, BigDecimal metrado, BigDecimal precioUnitario, Long apuSnapshotId) {
        return new OrdenCambioPartida(id, ordenCambioId, partidaId, item, descripcion, unidad, metrado, precioUnitario,
                apuSnapshotId);
    }

    private void validarInvariantes(OrdenCambioId ordenCambioId, String item, String descripcion, BigDecimal metrado,
            BigDecimal precioUnitario) {
        if (ordenCambioId == null)
            throw new IllegalArgumentException("El ordenCambioId no puede ser nulo");
        if (item == null || item.isBlank())
            throw new IllegalArgumentException("El item no puede estar vacío");
        if (descripcion == null || descripcion.isBlank())
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        if (metrado == null || metrado.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El metrado no puede ser negativo");
        if (precioUnitario == null || precioUnitario.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El precio unitario no puede ser negativo");
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public OrdenCambioId getOrdenCambioId() {
        return ordenCambioId;
    }

    public PartidaId getPartidaId() {
        return partidaId;
    }

    public String getItem() {
        return item;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public BigDecimal getMetrado() {
        return metrado;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public Long getApuSnapshotId() {
        return apuSnapshotId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OrdenCambioPartida that = (OrdenCambioPartida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
