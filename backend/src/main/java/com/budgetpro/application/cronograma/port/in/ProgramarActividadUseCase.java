package com.budgetpro.application.cronograma.port.in;

import com.budgetpro.application.cronograma.dto.ActividadProgramadaResponse;
import com.budgetpro.application.cronograma.dto.ProgramarActividadCommand;

/**
 * Puerto de entrada (Inbound Port) para programar o actualizar una actividad.
 */
public interface ProgramarActividadUseCase {

    /**
     * Programa o actualiza una actividad en el cronograma.
     * 
     * @param command Comando con los datos de la actividad
     * @return Respuesta con la actividad programada
     */
    ActividadProgramadaResponse programar(ProgramarActividadCommand command);
}
