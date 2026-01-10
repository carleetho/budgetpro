package com.budgetpro.application.finanzas.port.in;

import com.budgetpro.application.finanzas.dto.IngresarFondosCommand;
import com.budgetpro.application.finanzas.dto.MovimientoResponse;
import jakarta.validation.Valid;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de ingresar fondos a una billetera.
 * 
 * Define el contrato de ingreso de fondos sin depender de tecnologías específicas.
 */
public interface IngresarFondosUseCase {

    /**
     * Ingresa fondos a la billetera de un proyecto.
     * 
     * Crea un movimiento de tipo INGRESO y actualiza el saldo de la billetera.
     * Si el proyecto no tiene billetera, se crea automáticamente.
     * 
     * @param command El comando con los datos del ingreso (proyectoId, monto, referencia, evidenciaUrl)
     * @return La respuesta con los datos del movimiento creado
     */
    MovimientoResponse ejecutar(@Valid IngresarFondosCommand command);
}
