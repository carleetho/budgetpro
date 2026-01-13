package com.budgetpro.application.avance.port.in;

import com.budgetpro.application.avance.dto.AvanceFisicoResponse;
import com.budgetpro.application.avance.dto.RegistrarAvanceCommand;

/**
 * Puerto de entrada (Inbound Port) para registrar un avance físico.
 */
public interface RegistrarAvanceUseCase {

    /**
     * Registra un avance físico para una partida.
     * 
     * @param command Comando con los datos del avance
     * @return Respuesta con el avance registrado y el porcentaje de avance actualizado
     */
    AvanceFisicoResponse registrar(RegistrarAvanceCommand command);
}
