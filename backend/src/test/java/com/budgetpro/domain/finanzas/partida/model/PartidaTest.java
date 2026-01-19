package com.budgetpro.domain.finanzas.partida.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartidaTest {

    @Test
    void getSaldoDisponible_debeAplicarFormulaCanonica() {
        Partida partida = Partida.crearRaiz(
                PartidaId.nuevo(),
                UUID.randomUUID(),
                "01.01",
                "Partida base",
                "UND",
                BigDecimal.TEN
        );
        partida.actualizarPresupuestoAsignado(new BigDecimal("1000.00"));
        partida.actualizarGastosReales(new BigDecimal("250.00"));
        partida.actualizarCompromisosPendientes(new BigDecimal("150.00"));

        assertEquals(new BigDecimal("600.00"), partida.getSaldoDisponible());
    }

    @Test
    void reservarSaldo_debeIncrementarCompromisos() {
        Partida partida = Partida.crearRaiz(
                PartidaId.nuevo(),
                UUID.randomUUID(),
                "01.02",
                "Partida secundaria",
                "UND",
                BigDecimal.ONE
        );
        partida.actualizarPresupuestoAsignado(new BigDecimal("500.00"));
        partida.actualizarGastosReales(BigDecimal.ZERO);
        partida.actualizarCompromisosPendientes(BigDecimal.ZERO);

        partida.reservarSaldo(new BigDecimal("120.00"));

        assertEquals(new BigDecimal("120.00"), partida.getCompromisosPendientes());
        assertEquals(new BigDecimal("380.00"), partida.getSaldoDisponible());
    }

    @Test
    void reservarSaldo_debeLanzarSiSaldoInsuficiente() {
        Partida partida = Partida.crearRaiz(
                PartidaId.nuevo(),
                UUID.randomUUID(),
                "01.03",
                "Partida limitada",
                "UND",
                BigDecimal.ONE
        );
        partida.actualizarPresupuestoAsignado(new BigDecimal("100.00"));
        partida.actualizarGastosReales(new BigDecimal("90.00"));
        partida.actualizarCompromisosPendientes(BigDecimal.ZERO);

        assertThrows(IllegalStateException.class, () -> partida.reservarSaldo(new BigDecimal("20.00")));
    }
}
