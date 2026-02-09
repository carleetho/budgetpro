package com.budgetpro.domain.finanzas.apu.model;

import com.budgetpro.domain.finanzas.recurso.model.Recurso;
import com.budgetpro.domain.finanzas.recurso.model.RecursoId;
import com.budgetpro.domain.shared.model.TipoRecurso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ApuRecursoTest {

    @Test
    @DisplayName("ApuInsumo debe ser inmutable y recalcular subtotal")
    void apuInsumoDebeSerInmutable() {
        ApuInsumoId id = ApuInsumoId.nuevo();
        UUID recursoId = UUID.randomUUID();
        BigDecimal cantidad = new BigDecimal("10");
        BigDecimal precio = new BigDecimal("100");

        ApuInsumo original = ApuInsumo.crear(id, recursoId, cantidad, precio);
        assertEquals(new BigDecimal("1000"), original.getSubtotal());

        ApuInsumo actualizado = original.actualizarCantidad(new BigDecimal("20"));

        assertNotSame(original, actualizado);
        assertEquals(new BigDecimal("10"), original.getCantidad());
        assertEquals(new BigDecimal("20"), actualizado.getCantidad());
        assertEquals(new BigDecimal("1000"), original.getSubtotal());
        assertEquals(new BigDecimal("2000"), actualizado.getSubtotal());
    }

    @Test
    @DisplayName("APU debe ser inmutable al agregar insumos")
    void apuDebeSerInmutable() {
        ApuId id = ApuId.nuevo();
        UUID partidaId = UUID.randomUUID();
        APU original = APU.crear(id, partidaId, "M2");

        APU conInsumo = original.agregarInsumo(UUID.randomUUID(), new BigDecimal("5"), new BigDecimal("50"));

        assertNotSame(original, conInsumo);
        assertEquals(0, original.getInsumos().size());
        assertEquals(1, conInsumo.getInsumos().size());
        assertEquals(new BigDecimal("250"), conInsumo.calcularCostoTotal());
    }

    @Test
    @DisplayName("Recurso debe ser inmutable al actualizar datos")
    void recursoDebeSerInmutable() {
        RecursoId id = RecursoId.generate();
        Recurso original = Recurso.crear(id, "Cemento", TipoRecurso.MATERIAL, "KG");

        Recurso actualizado = original.actualizarNombre("Cemento Gris").agregarAtributo("Marca", "Sol");

        assertNotSame(original, actualizado);
        assertEquals("CEMENTO", original.getNombre()); // Normalizado
        assertEquals("CEMENTO GRIS", actualizado.getNombre());
        assertTrue(original.getAtributos().isEmpty());
        assertEquals("Sol", actualizado.getAtributos().get("Marca"));
    }
}
