package com.budgetpro.domain.finanzas.consumo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado CONSUMO_PARTIDA.
 * 
 * Representa el impacto económico real en una partida presupuestaria.
 */
public final class ConsumoPartida {

    private final ConsumoPartidaId id;
    private final UUID partidaId; // Relación N:1 con Partida
    private final UUID compraDetalleId; // Opcional: relación 1:1 con CompraDetalle (puede ser null)
    private final BigDecimal monto;
    private final LocalDate fecha;
    private final TipoConsumo tipo;
    private final Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private ConsumoPartida(ConsumoPartidaId id, UUID partidaId, UUID compraDetalleId, BigDecimal monto, LocalDate fecha,
            TipoConsumo tipo, Long version) {
        validarInvariantes(partidaId, monto, fecha, tipo);

        this.id = Objects.requireNonNull(id, "El ID del consumo no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "La partidaId no puede ser nula");
        this.compraDetalleId = compraDetalleId; // Opcional
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        // REGLA-034
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.tipo = Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        this.version = version != null ? version : 0L;
    }

    public static ConsumoPartida crearPorCompra(ConsumoPartidaId id, UUID partidaId, UUID compraDetalleId,
            BigDecimal monto, LocalDate fecha) {
        return new ConsumoPartida(id, partidaId, compraDetalleId, monto, fecha, TipoConsumo.COMPRA, 0L);
    }

    public static ConsumoPartida crearPorPlanilla(ConsumoPartidaId id, UUID partidaId, BigDecimal monto,
            LocalDate fecha) {
        return new ConsumoPartida(id, partidaId, null, monto, fecha, TipoConsumo.PLANILLA, 0L);
    }

    public static ConsumoPartida crearPorOtros(ConsumoPartidaId id, UUID partidaId, BigDecimal monto, LocalDate fecha) {
        return new ConsumoPartida(id, partidaId, null, monto, fecha, TipoConsumo.OTROS, 0L);
    }

    public static ConsumoPartida reconstruir(ConsumoPartidaId id, UUID partidaId, UUID compraDetalleId,
            BigDecimal monto, LocalDate fecha, TipoConsumo tipo, Long version) {
        return new ConsumoPartida(id, partidaId, compraDetalleId, monto, fecha, tipo, version);
    }

    private void validarInvariantes(UUID partidaId, BigDecimal monto, LocalDate fecha, TipoConsumo tipo) {
        if (partidaId == null) {
            throw new IllegalArgumentException("La partidaId no puede ser nula");
        }
        if (monto != null && monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo no puede ser nulo");
        }
    }

    public ConsumoPartida actualizarMonto(BigDecimal nuevoMonto) {
        BigDecimal m = nuevoMonto != null ? nuevoMonto : BigDecimal.ZERO;
        if (m.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        return new ConsumoPartida(this.id, this.partidaId, this.compraDetalleId, m, this.fecha, this.tipo,
                this.version);
    }

    public ConsumoPartida actualizarFecha(LocalDate nuevaFecha) {
        if (nuevaFecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        return new ConsumoPartida(this.id, this.partidaId, this.compraDetalleId, this.monto, nuevaFecha, this.tipo,
                this.version);
    }

    // Getters

    public ConsumoPartidaId getId() {
        return id;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public UUID getCompraDetalleId() {
        return compraDetalleId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TipoConsumo getTipo() {
        return tipo;
    }

    public Long getVersion() {
        return version;
    }

    public boolean esPorCompra() {
        return compraDetalleId != null && tipo == TipoConsumo.COMPRA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConsumoPartida that = (ConsumoPartida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ConsumoPartida{id=%s, partidaId=%s, monto=%s, fecha=%s, tipo=%s}", id, partidaId, monto,
                fecha, tipo);
    }
}
