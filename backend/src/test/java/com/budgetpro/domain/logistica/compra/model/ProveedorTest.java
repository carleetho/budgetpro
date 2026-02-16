package com.budgetpro.domain.logistica.compra.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el agregado Proveedor.
 */
class ProveedorTest {

    @Test
    @DisplayName("Debe crear un proveedor correctamente en estado ACTIVO")
    void debeCrearProveedorCorrectamente() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        String razonSocial = "Proveedor Test S.A.C.";
        String ruc = "20123456789";
        String contacto = "contacto@proveedor.com";
        String direccion = "Av. Principal 123";
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Proveedor proveedor = Proveedor.crear(id, razonSocial, ruc, contacto, direccion, userId, now);

        assertEquals(id, proveedor.getId());
        assertEquals("Proveedor Test S.A.C.", proveedor.getRazonSocial());
        assertEquals("20123456789", proveedor.getRuc());
        assertEquals(ProveedorEstado.ACTIVO, proveedor.getEstado());
        assertEquals(contacto, proveedor.getContacto());
        assertEquals(direccion, proveedor.getDireccion());
        assertEquals(0L, proveedor.getVersion());
        assertNotNull(proveedor.getCreatedAt());
        assertNotNull(proveedor.getCreatedBy());
    }

    @Test
    @DisplayName("Debe lanzar excepción si razonSocial es nulo o vacío")
    void debeLanzarExcepcionSiRazonSocialInvalido() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        assertThrows(IllegalArgumentException.class, 
            () -> Proveedor.crear(id, null, "20123456789", null, null, userId, now));
        assertThrows(IllegalArgumentException.class, 
            () -> Proveedor.crear(id, "  ", "20123456789", null, null, userId, now));
    }

    @Test
    @DisplayName("Debe lanzar excepción si ruc es nulo o vacío")
    void debeLanzarExcepcionSiRucInvalido() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        assertThrows(IllegalArgumentException.class, 
            () -> Proveedor.crear(id, "Proveedor Test", null, null, null, userId, now));
        assertThrows(IllegalArgumentException.class, 
            () -> Proveedor.crear(id, "Proveedor Test", "  ", null, null, userId, now));
    }

    @Test
    @DisplayName("Debe permitir transiciones de estado modificando el estado interno")
    void debeGestionarTransicionesDeEstado() {
        Proveedor proveedor = crearProveedorBase();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        assertEquals(ProveedorEstado.ACTIVO, proveedor.getEstado());

        proveedor.inactivar(userId, now);
        assertEquals(ProveedorEstado.INACTIVO, proveedor.getEstado());
        assertEquals(userId, proveedor.getUpdatedBy());
        assertEquals(now, proveedor.getUpdatedAt());

        proveedor.bloquear(userId, now.plusMinutes(1));
        assertEquals(ProveedorEstado.BLOQUEADO, proveedor.getEstado());

        proveedor.activar(userId, now.plusMinutes(2));
        assertEquals(ProveedorEstado.ACTIVO, proveedor.getEstado());
    }

    @Test
    @DisplayName("Debe permitir actualizar contacto y dirección modificando el estado interno")
    void debePermitirActualizarContactoYDireccion() {
        Proveedor proveedor = crearProveedorBase();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        assertNull(proveedor.getContacto());
        proveedor.actualizarContacto("contacto@proveedor.com", userId, now);
        assertEquals("contacto@proveedor.com", proveedor.getContacto());
        assertEquals(userId, proveedor.getUpdatedBy());
        assertEquals(now, proveedor.getUpdatedAt());

        assertNull(proveedor.getDireccion());
        proveedor.actualizarDireccion("Av. Principal 123", userId, now.plusMinutes(1));
        assertEquals("Av. Principal 123", proveedor.getDireccion());
    }

    @Test
    @DisplayName("Debe reconstruir proveedor desde persistencia correctamente")
    void debeReconstruirProveedorDesdePersistencia() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        String razonSocial = "Proveedor Reconstruido";
        String ruc = "20987654321";
        ProveedorEstado estado = ProveedorEstado.ACTIVO;
        String contacto = "contacto@test.com";
        String direccion = "Calle Test 456";
        Long version = 5L;
        UUID createdBy = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        UUID updatedBy = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        Proveedor proveedor = Proveedor.reconstruir(
            id, razonSocial, ruc, estado, contacto, direccion, version,
            createdBy, createdAt, updatedBy, updatedAt
        );

        assertEquals(id, proveedor.getId());
        assertEquals(razonSocial, proveedor.getRazonSocial());
        assertEquals(ruc, proveedor.getRuc());
        assertEquals(estado, proveedor.getEstado());
        assertEquals(contacto, proveedor.getContacto());
        assertEquals(direccion, proveedor.getDireccion());
        assertEquals(version, proveedor.getVersion());
        assertEquals(createdBy, proveedor.getCreatedBy());
        assertEquals(createdAt, proveedor.getCreatedAt());
        assertEquals(updatedBy, proveedor.getUpdatedBy());
        assertEquals(updatedAt, proveedor.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe actualizar campos de auditoría al cambiar estado")
    void debeActualizarCamposAuditoriaAlCambiarEstado() {
        Proveedor proveedor = crearProveedorBase();
        UUID originalCreatedBy = proveedor.getCreatedBy();
        LocalDateTime originalCreatedAt = proveedor.getCreatedAt();
        UUID newUserId = UUID.randomUUID();
        LocalDateTime newTimestamp = LocalDateTime.now();

        proveedor.inactivar(newUserId, newTimestamp);

        assertEquals(newUserId, proveedor.getUpdatedBy());
        assertEquals(newTimestamp, proveedor.getUpdatedAt());
        assertEquals(originalCreatedBy, proveedor.getCreatedBy());
        assertEquals(originalCreatedAt, proveedor.getCreatedAt());
    }

    private Proveedor crearProveedorBase() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        return Proveedor.crear(id, "Proveedor Test S.A.C.", "20123456789", null, null, userId, now);
    }
}
