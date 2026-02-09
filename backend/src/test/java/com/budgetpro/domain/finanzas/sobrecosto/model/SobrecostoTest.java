package com.budgetpro.domain.finanzas.sobrecosto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SobrecostoTest {

    @Test
    @DisplayName("ConfiguracionLaboral debe calcular FSR correctamente e ser inmutable")
    void configuracionLaboralDebeSerInmutable() {
        ConfiguracionLaboralId id = ConfiguracionLaboralId.nuevo();
        // 251 laborables, 15 vacaciones, 15 aguinaldo, 5 no trabajados = 286 pagados
        // FSR = 251 / 286 = 0.8776
        ConfiguracionLaboral original = ConfiguracionLaboral.crearGlobal(id, 15, 15, new BigDecimal("30"), 5, 251);

        BigDecimal fsr = original.calcularFSR();
        assertEquals(0, new BigDecimal("0.8776").compareTo(fsr));

        BigDecimal salarioBase = new BigDecimal("1000");
        BigDecimal salarioReal = original.calcularSalarioReal(salarioBase);
        assertEquals(0, new BigDecimal("877.6000").compareTo(salarioReal));
    }

    @Test
    @DisplayName("AnalisisSobrecosto debe ser inmutable al actualizar porcentajes")
    void analisisSobrecostoDebeSerInmutable() {
        AnalisisSobrecostoId id = AnalisisSobrecostoId.nuevo();
        UUID presupuestoId = UUID.randomUUID();
        AnalisisSobrecosto original = AnalisisSobrecosto.crear(id, presupuestoId);

        AnalisisSobrecosto actualizado = original.actualizarIndirectos(new BigDecimal("10"), new BigDecimal("5"))
                .actualizarUtilidad(new BigDecimal("8"));

        assertNotSame(original, actualizado);
        assertEquals(BigDecimal.ZERO, original.getPorcentajeUtilidad());
        assertEquals(new BigDecimal("8"), actualizado.getPorcentajeUtilidad());
        assertEquals(new BigDecimal("15"), actualizado.getPorcentajeIndirectosTotal());
    }
}
