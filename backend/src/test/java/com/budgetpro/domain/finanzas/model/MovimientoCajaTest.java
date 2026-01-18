package com.budgetpro.domain.finanzas.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovimientoCajaTest {

    @Test
    void crearEgreso_sinEvidenciaDebeQuedarPendiente() {
        MovimientoCaja movimiento = MovimientoCaja.crearEgreso(
                BilleteraId.generate(),
                new BigDecimal("100.00"),
                "Egreso sin evidencia",
                null
        );

        assertEquals(EstadoMovimientoCaja.PENDIENTE_DE_EVIDENCIA, movimiento.getEstado());
    }

    @Test
    void crearEgreso_conEvidenciaDebeQuedarSinEstadoPendiente() {
        MovimientoCaja movimiento = MovimientoCaja.crearEgreso(
                BilleteraId.generate(),
                new BigDecimal("100.00"),
                "Egreso con evidencia",
                "http://evidencia/ok"
        );

        assertNull(movimiento.getEstado());
    }
}
