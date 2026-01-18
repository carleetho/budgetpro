package com.budgetpro.application.estimacion.port.in;

import com.budgetpro.application.estimacion.dto.EstimacionResponse;
import com.budgetpro.application.estimacion.dto.GenerarEstimacionCommand;

/**
 * Puerto de entrada (Inbound Port) para generar una estimación.
 */
public interface GenerarEstimacionUseCase {

    /**
     * Genera una nueva estimación de avance.
     * 
     * @param command Comando con los datos de la estimación
     * @return Respuesta con la estimación generada
     */
    EstimacionResponse generar(GenerarEstimacionCommand command);
}
