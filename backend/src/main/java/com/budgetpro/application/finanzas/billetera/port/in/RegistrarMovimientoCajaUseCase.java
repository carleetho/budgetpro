package com.budgetpro.application.finanzas.billetera.port.in;

import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.model.MovimientoCaja;
import com.budgetpro.domain.finanzas.model.TipoMovimiento;

import java.math.BigDecimal;

/**
 * Puerto de entrada para registrar movimientos de caja.
 */
public interface RegistrarMovimientoCajaUseCase {

    /**
     * Registra un movimiento de INGRESO o EGRESO en la billetera.
     *
     * @param billeteraId  ID de la billetera
     * @param monto        Monto del movimiento
     * @param moneda       Moneda del movimiento (ISO-4217)
     * @param tipo         Tipo de movimiento (INGRESO o EGRESO)
     * @param referencia   Referencia o concepto
     * @param evidenciaUrl URL de evidencia (opcional para ingreso, obligatorio para
     *                     egreso)
     * @return El movimiento registrado
     */
    MovimientoCaja registrar(BilleteraId billeteraId, BigDecimal monto, String moneda, TipoMovimiento tipo,
            String referencia, String evidenciaUrl);
}
