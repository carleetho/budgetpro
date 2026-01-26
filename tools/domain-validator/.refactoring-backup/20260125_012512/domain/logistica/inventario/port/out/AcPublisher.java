package com.budgetpro.domain.logistica.inventario.port.out;

import com.budgetpro.domain.logistica.inventario.event.MaterialConsumed;

/**
 * Puerto para publicar eventos de Costo Real (AC) hacia EVM/Presupuestos.
 */
public interface AcPublisher {
    void publicar(MaterialConsumed event);
}
