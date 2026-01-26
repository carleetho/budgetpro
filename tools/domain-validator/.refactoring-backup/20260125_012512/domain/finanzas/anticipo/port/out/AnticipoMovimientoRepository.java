package com.budgetpro.domain.finanzas.anticipo.port.out;

import com.budgetpro.domain.finanzas.anticipo.model.AnticipoMovimiento;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de salida para movimientos de anticipo.
 */
public interface AnticipoMovimientoRepository {
    BigDecimal obtenerSaldoPendiente(UUID proyectoId);
    void registrar(AnticipoMovimiento movimiento);
}
