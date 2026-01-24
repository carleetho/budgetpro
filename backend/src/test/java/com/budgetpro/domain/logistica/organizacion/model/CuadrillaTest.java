package com.budgetpro.domain.logistica.organizacion.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de las entidades FrenteTrabajo y Cuadrilla.
 */
class CuadrillaTest {

    @Test
    void crear_cuadrillaVinculadaAFrente_debeCrearConRelacionFK() {
        UUID proyectoId = UUID.randomUUID();
        FrenteTrabajoId frenteId = FrenteTrabajoId.generate();
        CuadrillaId cuadrillaId = CuadrillaId.generate();

        // Crear frente de trabajo
        FrenteTrabajo frente = FrenteTrabajo.crear(
                frenteId,
                proyectoId,
                "FT-NORTE",
                "Frente Norte",
                "Ing. Juan Pérez"
        );

        assertNotNull(frente);
        assertEquals("FT-NORTE", frente.getCodigo());
        assertEquals("Frente Norte", frente.getNombre());
        assertEquals("Ing. Juan Pérez", frente.getResponsable());
        assertTrue(frente.isActivo());

        // Crear cuadrilla vinculada al frente
        Cuadrilla cuadrilla = Cuadrilla.crear(
                cuadrillaId,
                proyectoId,
                "CUAD-01",
                "Cuadrilla de Albañilería",
                "Carlos Rodríguez",
                frenteId
        );

        assertNotNull(cuadrilla);
        assertEquals(cuadrillaId, cuadrilla.getId());
        assertEquals(proyectoId, cuadrilla.getProyectoId());
        assertEquals("CUAD-01", cuadrilla.getCodigo());
        assertEquals("Cuadrilla de Albañilería", cuadrilla.getNombre());
        assertEquals("Carlos Rodríguez", cuadrilla.getCapataz());
        assertEquals(frenteId, cuadrilla.getFrenteTrabajoId());
        assertTrue(cuadrilla.isActiva());
    }

    @Test
    void crear_codigoEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        FrenteTrabajoId frenteId = FrenteTrabajoId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                Cuadrilla.crear(
                        CuadrillaId.generate(),
                        proyectoId,
                        "   ",
                        "Cuadrilla",
                        "Capataz",
                        frenteId
                )
        );
    }

    @Test
    void crear_capatazEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        FrenteTrabajoId frenteId = FrenteTrabajoId.generate();

        assertThrows(IllegalArgumentException.class, () ->
                Cuadrilla.crear(
                        CuadrillaId.generate(),
                        proyectoId,
                        "CUAD-01",
                        "Cuadrilla",
                        "",
                        frenteId
                )
        );
    }

    @Test
    void crear_frenteTrabajoIdNulo_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();

        assertThrows(NullPointerException.class, () ->
                Cuadrilla.crear(
                        CuadrillaId.generate(),
                        proyectoId,
                        "CUAD-01",
                        "Cuadrilla",
                        "Capataz",
                        null
                )
        );
    }

    @Test
    void activar_desactivar_debeAlternarEstado() {
        UUID proyectoId = UUID.randomUUID();
        FrenteTrabajoId frenteId = FrenteTrabajoId.generate();

        Cuadrilla cuadrilla = Cuadrilla.crear(
                CuadrillaId.generate(),
                proyectoId,
                "CUAD-01",
                "Cuadrilla",
                "Capataz",
                frenteId
        );

        assertTrue(cuadrilla.isActiva());
        cuadrilla.desactivar();
        assertFalse(cuadrilla.isActiva());
        cuadrilla.activar();
        assertTrue(cuadrilla.isActiva());
    }

    @Test
    void reconstruir_debeRecuperarCuadrillaDesdePersistencia() {
        UUID proyectoId = UUID.randomUUID();
        CuadrillaId id = CuadrillaId.generate();
        FrenteTrabajoId frenteId = FrenteTrabajoId.generate();

        Cuadrilla cuadrilla = Cuadrilla.reconstruir(
                id, proyectoId, "CUAD-02", "Cuadrilla de Electricidad",
                "Pedro Martínez", frenteId, false
        );

        assertNotNull(cuadrilla);
        assertEquals(id, cuadrilla.getId());
        assertEquals("CUAD-02", cuadrilla.getCodigo());
        assertEquals("Cuadrilla de Electricidad", cuadrilla.getNombre());
        assertEquals("Pedro Martínez", cuadrilla.getCapataz());
        assertEquals(frenteId, cuadrilla.getFrenteTrabajoId());
        assertFalse(cuadrilla.isActiva());
    }

    @Test
    void crear_frenteTrabajoConDatosValidos_debeCrearFrente() {
        UUID proyectoId = UUID.randomUUID();
        FrenteTrabajoId id = FrenteTrabajoId.generate();

        FrenteTrabajo frente = FrenteTrabajo.crear(
                id,
                proyectoId,
                "FT-SUR",
                "Frente Sur",
                "Ing. María López"
        );

        assertNotNull(frente);
        assertEquals(id, frente.getId());
        assertEquals(proyectoId, frente.getProyectoId());
        assertEquals("FT-SUR", frente.getCodigo());
        assertEquals("Frente Sur", frente.getNombre());
        assertEquals("Ing. María López", frente.getResponsable());
        assertTrue(frente.isActivo());
    }

    @Test
    void crear_frenteTrabajoResponsableEnBlanco_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                FrenteTrabajo.crear(
                        FrenteTrabajoId.generate(),
                        proyectoId,
                        "FT-01",
                        "Frente",
                        null
                )
        );
    }
}
