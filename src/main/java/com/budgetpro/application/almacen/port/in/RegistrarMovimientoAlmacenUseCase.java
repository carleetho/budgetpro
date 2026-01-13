package com.budgetpro.application.almacen.port.in;

import com.budgetpro.application.almacen.dto.MovimientoAlmacenResponse;

import java.util.UUID;

/**
 * Caso de uso para registrar un movimiento de almacén.
 */
public interface RegistrarMovimientoAlmacenUseCase {
    
    /**
     * Registra un movimiento de entrada o salida de almacén.
     * 
     * @param almacenId ID del almacén
     * @param recursoId ID del recurso
     * @param tipoMovimiento Tipo de movimiento (ENTRADA o SALIDA)
     * @param fechaMovimiento Fecha del movimiento
     * @param cantidad Cantidad
     * @param precioUnitario Precio unitario
     * @param numeroDocumento Número de documento (factura, remisión, etc.)
     * @param partidaId ID de la partida (para salidas)
     * @param centroCostoId ID del centro de costo (opcional)
     * @param observaciones Observaciones
     * @return MovimientoAlmacenResponse con el movimiento registrado y stock actualizado
     */
    MovimientoAlmacenResponse registrar(UUID almacenId, UUID recursoId, String tipoMovimiento,
                                       java.time.LocalDate fechaMovimiento, java.math.BigDecimal cantidad,
                                       java.math.BigDecimal precioUnitario, String numeroDocumento,
                                       UUID partidaId, UUID centroCostoId, String observaciones);
}
