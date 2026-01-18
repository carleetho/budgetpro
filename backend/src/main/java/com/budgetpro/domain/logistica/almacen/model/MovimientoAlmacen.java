package com.budgetpro.domain.logistica.almacen.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado que representa un movimiento de entrada o salida de almacén.
 */
public final class MovimientoAlmacen {
    
    private final MovimientoAlmacenId id;
    private final AlmacenId almacenId;
    private final UUID recursoId;
    private final TipoMovimientoAlmacen tipoMovimiento;
    private final LocalDate fechaMovimiento;
    private final BigDecimal cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal importeTotal;
    private String numeroDocumento;
    private UUID partidaId;
    private UUID centroCostoId;
    private String observaciones;
    
    /**
     * Constructor privado. Usar factory methods.
     */
    private MovimientoAlmacen(MovimientoAlmacenId id, AlmacenId almacenId, UUID recursoId,
                             TipoMovimientoAlmacen tipoMovimiento, LocalDate fechaMovimiento,
                             BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importeTotal,
                             String numeroDocumento, UUID partidaId, UUID centroCostoId, String observaciones) {
        this.id = Objects.requireNonNull(id, "El ID del movimiento no puede ser nulo");
        this.almacenId = Objects.requireNonNull(almacenId, "El ID del almacén no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El ID del recurso no puede ser nulo");
        this.tipoMovimiento = Objects.requireNonNull(tipoMovimiento, "El tipo de movimiento no puede ser nulo");
        this.fechaMovimiento = fechaMovimiento != null ? fechaMovimiento : LocalDate.now();
        this.cantidad = Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");
        this.precioUnitario = Objects.requireNonNull(precioUnitario, "El precio unitario no puede ser nulo");
        this.importeTotal = Objects.requireNonNull(importeTotal, "El importe total no puede ser nulo");
        
        if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (precioUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio unitario debe ser mayor o igual a cero");
        }
        if (importeTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El importe total debe ser mayor o igual a cero");
        }
        
        this.numeroDocumento = numeroDocumento;
        this.partidaId = partidaId;
        this.centroCostoId = centroCostoId;
        this.observaciones = observaciones;
    }
    
    /**
     * Factory method para crear un movimiento de entrada.
     */
    public static MovimientoAlmacen crearEntrada(MovimientoAlmacenId id, AlmacenId almacenId, UUID recursoId,
                                                  LocalDate fechaMovimiento, BigDecimal cantidad,
                                                  BigDecimal precioUnitario, String numeroDocumento,
                                                  String observaciones) {
        BigDecimal importeTotal = cantidad.multiply(precioUnitario);
        return new MovimientoAlmacen(id, almacenId, recursoId, TipoMovimientoAlmacen.ENTRADA,
                                    fechaMovimiento, cantidad, precioUnitario, importeTotal,
                                    numeroDocumento, null, null, observaciones);
    }
    
    /**
     * Factory method para crear un movimiento de salida.
     */
    public static MovimientoAlmacen crearSalida(MovimientoAlmacenId id, AlmacenId almacenId, UUID recursoId,
                                                LocalDate fechaMovimiento, BigDecimal cantidad,
                                                BigDecimal precioUnitario, UUID partidaId,
                                                UUID centroCostoId, String numeroDocumento, String observaciones) {
        BigDecimal importeTotal = cantidad.multiply(precioUnitario);
        return new MovimientoAlmacen(id, almacenId, recursoId, TipoMovimientoAlmacen.SALIDA,
                                    fechaMovimiento, cantidad, precioUnitario, importeTotal,
                                    numeroDocumento, partidaId, centroCostoId, observaciones);
    }
    
    /**
     * Factory method para reconstruir desde persistencia.
     */
    public static MovimientoAlmacen reconstruir(MovimientoAlmacenId id, AlmacenId almacenId, UUID recursoId,
                                               TipoMovimientoAlmacen tipoMovimiento, LocalDate fechaMovimiento,
                                               BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importeTotal,
                                               String numeroDocumento, UUID partidaId, UUID centroCostoId,
                                               String observaciones) {
        return new MovimientoAlmacen(id, almacenId, recursoId, tipoMovimiento, fechaMovimiento,
                                    cantidad, precioUnitario, importeTotal, numeroDocumento,
                                    partidaId, centroCostoId, observaciones);
    }
    
    // Getters
    
    public MovimientoAlmacenId getId() { return id; }
    public AlmacenId getAlmacenId() { return almacenId; }
    public UUID getRecursoId() { return recursoId; }
    public TipoMovimientoAlmacen getTipoMovimiento() { return tipoMovimiento; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public BigDecimal getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getImporteTotal() { return importeTotal; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public UUID getPartidaId() { return partidaId; }
    public UUID getCentroCostoId() { return centroCostoId; }
    public String getObservaciones() { return observaciones; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimientoAlmacen that = (MovimientoAlmacen) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
