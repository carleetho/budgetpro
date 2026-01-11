package com.budgetpro.application.compra.port.in;

import com.budgetpro.application.compra.dto.RegistrarCompraDirectaCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraDirectaResponse;
import jakarta.validation.Valid;

/**
 * Puerto de Entrada (Inbound Port) para el caso de uso de registrar una compra directa.
 * 
 * Define el contrato del caso de uso sin depender de tecnologías específicas.
 * 
 * Este Use Case coordina:
 * - Repositorios (Compra, Billetera, Inventario)
 * - Transacción (@Transactional)
 * - Domain Service (ProcesarCompraDirectaService)
 */
public interface RegistrarCompraDirectaUseCase {

    /**
     * Ejecuta el caso de uso para registrar una compra directa.
     * 
     * @param command El comando con los datos de la compra a registrar
     * @return La respuesta con el ID de la compra registrada
     * @throws com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException si la billetera no tiene saldo suficiente
     * @throws IllegalStateException si algún inventario no tiene stock suficiente o si algún recurso no existe
     * @throws jakarta.validation.ConstraintViolationException si los datos del comando no son válidos
     */
    RegistrarCompraDirectaResponse ejecutar(@Valid RegistrarCompraDirectaCommand command);
}
