package com.budgetpro.application.proyecto.port.in;

import com.budgetpro.application.proyecto.dto.CrearProyectoCommand;
import com.budgetpro.application.proyecto.dto.ProyectoResponse;

/**
 * Puerto de entrada (Inbound Port) para crear un nuevo proyecto.
 */
public interface CrearProyectoUseCase {

    /**
     * Crea un nuevo proyecto.
     * 
     * @param command Comando con los datos del proyecto
     * @return Respuesta con el proyecto creado
     * @throws com.budgetpro.application.proyecto.exception.ProyectoDuplicadoException si ya existe un proyecto con el mismo nombre
     */
    ProyectoResponse crear(CrearProyectoCommand command);
}
