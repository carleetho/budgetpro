package com.budgetpro.domain.finanzas.evm.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EVMSnapshotTest {

    @Test
    @DisplayName("Debe calcular correctamente las métricas derivadas")
    void debeCalcularMetricasDerivadas() {
        // Given
        EVMSnapshotId id = EVMSnapshotId.nuevo();
        UUID proyectoId = UUID.randomUUID();
        LocalDateTime fechaCorte = LocalDateTime.now();
        BigDecimal pv = new BigDecimal("1000.00");
        BigDecimal ev = new BigDecimal("800.00");
        BigDecimal ac = new BigDecimal("900.00");
        BigDecimal bac = new BigDecimal("5000.00");

        // When
        EVMSnapshot snapshot = EVMSnapshot.calcular(id, proyectoId, fechaCorte, pv, ev, ac, bac);

        // Then
        // CV = EV - AC = 800 - 900 = -100
        assertEquals(new BigDecimal("-100.00"), snapshot.getCv());
        // SV = EV - PV = 800 - 1000 = -200
        assertEquals(new BigDecimal("-200.00"), snapshot.getSv());
        // CPI = EV / AC = 800 / 900 = 0.8889
        assertEquals(new BigDecimal("0.8889"), snapshot.getCpi());
        // SPI = EV / PV = 800 / 1000 = 0.8000
        assertEquals(new BigDecimal("0.8000"), snapshot.getSpi());

        // EAC = BAC / CPI = 5000 / 0.8889 = 5624.93
        assertEquals(new BigDecimal("5624.93"), snapshot.getEac());
        // ETC = EAC - AC = 5624.93 - 900 = 4724.93
        assertEquals(new BigDecimal("4724.93"), snapshot.getEtc());
        // VAC = BAC - EAC = 5000 - 5624.93 = -624.93
        assertEquals(new BigDecimal("-624.93"), snapshot.getVac());
    }

    @Test
    @DisplayName("Debe generar interpretación correcta para bajo rendimiento")
    void debeGenerarInterpretacionBajoRendimiento() {
        // Given
        EVMSnapshot snapshot = EVMSnapshot.calcular(EVMSnapshotId.nuevo(), UUID.randomUUID(), LocalDateTime.now(),
                new BigDecimal("1000.00"), new BigDecimal("800.00"), new BigDecimal("1100.00"),
                new BigDecimal("5000.00"));

        // Then
        assertTrue(snapshot.getInterpretacion().contains("Proyecto bajo presupuesto (CPI < 1.0)"));
        assertTrue(snapshot.getInterpretacion().contains("Proyecto retrasado (SPI < 1.0)"));
    }

    @Test
    @DisplayName("Debe generar interpretación correcta para alto rendimiento")
    void debeGenerarInterpretacionAltoRendimiento() {
        // Given
        EVMSnapshot snapshot = EVMSnapshot.calcular(EVMSnapshotId.nuevo(), UUID.randomUUID(), LocalDateTime.now(),
                new BigDecimal("1000.00"), new BigDecimal("1200.00"), new BigDecimal("1100.00"),
                new BigDecimal("5000.00"));

        // Then
        assertTrue(snapshot.getInterpretacion().contains("Proyecto con ahorro en costos (CPI > 1.0)"));
        assertTrue(snapshot.getInterpretacion().contains("Proyecto adelantado (SPI > 1.0)"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si EV excede BAC")
    void debeFallarSiEVExcedeBAC() {
        EVMSnapshotId id = EVMSnapshotId.nuevo();
        UUID proyectoId = UUID.randomUUID();
        LocalDateTime fechaCorte = LocalDateTime.now();
        BigDecimal pv = new BigDecimal("1000.00");
        BigDecimal ev = new BigDecimal("5500.00");
        BigDecimal ac = new BigDecimal("1100.00");
        BigDecimal bac = new BigDecimal("5000.00");

        assertThrows(IllegalStateException.class,
                () -> EVMSnapshot.calcular(id, proyectoId, fechaCorte, pv, ev, ac, bac));
    }
}
