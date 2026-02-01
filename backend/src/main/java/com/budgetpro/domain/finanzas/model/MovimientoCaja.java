package com.budgetpro.domain.finanzas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un movimiento de caja dentro del agregado Billetera.
 * 
 * Invariantes: - El monto debe ser positivo (estricto > 0) - La referencia no
 * puede ser nula ni vacía - El billeteraId no puede ser nulo - El tipo no puede
 * ser nulo
 * 
 * El tipo de movimiento define si el monto suma o resta al saldo.
 */
public final class MovimientoCaja {

    private final UUID id;
    private final BilleteraId billeteraId;
    private final BigDecimal monto;
    private final String moneda;
    private final TipoMovimiento tipo;
    private final LocalDateTime fecha;
    private final String referencia;
    private final String evidenciaUrl;
    private final EstadoMovimientoCaja estado;

    /**
     * Constructor privado. Usar factory methods.
     */
    private MovimientoCaja(UUID id, BilleteraId billeteraId, BigDecimal monto, String moneda, TipoMovimiento tipo,
            LocalDateTime fecha, String referencia, String evidenciaUrl, EstadoMovimientoCaja estado) {
        validarInvariantes(billeteraId, monto, tipo, referencia, moneda);

        this.id = Objects.requireNonNull(id, "El ID del movimiento no puede ser nulo");
        this.billeteraId = billeteraId;
        this.monto = monto;
        this.moneda = moneda;
        this.tipo = tipo;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.referencia = referencia;
        this.evidenciaUrl = evidenciaUrl;
        this.estado = estado;
    }

    /**
     * Factory method para crear un movimiento de INGRESO.
     */
    public static MovimientoCaja crearIngreso(BilleteraId billeteraId, BigDecimal monto, String moneda,
            String referencia, String evidenciaUrl) {
        return new MovimientoCaja(UUID.randomUUID(), billeteraId, monto, moneda, TipoMovimiento.INGRESO,
                LocalDateTime.now(), referencia, evidenciaUrl, null);
    }

    /**
     * Factory method para crear un movimiento de INGRESO (Legacy - Default PEN).
     */
    public static MovimientoCaja crearIngreso(BilleteraId billeteraId, BigDecimal monto, String referencia,
            String evidenciaUrl) {
        return crearIngreso(billeteraId, monto, "PEN", referencia, evidenciaUrl);
    }

    /**
     * Factory method para crear un movimiento de EGRESO.
     */
    public static MovimientoCaja crearEgreso(BilleteraId billeteraId, BigDecimal monto, String moneda,
            String referencia, String evidenciaUrl) {
        EstadoMovimientoCaja estado = (evidenciaUrl == null || evidenciaUrl.isBlank())
                ? EstadoMovimientoCaja.PENDIENTE_DE_EVIDENCIA
                : null;
        return new MovimientoCaja(UUID.randomUUID(), billeteraId, monto, moneda, TipoMovimiento.EGRESO,
                LocalDateTime.now(), referencia, evidenciaUrl, estado);
    }

    /**
     * Factory method para crear un movimiento de EGRESO (Legacy - Default PEN).
     */
    public static MovimientoCaja crearEgreso(BilleteraId billeteraId, BigDecimal monto, String referencia,
            String evidenciaUrl) {
        return crearEgreso(billeteraId, monto, "PEN", referencia, evidenciaUrl);
    }

    /**
     * Factory method para reconstruir un movimiento desde persistencia.
     */
    public static MovimientoCaja reconstruir(UUID id, BilleteraId billeteraId, BigDecimal monto, String moneda,
            TipoMovimiento tipo, LocalDateTime fecha, String referencia, String evidenciaUrl,
            EstadoMovimientoCaja estado) {
        return new MovimientoCaja(id, billeteraId, monto, moneda, tipo, fecha, referencia, evidenciaUrl, estado);
    }

    private static void validarInvariantes(BilleteraId billeteraId, BigDecimal monto, TipoMovimiento tipo,
            String referencia, String moneda) {
        Objects.requireNonNull(billeteraId, "El billeteraId no puede ser nulo");
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        Objects.requireNonNull(tipo, "El tipo de movimiento no puede ser nulo");

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser positivo (mayor que cero)");
        }

        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede ser nula ni vacía");
        }

        if (moneda == null) {
            throw new IllegalArgumentException("La moneda no puede ser nula");
        }

        if (moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }

        if (moneda.length() != 3) {
            throw new IllegalArgumentException("La moneda debe tener 3 caracteres (ISO-4217)");
        }
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getMoneda() {
        return moneda;
    }

    public BilleteraId getBilleteraId() {
        return billeteraId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getEvidenciaUrl() {
        return evidenciaUrl;
    }

    public EstadoMovimientoCaja getEstado() {
        return estado;
    }

    public boolean isPendienteEvidencia() {
        return estado == EstadoMovimientoCaja.PENDIENTE_DE_EVIDENCIA;
    }

    /**
     * Retorna el monto como valor absoluto para cálculos. El tipo ya define la
     * dirección (suma o resta).
     */
    public BigDecimal getMontoAbsoluto() {
        return monto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MovimientoCaja that = (MovimientoCaja) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("MovimientoCaja{id=%s, tipo=%s, monto=%s, moneda='%s', referencia='%s'}", id, tipo, monto,
                moneda, referencia);
    }
}
