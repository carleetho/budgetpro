package com.budgetpro.domain.finanzas.exception;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaldoInsuficienteExceptionTest {

    @Test
    void constructorConDetalleDebeIncluirContexto() {
        UUID proyectoId = UUID.randomUUID();
        SaldoInsuficienteException ex = new SaldoInsuficienteException(
                proyectoId,
                new BigDecimal("200.00"),
                new BigDecimal("350.00"),
                "Detalle adicional"
        );

        assertTrue(ex.getMessage().contains(proyectoId.toString()));
        assertTrue(ex.getMessage().contains("200.00"));
        assertTrue(ex.getMessage().contains("350.00"));
        assertTrue(ex.getMessage().contains("Detalle adicional"));
    }

    @Test
    void gettersDebenExponerValores() {
        UUID proyectoId = UUID.randomUUID();
        BigDecimal disponible = new BigDecimal("500.00");
        BigDecimal requerido = new BigDecimal("600.00");
        SaldoInsuficienteException ex = new SaldoInsuficienteException(proyectoId, disponible, requerido, "detalle");

        assertEquals(proyectoId, ex.getProyectoId());
        assertEquals(disponible, ex.getSaldoDisponible());
        assertEquals(requerido, ex.getMontoRequerido());
    }
}
