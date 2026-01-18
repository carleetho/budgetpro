package com.budgetpro.application.control.port.in;

import com.budgetpro.application.control.dto.ReporteControlCostosResponse;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para consultar el reporte de control de costos.
 */
public interface ConsultarControlCostosUseCase {

    /**
     * Consulta el reporte de control de costos (Plan vs Real) de un presupuesto.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Reporte con datos del plan, real y desviaciones
     */
    ReporteControlCostosResponse consultar(UUID presupuestoId);
}
