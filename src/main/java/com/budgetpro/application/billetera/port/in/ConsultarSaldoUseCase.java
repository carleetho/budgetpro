package com.budgetpro.application.billetera.port.in;

import com.budgetpro.application.billetera.dto.SaldoResponse;

import java.util.UUID;

/**
 * Puerto de Entrada (Inbound Port) para la query de consultar saldo por proyecto.
 * 
 * Query READ según CQRS-Lite. NO usa agregados del dominio.
 * 
 * Define el contrato de la query sin depender de tecnologías específicas.
 */
public interface ConsultarSaldoUseCase {

    /**
     * Consulta el saldo actual de la billetera de un proyecto.
     * 
     * REGLA CQRS-Lite: Esta query lee directamente desde el read model (BilleteraEntity),
     * sin usar agregados ni repositorios del dominio.
     * 
     * @param proyectoId El ID del proyecto
     * @return El saldo actual de la billetera del proyecto
     * @throws IllegalArgumentException si no existe una billetera para el proyecto
     */
    SaldoResponse consultarPorProyecto(UUID proyectoId);
}
