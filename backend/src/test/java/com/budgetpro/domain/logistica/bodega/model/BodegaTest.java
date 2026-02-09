package com.budgetpro.domain.logistica.bodega.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BodegaTest {

    @Test
    void crear_conDatosValidos_debeCrearBodega() {
        BodegaId id = BodegaId.generate();
        UUID proyectoId = UUID.randomUUID();

        Bodega bodega = Bodega.crear(id, proyectoId, "BOD-001", "Bodega Principal", "Sitio A", "Juan Perez");

        assertNotNull(bodega);
        assertEquals(id, bodega.getId());
        assertEquals(proyectoId, bodega.getProyectoId());
        assertEquals("BOD-001", bodega.getCodigo());
        assertTrue(bodega.isActiva());
        assertEquals(0L, bodega.getVersion());
    }

    @Test
    void desactivar_debeRetornarNuevaInstanciaInactiva() {
        Bodega bodega = crearBodegaBase();

        Bodega bodegaInactiva = bodega.desactivar();

        assertNotSame(bodega, bodegaInactiva); // Immutability check
        assertTrue(bodega.isActiva()); // Original remains active
        assertFalse(bodegaInactiva.isActiva()); // New instance is inactive
        assertEquals(bodega.getVersion() + 1, bodegaInactiva.getVersion());
    }

    @Test
    void activar_debeRetornarNuevaInstanciaActiva() {
        Bodega bodega = crearBodegaBase().desactivar(); // Start with inactive

        Bodega bodegaActiva = bodega.activar();

        assertNotSame(bodega, bodegaActiva);
        assertFalse(bodega.isActiva());
        assertTrue(bodegaActiva.isActiva());
        assertEquals(bodega.getVersion() + 1, bodegaActiva.getVersion());
    }

    @Test
    void activar_siYaEstaActiva_debeRetornarMismaInstancia() {
        Bodega bodega = crearBodegaBase();

        Bodega bodegaMisma = bodega.activar();

        assertSame(bodega, bodegaMisma); // Optimization: return this if no change
    }

    @Test
    void desactivar_siYaEstaInactiva_debeRetornarMismaInstancia() {
        Bodega bodega = crearBodegaBase().desactivar();

        Bodega bodegaMisma = bodega.desactivar();

        assertSame(bodega, bodegaMisma);
    }

    private Bodega crearBodegaBase() {
        return Bodega.crear(BodegaId.generate(), UUID.randomUUID(), "BOD-001", "Bodega Test", "Ubicacion Test",
                "Admin");
    }
}
