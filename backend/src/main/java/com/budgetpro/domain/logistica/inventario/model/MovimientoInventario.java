package com.budgetpro.domain.logistica.inventario.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna del agregado InventarioItem que representa un movimiento en el Kardex.
 * 
 * Cada entrada o salida queda registrada de forma inmutable para trazabilidad completa.
 */
public final class MovimientoInventario {

    private final MovimientoInventarioId id;
    private final UUID inventarioItemId; // ID del item de inventario asociado
    private final TipoMovimientoInventario tipo;
    private final BigDecimal cantidad; // Positiva para ENTRADA, negativa para SALIDA
    private final BigDecimal costoUnitario; // Costo unitario al momento del movimiento
    private final BigDecimal costoTotal; // cantidad * costoUnitario
    private final UUID compraDetalleId; // Opcional: para trazabilidad de compras
    private final UUID requisicionId; // Opcional: ID de la requisición (solo para SALIDA_CONSUMO)
    private final UUID requisicionItemId; // Opcional: ID del ítem de requisición (solo para SALIDA_CONSUMO)
    private final UUID partidaId; // Opcional: Partida presupuestal (imputación AC)
    private final UUID transferenciaId; // Opcional: ID para vincular transferencias (SALIDA_TRANSFERENCIA <-> ENTRADA_TRANSFERENCIA)
    private final UUID actividadId; // Opcional: ID de actividad (placeholder para validación temporal futura)
    private final String justificacion; // Opcional: Justificación detallada (obligatoria para AJUSTE, min 20 chars)
    private final String referencia; // Descripción o referencia del movimiento
    private final LocalDateTime fechaHora; // Fecha y hora exacta del movimiento

    /**
     * Constructor privado. Usar factory methods.
     */
    private MovimientoInventario(MovimientoInventarioId id, UUID inventarioItemId,
                                  TipoMovimientoInventario tipo, BigDecimal cantidad,
                                  BigDecimal costoUnitario, BigDecimal costoTotal,
                                  UUID compraDetalleId, UUID requisicionId, UUID requisicionItemId,
                                  UUID partidaId, UUID transferenciaId, UUID actividadId,
                                  String justificacion, String referencia, LocalDateTime fechaHora) {
        this.id = Objects.requireNonNull(id, "El ID del movimiento no puede ser nulo");
        this.inventarioItemId = Objects.requireNonNull(inventarioItemId, "El inventarioItemId no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de movimiento no puede ser nulo");
        this.cantidad = Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
        this.costoUnitario = Objects.requireNonNull(costoUnitario, "El costo unitario no puede ser nulo");
        this.costoTotal = Objects.requireNonNull(costoTotal, "El costo total no puede ser nulo");
        this.compraDetalleId = compraDetalleId; // Opcional
        this.requisicionId = requisicionId; // Opcional
        this.requisicionItemId = requisicionItemId; // Opcional
        this.partidaId = partidaId; // Opcional
        this.transferenciaId = transferenciaId; // Opcional
        this.actividadId = actividadId; // Opcional
        this.justificacion = justificacion != null ? justificacion.trim() : null;
        this.referencia = Objects.requireNonNull(referencia, "La referencia no puede ser nula");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora no puede ser nula");
        
        // Validaciones de negocio
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        if (costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo unitario no puede ser negativo");
        }
        if (referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede estar vacía");
        }
        
        // Validación: justificacion obligatoria y >= 20 chars cuando tipo = AJUSTE
        if (tipo == TipoMovimientoInventario.AJUSTE) {
            if (justificacion == null || justificacion.isBlank()) {
                throw new IllegalArgumentException("La justificación es obligatoria para movimientos de tipo AJUSTE");
            }
            if (justificacion.trim().length() < 20) {
                throw new IllegalArgumentException("La justificación debe tener al menos 20 caracteres para movimientos de tipo AJUSTE");
            }
        }
    }

    /**
     * Factory method para crear un movimiento de entrada por compra.
     */
    public static MovimientoInventario crearEntradaPorCompra(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, UUID compraDetalleId, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.ENTRADA_COMPRA,
            cantidad, costoUnitario, costoTotal, compraDetalleId, null, null,
            null, null, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un movimiento de salida por consumo.
     */
    public static MovimientoInventario crearSalidaPorConsumo(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.SALIDA_CONSUMO,
            cantidad, costoUnitario, costoTotal, null, null, null,
            null, null, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un movimiento de salida por consumo con referencia a requisición.
     */
    public static MovimientoInventario crearSalidaConRequisicion(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, UUID requisicionId, UUID requisicionItemId,
            UUID partidaId, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.SALIDA_CONSUMO,
            cantidad, costoUnitario, costoTotal, null, requisicionId, requisicionItemId,
            partidaId, null, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un ajuste de inventario.
     * 
     * @param justificacion Justificación detallada (obligatoria, mínimo 20 caracteres)
     */
    public static MovimientoInventario crearAjuste(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, String justificacion, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.AJUSTE,
            cantidad, costoUnitario, costoTotal, null, null, null,
            null, null, null, justificacion, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un movimiento de salida por transferencia.
     */
    public static MovimientoInventario crearSalidaTransferencia(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, UUID transferenciaId, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.SALIDA_TRANSFERENCIA,
            cantidad, costoUnitario, costoTotal, null, null, null,
            null, transferenciaId, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un movimiento de entrada por transferencia.
     */
    public static MovimientoInventario crearEntradaTransferencia(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, UUID transferenciaId, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.ENTRADA_TRANSFERENCIA,
            cantidad, costoUnitario, costoTotal, null, null, null,
            null, transferenciaId, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un movimiento de salida por préstamo.
     */
    public static MovimientoInventario crearSalidaPrestamo(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.SALIDA_PRESTAMO,
            cantidad, costoUnitario, costoTotal, null, null, null,
            null, null, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para crear un movimiento de entrada por devolución de préstamo.
     */
    public static MovimientoInventario crearEntradaPrestamo(
            MovimientoInventarioId id, UUID inventarioItemId, BigDecimal cantidad,
            BigDecimal costoUnitario, String referencia) {
        BigDecimal costoTotal = cantidad.multiply(costoUnitario);
        return new MovimientoInventario(
            id, inventarioItemId, TipoMovimientoInventario.ENTRADA_PRESTAMO,
            cantidad, costoUnitario, costoTotal, null, null, null,
            null, null, null, null, referencia, LocalDateTime.now()
        );
    }

    /**
     * Factory method para reconstruir un movimiento desde persistencia.
     */
    public static MovimientoInventario reconstruir(
            MovimientoInventarioId id, UUID inventarioItemId, TipoMovimientoInventario tipo,
            BigDecimal cantidad, BigDecimal costoUnitario, BigDecimal costoTotal,
            UUID compraDetalleId, UUID requisicionId, UUID requisicionItemId,
            UUID partidaId, UUID transferenciaId, UUID actividadId,
            String justificacion, String referencia, LocalDateTime fechaHora) {
        return new MovimientoInventario(
            id, inventarioItemId, tipo, cantidad, costoUnitario, costoTotal,
            compraDetalleId, requisicionId, requisicionItemId, partidaId,
            transferenciaId, actividadId, justificacion, referencia, fechaHora
        );
    }

    // Getters

    public MovimientoInventarioId getId() {
        return id;
    }

    public UUID getInventarioItemId() {
        return inventarioItemId;
    }

    public TipoMovimientoInventario getTipo() {
        return tipo;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public UUID getCompraDetalleId() {
        return compraDetalleId;
    }

    public UUID getRequisicionId() {
        return requisicionId;
    }

    public UUID getRequisicionItemId() {
        return requisicionItemId;
    }

    public UUID getPartidaId() {
        return partidaId;
    }

    public UUID getTransferenciaId() {
        return transferenciaId;
    }

    public UUID getActividadId() {
        return actividadId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public String getReferencia() {
        return referencia;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public boolean esEntrada() {
        return tipo == TipoMovimientoInventario.ENTRADA_COMPRA ||
               tipo == TipoMovimientoInventario.ENTRADA_TRANSFERENCIA ||
               tipo == TipoMovimientoInventario.ENTRADA_PRESTAMO ||
               tipo == TipoMovimientoInventario.AJUSTE;
    }

    public boolean esSalida() {
        return tipo == TipoMovimientoInventario.SALIDA_CONSUMO ||
               tipo == TipoMovimientoInventario.SALIDA_TRANSFERENCIA ||
               tipo == TipoMovimientoInventario.SALIDA_PRESTAMO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimientoInventario that = (MovimientoInventario) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("MovimientoInventario{id=%s, tipo=%s, cantidad=%s, fechaHora=%s}",
                           id, tipo, cantidad, fechaHora);
    }
}
