package com.budgetpro.application.sobrecosto.port.in;

import com.budgetpro.application.sobrecosto.dto.ConfiguracionLaboralResponse;
import com.budgetpro.application.sobrecosto.dto.ConfigurarLaboralCommand;

/**
 * Puerto de entrada (Inbound Port) para configurar los parámetros laborales (FSR).
 */
public interface ConfigurarLaboralUseCase {

    /**
     * Configura o actualiza la configuración laboral (global o por proyecto).
     * 
     * @param command Comando con los parámetros laborales
     * @return Respuesta con la configuración y el FSR calculado
     */
    ConfiguracionLaboralResponse configurar(ConfigurarLaboralCommand command);
}
