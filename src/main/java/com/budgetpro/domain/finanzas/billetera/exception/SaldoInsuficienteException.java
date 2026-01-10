package com.budgetpro.domain.finanzas.billetera.exception;

import com.budgetpro.domain.finanzas.billetera.Monto;

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
    private final Monto saldoActual;
    private final Monto montoIntentado;

    public SaldoInsuficienteException(UUID proyectoId, Monto saldoActual, Monto montoIntentado) {
        super(String.format(
            "Saldo insuficiente en billetera del proyecto %s. Saldo actual: %s, Monto intentado: %s",
            proyectoId, saldoActual, montoIntentado
        ));
        this.proyectoId = proyectoId;
        this.saldoActual = saldoActual;
        this.montoIntentado = montoIntentado;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public Monto getSaldoActual() {
        return saldoActual;
    }

    public Monto getMontoIntentado() {
        return montoIntentado;
    }
}
