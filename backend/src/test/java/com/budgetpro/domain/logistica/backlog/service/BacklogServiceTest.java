package com.budgetpro.domain.logistica.backlog.service;

import com.budgetpro.domain.logistica.backlog.model.EstadoRequerimiento;
import com.budgetpro.domain.logistica.backlog.model.PrioridadCompra;
import com.budgetpro.domain.logistica.backlog.model.RequerimientoCompra;
import com.budgetpro.domain.logistica.backlog.port.out.RequerimientoCompraRepository;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de BacklogService, incluyendo creación y resolución de backlog.
 */
@ExtendWith(MockitoExtension.class)
class BacklogServiceTest {

    private static final UUID PROYECTO_ID = UUID.randomUUID();
    private static final UUID RESIDENTE_ID = UUID.randomUUID();

    @Mock
    private RequerimientoCompraRepository requerimientoCompraRepository;

    @Mock
    private RequisicionRepository requisicionRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    private BacklogService backlogService;

    @BeforeEach
    void setUp() {
        backlogService = new BacklogService(
                requerimientoCompraRepository, requisicionRepository, inventarioRepository);
    }

    /**
     * Backlog creation and resolution scenario.
     * Setup: Requisicion for 100 units, stock = 0
     * Action: Attempt dispatch
     * Expect: RequerimientoCompra created with URGENTE priority, requisicion = PENDIENTE_COMPRA
     * Action: Register purchase entry of 100 units
     * Expect: Requisicion transitions to APROBADA
     */
    @Test
    void backlog_creacionYResolucion_transicionaRequisicionCorrectamente() {
        // Setup: Requisición aprobada con 100 unidades solicitadas
        RequisicionItemId itemId = RequisicionItemId.generate();
        RequisicionItem item = RequisicionItem.crear(
                itemId, "MAT-001", null, new BigDecimal("100"), "BOL", "Justificación"
        );
        List<RequisicionItem> items = new ArrayList<>();
        items.add(item);

        Requisicion requisicion = Requisicion.crear(
                RequisicionId.generate(), PROYECTO_ID, "Juan Pérez", null, null, items
        );
        requisicion.solicitar();
        requisicion.aprobar(RESIDENTE_ID, RESIDENTE_ID);

        // Setup: Inventario con stock = 0 (no existe o cantidad = 0)
        when(inventarioRepository.findByProyectoId(PROYECTO_ID)).thenReturn(List.of());

        // Action 1: Crear RequerimientoCompra cuando hay stock insuficiente
        RequerimientoCompra requerimiento = backlogService.crearRequerimiento(
                PROYECTO_ID,
                requisicion.getId(),
                "MAT-001",
                new BigDecimal("100"),
                "BOL",
                PrioridadCompra.URGENTE
        );

        // Expect: RequerimientoCompra creado con URGENTE priority
        assertThat(requerimiento.getPrioridad()).isEqualTo(PrioridadCompra.URGENTE);
        assertThat(requerimiento.getEstado()).isEqualTo(EstadoRequerimiento.PENDIENTE);
        assertThat(requerimiento.getCantidadNecesaria()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(requerimiento.getRequisicionId()).isEqualTo(requisicion.getId());

        ArgumentCaptor<RequerimientoCompra> captor = ArgumentCaptor.forClass(RequerimientoCompra.class);
        verify(requerimientoCompraRepository).save(captor.capture());
        RequerimientoCompra guardado = captor.getValue();
        assertThat(guardado.getPrioridad()).isEqualTo(PrioridadCompra.URGENTE);

        // Setup: Requisición en PENDIENTE_COMPRA
        requisicion.marcarPendienteCompra();
        assertThat(requisicion.getEstado()).isEqualTo(EstadoRequisicion.PENDIENTE_COMPRA);

        // Setup: Inventario con 100 unidades (llegó la compra)
        InventarioItem inventarioItem = InventarioItem.crearConSnapshot(
                InventarioId.generate(), PROYECTO_ID, "MAT-001",
                com.budgetpro.domain.logistica.bodega.model.BodegaId.generate(),
                "Cemento", "MATERIAL", "BOL"
        );
        inventarioItem.ingresar(new BigDecimal("100"), new BigDecimal("25.50"), UUID.randomUUID(), "Compra");

        when(inventarioRepository.findByProyectoId(PROYECTO_ID))
                .thenReturn(List.of(inventarioItem));
        when(requisicionRepository.findById(requisicion.getId()))
                .thenReturn(Optional.of(requisicion));
        when(requerimientoCompraRepository.findPendientesPorRecurso(
                eq(PROYECTO_ID), eq("MAT-001"), eq("BOL")))
                .thenReturn(List.of(requerimiento));

        // Action 2: Resolver backlog cuando llega stock
        backlogService.resolverBacklog(PROYECTO_ID, "MAT-001", "BOL");

        // Expect: Requisición transiciona a APROBADA
        ArgumentCaptor<Requisicion> requisicionCaptor = ArgumentCaptor.forClass(Requisicion.class);
        verify(requisicionRepository, atLeastOnce()).save(requisicionCaptor.capture());
        Requisicion requisicionGuardada = requisicionCaptor.getAllValues().get(requisicionCaptor.getAllValues().size() - 1);
        assertThat(requisicionGuardada.getEstado()).isEqualTo(EstadoRequisicion.APROBADA);

        // Expect: RequerimientoCompra marcado como RECIBIDA
        ArgumentCaptor<RequerimientoCompra> requerimientoCaptor = ArgumentCaptor.forClass(RequerimientoCompra.class);
        verify(requerimientoCompraRepository, atLeastOnce()).save(requerimientoCaptor.capture());
        RequerimientoCompra requerimientoGuardado = requerimientoCaptor.getAllValues()
                .get(requerimientoCaptor.getAllValues().size() - 1);
        assertThat(requerimientoGuardado.getEstado()).isEqualTo(EstadoRequerimiento.RECIBIDA);
    }

    @Test
    void crearRequerimiento_conDatosValidos_debeCrearRequerimientoConPrioridad() {
        RequisicionId requisicionId = RequisicionId.generate();

        RequerimientoCompra requerimiento = backlogService.crearRequerimiento(
                PROYECTO_ID, requisicionId, "MAT-001", new BigDecimal("50"), "BOL", PrioridadCompra.NORMAL
        );

        assertThat(requerimiento.getPrioridad()).isEqualTo(PrioridadCompra.NORMAL);
        assertThat(requerimiento.getEstado()).isEqualTo(EstadoRequerimiento.PENDIENTE);
        assertThat(requerimiento.getRecursoExternalId()).isEqualTo("MAT-001");
        assertThat(requerimiento.getCantidadNecesaria()).isEqualByComparingTo(new BigDecimal("50"));

        verify(requerimientoCompraRepository).save(any(RequerimientoCompra.class));
    }
}
