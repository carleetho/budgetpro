package com.budgetpro.application.presupuesto.port.in;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para aprobar un presupuesto.
 */
public interface AprobarPresupuestoUseCase {

    /**
     * Aprueba un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto a aprobar
     * @throws com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException si el presupuesto no existe
     * @throws com.budgetpro.application.presupuesto.exception.PresupuestoNoPuedeAprobarseException si el presupuesto no puede aprobarse (falta APU en partidas hoja, ya está aprobado, etc.)
     */
    void aprobar(UUID presupuestoId);
}
