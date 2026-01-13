package com.budgetpro.application.inventario.port.in;

import com.budgetpro.application.inventario.dto.InventarioItemResponse;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para consultar inventario.
 */
public interface ConsultarInventarioUseCase {

    /**
     * Consulta todos los items de inventario de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de items de inventario del proyecto
     */
    List<InventarioItemResponse> consultarPorProyecto(UUID proyectoId);
}
