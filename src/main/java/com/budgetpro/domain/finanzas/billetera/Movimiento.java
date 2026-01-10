package com.budgetpro.domain.finanzas.billetera;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad que representa un movimiento de fondos dentro del agregado Billetera.
 * 
 * Invariantes:
 * - El monto debe ser positivo (estricto > 0)
 * - La referencia no puede ser nula ni vacía
 * - El billeteraId no puede ser nulo
 * - El tipo no puede ser nulo
 * 
 * El tipo de movimiento define si el monto suma o resta al saldo.
 */
public final class Movimiento {

    private final UUID id;
    private final BilleteraId billeteraId;
    private final Monto monto;
    private final TipoMovimiento tipo;
    private final LocalDateTime fecha;
    private final String referencia;
    private final String evidenciaUrl;

    /**
     * Constructor privado. Usar factory methods.
     */
    private Movimiento(UUID id, BilleteraId billeteraId, Monto monto, 
                      TipoMovimiento tipo, LocalDateTime fecha, 
                      String referencia, String evidenciaUrl) {
        validarInvariantes(billeteraId, monto, tipo, referencia);
        
        this.id = Objects.requireNonNull(id, "El ID del movimiento no puede ser nulo");
        this.billeteraId = billeteraId;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.referencia = referencia;
        this.evidenciaUrl = evidenciaUrl;
    }

    /**
     * Factory method para crear un movimiento de INGRESO.
     */
    public static Movimiento crearIngreso(BilleteraId billeteraId, Monto monto, 
                                         String referencia, String evidenciaUrl) {
        return new Movimiento(
            UUID.randomUUID(),
            billeteraId,
            monto,
            TipoMovimiento.INGRESO,
            LocalDateTime.now(),
            referencia,
            evidenciaUrl
        );
    }

    /**
     * Factory method para crear un movimiento de EGRESO.
     */
    public static Movimiento crearEgreso(BilleteraId billeteraId, Monto monto, 
                                        String referencia, String evidenciaUrl) {
        return new Movimiento(
            UUID.randomUUID(),
            billeteraId,
            monto,
            TipoMovimiento.EGRESO,
            LocalDateTime.now(),
            referencia,
            evidenciaUrl
        );
    }

    /**
     * Factory method para reconstruir un movimiento desde persistencia.
     */
    public static Movimiento reconstruir(UUID id, BilleteraId billeteraId, Monto monto,
                                        TipoMovimiento tipo, LocalDateTime fecha,
                                        String referencia, String evidenciaUrl) {
        return new Movimiento(id, billeteraId, monto, tipo, fecha, referencia, evidenciaUrl);
    }

    private static void validarInvariantes(BilleteraId billeteraId, Monto monto, 
                                          TipoMovimiento tipo, String referencia) {
        Objects.requireNonNull(billeteraId, "El billeteraId no puede ser nulo");
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        Objects.requireNonNull(tipo, "El tipo de movimiento no puede ser nulo");
        
        if (monto.esNegativo() || monto.esCero()) {
            throw new IllegalArgumentException("El monto debe ser positivo (mayor que cero)");
        }
        
        if (referencia == null || referencia.isBlank()) {
            throw new IllegalArgumentException("La referencia no puede ser nula ni vacía");
        }
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public BilleteraId getBilleteraId() {
        return billeteraId;
    }

    public Monto getMonto() {
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

    /**
     * Verifica si es un movimiento de ingreso.
     */
    public boolean esIngreso() {
        return tipo == TipoMovimiento.INGRESO;
    }

    /**
     * Verifica si es un movimiento de egreso.
     */
    public boolean esEgreso() {
        return tipo == TipoMovimiento.EGRESO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movimiento that = (Movimiento) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Movimiento{id=%s, tipo=%s, monto=%s, referencia='%s', fecha=%s}", 
                           id, tipo, monto, referencia, fecha);
    }
}
