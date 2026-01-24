package com.budgetpro.domain.logistica.requisicion.service;

import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.bodega.port.out.DefaultBodegaPort;
import com.budgetpro.domain.logistica.inventario.exception.CantidadInsuficienteException;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.service.ImputacionService;
import com.budgetpro.domain.logistica.requisicion.exception.RequisicionNoAprobadaException;
import com.budgetpro.domain.logistica.requisicion.model.EstadoRequisicion;
import com.budgetpro.domain.logistica.requisicion.model.Requisicion;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionId;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionItem;
import com.budgetpro.domain.logistica.requisicion.model.RequisicionItemId;
import com.budgetpro.domain.logistica.requisicion.port.out.RequisicionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests de DespachoRequisicionService, incluyendo despacho parcial.
 */
@ExtendWith(MockitoExtension.class)
class DespachoRequisicionServiceTest {

        private static final UUID PROYECTO_ID = UUID.randomUUID();
        private static final UUID RESIDENTE_ID = UUID.randomUUID();
        private static final BodegaId BODEGA_ID = BodegaId.generate();

        @Mock
        private RequisicionRepository requisicionRepository;

        @Mock
        private InventarioRepository inventarioRepository;

        @Mock
        private DefaultBodegaPort defaultBodegaPort;

        @Mock

        private BacklogService backlogService;

        @Mock
        private ImputacionService imputacionService;

        private DespachoRequisicionService despachoService;

        @BeforeEach
        void setUp() {
                despachoService = new DespachoRequisicionService(requisicionRepository, inventarioRepository,
                                defaultBodegaPort, backlogService, imputacionService);
        }

        /**
         * Partial dispatch scenario. Setup: Requisicion with 100 units requested, 60
         * units in stock. Action: Dispatch 60 units. Expect: cantidadDespachada = 60,
         * estado = DESPACHADA_PARCIAL, MovimientoInventario created.
         */
        @Test
        void despacharRequisicion_despachoParcial_actualizaCantidadDespachadaYEstado() {
                // Setup: Requisición aprobada con 100 unidades solicitadas
                RequisicionItemId itemId = RequisicionItemId.generate();
                RequisicionItem item = RequisicionItem.crear(itemId, "MAT-001", null, new BigDecimal("100"), // cantidadSolicitada
                                "BOL", "Para construcción");
                List<RequisicionItem> items = new ArrayList<>();
                items.add(item);

                Requisicion requisicion = Requisicion.crear(RequisicionId.generate(), PROYECTO_ID, "Juan Pérez",
                                "Frente A", "Requisición de prueba", items);
                requisicion.solicitar();
                requisicion.aprobar(RESIDENTE_ID, RESIDENTE_ID);

                // Setup: Inventario con 60 unidades disponibles
                InventarioItem inventarioItem = InventarioItem.crearConSnapshot(InventarioId.generate(), PROYECTO_ID,
                                "MAT-001", BODEGA_ID, "Cemento", "MATERIAL", "BOL");
                // Agregar 60 unidades al inventario (usando compraDetalleId null para test)
                inventarioItem.ingresar(new BigDecimal("60"), new BigDecimal("25.50"), UUID.randomUUID(), // compraDetalleId
                                                                                                          // para test
                                "Stock inicial");

                when(requisicionRepository.findById(any(RequisicionId.class))).thenReturn(Optional.of(requisicion));
                when(defaultBodegaPort.getDefaultForProject(PROYECTO_ID)).thenReturn(Optional.of(BODEGA_ID));
                when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(eq(PROYECTO_ID),
                                eq("MAT-001"), eq("BOL"), eq(BODEGA_ID))).thenReturn(Optional.of(inventarioItem));

                // Action: Despachar 60 unidades
                DespachoItem despachoItem = new DespachoItem(itemId, new BigDecimal("60"));
                despachoService.despacharRequisicion(requisicion.getId(), List.of(despachoItem));

                // Expect: cantidadDespachada = 60, estado = DESPACHADA_PARCIAL
                RequisicionItem itemActualizado = requisicion.getItems().get(0);
                assertThat(itemActualizado.getCantidadDespachada()).isEqualByComparingTo(new BigDecimal("60"));
                assertThat(itemActualizado.getCantidadPendiente()).isEqualByComparingTo(new BigDecimal("40"));
                assertThat(requisicion.getEstado()).isEqualTo(EstadoRequisicion.DESPACHADA_PARCIAL);

                // Verificar que se guardó la requisición y el inventario
                ArgumentCaptor<Requisicion> requisicionCaptor = ArgumentCaptor.forClass(Requisicion.class);
                verify(requisicionRepository).save(requisicionCaptor.capture());
                Requisicion requisicionGuardada = requisicionCaptor.getValue();
                assertThat(requisicionGuardada.getEstado()).isEqualTo(EstadoRequisicion.DESPACHADA_PARCIAL);

                ArgumentCaptor<InventarioItem> inventarioCaptor = ArgumentCaptor.forClass(InventarioItem.class);
                verify(inventarioRepository).save(inventarioCaptor.capture());
                InventarioItem inventarioGuardado = inventarioCaptor.getValue();
                assertThat(inventarioGuardado.getCantidadFisica()).isEqualByComparingTo(BigDecimal.ZERO); // 60 - 60 = 0

                // Verificar que hay un movimiento de salida con requisición (puede haber otros
                // movimientos previos)
                var movimientoSalida = inventarioGuardado.getMovimientosNuevos().stream()
                                .filter(m -> m.getRequisicionId() != null).findFirst()
                                .orElseThrow(() -> new AssertionError(
                                                "Debe existir un movimiento de salida con requisición"));
                assertThat(movimientoSalida.getRequisicionId()).isEqualTo(requisicion.getId().getValue());
                assertThat(movimientoSalida.getRequisicionItemId()).isEqualTo(itemId.getValue());
                assertThat(movimientoSalida.getCantidad()).isEqualByComparingTo(new BigDecimal("60"));

                // Verificar registro de AC
                verify(imputacionService).validarYRegistrarAC(eq(item.getPartidaId()), any(), // NaturalezaGasto
                                                                                              // (GASTO_GENERAL_OBRA as
                                                                                              // setup has null
                                                                                              // partidaID)
                                eq(new BigDecimal("60")), any(), // Costo promedio
                                anyString());
        }

        @Test
        void despacharRequisicion_stockInsuficiente_transicionaAPendienteCompra() {
                RequisicionItemId itemId = RequisicionItemId.generate();
                RequisicionItem item = RequisicionItem.crear(itemId, "MAT-001", null, new BigDecimal("100"), "BOL",
                                "Justificación");
                Requisicion requisicion = Requisicion.crear(RequisicionId.generate(), PROYECTO_ID, "Juan", null, null,
                                List.of(item));
                requisicion.solicitar();
                requisicion.aprobar(RESIDENTE_ID, RESIDENTE_ID);

                InventarioItem inventarioItem = InventarioItem.crearConSnapshot(InventarioId.generate(), PROYECTO_ID,
                                "MAT-001", BODEGA_ID, "Cemento", "MATERIAL", "BOL");
                inventarioItem.ingresar(new BigDecimal("30"), new BigDecimal("25.50"), UUID.randomUUID(), "Stock");

                when(requisicionRepository.findById(any(RequisicionId.class))).thenReturn(Optional.of(requisicion));
                when(defaultBodegaPort.getDefaultForProject(PROYECTO_ID)).thenReturn(Optional.of(BODEGA_ID));
                when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(eq(PROYECTO_ID),
                                eq("MAT-001"), eq("BOL"), eq(BODEGA_ID))).thenReturn(Optional.of(inventarioItem));

                DespachoItem despachoItem = new DespachoItem(itemId, new BigDecimal("100"));

                assertThatThrownBy(
                                () -> despachoService.despacharRequisicion(requisicion.getId(), List.of(despachoItem)))
                                                .isInstanceOf(CantidadInsuficienteException.class)
                                                .hasMessageContaining("Stock insuficiente");

                // Verificar que se transicionó a PENDIENTE_COMPRA
                ArgumentCaptor<Requisicion> captor = ArgumentCaptor.forClass(Requisicion.class);
                verify(requisicionRepository).save(captor.capture());
                assertThat(captor.getValue().getEstado()).isEqualTo(EstadoRequisicion.PENDIENTE_COMPRA);

                // Verificar que se creó RequerimientoCompra con prioridad URGENTE
                verify(backlogService).crearRequerimiento(eq(PROYECTO_ID), eq(requisicion.getId()), eq("MAT-001"),
                                any(java.math.BigDecimal.class), // cantidadNecesaria (cantidad pendiente)
                                eq("BOL"), eq(com.budgetpro.domain.logistica.backlog.model.PrioridadCompra.URGENTE));
        }

        @Test
        void despacharRequisicion_requisicionNoAprobada_lanzaExcepcion() {
                Requisicion requisicion = Requisicion.crear(RequisicionId.generate(), PROYECTO_ID, "Juan", null, null,
                                List.of(RequisicionItem.crear(RequisicionItemId.generate(), "MAT-001", null,
                                                new BigDecimal("100"), "BOL", "Justificación")));
                // Estado BORRADOR (no aprobada)

                when(requisicionRepository.findById(any(RequisicionId.class))).thenReturn(Optional.of(requisicion));

                DespachoItem despachoItem = new DespachoItem(requisicion.getItems().get(0).getId(),
                                new BigDecimal("50"));

                assertThatThrownBy(
                                () -> despachoService.despacharRequisicion(requisicion.getId(), List.of(despachoItem)))
                                                .isInstanceOf(RequisicionNoAprobadaException.class)
                                                .hasMessageContaining("BORRADOR");
        }

        @Test
        void despacharRequisicion_despachoTotal_transicionaADespachadaTotal() {
                RequisicionItemId itemId = RequisicionItemId.generate();
                RequisicionItem item = RequisicionItem.crear(itemId, "MAT-001", null, new BigDecimal("100"), "BOL",
                                "Justificación");
                Requisicion requisicion = Requisicion.crear(RequisicionId.generate(), PROYECTO_ID, "Juan", null, null,
                                List.of(item));
                requisicion.solicitar();
                requisicion.aprobar(RESIDENTE_ID, RESIDENTE_ID);

                InventarioItem inventarioItem = InventarioItem.crearConSnapshot(InventarioId.generate(), PROYECTO_ID,
                                "MAT-001", BODEGA_ID, "Cemento", "MATERIAL", "BOL");
                inventarioItem.ingresar(new BigDecimal("100"), new BigDecimal("25.50"), UUID.randomUUID(), "Stock");

                when(requisicionRepository.findById(any(RequisicionId.class))).thenReturn(Optional.of(requisicion));
                when(defaultBodegaPort.getDefaultForProject(PROYECTO_ID)).thenReturn(Optional.of(BODEGA_ID));
                when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(eq(PROYECTO_ID),
                                eq("MAT-001"), eq("BOL"), eq(BODEGA_ID))).thenReturn(Optional.of(inventarioItem));

                DespachoItem despachoItem = new DespachoItem(itemId, new BigDecimal("100"));
                despachoService.despacharRequisicion(requisicion.getId(), List.of(despachoItem));

                ArgumentCaptor<Requisicion> captor = ArgumentCaptor.forClass(Requisicion.class);
                verify(requisicionRepository).save(captor.capture());
                assertThat(captor.getValue().getEstado()).isEqualTo(EstadoRequisicion.DESPACHADA_TOTAL);
                assertThat(captor.getValue().getItems().get(0).getCantidadDespachada())
                                .isEqualByComparingTo(new BigDecimal("100"));
        }
}
