package com.budgetpro.domain.finanzas.evm.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Puerto de salida para obtener los datos base necesarios para el cálculo de
 * EVM.
 * 
 * Permite desacoplar el módulo de EVM de los módulos de Presupuesto, Avance y
 * Costos.
 */
public interface EVMDataProvider {

    /**
     * Obtiene el Budget at Completion (BAC) - Presupuesto Total del proyecto.
     */
    BigDecimal getBudgetAtCompletion(UUID proyectoId);

    /**
     * Obtiene el Planned Value (PV) - Valor Planificado a la fecha de corte.
     */
    BigDecimal getPlannedValue(UUID proyectoId, LocalDateTime fechaCorte);

    /**
     * Obtiene el Earned Value (EV) - Valor Ganado a la fecha de corte.
     */
    BigDecimal getEarnedValue(UUID proyectoId, LocalDateTime fechaCorte);

    /**
     * Obtiene el Actual Cost (AC) - Costo Real a la fecha de corte.
     */
    BigDecimal getActualCost(UUID proyectoId, LocalDateTime fechaCorte);
}
