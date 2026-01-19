package com.budgetpro.domain.finanzas.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BilleteraTest {

    @Test
    void egresar_debeBloquearSiHayMasDeTresPendientes() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), UUID.randomUUID());
        billetera.ingresar(new BigDecimal("1000.00"), "Ingreso base", "http://evidencia/ok");

        billetera.egresar(new BigDecimal("100.00"), "Egreso 1", null);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 2", null);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 3", null);

        assertEquals(3, billetera.contarMovimientosPendientesEvidencia());
        assertThrows(IllegalStateException.class,
                () -> billetera.egresar(new BigDecimal("50.00"), "Egreso 4", null));
    }

    @Test
    void egresar_conEvidenciaDebePermitirCuandoHayTresPendientes() {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), UUID.randomUUID());
        billetera.ingresar(new BigDecimal("1000.00"), "Ingreso base", "http://evidencia/ok");

        billetera.egresar(new BigDecimal("100.00"), "Egreso 1", null);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 2", null);
        billetera.egresar(new BigDecimal("100.00"), "Egreso 3", null);

        billetera.egresar(new BigDecimal("50.00"), "Egreso 4", "http://evidencia/ok");

        assertEquals(3, billetera.contarMovimientosPendientesEvidencia());
    }
}
