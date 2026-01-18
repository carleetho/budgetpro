package com.budgetpro.application.presupuesto.port.in;

import com.budgetpro.application.presupuesto.dto.PresupuestoResponse;

import java.util.UUID;

/**
 * Puerto de entrada (Inbound Port) para consultar un presupuesto.
 */
public interface ConsultarPresupuestoUseCase {

    /**
     * Consulta un presupuesto por su ID.
     * 
     * @param presupuestoId El ID del presupuesto
     * @return Respuesta con los datos del presupuesto incluyendo el costo total calculado
     * @throws com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException si el presupuesto no existe
     */
    PresupuestoResponse consultar(UUID presupuestoId);
}
