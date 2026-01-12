package com.budgetpro.application.compra.dto;

import com.budgetpro.domain.finanzas.compra.EstadoCompra;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO (Data Transfer Object) que representa la respuesta del caso de uso RegistrarCompraDirectaUseCase.
 * 
 * Contiene el identificador de la compra registrada, su estado transaccional actual,
 * el saldo actualizado de la billetera y el stock actualizado de los recursos comprados.
 */
public record RegistrarCompraDirectaResponse(
        UUID compraId,
        EstadoCompra estado,
        String mensajeUsuario,
        BigDecimal saldoActual,
        List<StockInfo> stockActualizado
) {
    /**
     * DTO anidado que representa la información de stock actualizado de un recurso.
     */
    public record StockInfo(
            UUID recursoId,
            String recursoNombre,
            BigDecimal stockAnterior,
            BigDecimal stockActual,
            String unidad
    ) {
    }

    /**
     * Constructor auxiliar para crear respuesta sin mensaje de usuario.
     */
    public RegistrarCompraDirectaResponse(UUID compraId, EstadoCompra estado, BigDecimal saldoActual, List<StockInfo> stockActualizado) {
        this(compraId, estado, null, saldoActual, stockActualizado);
    }
}
