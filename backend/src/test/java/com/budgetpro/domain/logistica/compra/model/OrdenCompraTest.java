package com.budgetpro.domain.logistica.compra.model;

import com.budgetpro.domain.logistica.compra.port.out.PartidaValidator;
import com.budgetpro.domain.logistica.compra.port.out.PresupuestoValidator;
import com.budgetpro.domain.logistica.compra.port.out.ProveedorValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el agregado OrdenCompra.
 */
class OrdenCompraTest {

    @Test
    @DisplayName("Debe crear una orden de compra correctamente en estado BORRADOR")
    void debeCrearOrdenCompraCorrectamente() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        String numero = "PO-2024-001";
        UUID proyectoId = UUID.randomUUID();
        ProveedorId proveedorId = ProveedorId.from(UUID.randomUUID());
        LocalDate fecha = LocalDate.now();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        List<DetalleOrdenCompra> detalles = List.of(
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Cemento", new BigDecimal("100"), "kg", new BigDecimal("10.00"))
        );

        OrdenCompra orden = OrdenCompra.crear(id, numero, proyectoId, proveedorId, fecha, null, null, detalles, userId, now);

        assertEquals(id, orden.getId());
        assertEquals("PO-2024-001", orden.getNumero());
        assertEquals(proyectoId, orden.getProyectoId());
        assertEquals(proveedorId, orden.getProveedorId());
        assertEquals(fecha, orden.getFecha());
        assertEquals(OrdenCompraEstado.BORRADOR, orden.getEstado());
        assertEquals(new BigDecimal("1000.00"), orden.getMontoTotal());
        assertEquals(1, orden.getDetalles().size());
        assertEquals(0L, orden.getVersion());
        assertNotNull(orden.getCreatedAt());
        assertNotNull(orden.getCreatedBy());
    }

    @Test
    @DisplayName("Debe calcular montoTotal como suma de subtotales de detalles")
    void debeCalcularMontoTotalCorrectamente() {
        OrdenCompra orden = crearOrdenCompraBase();

        // Detalle 1: 100 * 10.00 = 1000.00
        // Detalle 2: 50 * 20.00 = 1000.00
        // Total esperado: 2000.00
        assertEquals(new BigDecimal("2000.00"), orden.getMontoTotal());
    }

    @Test
    @DisplayName("Debe lanzar excepción si proyectoId es nulo")
    void debeLanzarExcepcionSiProyectoIdEsNulo() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        ProveedorId proveedorId = ProveedorId.from(UUID.randomUUID());
        List<DetalleOrdenCompra> detalles = crearDetallesBase();

        assertThrows(IllegalArgumentException.class, () ->
            OrdenCompra.crear(id, "PO-2024-001", null, proveedorId, LocalDate.now(), null, null, detalles,
                UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si la lista de detalles está vacía")
    void debeLanzarExcepcionSiDetallesVacios() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        UUID proyectoId = UUID.randomUUID();
        ProveedorId proveedorId = ProveedorId.from(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () ->
            OrdenCompra.crear(id, "PO-2024-001", proyectoId, proveedorId, LocalDate.now(), null, null,
                new ArrayList<>(), UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe permitir transición BORRADOR → SOLICITADA con validaciones")
    void debePermitirTransicionBorradorASolicitada() {
        OrdenCompra orden = crearOrdenCompraBase();
        PresupuestoValidator presupuestoValidator = mock(PresupuestoValidator.class);
        PartidaValidator partidaValidator = mock(PartidaValidator.class);
        ProveedorValidator proveedorValidator = mock(ProveedorValidator.class);

        // Configurar mocks para validaciones exitosas
        when(proveedorValidator.esProveedorActivo(orden.getProveedorId())).thenReturn(true);
        when(partidaValidator.esPartidaLeafValida(any(UUID.class))).thenReturn(true);
        doNothing().when(presupuestoValidator).validarDisponibilidadPresupuesto(any(UUID.class), any(BigDecimal.class));

        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        orden.solicitar(presupuestoValidator, partidaValidator, proveedorValidator, userId, now);

        assertEquals(OrdenCompraEstado.SOLICITADA, orden.getEstado());
        assertEquals(userId, orden.getUpdatedBy());
        assertEquals(now, orden.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta solicitar desde estado diferente a BORRADOR")
    void debeLanzarExcepcionSiSolicitarDesdeEstadoInvalido() {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);

        assertThrows(IllegalStateException.class, () ->
            orden.solicitar(mock(PresupuestoValidator.class), mock(PartidaValidator.class),
                mock(ProveedorValidator.class), UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe validar proveedor activo al solicitar (L-04)")
    void debeValidarProveedorActivoAlSolicitar() {
        OrdenCompra orden = crearOrdenCompraBase();
        ProveedorValidator proveedorValidator = mock(ProveedorValidator.class);

        when(proveedorValidator.esProveedorActivo(orden.getProveedorId())).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
            orden.solicitar(mock(PresupuestoValidator.class), mock(PartidaValidator.class),
                proveedorValidator, UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe validar partidas leaf al solicitar (REGLA-153)")
    void debeValidarPartidasLeafAlSolicitar() {
        OrdenCompra orden = crearOrdenCompraBase();
        PartidaValidator partidaValidator = mock(PartidaValidator.class);
        ProveedorValidator proveedorValidator = mock(ProveedorValidator.class);

        when(proveedorValidator.esProveedorActivo(any())).thenReturn(true);
        when(partidaValidator.esPartidaLeafValida(any(UUID.class))).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
            orden.solicitar(mock(PresupuestoValidator.class), partidaValidator,
                proveedorValidator, UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe validar presupuesto disponible al solicitar (L-01)")
    void debeValidarPresupuestoDisponibleAlSolicitar() {
        OrdenCompra orden = crearOrdenCompraBase();
        PresupuestoValidator presupuestoValidator = mock(PresupuestoValidator.class);
        PartidaValidator partidaValidator = mock(PartidaValidator.class);
        ProveedorValidator proveedorValidator = mock(ProveedorValidator.class);

        when(proveedorValidator.esProveedorActivo(any())).thenReturn(true);
        when(partidaValidator.esPartidaLeafValida(any(UUID.class))).thenReturn(true);
        doThrow(new IllegalStateException("L-01: Presupuesto insuficiente"))
            .when(presupuestoValidator).validarDisponibilidadPresupuesto(any(UUID.class), any(BigDecimal.class));

        assertThrows(IllegalStateException.class, () ->
            orden.solicitar(presupuestoValidator, partidaValidator, proveedorValidator,
                UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe permitir transición SOLICITADA → APROBADA")
    void debePermitirTransicionSolicitadaAAprobada() {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        orden.aprobar(userId, now);

        assertEquals(OrdenCompraEstado.APROBADA, orden.getEstado());
        assertEquals(userId, orden.getUpdatedBy());
        assertEquals(now, orden.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta aprobar desde estado diferente a SOLICITADA")
    void debeLanzarExcepcionSiAprobarDesdeEstadoInvalido() {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.BORRADOR);

        assertThrows(IllegalStateException.class, () ->
            orden.aprobar(UUID.randomUUID(), LocalDateTime.now())
        );
    }

    @Test
    @DisplayName("Debe permitir transición APROBADA → ENVIADA")
    void debePermitirTransicionAprobadaAEnviada() {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.APROBADA);
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        orden.enviar(userId, now);

        assertEquals(OrdenCompraEstado.ENVIADA, orden.getEstado());
        assertEquals(userId, orden.getUpdatedBy());
        assertEquals(now, orden.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe permitir transición ENVIADA → RECIBIDA")
    void debePermitirTransicionEnviadaARecibida() {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.ENVIADA);
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        orden.confirmarRecepcion(userId, now);

        assertEquals(OrdenCompraEstado.RECIBIDA, orden.getEstado());
        assertEquals(userId, orden.getUpdatedBy());
        assertEquals(now, orden.getUpdatedAt());
    }

    @Test
    @DisplayName("Debe permitir rechazar orden (SOLICITADA → BORRADOR)")
    void debePermitirRechazarOrden() {
        OrdenCompra orden = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        orden.rechazar(userId, now);

        assertEquals(OrdenCompraEstado.BORRADOR, orden.getEstado());
        assertEquals(userId, orden.getUpdatedBy());
        assertEquals(now, orden.getUpdatedAt());
    }

    @Test
    @DisplayName("puedeModificar debe retornar true solo en estado BORRADOR")
    void puedeModificarDebeRetornarTrueSoloEnBorrador() {
        OrdenCompra borrador = crearOrdenCompraEnEstado(OrdenCompraEstado.BORRADOR);
        OrdenCompra solicitada = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);
        OrdenCompra aprobada = crearOrdenCompraEnEstado(OrdenCompraEstado.APROBADA);

        assertTrue(borrador.puedeModificar());
        assertFalse(solicitada.puedeModificar());
        assertFalse(aprobada.puedeModificar());
    }

    @Test
    @DisplayName("puedeEliminar debe retornar true solo en estado BORRADOR")
    void puedeEliminarDebeRetornarTrueSoloEnBorrador() {
        OrdenCompra borrador = crearOrdenCompraEnEstado(OrdenCompraEstado.BORRADOR);
        OrdenCompra solicitada = crearOrdenCompraEnEstado(OrdenCompraEstado.SOLICITADA);
        OrdenCompra recibida = crearOrdenCompraEnEstado(OrdenCompraEstado.RECIBIDA);

        assertTrue(borrador.puedeEliminar());
        assertFalse(solicitada.puedeEliminar());
        assertFalse(recibida.puedeEliminar());
    }

    @Test
    @DisplayName("Debe permitir actualizar condicionesPago y observaciones")
    void debePermitirActualizarCondicionesPagoYObservaciones() {
        OrdenCompra orden = crearOrdenCompraBase();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        assertNull(orden.getCondicionesPago());
        orden.actualizarCondicionesPago("Pago a 30 días", userId, now);
        assertEquals("Pago a 30 días", orden.getCondicionesPago());
        assertEquals(userId, orden.getUpdatedBy());
        assertEquals(now, orden.getUpdatedAt());

        assertNull(orden.getObservaciones());
        orden.actualizarObservaciones("Urgente", userId, now.plusMinutes(1));
        assertEquals("Urgente", orden.getObservaciones());
    }

    @Test
    @DisplayName("Debe reconstruir orden desde persistencia correctamente")
    void debeReconstruirOrdenDesdePersistencia() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        String numero = "PO-2024-001";
        UUID proyectoId = UUID.randomUUID();
        ProveedorId proveedorId = ProveedorId.from(UUID.randomUUID());
        LocalDate fecha = LocalDate.now();
        OrdenCompraEstado estado = OrdenCompraEstado.APROBADA;
        BigDecimal montoTotal = new BigDecimal("2000.00");
        String condicionesPago = "Pago a 30 días";
        String observaciones = "Urgente";
        Long version = 3L;
        UUID createdBy = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        UUID updatedBy = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        List<DetalleOrdenCompra> detalles = crearDetallesBase();

        OrdenCompra orden = OrdenCompra.reconstruir(id, numero, proyectoId, proveedorId, fecha, estado, montoTotal,
            condicionesPago, observaciones, version, createdBy, createdAt, updatedBy, updatedAt, detalles);

        assertEquals(id, orden.getId());
        assertEquals(numero, orden.getNumero());
        assertEquals(estado, orden.getEstado());
        assertEquals(montoTotal, orden.getMontoTotal());
        assertEquals(condicionesPago, orden.getCondicionesPago());
        assertEquals(observaciones, orden.getObservaciones());
        assertEquals(version, orden.getVersion());
        assertEquals(createdBy, orden.getCreatedBy());
        assertEquals(createdAt, orden.getCreatedAt());
        assertEquals(updatedBy, orden.getUpdatedBy());
        assertEquals(updatedAt, orden.getUpdatedAt());
    }

    @Test
    @DisplayName("getDetalles debe retornar copia defensiva")
    void getDetallesDebeRetornarCopiaDefensiva() {
        OrdenCompra orden = crearOrdenCompraBase();
        List<DetalleOrdenCompra> detalles1 = orden.getDetalles();
        List<DetalleOrdenCompra> detalles2 = orden.getDetalles();

        assertNotSame(detalles1, detalles2);
        assertEquals(detalles1, detalles2);
    }

    private OrdenCompra crearOrdenCompraBase() {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        UUID proyectoId = UUID.randomUUID();
        ProveedorId proveedorId = ProveedorId.from(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<DetalleOrdenCompra> detalles = crearDetallesBase();

        return OrdenCompra.crear(id, "PO-2024-001", proyectoId, proveedorId, LocalDate.now(), null, null, detalles, userId, now);
    }

    private OrdenCompra crearOrdenCompraEnEstado(OrdenCompraEstado estado) {
        OrdenCompraId id = OrdenCompraId.from(UUID.randomUUID());
        UUID proyectoId = UUID.randomUUID();
        ProveedorId proveedorId = ProveedorId.from(UUID.randomUUID());
        LocalDate fecha = LocalDate.now();
        BigDecimal montoTotal = new BigDecimal("2000.00");
        Long version = 1L;
        UUID createdBy = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        UUID updatedBy = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        List<DetalleOrdenCompra> detalles = crearDetallesBase();

        return OrdenCompra.reconstruir(id, "PO-2024-001", proyectoId, proveedorId, fecha, estado, montoTotal,
            null, null, version, createdBy, createdAt, updatedBy, updatedAt, detalles);
    }

    private List<DetalleOrdenCompra> crearDetallesBase() {
        return List.of(
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Cemento", new BigDecimal("100"), "kg", new BigDecimal("10.00")),
            DetalleOrdenCompra.crear(UUID.randomUUID(), "Arena", new BigDecimal("50"), "m³", new BigDecimal("20.00"))
        );
    }
}
