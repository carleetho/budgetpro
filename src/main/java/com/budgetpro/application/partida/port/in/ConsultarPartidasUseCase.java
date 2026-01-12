package com.budgetpro.application.partida.port.in;

import com.budgetpro.application.partida.dto.PartidaResponse;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de consultar partidas.
 * 
 * Define el contrato del caso de uso sin depender de tecnologías específicas.
 */
public interface ConsultarPartidasUseCase {

    /**
     * Consulta todas las partidas de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Lista de partidas del presupuesto (puede estar vacía)
     */
    List<PartidaResponse> consultarPorPresupuesto(UUID presupuestoId);

    /**
     * Consulta todas las partidas de un proyecto.
     * 
     * @param proyectoId El ID del proyecto
     * @return Lista de partidas del proyecto (puede estar vacía)
     */
    List<PartidaResponse> consultarPorProyecto(UUID proyectoId);
}
