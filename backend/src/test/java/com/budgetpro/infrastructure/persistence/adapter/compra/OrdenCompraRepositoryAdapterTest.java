package com.budgetpro.infrastructure.persistence.adapter.compra;

import com.budgetpro.domain.logistica.compra.model.*;
import com.budgetpro.domain.logistica.compra.port.out.OrdenCompraRepository;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests de integración para OrdenCompraRepositoryAdapter.
 */
@Transactional
class OrdenCompraRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    private UUID testUserId;
    private UUID proyectoId;
    private ProveedorId proveedorId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        proyectoId = UUID.randomUUID();

        // Crear un proveedor de prueba
        proveedorId = ProveedorId.from(UUID.randomUUID());
        Proveedor proveedor = Proveedor.crear(proveedorId, "Proveedor Test", "20123456789", null, null, testUserId, LocalDateTime.now());
        proveedorRepository.save(proveedor);
    }

    @Test
    @DisplayName("Debe guardar y recuperar una orden de compra con detalles correctamente")
    void debeGuardarYRecuperarOrdenCompraConDetalles() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        String numero = "PO-2024-001";
        LocalDate fecha = LocalDate.now();
        List<DetalleOrdenCompra> detalles = List.of(
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Cemento", new BigDecimal("100"), "kg", new BigDecimal("10.00")),
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Arena", new BigDecimal("50"), "m³", new BigDecimal("20.00"))
        );

        OrdenCompra orden = OrdenCompra.crear(id, numero, proyectoId, proveedorId, fecha, null, null, detalles, testUserId, LocalDateTime.now());

        ordenCompraRepository.save(orden);

        Optional<OrdenCompra> encontrada = ordenCompraRepository.findById(id);
        assertTrue(encontrada.isPresent());
        OrdenCompra recuperada = encontrada.get();

        assertEquals(id, recuperada.getId());
        assertEquals(numero, recuperada.getNumero());
        assertEquals(proyectoId, recuperada.getProyectoId());
        assertEquals(proveedorId, recuperada.getProveedorId());
        assertEquals(fecha, recuperada.getFecha());
        assertEquals(OrdenCompraEstado.BORRADOR, recuperada.getEstado());
        assertEquals(new BigDecimal("2000.00"), recuperada.getMontoTotal());
        assertEquals(2, recuperada.getDetalles().size());
        assertEquals(1L, recuperada.getVersion());
        assertNotNull(recuperada.getCreatedAt());
        assertEquals(testUserId, recuperada.getCreatedBy());
    }

    @Test
    @DisplayName("Debe actualizar una orden de compra existente correctamente")
    void debeActualizarOrdenCompraExistente() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden = crearOrdenCompraBase(id);
        ordenCompraRepository.save(orden);

        UUID updateUserId = UUID.randomUUID();
        LocalDateTime updateTime = LocalDateTime.now();
        orden.actualizarCondicionesPago("Pago a 30 días", updateUserId, updateTime);
        orden.actualizarObservaciones("Urgente", updateUserId, updateTime);

        ordenCompraRepository.save(orden);

        Optional<OrdenCompra> actualizada = ordenCompraRepository.findById(id);
        assertTrue(actualizada.isPresent());
        OrdenCompra recuperada = actualizada.get();

        assertEquals("Pago a 30 días", recuperada.getCondicionesPago());
        assertEquals("Urgente", recuperada.getObservaciones());
        assertEquals(updateUserId, recuperada.getUpdatedBy());
        assertEquals(2L, recuperada.getVersion());
    }

    @Test
    @DisplayName("Debe encontrar órdenes por proyecto")
    void debeEncontrarOrdenesPorProyecto() {
        UUID otroProyectoId = UUID.randomUUID();

        OrdenCompraId id1 = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden1 = crearOrdenCompraBase(id1);
        ordenCompraRepository.save(orden1);

        OrdenCompraId id2 = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden2 = crearOrdenCompraBase(id2);
        ordenCompraRepository.save(orden2);

        OrdenCompraId id3 = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden3 = OrdenCompra.crear(id3, "PO-2024-003", otroProyectoId, proveedorId, LocalDate.now(), null, null,
            crearDetallesBase(), testUserId, LocalDateTime.now());
        ordenCompraRepository.save(orden3);

        List<OrdenCompra> ordenesProyecto = ordenCompraRepository.findByProyectoId(proyectoId);
        assertThat(ordenesProyecto).hasSizeGreaterThanOrEqualTo(2);
        assertThat(ordenesProyecto).anyMatch(o -> o.getId().equals(id1));
        assertThat(ordenesProyecto).anyMatch(o -> o.getId().equals(id2));
        assertThat(ordenesProyecto).noneMatch(o -> o.getId().equals(id3));
    }

    @Test
    @DisplayName("Debe encontrar órdenes por estado")
    void debeEncontrarOrdenesPorEstado() {
        OrdenCompraId id1 = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden1 = crearOrdenCompraBase(id1);
        ordenCompraRepository.save(orden1);

        OrdenCompraId id2 = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden2 = crearOrdenCompraBase(id2);
        ordenCompraRepository.save(orden2);

        // Cambiar estado de orden2 a SOLICITADA
        orden2.solicitar(mock(com.budgetpro.domain.logistica.compra.port.out.PresupuestoValidator.class),
            mock(com.budgetpro.domain.logistica.compra.port.out.PartidaValidator.class),
            mock(com.budgetpro.domain.logistica.compra.port.out.ProveedorValidator.class),
            testUserId, LocalDateTime.now());
        ordenCompraRepository.save(orden2);

        List<OrdenCompra> borradores = ordenCompraRepository.findByEstado(OrdenCompraEstado.BORRADOR);
        assertThat(borradores).hasSizeGreaterThanOrEqualTo(1);
        assertThat(borradores).anyMatch(o -> o.getId().equals(id1));

        List<OrdenCompra> solicitadas = ordenCompraRepository.findByEstado(OrdenCompraEstado.SOLICITADA);
        assertThat(solicitadas).hasSizeGreaterThanOrEqualTo(1);
        assertThat(solicitadas).anyMatch(o -> o.getId().equals(id2));
    }

    @Test
    @DisplayName("Debe generar número secuencial correctamente")
    void debeGenerarNumeroSecuencial() {
        int year = LocalDate.now().getYear();
        String numero1 = ordenCompraRepository.generateNextNumero(year);
        assertThat(numero1).matches("PO-" + year + "-\\d{3}");

        // Crear una orden con ese número
        OrdenCompraId id1 = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden1 = OrdenCompra.crear(id1, numero1, proyectoId, proveedorId, LocalDate.now(), null, null,
            crearDetallesBase(), testUserId, LocalDateTime.now());
        ordenCompraRepository.save(orden1);

        // Generar siguiente número
        String numero2 = ordenCompraRepository.generateNextNumero(year);
        assertThat(numero2).matches("PO-" + year + "-\\d{3}");
        assertThat(numero2).isNotEqualTo(numero1);
    }

    @Test
    @DisplayName("Debe retornar Optional.empty cuando la orden no existe")
    void debeRetornarEmptyCuandoNoExiste() {
        OrdenCompraId idNoExistente = OrdenCompraId.from(UUID.randomUUID());
        Optional<OrdenCompra> resultado = ordenCompraRepository.findById(idNoExistente);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Debe eliminar una orden de compra correctamente")
    void debeEliminarOrdenCompra() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        OrdenCompra orden = crearOrdenCompraBase(id);
        ordenCompraRepository.save(orden);

        ordenCompraRepository.delete(id);

        Optional<OrdenCompra> eliminada = ordenCompraRepository.findById(id);
        assertFalse(eliminada.isPresent());
    }

    private OrdenCompra crearOrdenCompraBase(OrdenCompraId id) {
        return OrdenCompra.crear(id, "PO-2024-001", proyectoId, proveedorId, LocalDate.now(), null, null,
            crearDetallesBase(), testUserId, LocalDateTime.now());
    }

    private List<DetalleOrdenCompra> crearDetallesBase() {
        return List.of(
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Cemento", new BigDecimal("100"), "kg", new BigDecimal("10.00"))
        );
    }
}
