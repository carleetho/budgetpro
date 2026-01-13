package com.budgetpro.application.partida.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando se intenta crear una partida con un padre que pertenece a otro presupuesto.
 */
public class PartidaPadreDiferentePresupuestoException extends RuntimeException {

    public PartidaPadreDiferentePresupuestoException(UUID padreId, UUID presupuestoIdEsperado, UUID presupuestoIdActual) {
        super(String.format(
            "La partida padre con ID '%s' pertenece al presupuesto '%s', pero se intenta crear la partida en el presupuesto '%s'",
            padreId, presupuestoIdActual, presupuestoIdEsperado
        ));
    }
}
