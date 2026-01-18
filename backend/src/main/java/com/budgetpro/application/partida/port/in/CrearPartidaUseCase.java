package com.budgetpro.application.partida.port.in;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.budgetpro.application.partida.dto.PartidaResponse;

/**
 * Puerto de entrada (Inbound Port) para crear una nueva partida.
 */
public interface CrearPartidaUseCase {

    /**
     * Crea una nueva partida.
     * 
     * @param command Comando con los datos de la partida
     * @return Respuesta con la partida creada
     * @throws com.budgetpro.application.partida.exception.PresupuestoNoEncontradoException si el presupuesto no existe
     * @throws com.budgetpro.application.partida.exception.PartidaPadreNoEncontradaException si se especifica padreId pero no existe
     * @throws com.budgetpro.application.partida.exception.PartidaPadreDiferentePresupuestoException si el padre pertenece a otro presupuesto
     */
    PartidaResponse crear(CrearPartidaCommand command);
}
