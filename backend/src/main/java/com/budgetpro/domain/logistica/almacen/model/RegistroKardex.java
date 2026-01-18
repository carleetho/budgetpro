package com.budgetpro.domain.logistica.almacen.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa un registro de Kárdex.
 * 
 * Registra un movimiento de entrada o salida y calcula los saldos resultantes.
 */
public final class RegistroKardex {
    
    private final UUID id;
    private final UUID almacenId;
    private final UUID recursoId;
    private final LocalDate fechaMovimiento;
    private final UUID movimientoId;
    private final TipoMovimientoAlmacen tipoMovimiento;
    private final BigDecimal cantidadEntrada;
    private final BigDecimal cantidadSalida;
    private final BigDecimal precioUnitario;
    private final BigDecimal saldoCantidad;
    private final BigDecimal saldoValor;
    private final BigDecimal costoPromedioPonderado;
    
    /**
     * Constructor privado. Usar factory methods.
     */
    private RegistroKardex(UUID id, UUID almacenId, UUID recursoId, LocalDate fechaMovimiento,
                          UUID movimientoId, TipoMovimientoAlmacen tipoMovimiento,
                          BigDecimal cantidadEntrada, BigDecimal cantidadSalida,
                          BigDecimal precioUnitario, BigDecimal saldoCantidad,
                          BigDecimal saldoValor, BigDecimal costoPromedioPonderado) {
        this.id = Objects.requireNonNull(id, "El ID del registro no puede ser nulo");
        this.almacenId = Objects.requireNonNull(almacenId, "El ID del almacén no puede ser nulo");
        this.recursoId = Objects.requireNonNull(recursoId, "El ID del recurso no puede ser nulo");
        this.fechaMovimiento = fechaMovimiento != null ? fechaMovimiento : LocalDate.now();
        this.movimientoId = Objects.requireNonNull(movimientoId, "El ID del movimiento no puede ser nulo");
        this.tipoMovimiento = Objects.requireNonNull(tipoMovimiento, "El tipo de movimiento no puede ser nulo");
        this.cantidadEntrada = cantidadEntrada != null ? cantidadEntrada : BigDecimal.ZERO;
        this.cantidadSalida = cantidadSalida != null ? cantidadSalida : BigDecimal.ZERO;
        this.precioUnitario = precioUnitario;
        this.saldoCantidad = Objects.requireNonNull(saldoCantidad, "El saldo de cantidad no puede ser nulo");
        this.saldoValor = Objects.requireNonNull(saldoValor, "El saldo de valor no puede ser nulo");
        this.costoPromedioPonderado = Objects.requireNonNull(costoPromedioPonderado, "El CPP no puede ser nulo");
    }
    
    /**
     * Factory method para crear un registro de entrada.
     */
    public static RegistroKardex crearEntrada(UUID almacenId, UUID recursoId, UUID movimientoId,
                                             BigDecimal cantidad, BigDecimal precioUnitario,
                                             BigDecimal importeTotal, BigDecimal saldoCantidad,
                                             BigDecimal saldoValor, BigDecimal costoPromedioPonderado) {
        return new RegistroKardex(
            UUID.randomUUID(),
            almacenId,
            recursoId,
            LocalDate.now(),
            movimientoId,
            TipoMovimientoAlmacen.ENTRADA,
            cantidad,
            BigDecimal.ZERO,
            precioUnitario,
            saldoCantidad,
            saldoValor,
            costoPromedioPonderado
        );
    }
    
    /**
     * Factory method para crear un registro de salida.
     */
    public static RegistroKardex crearSalida(UUID almacenId, UUID recursoId, UUID movimientoId,
                                            BigDecimal cantidad, BigDecimal valorSalida,
                                            BigDecimal saldoCantidad, BigDecimal saldoValor,
                                            BigDecimal costoPromedioPonderado) {
        return new RegistroKardex(
            UUID.randomUUID(),
            almacenId,
            recursoId,
            LocalDate.now(),
            movimientoId,
            TipoMovimientoAlmacen.SALIDA,
            BigDecimal.ZERO,
            cantidad,
            costoPromedioPonderado, // Precio unitario = CPP para salidas
            saldoCantidad,
            saldoValor,
            costoPromedioPonderado
        );
    }
    
    // Getters
    
    public UUID getId() { return id; }
    public UUID getAlmacenId() { return almacenId; }
    public UUID getRecursoId() { return recursoId; }
    public LocalDate getFechaMovimiento() { return fechaMovimiento; }
    public UUID getMovimientoId() { return movimientoId; }
    public TipoMovimientoAlmacen getTipoMovimiento() { return tipoMovimiento; }
    public BigDecimal getCantidadEntrada() { return cantidadEntrada; }
    public BigDecimal getCantidadSalida() { return cantidadSalida; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSaldoCantidad() { return saldoCantidad; }
    public BigDecimal getSaldoValor() { return saldoValor; }
    public BigDecimal getCostoPromedioPonderado() { return costoPromedioPonderado; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroKardex that = (RegistroKardex) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
