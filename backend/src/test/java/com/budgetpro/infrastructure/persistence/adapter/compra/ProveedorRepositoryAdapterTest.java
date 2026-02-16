package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.Proveedor;
import com.budgetpro.domain.logistica.compra.model.ProveedorEstado;
import com.budgetpro.domain.logistica.compra.model.ProveedorId;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de integración para ProveedorRepositoryAdapter.
 */
@Transactional
class ProveedorRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private ProveedorRepository proveedorRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe guardar y recuperar un proveedor correctamente")
    void debeGuardarYRecuperarProveedor() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        String razonSocial = "Proveedor Test S.A.C.";
        String ruc = "20123456789";
        String contacto = "contacto@test.com";
        String direccion = "Av. Test 123";
        LocalDateTime now = LocalDateTime.now();

        Proveedor proveedor = Proveedor.crear(id, razonSocial, ruc, contacto, direccion, testUserId, now);

        proveedorRepository.save(proveedor);

        Optional<Proveedor> encontrado = proveedorRepository.findById(id);
        assertTrue(encontrado.isPresent());
        Proveedor recuperado = encontrado.get();

        assertEquals(id, recuperado.getId());
        assertEquals(razonSocial, recuperado.getRazonSocial());
        assertEquals(ruc, recuperado.getRuc());
        assertEquals(contacto, recuperado.getContacto());
        assertEquals(direccion, recuperado.getDireccion());
        assertEquals(ProveedorEstado.ACTIVO, recuperado.getEstado());
        assertEquals(1L, recuperado.getVersion()); // Version incrementado por Hibernate
        assertNotNull(recuperado.getCreatedAt());
        assertEquals(testUserId, recuperado.getCreatedBy());
    }

    @Test
    @DisplayName("Debe actualizar un proveedor existente correctamente")
    void debeActualizarProveedorExistente() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        Proveedor proveedor = Proveedor.crear(id, "Proveedor Original", "20123456789", null, null, testUserId, LocalDateTime.now());
        proveedorRepository.save(proveedor);

        // Actualizar contacto y dirección
        UUID updateUserId = UUID.randomUUID();
        LocalDateTime updateTime = LocalDateTime.now();
        proveedor.actualizarContacto("nuevo@contacto.com", updateUserId, updateTime);
        proveedor.actualizarDireccion("Nueva Dirección 456", updateUserId, updateTime);

        proveedorRepository.save(proveedor);

        Optional<Proveedor> actualizado = proveedorRepository.findById(id);
        assertTrue(actualizado.isPresent());
        Proveedor recuperado = actualizado.get();

        assertEquals("nuevo@contacto.com", recuperado.getContacto());
        assertEquals("Nueva Dirección 456", recuperado.getDireccion());
        assertEquals(updateUserId, recuperado.getUpdatedBy());
        assertEquals(2L, recuperado.getVersion()); // Version incrementado
    }

    @Test
    @DisplayName("Debe verificar existencia de proveedor por RUC")
    void debeVerificarExistenciaPorRuc() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        Proveedor proveedor = Proveedor.crear(id, "Proveedor Test", "20123456789", null, null, testUserId, LocalDateTime.now());
        proveedorRepository.save(proveedor);

        assertTrue(proveedorRepository.existsByRuc("20123456789"));
        assertFalse(proveedorRepository.existsByRuc("20987654321"));
    }

    @Test
    @DisplayName("Debe encontrar todos los proveedores")
    void debeEncontrarTodosLosProveedores() {
        ProveedorId id1 = ProveedorId.from(UUID.randomUUID());
        Proveedor proveedor1 = Proveedor.crear(id1, "Proveedor 1", "20123456789", null, null, testUserId, LocalDateTime.now());
        proveedorRepository.save(proveedor1);

        ProveedorId id2 = ProveedorId.from(UUID.randomUUID());
        Proveedor proveedor2 = Proveedor.crear(id2, "Proveedor 2", "20987654321", null, null, testUserId, LocalDateTime.now());
        proveedorRepository.save(proveedor2);

        List<Proveedor> todos = proveedorRepository.findAll();
        assertThat(todos).hasSizeGreaterThanOrEqualTo(2);
        assertThat(todos).anyMatch(p -> p.getId().equals(id1));
        assertThat(todos).anyMatch(p -> p.getId().equals(id2));
    }

    @Test
    @DisplayName("Debe gestionar transiciones de estado correctamente")
    void debeGestionarTransicionesDeEstado() {
        ProveedorId id = ProveedorId.from(UUID.randomUUID());
        Proveedor proveedor = Proveedor.crear(id, "Proveedor Test", "20123456789", null, null, testUserId, LocalDateTime.now());
        proveedorRepository.save(proveedor);

        UUID updateUserId = UUID.randomUUID();
        LocalDateTime updateTime = LocalDateTime.now();

        // Inactivar
        proveedor.inactivar(updateUserId, updateTime);
        proveedorRepository.save(proveedor);

        Optional<Proveedor> inactivo = proveedorRepository.findById(id);
        assertTrue(inactivo.isPresent());
        assertEquals(ProveedorEstado.INACTIVO, inactivo.get().getEstado());

        // Bloquear
        proveedor.bloquear(updateUserId, updateTime.plusMinutes(1));
        proveedorRepository.save(proveedor);

        Optional<Proveedor> bloqueado = proveedorRepository.findById(id);
        assertTrue(bloqueado.isPresent());
        assertEquals(ProveedorEstado.BLOQUEADO, bloqueado.get().getEstado());

        // Reactivar
        proveedor.activar(updateUserId, updateTime.plusMinutes(2));
        proveedorRepository.save(proveedor);

        Optional<Proveedor> reactivado = proveedorRepository.findById(id);
        assertTrue(reactivado.isPresent());
        assertEquals(ProveedorEstado.ACTIVO, reactivado.get().getEstado());
    }

    @Test
    @DisplayName("Debe retornar Optional.empty cuando el proveedor no existe")
    void debeRetornarEmptyCuandoNoExiste() {
        ProveedorId idNoExistente = ProveedorId.from(UUID.randomUUID());
        Optional<Proveedor> resultado = proveedorRepository.findById(idNoExistente);

        assertFalse(resultado.isPresent());
    }
}
