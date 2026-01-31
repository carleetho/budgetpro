package com.budgetpro.application.estimacion.port.in;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para aprobar una estimación.
 */
public interface AprobarEstimacionUseCase {

    /**
     * Aprueba una estimación y registra el ingreso en la billetera.
     * 
     * @param estimacionId El ID de la estimación
     */
    void aprobar(UUID estimacionId);
}
