package com.budgetpro.application.partida.port.in;

import com.budgetpro.application.partida.dto.CrearPartidaCommand;
import com.budgetpro.application.partida.dto.PartidaResponse;
import jakarta.validation.Valid;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de crear una nueva partida.
 * 
 * Define el contrato del caso de uso sin depender de tecnologías específicas.
 */
public interface CrearPartidaUseCase {

    /**
     * Ejecuta el caso de uso para crear una nueva partida.
     * 
     * @param command El comando con los datos de la partida a crear
     * @return La respuesta con la partida creada
     * @throws com.budgetpro.infrastructure.persistence.exception.PartidaDuplicadaException si ya existe una partida con el mismo código en el presupuesto
     * @throws IllegalArgumentException si los datos del comando son inválidos
     */
    PartidaResponse ejecutar(@Valid CrearPartidaCommand command);
}
