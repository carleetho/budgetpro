package com.budgetpro.domain.finanzas.anticipo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio para movimientos de anticipo (registro y amortización).
 */
public final class AnticipoMovimiento {
    private final UUID id;
    private final UUID proyectoId;
    private final BigDecimal monto;
    private final TipoMovimientoAnticipo tipo;
    private final LocalDateTime fecha;
    private final String referencia;

    private AnticipoMovimiento(UUID id, UUID proyectoId, BigDecimal monto, TipoMovimientoAnticipo tipo,
                               LocalDateTime fecha, String referencia) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo");
        }
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.proyectoId = Objects.requireNonNull(proyectoId, "El proyectoId no puede ser nulo");
        this.monto = monto;
        this.tipo = Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.referencia = Objects.requireNonNull(referencia, "La referencia no puede ser nula");
    }

    public static AnticipoMovimiento registrar(UUID proyectoId, BigDecimal monto, String referencia) {
        return new AnticipoMovimiento(UUID.randomUUID(), proyectoId, monto, TipoMovimientoAnticipo.REGISTRO,
                LocalDateTime.now(), referencia);
    }

    public static AnticipoMovimiento amortizar(UUID proyectoId, BigDecimal monto, String referencia) {
        return new AnticipoMovimiento(UUID.randomUUID(), proyectoId, monto, TipoMovimientoAnticipo.AMORTIZACION,
                LocalDateTime.now(), referencia);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public TipoMovimientoAnticipo getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }
}
