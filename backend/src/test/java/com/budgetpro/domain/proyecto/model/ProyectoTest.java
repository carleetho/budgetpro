package com.budgetpro.domain.proyecto.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProyectoTest {

    @Test
    @DisplayName("Debe crear un proyecto correctamente en estado BORRADOR")
    void debeCrearProyectoCorrectamente() {
        ProyectoId id = ProyectoId.nuevo();
        String nombre = "Proyecto Test";
        String ubicacion = "San Salvador";

        Proyecto proyecto = Proyecto.crear(id, nombre, ubicacion);

        assertEquals(id, proyecto.getId());
        assertEquals("Proyecto Test", proyecto.getNombre());
        assertEquals("San Salvador", proyecto.getUbicacion());
        assertEquals(EstadoProyecto.BORRADOR, proyecto.getEstado());
        assertFalse(proyecto.isActivo());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el nombre es nulo o vacío")
    void debeLanzarExcepcionSiNombreInvalido() {
        ProyectoId id = ProyectoId.nuevo();
        assertThrows(IllegalArgumentException.class, () -> Proyecto.crear(id, null, "Ubicación"));
        assertThrows(IllegalArgumentException.class, () -> Proyecto.crear(id, "  ", "Ubicación"));
    }

    @Test
    @DisplayName("Debe permitir actualizar nombre y ubicación retornando nueva instancia")
    void debePermitirActualizarDatos() {
        Proyecto original = Proyecto.crear(ProyectoId.nuevo(), "Original", "Original");

        Proyecto actualizado = original.actualizarNombre("Nuevo Nombre").actualizarUbicacion("Nueva Ubicación");

        assertEquals("Original", original.getNombre());
        assertEquals("Original", original.getUbicacion());
        assertEquals("Nuevo Nombre", actualizado.getNombre());
        assertEquals("Nueva Ubicación", actualizado.getUbicacion());
        assertEquals(original.getId(), actualizado.getId());
    }

    @Test
    @DisplayName("Debe gestionar transiciones de estado retornando nueva instancia")
    void debeGestionarEstados() {
        Proyecto borrador = Proyecto.crear(ProyectoId.nuevo(), "Proyecto", "Ubicación");

        Proyecto activo = borrador.activar();
        assertEquals(EstadoProyecto.BORRADOR, borrador.getEstado());
        assertEquals(EstadoProyecto.ACTIVO, activo.getEstado());
        assertTrue(activo.isActivo());

        Proyecto suspendido = activo.suspender();
        assertEquals(EstadoProyecto.SUSPENDIDO, suspendido.getEstado());

        Proyecto cerrado = suspendido.cerrar();
        assertEquals(EstadoProyecto.CERRADO, cerrado.getEstado());
    }
}
