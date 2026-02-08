package com.budgetpro.domain.finanzas.consumo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root del agregado CONSUMO_PARTIDA.
 * 
 * Representa el impacto económico real en una partida presupuestaria.
 * 
 * Relación: N:1 con Partida, 1:1 con CompraDetalle
 * 
 * Invariantes:
 * - La partidaId es obligatoria
 * - El monto no puede ser negativo
 * - La fecha no puede ser nula
 * - El tipo no puede ser nulo
 * - El compraDetalleId es opcional (puede ser null para consumos no relacionados a compras)
 * 
 * Contexto: Logística & Costos
 */
public final class ConsumoPartida {

    private final ConsumoPartidaId id;
    private final UUID partidaId; // Relación N:1 con Partida
    // nosemgrep
    private UUID compraDetalleId; // Opcional: relación 1:1 con CompraDetalle (puede ser null)
    // nosemgrep
    private BigDecimal monto;
    // nosemgrep
    private LocalDate fecha;
    // nosemgrep
    private TipoConsumo tipo;
    // Justificación: Optimistic locking JPA @Version
    // nosemgrep
    private Long version;

    /**
     * Constructor privado. Usar factory methods.
     */
    private ConsumoPartida(ConsumoPartidaId id, UUID partidaId, UUID compraDetalleId,
                          BigDecimal monto, LocalDate fecha, TipoConsumo tipo, Long version) {
        validarInvariantes(partidaId, monto, fecha, tipo);
        
        this.id = Objects.requireNonNull(id, "El ID del consumo no puede ser nulo");
        this.partidaId = Objects.requireNonNull(partidaId, "La partidaId no puede ser nula");
        this.compraDetalleId = compraDetalleId; // Opcional
        this.monto = monto != null ? monto : BigDecimal.ZERO;
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.tipo = Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        this.version = version != null ? version : 0L;
    }

    /**
     * Factory method para crear un nuevo ConsumoPartida relacionado a una compra.
     */
    public static ConsumoPartida crearPorCompra(ConsumoPartidaId id, UUID partidaId, UUID compraDetalleId,
                                                BigDecimal monto, LocalDate fecha) {
        return new ConsumoPartida(id, partidaId, compraDetalleId, monto, fecha, TipoConsumo.COMPRA, 0L);
    }

    /**
     * Factory method para crear un nuevo ConsumoPartida por planilla.
     */
    public static ConsumoPartida crearPorPlanilla(ConsumoPartidaId id, UUID partidaId, BigDecimal monto, LocalDate fecha) {
        return new ConsumoPartida(id, partidaId, null, monto, fecha, TipoConsumo.PLANILLA, 0L);
    }

    /**
     * Factory method para crear un nuevo ConsumoPartida por otros conceptos.
     */
    public static ConsumoPartida crearPorOtros(ConsumoPartidaId id, UUID partidaId, BigDecimal monto, LocalDate fecha) {
        return new ConsumoPartida(id, partidaId, null, monto, fecha, TipoConsumo.OTROS, 0L);
    }

    /**
     * Factory method para reconstruir un ConsumoPartida desde persistencia.
     */
    public static ConsumoPartida reconstruir(ConsumoPartidaId id, UUID partidaId, UUID compraDetalleId,
                                            BigDecimal monto, LocalDate fecha, TipoConsumo tipo, Long version) {
        return new ConsumoPartida(id, partidaId, compraDetalleId, monto, fecha, tipo, version);
    }

    /**
     * Valida las invariantes del agregado.
     */
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

    /**
     * Actualiza el monto del consumo.
     */
    public void actualizarMonto(BigDecimal nuevoMonto) {
        if (nuevoMonto == null) {
            this.monto = BigDecimal.ZERO;
        } else if (nuevoMonto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        } else {
            this.monto = nuevoMonto;
        }
    }

    /**
     * Actualiza la fecha del consumo.
     */
    public void actualizarFecha(LocalDate nuevaFecha) {
        if (nuevaFecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser nula");
        }
        this.fecha = nuevaFecha;
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

    /**
     * Verifica si el consumo está relacionado a una compra.
     */
    public boolean esPorCompra() {
        return compraDetalleId != null && tipo == TipoConsumo.COMPRA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsumoPartida that = (ConsumoPartida) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ConsumoPartida{id=%s, partidaId=%s, compraDetalleId=%s, monto=%s, fecha=%s, tipo=%s}", 
                           id, partidaId, compraDetalleId, monto, fecha, tipo);
    }
}
