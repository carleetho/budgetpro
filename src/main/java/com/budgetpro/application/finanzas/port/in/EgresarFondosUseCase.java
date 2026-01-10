package com.budgetpro.application.finanzas.port.in;

import com.budgetpro.application.finanzas.dto.EgresarFondosCommand;
import com.budgetpro.application.finanzas.dto.MovimientoResponse;
import jakarta.validation.Valid;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de egresar fondos de una billetera.
 * 
 * Define el contrato de egreso de fondos sin depender de tecnologías específicas.
 */
public interface EgresarFondosUseCase {

    /**
     * Egresar fondos de la billetera de un proyecto.
     * 
     * Crea un movimiento de tipo EGRESO y actualiza el saldo de la billetera.
     * 
     * INVARIANTE CRÍTICA: Si el saldo resultante sería negativo, se lanza SaldoInsuficienteException.
     * 
     * @param command El comando con los datos del egreso (proyectoId, monto, referencia, evidenciaUrl)
     * @return La respuesta con los datos del movimiento creado
     * @throws com.budgetpro.domain.finanzas.billetera.exception.SaldoInsuficienteException 
     *         si el saldo resultante sería negativo
     */
    MovimientoResponse ejecutar(@Valid EgresarFondosCommand command);
}
