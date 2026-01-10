package com.budgetpro.domain.finanzas.partida.exception;

import com.budgetpro.domain.finanzas.model.Monto;
import com.budgetpro.domain.finanzas.partida.PartidaId;

import java.util.UUID;

/**
 * Excepción de dominio lanzada cuando una operación sobre una Partida
 * resultaría en exceder el presupuesto disponible.
 * 
 * Invariante protegida: Saldo Disponible = Presupuestado - (Reservado + Ejecutado)
 * nunca puede ser negativo.
 */
public class PresupuestoExcedidoException extends RuntimeException {

    private final PartidaId partidaId;
    private final UUID proyectoId;
    private final Monto montoPresupuestado;
    private final Monto montoReservado;
    private final Monto montoEjecutado;
    private final Monto montoDisponible;
    private final Monto montoIntentado;

    public PresupuestoExcedidoException(PartidaId partidaId,
                                       UUID proyectoId,
                                       Monto montoPresupuestado,
                                       Monto montoReservado,
                                       Monto montoEjecutado,
                                       Monto montoDisponible,
                                       Monto montoIntentado) {
        super(String.format(
            "Presupuesto excedido en partida %s del proyecto %s. " +
            "Presupuestado: %s, Reservado: %s, Ejecutado: %s, Disponible: %s, Monto intentado: %s",
            partidaId, proyectoId, montoPresupuestado, montoReservado, montoEjecutado,
            montoDisponible, montoIntentado
        ));
        this.partidaId = partidaId;
        this.proyectoId = proyectoId;
        this.montoPresupuestado = montoPresupuestado;
        this.montoReservado = montoReservado;
        this.montoEjecutado = montoEjecutado;
        this.montoDisponible = montoDisponible;
        this.montoIntentado = montoIntentado;
    }

    public PartidaId getPartidaId() {
        return partidaId;
    }

    public UUID getProyectoId() {
        return proyectoId;
    }

    public Monto getMontoPresupuestado() {
        return montoPresupuestado;
    }

    public Monto getMontoReservado() {
        return montoReservado;
    }

    public Monto getMontoEjecutado() {
        return montoEjecutado;
    }

    public Monto getMontoDisponible() {
        return montoDisponible;
    }

    public Monto getMontoIntentado() {
        return montoIntentado;
    }
}
