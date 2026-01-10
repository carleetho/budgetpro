package com.budgetpro.application.finanzas.port.in;

import com.budgetpro.application.finanzas.dto.SaldoResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de consultar el saldo de una billetera.
 * 
 * Define el contrato de consulta de saldo sin depender de tecnologías específicas.
 * 
 * Cumple con el requisito S1-07: "Query: Saldo actual por proyecto"
 */
public interface ConsultarSaldoUseCase {

    /**
     * Consulta el saldo actual de la billetera de un proyecto.
     * 
     * Si el proyecto no tiene billetera, retorna Optional.empty().
     * 
     * @param proyectoId El ID del proyecto
     * @return Un Optional con el saldo si existe la billetera, vacío en caso contrario
     */
    Optional<SaldoResponse> ejecutar(UUID proyectoId);
}
