package com.budgetpro.application.presupuesto.port.in;

import com.budgetpro.application.presupuesto.dto.CrearPresupuestoCommand;
import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;

/**
 * Puerto de entrada (Inbound Port) para crear un nuevo presupuesto.
 */
public interface CrearPresupuestoUseCase {

    /**
     * Crea un nuevo presupuesto para un proyecto.
     * 
     * @param command Comando con los datos del presupuesto
     * @return Respuesta con el presupuesto creado
     * @throws com.budgetpro.application.presupuesto.exception.PresupuestoYaExisteException si el proyecto ya tiene un presupuesto
     * @throws com.budgetpro.application.presupuesto.exception.ProyectoNoEncontradoException si el proyecto no existe
     */
    PresupuestoResponse crear(CrearPresupuestoCommand command);
}
