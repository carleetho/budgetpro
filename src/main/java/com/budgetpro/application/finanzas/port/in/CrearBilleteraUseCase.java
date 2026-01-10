package com.budgetpro.application.finanzas.port.in;

import com.budgetpro.application.finanzas.dto.BilleteraResponse;
import com.budgetpro.application.finanzas.dto.CrearBilleteraCommand;
import jakarta.validation.Valid;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de crear una billetera.
 * 
 * Define el contrato de creación de billeteras sin depender de tecnologías específicas.
 */
public interface CrearBilleteraUseCase {

    /**
     * Crea una nueva billetera para un proyecto.
     * 
     * REGLA: Cada proyecto tiene UNA sola billetera (relación 1:1).
     * Si el proyecto ya tiene una billetera, se lanza BilleteraDuplicadaException.
     * 
     * @param command El comando con los datos necesarios para crear la billetera
     * @return La respuesta con los datos de la billetera creada
     */
    BilleteraResponse ejecutar(@Valid CrearBilleteraCommand command);
}
