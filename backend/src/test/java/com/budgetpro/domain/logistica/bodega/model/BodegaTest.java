package com.budgetpro.domain.logistica.bodega.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests del agregado Bodega.
 */
class BodegaTest {

    @Test
    void crear_conDatosValidos_debeCrearBodegaActivaConFechaCreacion() {
        UUID proyectoId = UUID.randomUUID();
        BodegaId id = BodegaId.generate();

        Bodega bodega = Bodega.crear(
                id,
                proyectoId,
                "BOD-01",
                "Bodega Central",
                null,
                "Juan Pérez"
        );

        assertNotNull(bodega);
        assertEquals(id, bodega.getId());
        assertEquals(proyectoId, bodega.getProyectoId());
        assertEquals("BOD-01", bodega.getCodigo());
        assertEquals("Bodega Central", bodega.getNombre());
        assertEquals("Juan Pérez", bodega.getResponsable());
        assertTrue(bodega.isActiva());
        assertNotNull(bodega.getFechaCreacion());
        assertEquals(0L, bodega.getVersion());
    }

    @Test
    void crear_codigoEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                Bodega.crear(
                        BodegaId.generate(),
                        proyectoId,
                        "   ",
                        "Bodega Central",
                        null,
                        "Juan Pérez"
                )
        );
    }

    @Test
    void crear_proyectoIdNulo_debeLanzarExcepcion() {
        assertThrows(NullPointerException.class, () ->
                Bodega.crear(
                        BodegaId.generate(),
                        null,
                        "BOD-01",
                        "Bodega Central",
                        null,
                        "Juan Pérez"
                )
        );
    }

    @Test
    void crear_responsableEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                Bodega.crear(
                        BodegaId.generate(),
                        proyectoId,
                        "BOD-01",
                        "Bodega Central",
                        null,
                        ""
                )
        );
    }

    @Test
    void desactivar_activar_debeAlternarEstado() {
        Bodega bodega = Bodega.crear(
                BodegaId.generate(),
                UUID.randomUUID(),
                "BOD-01",
                "Bodega Central",
                null,
                "Juan Pérez"
        );

        assertTrue(bodega.isActiva());
        bodega.desactivar();
        assertFalse(bodega.isActiva());
        assertEquals(1L, bodega.getVersion());

        bodega.activar();
        assertTrue(bodega.isActiva());
        assertEquals(2L, bodega.getVersion());
    }

    @Test
    void reconstruir_debeRecuperarBodegaDesdePersistencia() {
        BodegaId id = BodegaId.generate();
        UUID proyectoId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now().minusDays(1);

        Bodega bodega = Bodega.reconstruir(
                id, proyectoId, "BOD-02", "Bodega Secundaria",
                "Edificio B", "María López", false, fecha, 3L
        );

        assertNotNull(bodega);
        assertEquals(id, bodega.getId());
        assertEquals("BOD-02", bodega.getCodigo());
        assertEquals("Bodega Secundaria", bodega.getNombre());
        assertEquals("Edificio B", bodega.getUbicacionFisica());
        assertEquals("María López", bodega.getResponsable());
        assertFalse(bodega.isActiva());
        assertEquals(fecha, bodega.getFechaCreacion());
        assertEquals(3L, bodega.getVersion());
    }
}
