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
    private final BigDecimal saldoDisponible;
    private final BigDecimal montoRequerido;

    public SaldoInsuficienteException(UUID proyectoId, BigDecimal saldoActual, BigDecimal montoIntentado) {
        super(String.format(
            "Saldo insuficiente en billetera del proyecto %s. Saldo actual: %s, Monto intentado: %s",
            proyectoId, saldoActual, montoIntentado
        ));
        this.proyectoId = proyectoId;
        this.saldoDisponible = saldoActual;
        this.montoRequerido = montoIntentado;
    }

    public SaldoInsuficienteException(UUID proyectoId, BigDecimal saldoDisponible, BigDecimal montoRequerido, String detalleAdicional) {
        super(String.format(
            "Saldo insuficiente en proyecto %s. Disponible: %s, Requerido: %s. %s",
            proyectoId, saldoDisponible, montoRequerido, detalleAdicional
        ));
        this.proyectoId = proyectoId;
        this.saldoDisponible = saldoDisponible;
        this.montoRequerido = montoRequerido;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public BigDecimal getSaldoActual() {
        return saldoDisponible;
    }

    public BigDecimal getMontoIntentado() {
        return montoRequerido;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public BigDecimal getMontoRequerido() {
        return montoRequerido;
    }
}
