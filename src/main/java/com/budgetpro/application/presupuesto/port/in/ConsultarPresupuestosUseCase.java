package com.budgetpro.application.presupuesto.port.in;

import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de Entrada (Inbound Port) para consultar presupuestos.
 * 
 * Define el contrato del caso de uso de lectura sin depender de tecnologías específicas.
 * 
 * REGLA: Este es un puerto puro de la capa de aplicación. NO contiene anotaciones Spring.
 */
public interface ConsultarPresupuestosUseCase {

    /**
     * Consulta todos los presupuestos de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de presupuestos del proyecto
     */
    List<PresupuestoResponse> consultarPorProyecto(UUID proyectoId);
}
