package com.budgetpro.domain.logistica.inventario.model;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del agregado InventarioItem.
 */
class InventarioItemTest {

    @Test
    void crearConSnapshot_conDatosValidos_debeCrearItemConCamposSnapshot() {
        UUID proyectoId = UUID.randomUUID();
        InventarioId id = InventarioId.generate();
        BodegaId bodegaId = BodegaId.generate();

        InventarioItem item = InventarioItem.crearConSnapshot(
                id,
                proyectoId,
                "MAT-001",
                bodegaId,
                "Cemento",
                "Materiales",
                "SACOS"
        );

        assertNotNull(item);
        assertEquals(id, item.getId());
        assertEquals(proyectoId, item.getProyectoId());
        assertEquals("MAT-001", item.getRecursoExternalId());
        assertEquals(bodegaId, item.getBodegaId());
        assertEquals("Cemento", item.getNombre());
        assertEquals("Materiales", item.getClasificacion());
        assertEquals("SACOS", item.getUnidadBase());
        assertEquals(BigDecimal.ZERO, item.getCantidadFisica());
        assertEquals(BigDecimal.ZERO, item.getCostoPromedio());
        assertNotNull(item.getUltimaActualizacion());
        assertEquals(0L, item.getVersion());
    }

    @Test
    void crearConSnapshot_recursoExternalIdEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        BodegaId bodegaId = BodegaId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                InventarioItem.crearConSnapshot(
                        InventarioId.generate(),
                        proyectoId,
                        "   ",
                        bodegaId,
                        "Cemento",
                        "Materiales",
                        "SACOS"
                )
        );
    }

    @Test
    void crearConSnapshot_nombreEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        BodegaId bodegaId = BodegaId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                InventarioItem.crearConSnapshot(
                        InventarioId.generate(),
                        proyectoId,
                        "MAT-001",
                        bodegaId,
                        "",
                        "Materiales",
                        "SACOS"
                )
        );
    }

    @Test
    void crearConSnapshot_clasificacionEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        BodegaId bodegaId = BodegaId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                InventarioItem.crearConSnapshot(
                        InventarioId.generate(),
                        proyectoId,
                        "MAT-001",
                        bodegaId,
                        "Cemento",
                        null,
                        "SACOS"
                )
        );
    }

    @Test
    void crearConSnapshot_unidadBaseEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        BodegaId bodegaId = BodegaId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                InventarioItem.crearConSnapshot(
                        InventarioId.generate(),
                        proyectoId,
                        "MAT-001",
                        bodegaId,
                        "Cemento",
                        "Materiales",
                        "  "
                )
        );
    }

    @Test
    void crearConSnapshot_bodegaIdNulo_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () ->
                InventarioItem.crearConSnapshot(
                        InventarioId.generate(),
                        proyectoId,
                        "MAT-001",
                        null,
                        "Cemento",
                        "Materiales",
                        "SACOS"
                )
        );
    }

    @Test
    void reconstruir_debeRecuperarItemDesdePersistencia() {
        InventarioId id = InventarioId.generate();
        UUID proyectoId = UUID.randomUUID();
        UUID recursoId = UUID.randomUUID();
        BodegaId bodegaId = BodegaId.generate();
        LocalDateTime fecha = LocalDateTime.now().minusDays(1);

        InventarioItem item = InventarioItem.reconstruir(
                id, proyectoId, recursoId, "MAT-002", bodegaId,
                "Acero", "Materiales", "TONELADAS",
                new BigDecimal("100.50"), new BigDecimal("25.75"),
                null, fecha, 5L
        );

        assertNotNull(item);
        assertEquals(id, item.getId());
        assertEquals(proyectoId, item.getProyectoId());
        assertEquals(recursoId, item.getRecursoId());
        assertEquals("MAT-002", item.getRecursoExternalId());
        assertEquals(bodegaId, item.getBodegaId());
        assertEquals("Acero", item.getNombre());
        assertEquals("Materiales", item.getClasificacion());
        assertEquals("TONELADAS", item.getUnidadBase());
        assertEquals(new BigDecimal("100.50"), item.getCantidadFisica());
        assertEquals(new BigDecimal("25.75"), item.getCostoPromedio());
        assertEquals(fecha, item.getUltimaActualizacion());
        assertEquals(5L, item.getVersion());
    }
}
