package com.budgetpro.domain.logistica.transferencia.service;

import com.budgetpro.domain.logistica.bodega.model.BodegaId;
import com.budgetpro.domain.logistica.inventario.model.InventarioId;
import com.budgetpro.domain.logistica.inventario.model.InventarioItem;
import com.budgetpro.domain.logistica.inventario.model.MovimientoInventario;
import com.budgetpro.domain.logistica.inventario.model.TipoMovimientoInventario;
import com.budgetpro.domain.logistica.inventario.port.out.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.budgetpro.domain.logistica.transferencia.event.MaterialTransferredBetweenProjects;
import com.budgetpro.domain.logistica.transferencia.port.out.ExcepcionValidator;
import com.budgetpro.domain.logistica.transferencia.port.out.TransferenciaEventPublisher;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

        @Mock
        private InventarioRepository inventarioRepository;

        @Mock
        private ExcepcionValidator excepcionValidator;

        @Mock
        private TransferenciaEventPublisher eventPublisher;

        private TransferenciaService transferenciaService;

        @BeforeEach
        void setUp() {
                transferenciaService = new TransferenciaService(inventarioRepository, excepcionValidator,
                                eventPublisher);
        }

        @Test
        void transferirEntreBodegas_recalculationPMP() {
                // Setup scenarios as requested
                UUID proyectoId = UUID.randomUUID();
                BodegaId bodegaAId = BodegaId.generate(); // Origin
                BodegaId bodegaBId = BodegaId.generate(); // Destination
                InventarioId origenId = InventarioId.generate();
                InventarioId destinoId = InventarioId.generate();

                // Bodega A: 100 units @ $10 PMP
                InventarioItem origen = InventarioItem.reconstruir(origenId, proyectoId, null, "MAT-001", bodegaAId,
                                "Material Test", "General", "UNIDAD", new BigDecimal("100"), new BigDecimal("10"), null,
                                LocalDateTime.now(), 1L);

                // Bodega B: 50 units @ $12 PMP
                InventarioItem destino = InventarioItem.reconstruir(destinoId, proyectoId, null, "MAT-001", bodegaBId,
                                "Material Test", "General", "UNIDAD", new BigDecimal("50"), new BigDecimal("12"), null,
                                LocalDateTime.now(), 1L);

                when(inventarioRepository.findById(origenId)).thenReturn(Optional.of(origen));
                when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(proyectoId,
                                "MAT-001", "UNIDAD", bodegaBId)).thenReturn(Optional.of(destino));

                // Action: Transfer 50 units from A to B
                transferenciaService.transferirEntreBodegas(origenId, bodegaBId, new BigDecimal("50"),
                                "Transferencia Test");

                // Expect
                // 1. Origen: 100 - 50 = 50 units left @ $10 (PMP unchanged on exit)
                assertThat(origen.getCantidadFisica()).isEqualByComparingTo(new BigDecimal("50"));
                assertThat(origen.getCostoPromedio()).isEqualByComparingTo(new BigDecimal("10"));
                assertThat(origen.getMovimientosNuevos()).hasSize(1);
                MovimientoInventario movSalida = origen.getMovimientosNuevos().get(0);
                assertThat(movSalida.getTipo()).isEqualTo(TipoMovimientoInventario.SALIDA_TRANSFERENCIA);
                assertThat(movSalida.getCantidad()).isEqualByComparingTo(new BigDecimal("50"));

                // 2. Destino: 50 + 50 = 100 units
                // PMP Calculation:
                // Stock Value B = 50 * 12 = 600
                // Incoming Value = 50 * 10 (Source PMP) = 500
                // Total Value = 1100
                // Total Qty = 100
                // New PMP = 1100 / 100 = 11
                assertThat(destino.getCantidadFisica()).isEqualByComparingTo(new BigDecimal("100"));
                assertThat(destino.getCostoPromedio()).isEqualByComparingTo(new BigDecimal("11"));
                assertThat(destino.getMovimientosNuevos()).hasSize(1);
                MovimientoInventario movEntrada = destino.getMovimientosNuevos().get(0);
                assertThat(movEntrada.getTipo()).isEqualTo(TipoMovimientoInventario.ENTRADA_TRANSFERENCIA);
                assertThat(movEntrada.getCostoUnitario()).isEqualByComparingTo(new BigDecimal("10")); // Incoming cost

                // 3. Link check
                assertThat(movSalida.getTransferenciaId()).isNotNull();
                assertThat(movEntrada.getTransferenciaId()).isEqualTo(movSalida.getTransferenciaId());

                // 4. Verify save
                verify(inventarioRepository).save(destino);
        }

        @Test
        void transferirEntreProyectos_validFlow() {
                UUID proyectoAId = UUID.randomUUID();
                UUID proyectoBId = UUID.randomUUID();
                BodegaId bodegaAId = BodegaId.generate();
                BodegaId bodegaBId = BodegaId.generate();
                InventarioId origenId = InventarioId.generate();
                UUID excepcionId = UUID.randomUUID();

                // Origen: 100 units @ $10
                InventarioItem origen = InventarioItem.reconstruir(origenId, proyectoAId, null, "MAT-001", bodegaAId,
                                "Material Test", "General", "UNIDAD", new BigDecimal("100"), new BigDecimal("10"), null,
                                LocalDateTime.now(), 1L);

                when(inventarioRepository.findById(origenId)).thenReturn(Optional.of(origen));
                when(excepcionValidator.esExcepcionAprobada(excepcionId)).thenReturn(true);

                // Destino mock (created new for simplicity)
                when(inventarioRepository.findByProyectoIdAndRecursoExternalIdAndUnidadBaseAndBodegaId(proyectoBId,
                                "MAT-001", "UNIDAD", bodegaBId)).thenReturn(Optional.empty());

                // Action: Transfer 50
                transferenciaService.transferirEntreProyectos(origenId, bodegaBId, proyectoBId, new BigDecimal("50"),
                                excepcionId, "Ref");

                // Assert Origen
                assertThat(origen.getCantidadFisica()).isEqualByComparingTo(new BigDecimal("50"));
                assertThat(origen.getMovimientosNuevos()).hasSize(1);
                MovimientoInventario movSalida = origen.getMovimientosNuevos().get(0);
                assertThat(movSalida.getTipo()).isEqualTo(TipoMovimientoInventario.SALIDA_PRESTAMO);
                assertThat(movSalida.getCantidad()).isEqualByComparingTo(new BigDecimal("50"));
                assertThat(movSalida.getCostoTotal()).isEqualByComparingTo(new BigDecimal("500")); // 50 * 10

                // Assert Event Emission
                ArgumentCaptor<MaterialTransferredBetweenProjects> eventCaptor = ArgumentCaptor
                                .forClass(MaterialTransferredBetweenProjects.class);
                verify(eventPublisher).publicar(eventCaptor.capture());
                MaterialTransferredBetweenProjects event = eventCaptor.getValue();

                assertThat(event.proyectoOrigenId()).isEqualTo(proyectoAId);
                assertThat(event.proyectoDestinoId()).isEqualTo(proyectoBId);
                assertThat(event.recursoExternalId()).isEqualTo("MAT-001");
                assertThat(event.cantidad()).isEqualByComparingTo(new BigDecimal("50"));
                assertThat(event.valorPMP()).isEqualByComparingTo(new BigDecimal("500"));
                assertThat(event.transferenciaId().getValue()).isEqualTo(movSalida.getTransferenciaId());

                // Verify Save
                verify(inventarioRepository).save(origen);
                verify(inventarioRepository, org.mockito.Mockito.times(2)).save(any(InventarioItem.class)); // Destino
                                                                                                            // created +
                                                                                                            // Origen
                                                                                                            // updated
        }
}
