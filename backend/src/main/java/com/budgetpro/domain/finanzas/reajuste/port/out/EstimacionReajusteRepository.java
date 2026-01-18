package com.budgetpro.domain.finanzas.reajuste.port.out;

import com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajuste;
import com.budgetpro.domain.finanzas.reajuste.model.EstimacionReajusteId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de estimaciones de reajuste.
 */
public interface EstimacionReajusteRepository {
    
    /**
     * Guarda una estimación de reajuste.
     */
    void guardar(EstimacionReajuste estimacion);
    
    /**
     * Busca una estimación por ID.
     */
    Optional<EstimacionReajuste> buscarPorId(EstimacionReajusteId id);
    
    /**
     * Busca todas las estimaciones de un proyecto.
     */
    List<EstimacionReajuste> buscarPorProyectoId(UUID proyectoId);
    
    /**
     * Busca todas las estimaciones de un presupuesto.
     */
    List<EstimacionReajuste> buscarPorPresupuestoId(UUID presupuestoId);
    
    /**
     * Obtiene el siguiente número de estimación para un proyecto.
     */
    Integer obtenerSiguienteNumeroEstimacion(UUID proyectoId);
}
