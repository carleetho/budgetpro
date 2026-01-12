package com.budgetpro.application.proyecto.port.in;

import com.budgetpro.application.proyecto.dto.ProyectoResponse;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Entrada (Inbound Port) para consultar proyectos.
 * 
 * Define el contrato del caso de uso de lectura sin depender de tecnologías específicas.
 * 
 * REGLA: Este es un puerto puro de la capa de aplicación. NO contiene anotaciones Spring.
 */
public interface ConsultarProyectosUseCase {

    /**
     * Consulta todos los proyectos.
     * 
     * @return Lista de proyectos
     */
    List<ProyectoResponse> consultarTodos();

    /**
     * Consulta proyectos por estado.
     * 
     * @param estado El estado del proyecto (opcional, puede ser null para todos)
     * @return Lista de proyectos con el estado especificado
     */
    List<ProyectoResponse> consultarPorEstado(String estado);
}
