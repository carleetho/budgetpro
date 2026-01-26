package com.budgetpro.domain.logistica.compra.service;

import com.budgetpro.domain.finanzas.exception.SaldoInsuficienteException;
import com.budgetpro.domain.finanzas.model.Billetera;
import com.budgetpro.domain.finanzas.model.BilleteraId;
import com.budgetpro.domain.finanzas.partida.model.Partida;
import com.budgetpro.domain.finanzas.partida.model.PartidaId;
import com.budgetpro.domain.finanzas.partida.port.out.PartidaRepository;
import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityAuditLog;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.shared.port.out.ObservabilityPort;
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraDetalleId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.compra.model.RelacionContractual;
import com.budgetpro.domain.logistica.compra.model.RubroInsumo;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcesarCompraServiceTest {

        @Mock
        private PartidaRepository partidaRepository;

        @Mock
        private PresupuestoRepository presupuestoRepository;

        @Mock
        private IntegrityHashService integrityHashService;

        @Mock
        private IntegrityAuditLog auditLog;

        @Mock
        private GestionInventarioService gestionInventarioService;

        @Mock
        private ObservabilityPort observability;

        private ProcesarCompraService service;

        @BeforeEach
        void setUp() {
                service = new ProcesarCompraService(partidaRepository, presupuestoRepository, integrityHashService,
                                auditLog, gestionInventarioService, observability);
        }

        @Test
        void procesar_conSaldoSuficiente_debeReservarYaprobar() {
                Partida partida = crearPartidaConSaldo(new BigDecimal("1000.00"));
                CompraDetalle detalle = crearDetalle(partida.getId().getValue(), new BigDecimal("2"),
                                new BigDecimal("100.00"));
                Compra compra = crearCompra(List.of(detalle));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));
                Presupuesto presupuesto = crearPresupuestoNoAprobado(compra.getProyectoId());

                when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId())))
                                .thenReturn(Optional.of(partida));
                when(presupuestoRepository.findByProyectoId(compra.getProyectoId()))
                                .thenReturn(Optional.of(presupuesto));

                service.procesar(compra, billetera);

                assertEquals(new BigDecimal("200.00"), partida.getCompromisosPendientes());
                assertEquals(new BigDecimal("800.00"), partida.getSaldoDisponible());
                assertEquals(true, compra.isAprobada());
                verify(partidaRepository).save(partida);
                verify(gestionInventarioService).registrarEntradaPorCompra(eq(compra), anyMap());
        }

        @Test
        void procesar_conSaldoInsuficiente_debeLanzarExcepcion() {
                Partida partida = crearPartidaConSaldo(new BigDecimal("100.00"));
                CompraDetalle detalle = crearDetalle(partida.getId().getValue(), new BigDecimal("2"),
                                new BigDecimal("100.00"));
                Compra compra = crearCompra(List.of(detalle));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));
                Presupuesto presupuesto = crearPresupuestoNoAprobado(compra.getProyectoId());

                when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId())))
                                .thenReturn(Optional.of(partida));
                when(presupuestoRepository.findByProyectoId(compra.getProyectoId()))
                                .thenReturn(Optional.of(presupuesto));

                assertThrows(SaldoInsuficienteException.class, () -> service.procesar(compra, billetera));
                assertEquals(BigDecimal.ZERO, partida.getCompromisosPendientes());
                assertFalse(compra.isAprobada());
                verify(partidaRepository, never()).save(any());
                verify(gestionInventarioService, never()).registrarEntradaPorCompra(any(), any());
        }

        @Test
        void procesar_partidaNoEncontrada_debeLanzarExcepcion() {
                CompraDetalle detalle = crearDetalle(UUID.randomUUID(), new BigDecimal("1"), new BigDecimal("50.00"));
                Compra compra = crearCompra(List.of(detalle));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));
                Presupuesto presupuesto = crearPresupuestoNoAprobado(compra.getProyectoId());

                when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId()))).thenReturn(Optional.empty());
                when(presupuestoRepository.findByProyectoId(compra.getProyectoId()))
                                .thenReturn(Optional.of(presupuesto));

                assertThrows(IllegalArgumentException.class, () -> service.procesar(compra, billetera));
        }

        @Test
        void procesar_multipleDetalles_unSaldoInsuficiente_debeRechazarTodo() {
                Partida partidaOk = crearPartidaConSaldo(new BigDecimal("500.00"));
                Partida partidaBad = crearPartidaConSaldo(new BigDecimal("50.00"));

                CompraDetalle detalleOk = crearDetalle(partidaOk.getId().getValue(), new BigDecimal("1"),
                                new BigDecimal("100.00"));
                CompraDetalle detalleBad = crearDetalle(partidaBad.getId().getValue(), new BigDecimal("1"),
                                new BigDecimal("100.00"));
                Compra compra = crearCompra(List.of(detalleOk, detalleBad));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("1000.00"));
                Presupuesto presupuesto = crearPresupuestoNoAprobado(compra.getProyectoId());

                when(partidaRepository.findById(PartidaId.from(detalleOk.getPartidaId())))
                                .thenReturn(Optional.of(partidaOk));
                when(partidaRepository.findById(PartidaId.from(detalleBad.getPartidaId())))
                                .thenReturn(Optional.of(partidaBad));
                when(presupuestoRepository.findByProyectoId(compra.getProyectoId()))
                                .thenReturn(Optional.of(presupuesto));

                assertThrows(SaldoInsuficienteException.class, () -> service.procesar(compra, billetera));
                assertEquals(BigDecimal.ZERO, partidaOk.getCompromisosPendientes());
                assertEquals(BigDecimal.ZERO, partidaBad.getCompromisosPendientes());
                verify(partidaRepository, never()).save(any());
        }

        private Partida crearPartidaConSaldo(BigDecimal presupuestoAsignado) {
                Partida partida = Partida.crearRaiz(PartidaId.nuevo(), UUID.randomUUID(), "01.01", "Partida test",
                                "UND", BigDecimal.ONE);
                partida.actualizarPresupuestoAsignado(presupuestoAsignado);
                partida.actualizarGastosReales(BigDecimal.ZERO);
                partida.actualizarCompromisosPendientes(BigDecimal.ZERO);
                return partida;
        }

        private CompraDetalle crearDetalle(UUID partidaId, BigDecimal cantidad, BigDecimal precioUnitario) {
                return CompraDetalle.crear(CompraDetalleId.nuevo(), "MAT-001", // recursoExternalId
                                "Cemento Portland", // recursoNombre
                                "BOL", // unidad (Authority by PO)
                                partidaId, NaturalezaGasto.DIRECTO_PARTIDA, RelacionContractual.CONTRACTUAL,
                                RubroInsumo.MATERIAL_CONSTRUCCION, cantidad, precioUnitario);
        }

        private Compra crearCompra(List<CompraDetalle> detalles) {
                return Compra.crear(CompraId.nuevo(), UUID.randomUUID(), LocalDate.now(), "Proveedor test", detalles);
        }

        private Billetera crearBilleteraConSaldo(BigDecimal saldo) {
                Billetera billetera = Billetera.crear(BilleteraId.generate(), UUID.randomUUID());
                billetera.ingresar(saldo, "Ingreso test", "http://evidencia/ok");
                return billetera;
        }

        private Presupuesto crearPresupuestoNoAprobado(UUID proyectoId) {
                return Presupuesto.crear(PresupuestoId.from(UUID.randomUUID()), proyectoId, "Presupuesto Test");
        }

        private Presupuesto crearPresupuestoAprobado(UUID proyectoId) {
                String approvalHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
                String executionHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
                return Presupuesto.reconstruir(PresupuestoId.from(UUID.randomUUID()), proyectoId,
                                "Presupuesto Aprobado", EstadoPresupuesto.CONGELADO, true, 1L, approvalHash,
                                executionHash, java.time.LocalDateTime.now(), UUID.randomUUID(), "SHA-256-v1");
        }

        @Test
        void procesar_conPresupuestoAprobadoYHashValido_debeProcesarYActualizarHashEjecucion() {
                Partida partida = crearPartidaConSaldo(new BigDecimal("1000.00"));
                CompraDetalle detalle = crearDetalle(partida.getId().getValue(), new BigDecimal("2"),
                                new BigDecimal("100.00"));
                Compra compra = crearCompra(List.of(detalle));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));
                Presupuesto presupuesto = crearPresupuestoAprobado(compra.getProyectoId());

                when(partidaRepository.findById(PartidaId.from(detalle.getPartidaId())))
                                .thenReturn(Optional.of(partida));
                when(presupuestoRepository.findByProyectoId(compra.getProyectoId()))
                                .thenReturn(Optional.of(presupuesto));
                // Mock hash service para que retorne el mismo hash que el presupuesto tiene
                // almacenado
                when(integrityHashService.calculateApprovalHash(presupuesto))
                                .thenReturn(presupuesto.getIntegrityHashApproval());
                when(integrityHashService.calculateExecutionHash(presupuesto)).thenReturn(
                                "new_execution_hash_123456789012345678901234567890123456789012345678901234567890");

                service.procesar(compra, billetera);

                assertEquals(true, compra.isAprobada());
                verify(presupuestoRepository).save(presupuesto);
                verify(auditLog).logHashValidation(any(Presupuesto.class), any(), eq(true), any(String.class));
        }

        @Test
        void procesar_conPresupuestoAprobadoYHashInvalido_debeRechazar() {
                Partida partida = crearPartidaConSaldo(new BigDecimal("1000.00"));
                CompraDetalle detalle = crearDetalle(partida.getId().getValue(), new BigDecimal("2"),
                                new BigDecimal("100.00"));
                Compra compra = crearCompra(List.of(detalle));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));
                Presupuesto presupuesto = crearPresupuestoAprobado(compra.getProyectoId());

                when(presupuestoRepository.findByProyectoId(compra.getProyectoId()))
                                .thenReturn(Optional.of(presupuesto));

                // Simular violación de integridad: el hash calculado no coincide con el
                // almacenado
                // validarIntegridad() llama a calculateApprovalHash() y compara con el hash
                // almacenado
                when(integrityHashService.calculateApprovalHash(presupuesto)).thenReturn(
                                "tampered_hash_123456789012345678901234567890123456789012345678901234567890");

                assertThrows(BudgetIntegrityViolationException.class, () -> service.procesar(compra, billetera));
                assertFalse(compra.isAprobada());
                verify(partidaRepository, never()).save(any());
                verify(auditLog).logIntegrityViolation(any(BudgetIntegrityViolationException.class), any());
        }

        @Test
        void procesar_conPresupuestoNoEncontrado_debeLanzarExcepcion() {
                CompraDetalle detalle = crearDetalle(UUID.randomUUID(), new BigDecimal("1"), new BigDecimal("50.00"));
                Compra compra = crearCompra(List.of(detalle));
                Billetera billetera = crearBilleteraConSaldo(new BigDecimal("500.00"));

                when(presupuestoRepository.findByProyectoId(compra.getProyectoId())).thenReturn(Optional.empty());

                assertThrows(IllegalArgumentException.class, () -> service.procesar(compra, billetera));
                verify(partidaRepository, never()).save(any());
        }
}
