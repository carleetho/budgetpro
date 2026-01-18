package com.budgetpro.application.proyecto.port.in;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;
import java.util.List;

/**
 * Puerto de Entrada (Inbound Port) para consultar proyectos.
 */
public interface ConsultarProyectosUseCase {
    
    /**
     * Obtiene todos los proyectos.
     * 
     * @return Lista de proyectos
     */
    List<ProyectoResponse> listar();
}
