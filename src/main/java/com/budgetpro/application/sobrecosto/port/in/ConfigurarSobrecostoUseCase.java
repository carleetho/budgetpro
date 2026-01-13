package com.budgetpro.application.sobrecosto.port.in;

import com.budgetpro.application.sobrecosto.dto.AnalisisSobrecostoResponse;
import com.budgetpro.application.sobrecosto.dto.ConfigurarSobrecostoCommand;

/**
 * Puerto de entrada (Inbound Port) para configurar el análisis de sobrecosto de un presupuesto.
 */
public interface ConfigurarSobrecostoUseCase {

    /**
     * Configura o actualiza el análisis de sobrecosto de un presupuesto.
     * 
     * @param command Comando con los porcentajes de sobrecosto
     * @return Respuesta con el análisis configurado
     */
    AnalisisSobrecostoResponse configurar(ConfigurarSobrecostoCommand command);
}
