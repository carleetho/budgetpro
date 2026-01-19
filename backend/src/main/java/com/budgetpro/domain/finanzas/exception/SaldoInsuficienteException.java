package com.budgetpro.domain.finanzas.exception;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando se intenta realizar un egreso
 * que resultaría en un saldo negativo.
 * 
 * Esta excepción protege la invariante crítica del agregado Billetera:
 * "El saldo nunca puede ser negativo".
 */
public class SaldoInsuficienteException extends RuntimeException {

    private final UUID proyectoId;
    private final BigDecimal saldoActual;
    private final BigDecimal montoIntentado;

    public SaldoInsuficienteException(UUID proyectoId, BigDecimal saldoActual, BigDecimal montoIntentado) {
        super(String.format(
            "Saldo insuficiente en billetera del proyecto %s. Saldo actual: %s, Monto intentado: %s",
            proyectoId, saldoActual, montoIntentado
        ));
        this.proyectoId = proyectoId;
        this.saldoActual = saldoActual;
        this.montoIntentado = montoIntentado;
    }

    public SaldoInsuficienteException(UUID proyectoId, BigDecimal saldoDisponible, BigDecimal montoRequerido, String detalleAdicional) {
        super(String.format(
            "Saldo insuficiente en proyecto %s. Disponible: %s, Requerido: %s. %s",
            proyectoId, saldoDisponible, montoRequerido, detalleAdicional
        ));
        this.proyectoId = proyectoId;
        this.saldoActual = saldoDisponible;
        this.montoIntentado = montoRequerido;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public BigDecimal getMontoIntentado() {
        return montoIntentado;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoActual;
    }

    public BigDecimal getMontoRequerido() {
        return montoIntentado;
    }
}
