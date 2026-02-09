package com.budgetpro.domain.finanzas.partida.model;

import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartida;
import com.budgetpro.domain.finanzas.consumo.model.ConsumoPartidaId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartidaConsumoTest {

    @Test
    @DisplayName("Partida debe ser inmutable y calcular saldo correctamente")
    void partidaDebeSerInmutable() {
        PartidaId id = PartidaId.nuevo();
        UUID presupuestoId = UUID.randomUUID();
        Partida original = Partida.crearRaiz(id, presupuestoId, "01", "Preliminares", "M2", new BigDecimal("100"))
                .actualizarPresupuestoAsignado(new BigDecimal("5000"));

        assertEquals(new BigDecimal("5000"), original.getSaldoDisponible());

        Partida conReserva = original.reservarSaldo(new BigDecimal("1000"));

        assertNotSame(original, conReserva);
        assertEquals(new BigDecimal("5000"), original.getSaldoDisponible());
        assertEquals(new BigDecimal("4000"), conReserva.getSaldoDisponible());
        assertEquals(new BigDecimal("1000"), conReserva.getCompromisosPendientes());

        assertThrows(IllegalStateException.class, () -> conReserva.reservarSaldo(new BigDecimal("5000")));
    }

    @Test
    @DisplayName("ConsumoPartida debe ser inmutable")
    void consumoPartidaDebeSerInmutable() {
        ConsumoPartidaId id = ConsumoPartidaId.nuevo();
        UUID partidaId = UUID.randomUUID();
        ConsumoPartida original = ConsumoPartida.crearPorPlanilla(id, partidaId, new BigDecimal("100"),
                LocalDate.now());

        ConsumoPartida actualizado = original.actualizarMonto(new BigDecimal("200"));

        assertNotSame(original, actualizado);
        assertEquals(new BigDecimal("100"), original.getMonto());
        assertEquals(new BigDecimal("200"), actualizado.getMonto());
    }
}
