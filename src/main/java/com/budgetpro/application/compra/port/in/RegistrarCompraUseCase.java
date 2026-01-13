package com.budgetpro.application.compra.port.in;

import com.budgetpro.application.compra.dto.RegistrarCompraCommand;
import com.budgetpro.application.compra.dto.RegistrarCompraResponse;

/**
 * Puerto de entrada (Inbound Port) para registrar una nueva compra.
 */
public interface RegistrarCompraUseCase {

    /**
     * Registra una nueva compra.
     * 
     * @param command Comando con los datos de la compra
     * @return Respuesta con la compra registrada
     * @throws com.budgetpro.application.compra.exception.ProyectoNoEncontradoException si el proyecto no existe
     * @throws com.budgetpro.application.compra.exception.PartidaNoEncontradaException si alguna partida no existe
     * @throws com.budgetpro.application.compra.exception.BilleteraNoEncontradaException si la billetera no existe
     * @throws com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException si la billetera no tiene saldo suficiente
     */
    RegistrarCompraResponse registrar(RegistrarCompraCommand command);
}
