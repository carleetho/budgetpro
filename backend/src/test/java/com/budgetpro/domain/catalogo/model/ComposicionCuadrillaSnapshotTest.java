package com.budgetpro.domain.catalogo.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ComposicionCuadrillaSnapshotTest {

    @Test
    void deberiaCrearComposicionValida() {
        ComposicionCuadrillaSnapshot composicion = new ComposicionCuadrillaSnapshot(
                "PERS-001",
                "Capataz",
                new BigDecimal("0.1"),
                new BigDecimal("120.00"),
                "PEN"
        );

        assertEquals("PERS-001", composicion.personalExternalId());
        assertEquals("Capataz", composicion.personalNombre());
        assertEquals(new BigDecimal("0.1"), composicion.cantidad());
        assertEquals(new BigDecimal("120.00"), composicion.costoDia());
        assertEquals("PEN", composicion.moneda());
    }

    @Test
    void deberiaRechazarPersonalExternalIdVacio() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComposicionCuadrillaSnapshot(
                    "",
                    "Capataz",
                    new BigDecimal("0.1"),
                    new BigDecimal("120.00"),
                    "PEN"
            );
        });
    }

    @Test
    void deberiaRechazarCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComposicionCuadrillaSnapshot(
                    "PERS-001",
                    "Capataz",
                    new BigDecimal("-1.0"),
                    new BigDecimal("120.00"),
                    "PEN"
            );
        });
    }

    @Test
    void deberiaRechazarCantidadCero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComposicionCuadrillaSnapshot(
                    "PERS-001",
                    "Capataz",
                    BigDecimal.ZERO,
                    new BigDecimal("120.00"),
                    "PEN"
            );
        });
    }

    @Test
    void deberiaRechazarCostoDiaNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ComposicionCuadrillaSnapshot(
                    "PERS-001",
                    "Capataz",
                    new BigDecimal("0.1"),
                    new BigDecimal("-10.00"),
                    "PEN"
            );
        });
    }

    @Test
    void deberiaPermitirCostoDiaCero() {
        ComposicionCuadrillaSnapshot composicion = new ComposicionCuadrillaSnapshot(
                "PERS-001",
                "Capataz",
                new BigDecimal("0.1"),
                BigDecimal.ZERO,
                "PEN"
        );
        assertEquals(BigDecimal.ZERO, composicion.costoDia());
    }
}
