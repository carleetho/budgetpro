package com.budgetpro.application.reajuste.port.in;

import com.budgetpro.application.reajuste.dto.EstimacionReajusteResponse;

import java.util.UUID;

/**
 * Caso de uso para calcular reajuste de costos.
 */
public interface CalcularReajusteUseCase {
    
    /**
     * Calcula el reajuste de costos para un presupuesto.
     * 
     * @param proyectoId ID del proyecto
     * @param presupuestoId ID del presupuesto
     * @param fechaCorte Fecha de corte para el cálculo
     * @param indiceBaseCodigo Código del índice base
     * @param indiceBaseFecha Fecha del índice base
     * @param indiceActualCodigo Código del índice actual
     * @param indiceActualFecha Fecha del índice actual
     * @return EstimacionReajusteResponse con el reajuste calculado
     */
    EstimacionReajusteResponse calcular(UUID proyectoId, UUID presupuestoId, java.time.LocalDate fechaCorte,
                                       String indiceBaseCodigo, java.time.LocalDate indiceBaseFecha,
                                       String indiceActualCodigo, java.time.LocalDate indiceActualFecha);
}
