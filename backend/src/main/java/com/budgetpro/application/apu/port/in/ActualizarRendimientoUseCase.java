package com.budgetpro.application.apu.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de entrada para el caso de uso de actualizar rendimiento de un APU.
 */
public interface ActualizarRendimientoUseCase {

    /**
     * Actualiza el rendimiento vigente de un APU y recalcula automáticamente los costos afectados.
     * 
     * @param apuSnapshotId El ID del APUSnapshot a actualizar
     * @param nuevoRendimiento El nuevo rendimiento vigente (debe ser positivo)
     * @param usuarioId El ID del usuario que realiza la modificación
     * @throws IllegalArgumentException si el rendimiento es inválido
     * @throws com.budgetpro.application.apu.exception.ApuNoEncontradoException si el APU no existe
     */
    void actualizarRendimiento(UUID apuSnapshotId, BigDecimal nuevoRendimiento, UUID usuarioId);
}
