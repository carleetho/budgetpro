package com.budgetpro.domain.logistica.inventario.service;

import com.budgetpro.domain.logistica.backlog.service.BacklogService;
import com.budgetpro.domain.logistica.compra.model.*;
import com.budgetpro.domain.logistica.inventario.event.BudgetAlertEvent;
import com.budgetpro.domain.logistica.inventario.event.MaterialConsumed;
import com.budgetpro.domain.logistica.inventario.exception.ExcesoRecepcionException;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;
import com.budgetpro.domain.logistica.inventario.port.out.AcPublisher;
import com.budgetpro.domain.logistica.inventario.port.out.BudgetAlertPublisher;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import com.budgetpro.domain.logistica.inventario.port.out.PartidaValidator;
import com.budgetpro.domain.shared.port.out.SecurityPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionInventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;
    @Mock
    private InventarioSnapshotService inventarioSnapshotService;
    @Mock
    private BacklogService backlogService;
    @Mock
    private PartidaValidator partidaValidator;
    @Mock
    private AcPublisher acPublisher;
    @Mock
    private BudgetAlertPublisher budgetAlertPublisher;
    @Mock
    private SecurityPort securityPort;

    private GestionInventarioService service;

    @BeforeEach
    void setUp() {
        service = new GestionInventarioService(inventarioRepository, inventarioSnapshotService, backlogService,
                partidaValidator, acPublisher, budgetAlertPublisher, securityPort);
    }

    @Test
    void registrarEntradaPorCompra_withinTolerance_shouldSucceed() {
        UUID proyectoId = UUID.randomUUID();
        UUID detalleId = UUID.randomUUID();
        CompraDetalle detalle = CompraDetalle.crear(CompraDetalleId.from(detalleId), "MAT-001", "Cem", "UN", null,
                NaturalezaGasto.DIRECTO_PARTIDA, RelacionContractual.CONTRACTUAL, RubroInsumo.MATERIAL_CONSTRUCCION,
                new BigDecimal("100"), new BigDecimal("10"));
        Compra compra = Compra.crear(CompraId.nuevo(), proyectoId, LocalDate.now(), "Prov", List.of(detalle));

        InventarioItem itemMock = mock(InventarioItem.class);
        when(inventarioSnapshotService.crearDesdeCompra(compra, detalle)).thenReturn(itemMock);

        // Action: Receive 105 (Within 5% tolerance)
        service.registrarEntradaPorCompra(compra, Map.of(detalleId, new BigDecimal("105")));

        verify(itemMock).ingresar(eq(new BigDecimal("105")), any(), any(), any());
        verify(inventarioRepository).save(itemMock);
    }

    @Test
    void registrarEntradaPorCompra_exceedsTolerance_shouldThrowException() {
        UUID proyectoId = UUID.randomUUID();
        UUID detalleId = UUID.randomUUID();
        CompraDetalle detalle = CompraDetalle.crear(CompraDetalleId.from(detalleId), "MAT-001", "Cem", "UN", null,
                NaturalezaGasto.DIRECTO_PARTIDA, RelacionContractual.CONTRACTUAL, RubroInsumo.MATERIAL_CONSTRUCCION,
                new BigDecimal("100"), new BigDecimal("10"));
        Compra compra = Compra.crear(CompraId.nuevo(), proyectoId, LocalDate.now(), "Prov", List.of(detalle));

        // Action: Receive 106 (> 5% tolerance)
        assertThatThrownBy(() -> service.registrarEntradaPorCompra(compra, Map.of(detalleId, new BigDecimal("106"))))
                .isInstanceOf(ExcesoRecepcionException.class);

        verifyNoInteractions(inventarioSnapshotService);
    }

    @Test
    void registrarSalidaPorConsumo_validPartida_shouldPublishAcEvent() {
        UUID proyectoId = UUID.randomUUID();
        UUID recursoId = UUID.randomUUID();
        UUID partidaId = UUID.randomUUID();
        BigDecimal cantidad = new BigDecimal("10");

        when(partidaValidator.existeYEstaActiva(partidaId)).thenReturn(true);
        when(partidaValidator.getPorcentajeEjecucion(partidaId)).thenReturn(50.0); // No alert

        InventarioItem itemMock = mock(InventarioItem.class);
        when(inventarioRepository.findByProyectoIdAndRecursoId(proyectoId, recursoId))
                .thenReturn(Optional.of(itemMock));
        when(itemMock.getRecursoExternalId()).thenReturn("MAT-001");

        MovimientoInventario movMock = mock(MovimientoInventario.class);
        when(movMock.getCostoTotal()).thenReturn(new BigDecimal("100"));
        when(movMock.getFechaHora()).thenReturn(LocalDateTime.now());
        when(itemMock.egresar(cantidad, "Ref")).thenReturn(movMock);

        // Action
        service.registrarSalidaPorConsumo(proyectoId, recursoId, partidaId, cantidad, "Ref");

        verify(inventarioRepository).save(itemMock);

        ArgumentCaptor<MaterialConsumed> eventCaptor = ArgumentCaptor.forClass(MaterialConsumed.class);
        verify(acPublisher).publicar(eventCaptor.capture());
        MaterialConsumed event = eventCaptor.getValue();

        assertThat(event.partidaId()).isEqualTo(partidaId);
        assertThat(event.recursoExternalId()).isEqualTo("MAT-001");
        assertThat(event.costoTotal()).isEqualTo(new BigDecimal("100"));

        verifyNoInteractions(budgetAlertPublisher);
    }

    @Test
    void registrarSalidaPorConsumo_budgetAlert_shouldPublishWarning() {
        UUID proyectoId = UUID.randomUUID();
        UUID recursoId = UUID.randomUUID();
        UUID partidaId = UUID.randomUUID();

        when(partidaValidator.existeYEstaActiva(partidaId)).thenReturn(true);
        // Simulation execution > 80%
        when(partidaValidator.getPorcentajeEjecucion(partidaId)).thenReturn(85.0);

        InventarioItem itemMock = mock(InventarioItem.class);
        when(inventarioRepository.findByProyectoIdAndRecursoId(proyectoId, recursoId))
                .thenReturn(Optional.of(itemMock));
        when(itemMock.getRecursoExternalId()).thenReturn("MAT-001");

        MovimientoInventario movMock = mock(MovimientoInventario.class);
        when(movMock.getCostoTotal()).thenReturn(new BigDecimal("100"));
        when(movMock.getFechaHora()).thenReturn(LocalDateTime.now());
        when(itemMock.egresar(any(), any())).thenReturn(movMock);

        service.registrarSalidaPorConsumo(proyectoId, recursoId, partidaId, BigDecimal.ONE, "Ref");

        // Verify alert published
        verify(budgetAlertPublisher).publicar(any(BudgetAlertEvent.class));
    }

    @Test
    void registrarSalidaPorConsumo_invalidPartida_shouldThrowException() {
        UUID partidaId = UUID.randomUUID();
        when(partidaValidator.existeYEstaActiva(partidaId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrarSalidaPorConsumo(UUID.randomUUID(), UUID.randomUUID(), partidaId,
                BigDecimal.ONE, "Ref")).isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("no existe o no está activa");

        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarAjuste_unauthorizedUser_shouldThrowSecurityException() {
        InventarioId id = InventarioId.generate();
        when(securityPort.hasAnyRole(any())).thenReturn(false);

        assertThatThrownBy(() -> service.registrarAjuste(id, BigDecimal.ONE, "Justification", "Ref"))
                .isInstanceOf(SecurityException.class);

        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarAjuste_shortJustification_shouldThrowIllegalArgumentException() {
        InventarioId id = InventarioId.generate();
        when(securityPort.hasAnyRole(any())).thenReturn(true);

        assertThatThrownBy(() -> service.registrarAjuste(id, BigDecimal.ONE, "Short", "Ref"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registrarAjuste_validFlow_shouldSaveItem() {
        InventarioId id = InventarioId.generate();
        InventarioItem itemMock = mock(InventarioItem.class);

        when(securityPort.hasAnyRole(any())).thenReturn(true);
        when(inventarioRepository.findById(id)).thenReturn(Optional.of(itemMock));

        service.registrarAjuste(id, BigDecimal.ONE, "Justification > 20 chars required here", "Ref");

        verify(itemMock).ajustar(eq(BigDecimal.ONE), anyString(), anyString());
        verify(inventarioRepository).save(itemMock);
    }
}
