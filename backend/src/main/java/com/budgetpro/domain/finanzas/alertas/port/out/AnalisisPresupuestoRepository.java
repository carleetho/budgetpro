package com.budgetpro.domain.finanzas.alertas.port.out;

import com.budgetpro.domain.finanzas.alertas.model.AnalisisPresupuesto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para persistencia de análisis de presupuestos.
 */
public interface AnalisisPresupuestoRepository {
    
    /**
     * Guarda un análisis de presupuesto.
     */
    void guardar(AnalisisPresupuesto analisis);
    
    /**
     * Busca un análisis por ID.
     */
    Optional<AnalisisPresupuesto> buscarPorId(UUID id);
    
    /**
     * Busca el último análisis de un presupuesto.
     */
    Optional<AnalisisPresupuesto> buscarUltimoPorPresupuestoId(UUID presupuestoId);
    
    /**
     * Busca todos los análisis de un presupuesto.
     */
    List<AnalisisPresupuesto> buscarTodosPorPresupuestoId(UUID presupuestoId);
}
