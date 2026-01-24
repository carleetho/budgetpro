package com.budgetpro.domain.logistica.requisicion.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del agregado Requisicion.
 */
class RequisicionTest {

    @Test
    void registrarDespacho_despachoParcial_debeActualizarEstadoCorrectamente() {
        UUID proyectoId = UUID.randomUUID();
        RequisicionId requisicionId = RequisicionId.generate();
        RequisicionItemId itemId = RequisicionItemId.generate();
        
        // Crear ítem con cantidad solicitada = 100
        RequisicionItem item = RequisicionItem.crear(
                itemId,
                "MAT-001",
                UUID.randomUUID(),
                new BigDecimal("100"),
                "SACOS",
                "Material necesario para obra"
        );
        
        List<RequisicionItem> items = new ArrayList<>();
        items.add(item);
        
        // Crear requisición y aprobarla
        Requisicion requisicion = Requisicion.crear(
                requisicionId,
                proyectoId,
                "Juan Pérez",
                "Frente A",
                "Requisición de materiales",
                items
        );
        
        requisicion.solicitar();
        UUID residenteId = UUID.randomUUID();
        requisicion.aprobar(residenteId, residenteId); // Aprobar con el mismo residente
        
        assertEquals(EstadoRequisicion.APROBADA, requisicion.getEstado());
        assertEquals(BigDecimal.ZERO, item.getCantidadDespachada());
        
        // Primer despacho parcial: 60 unidades
        requisicion.registrarDespacho(itemId, new BigDecimal("60"));
        
        assertEquals(new BigDecimal("60"), item.getCantidadDespachada());
        assertEquals(new BigDecimal("40"), item.getCantidadPendiente());
        assertEquals(EstadoRequisicion.DESPACHADA_PARCIAL, requisicion.getEstado());
        
        // Segundo despacho: 40 unidades (completa el total)
        requisicion.registrarDespacho(itemId, new BigDecimal("40"));
        
        assertEquals(new BigDecimal("100"), item.getCantidadDespachada());
        assertEquals(BigDecimal.ZERO, item.getCantidadPendiente());
        assertTrue(item.estaCompletamenteDespachado());
        assertEquals(EstadoRequisicion.DESPACHADA_TOTAL, requisicion.getEstado());
    }

    @Test
    void aprobar_soloResidenteAsignado_debePermitirAprobacion() {
        UUID proyectoId = UUID.randomUUID();
        UUID residenteId = UUID.randomUUID();
        Requisicion requisicion = crearRequisicionBasica(proyectoId);
        
        requisicion.solicitar();
        requisicion.aprobar(residenteId, residenteId);
        
        assertEquals(EstadoRequisicion.APROBADA, requisicion.getEstado());
        assertEquals(residenteId, requisicion.getAprobadoPor());
    }

    @Test
    void aprobar_usuarioNoResidente_debeRechazar() {
        UUID proyectoId = UUID.randomUUID();
        UUID residenteId = UUID.randomUUID();
        UUID otroUsuarioId = UUID.randomUUID();
        Requisicion requisicion = crearRequisicionBasica(proyectoId);
        
        requisicion.solicitar();
        
        assertThrows(IllegalArgumentException.class, () ->
                requisicion.aprobar(otroUsuarioId, residenteId)
        );
        
        assertEquals(EstadoRequisicion.SOLICITADA, requisicion.getEstado());
    }

    @Test
    void solicitar_desdeBorrador_debeCambiarEstado() {
        Requisicion requisicion = crearRequisicionBasica(UUID.randomUUID());
        
        assertEquals(EstadoRequisicion.BORRADOR, requisicion.getEstado());
        requisicion.solicitar();
        
        assertEquals(EstadoRequisicion.SOLICITADA, requisicion.getEstado());
        assertNotNull(requisicion.getFechaSolicitud());
    }

    @Test
    void rechazar_desdeSolicitada_debeCambiarEstado() {
        Requisicion requisicion = crearRequisicionBasica(UUID.randomUUID());
        
        requisicion.solicitar();
        requisicion.rechazar();
        
        assertEquals(EstadoRequisicion.RECHAZADA, requisicion.getEstado());
    }

    @Test
    void marcarPendienteCompra_desdeAprobada_debeCambiarEstado() {
        UUID proyectoId = UUID.randomUUID();
        UUID residenteId = UUID.randomUUID();
        Requisicion requisicion = crearRequisicionBasica(proyectoId);
        
        requisicion.solicitar();
        requisicion.aprobar(residenteId, residenteId);
        requisicion.marcarPendienteCompra();
        
        assertEquals(EstadoRequisicion.PENDIENTE_COMPRA, requisicion.getEstado());
    }

    @Test
    void cerrar_desdeDespachadaTotal_debeCambiarEstado() {
        UUID proyectoId = UUID.randomUUID();
        UUID residenteId = UUID.randomUUID();
        Requisicion requisicion = crearRequisicionCompleta(proyectoId, residenteId);
        
        // Despachar completamente
        RequisicionItem item = requisicion.getItems().get(0);
        requisicion.registrarDespacho(item.getId(), new BigDecimal("100"));
        
        assertEquals(EstadoRequisicion.DESPACHADA_TOTAL, requisicion.getEstado());
        requisicion.cerrar();
        
        assertEquals(EstadoRequisicion.CERRADA, requisicion.getEstado());
    }

    @Test
    void registrarDespacho_cantidadExcedida_debeLanzarExcepcion() {
        UUID proyectoId = UUID.randomUUID();
        UUID residenteId = UUID.randomUUID();
        Requisicion requisicion = crearRequisicionCompleta(proyectoId, residenteId);
        
        RequisicionItem item = requisicion.getItems().get(0);
        
        assertThrows(IllegalArgumentException.class, () ->
                requisicion.registrarDespacho(item.getId(), new BigDecimal("150"))
        );
    }

    @Test
    void agregarItem_soloEnBorrador_debePermitir() {
        Requisicion requisicion = crearRequisicionBasica(UUID.randomUUID());
        
        RequisicionItem nuevoItem = RequisicionItem.crear(
                RequisicionItemId.generate(),
                "MAT-002",
                UUID.randomUUID(),
                new BigDecimal("50"),
                "TONELADAS",
                "Material adicional"
        );
        
        requisicion.agregarItem(nuevoItem);
        
        assertEquals(2, requisicion.getItems().size());
    }

    @Test
    void agregarItem_desdeSolicitada_debeRechazar() {
        Requisicion requisicion = crearRequisicionBasica(UUID.randomUUID());
        requisicion.solicitar();
        
        RequisicionItem nuevoItem = RequisicionItem.crear(
                RequisicionItemId.generate(),
                "MAT-002",
                UUID.randomUUID(),
                new BigDecimal("50"),
                "TONELADAS",
                "Material adicional"
        );
        
        assertThrows(IllegalStateException.class, () ->
                requisicion.agregarItem(nuevoItem)
        );
    }

    // Métodos helper

    private Requisicion crearRequisicionBasica(UUID proyectoId) {
        RequisicionItemId itemId = RequisicionItemId.generate();
        RequisicionItem item = RequisicionItem.crear(
                itemId,
                "MAT-001",
                UUID.randomUUID(),
                new BigDecimal("100"),
                "SACOS",
                "Material necesario"
        );
        
        List<RequisicionItem> items = new ArrayList<>();
        items.add(item);
        
        return Requisicion.crear(
                RequisicionId.generate(),
                proyectoId,
                "Juan Pérez",
                "Frente A",
                "Requisición básica",
                items
        );
    }

    private Requisicion crearRequisicionCompleta(UUID proyectoId, UUID residenteId) {
        Requisicion requisicion = crearRequisicionBasica(proyectoId);
        requisicion.solicitar();
        requisicion.aprobar(residenteId, residenteId);
        return requisicion;
    }
}
