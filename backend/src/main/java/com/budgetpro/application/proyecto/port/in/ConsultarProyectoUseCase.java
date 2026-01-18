package com.budgetpro.application.proyecto.port.in;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import com.budgetpro.domain.proyecto.model.ProyectoId;

import java.util.Optional;

/**
 * Puerto de Entrada (Inbound Port) para consultar un proyecto individual.
 */
public interface ConsultarProyectoUseCase {
    
    /**
     * Obtiene un proyecto por su ID.
     * 
     * @param id El ID del proyecto
     * @return Optional con el proyecto si existe, vacío en caso contrario
     */
    Optional<ProyectoResponse> obtenerPorId(ProyectoId id);
}
