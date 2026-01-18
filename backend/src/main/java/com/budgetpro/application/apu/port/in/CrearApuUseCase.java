package com.budgetpro.application.apu.port.in;

import com.budgetpro.application.apu.dto.CrearApuCommand;
import com.budgetpro.application.apu.dto.ApuResponse;

/**
 * Puerto de entrada (Inbound Port) para crear un nuevo APU.
 */
public interface CrearApuUseCase {

    /**
     * Crea un nuevo APU para una partida.
     * 
     * @param command Comando con los datos del APU
     * @return Respuesta con el APU creado
     * @throws com.budgetpro.application.apu.exception.PartidaNoEncontradaException si la partida no existe
     * @throws com.budgetpro.application.apu.exception.RecursoNoEncontradoException si algún recurso no existe
     * @throws com.budgetpro.application.apu.exception.ApuYaExisteException si la partida ya tiene un APU
     */
    ApuResponse crear(CrearApuCommand command);
}
