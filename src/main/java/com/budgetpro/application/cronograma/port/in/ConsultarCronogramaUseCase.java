package com.budgetpro.application.cronograma.port.in;

import com.budgetpro.application.cronograma.dto.CronogramaResponse;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para consultar el cronograma de un proyecto.
 */
public interface ConsultarCronogramaUseCase {

    /**
     * Consulta el cronograma completo de un proyecto (Gantt de datos).
     * 
     * @param proyectoId El ID del proyecto
     * @return Respuesta con el cronograma completo
     */
    CronogramaResponse consultar(UUID proyectoId);
}
