package com.budgetpro.domain.finanzas.presupuesto;

import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
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
import com.budgetpro.domain.logistica.compra.model.Compra;
import com.budgetpro.domain.logistica.compra.model.CompraDetalle;
import com.budgetpro.domain.logistica.compra.model.CompraDetalleId;
import com.budgetpro.domain.logistica.compra.model.CompraId;
import com.budgetpro.domain.logistica.compra.model.NaturalezaGasto;
import com.budgetpro.domain.logistica.compra.model.RelacionContractual;
import com.budgetpro.domain.logistica.compra.model.RubroInsumo;
import com.budgetpro.domain.logistica.compra.service.ProcesarCompraService;
import com.budgetpro.domain.logistica.inventario.service.GestionInventarioService;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import com.budgetpro.infrastructure.persistence.entity.PartidaEntity;
import com.budgetpro.infrastructure.persistence.entity.PresupuestoEntity;
import com.budgetpro.infrastructure.persistence.entity.ProyectoEntity;
import com.budgetpro.infrastructure.persistence.entity.RecursoEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuEntity;
import com.budgetpro.infrastructure.persistence.entity.apu.ApuInsumoEntity;
import com.budgetpro.infrastructure.persistence.repository.PartidaJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.PresupuestoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.ProyectoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuJpaRepository;
import com.budgetpro.infrastructure.persistence.repository.apu.ApuInsumoJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests de integración para el flujo completo de integridad criptográfica del presupuesto.
 * 
 * Verifica:
 * - Generación de hashes al aprobar presupuesto
 * - Validación de integridad en aprobación de compras
 * - Detección de tampering en base de datos
 * - Actualización de hash de ejecución después de transacciones
 * - Integración con reglas VD-02 y CD-04
 * - Generación de audit trail
 */
class BudgetIntegrityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AprobarPresupuestoUseCase aprobarPresupuestoUseCase;

    @Autowired
    private ProcesarCompraService procesarCompraService;

    @Autowired
    private IntegrityHashService integrityHashService;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private PresupuestoJpaRepository presupuestoJpaRepository;

    @Autowired
    private PartidaJpaRepository partidaJpaRepository;

    @Autowired
    private ProyectoJpaRepository proyectoJpaRepository;

    @Autowired
    private com.budgetpro.infrastructure.persistence.repository.RecursoJpaRepository recursoJpaRepository;

    @Autowired
    private com.budgetpro.infrastructure.persistence.repository.apu.ApuJpaRepository apuJpaRepository;

    @Autowired
    private com.budgetpro.infrastructure.persistence.repository.apu.ApuInsumoJpaRepository apuInsumoJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private IntegrityAuditLog auditLog;

    @MockBean
    private GestionInventarioService gestionInventarioService;

    private UUID proyectoId;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
    }

    @Test
    void aprobarPresupuesto_debeGenerarHashYBloquearModificaciones() {
        // Given: Presupuesto con partidas
        PresupuestoEntity presupuestoEntity = crearPresupuestoConPartidas();
        PresupuestoId presupuestoId = PresupuestoId.from(presupuestoEntity.getId());

        // When: Aprobar presupuesto
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        // Then: Hash generado
        entityManager.clear();
        Presupuesto approved = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        
        assertNotNull(approved.getIntegrityHashApproval(), "Hash de aprobación debe ser generado");
        assertNotNull(approved.getIntegrityHashExecution(), "Hash de ejecución debe ser generado");
        assertEquals("SHA-256-v1", approved.getIntegrityHashAlgorithm());
        assertTrue(approved.isAprobado(), "Presupuesto debe estar aprobado");
        assertEquals(EstadoPresupuesto.CONGELADO, approved.getEstado());

        // Verificar que no se puede modificar después de aprobación
        assertThrows(BudgetIntegrityViolationException.class, () -> {
            approved.actualizarNombre("Modified Name");
        }, "No se debe permitir modificar nombre después de aprobación");

        // Verificar que audit log fue llamado
        verify(auditLog).logHashGeneration(any(Presupuesto.class));
    }

    @Test
    void aprobarCompra_conHashValido_debePermitir() {
        // Given: Presupuesto aprobado con hash válido
        PresupuestoEntity presupuestoEntity = crearPresupuestoConPartidas();
        PresupuestoId presupuestoId = PresupuestoId.from(presupuestoEntity.getId());
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        entityManager.clear();
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        String initialExecutionHash = presupuesto.getIntegrityHashExecution();

        // Crear compra
        PartidaEntity partida = partidaJpaRepository.findByPresupuestoId(presupuestoId.getValue())
                .stream()
                .findFirst()
                .orElseThrow();
        Compra compra = crearCompra(partida.getId(), new BigDecimal("3"), new BigDecimal("100.00"));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("10000.00"));

        // When: Aprobar compra
        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        tx.executeWithoutResult(status -> procesarCompraService.procesar(compra, billetera));

        // Then: Compra aprobada exitosamente
        assertTrue(compra.isAprobada(), "Compra debe estar aprobada");

        // Hash de ejecución actualizado
        entityManager.clear();
        Presupuesto updated = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        assertNotEquals(initialExecutionHash, updated.getIntegrityHashExecution(),
                "Hash de ejecución debe cambiar después de transacción financiera");
        assertEquals(presupuesto.getIntegrityHashApproval(), updated.getIntegrityHashApproval(),
                "Hash de aprobación debe permanecer inmutable");

        // Verificar que audit log fue llamado
        verify(auditLog).logHashValidation(any(Presupuesto.class), any(), eq(true), any(String.class));
    }

    @Test
    void aprobarCompra_conHashInvalido_debeRechazar() {
        // Given: Presupuesto aprobado con hash válido
        PresupuestoEntity presupuestoEntity = crearPresupuestoConPartidas();
        PresupuestoId presupuestoId = PresupuestoId.from(presupuestoEntity.getId());
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        entityManager.clear();
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();

        // Simular tampering en base de datos (cambiar metrado directamente)
        PartidaEntity partida = partidaJpaRepository.findByPresupuestoId(presupuestoId.getValue())
                .stream()
                .findFirst()
                .orElseThrow();
        
        jdbcTemplate.update(
                "UPDATE partida SET metrado_vigente = ? WHERE id = ?",
                999.00, partida.getId()
        );

        entityManager.clear();

        // Crear compra
        Compra compra = crearCompra(partida.getId(), new BigDecimal("3"), new BigDecimal("100.00"));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("10000.00"));

        // When/Then: Aprobación de compra debe fallar
        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        assertThrows(BudgetIntegrityViolationException.class, () -> {
            tx.executeWithoutResult(status -> procesarCompraService.procesar(compra, billetera));
        }, "Debe rechazar compra cuando se detecta tampering");

        // Verificar que compra no fue aprobada
        assertFalse(compra.isAprobada(), "Compra no debe estar aprobada");

        // Verificar que audit log registró la violación
        verify(auditLog).logIntegrityViolation(any(BudgetIntegrityViolationException.class), any());
    }

    @Test
    void integrityWorkflow_conMultiplesTransacciones_debeActualizarHashEjecucion() {
        // Given: Presupuesto aprobado
        PresupuestoEntity presupuestoEntity = crearPresupuestoConPartidas();
        PresupuestoId presupuestoId = PresupuestoId.from(presupuestoEntity.getId());
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        entityManager.clear();
        Presupuesto presupuesto = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        String initialExecutionHash = presupuesto.getIntegrityHashExecution();
        String initialApprovalHash = presupuesto.getIntegrityHashApproval();

        PartidaEntity partida = partidaJpaRepository.findByPresupuestoId(presupuestoId.getValue())
                .stream()
                .findFirst()
                .orElseThrow();

        // When: Múltiples compras aprobadas
        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));

        // Primera compra
        Compra compra1 = crearCompra(partida.getId(), new BigDecimal("2"), new BigDecimal("100.00"));
        Billetera billetera1 = crearBilleteraConSaldo(new BigDecimal("10000.00"));
        tx.executeWithoutResult(status -> procesarCompraService.procesar(compra1, billetera1));

        entityManager.clear();
        Presupuesto after1 = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        String executionHash1 = after1.getIntegrityHashExecution();

        // Segunda compra
        Compra compra2 = crearCompra(partida.getId(), new BigDecimal("3"), new BigDecimal("150.00"));
        Billetera billetera2 = crearBilleteraConSaldo(new BigDecimal("10000.00"));
        tx.executeWithoutResult(status -> procesarCompraService.procesar(compra2, billetera2));

        entityManager.clear();
        Presupuesto after2 = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        String executionHash2 = after2.getIntegrityHashExecution();

        // Then: Hash de ejecución cambia, hash de aprobación permanece igual
        assertNotEquals(initialExecutionHash, executionHash1,
                "Hash de ejecución debe cambiar después de primera compra");
        assertNotEquals(executionHash1, executionHash2,
                "Hash de ejecución debe cambiar después de segunda compra");
        assertEquals(initialApprovalHash, after2.getIntegrityHashApproval(),
                "Hash de aprobación debe permanecer inmutable");
    }

    @Test
    void aprobarCompra_conSaldoInsuficiente_debeRechazarAntesDeValidarIntegridad() {
        // Given: Presupuesto aprobado
        PresupuestoEntity presupuestoEntity = crearPresupuestoConPartidas();
        PresupuestoId presupuestoId = PresupuestoId.from(presupuestoEntity.getId());
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        entityManager.clear();

        // Crear compra con saldo insuficiente en partida
        // Partida tiene metrado 10.00 * precio 100.00 = 1000.00 de presupuesto asignado
        // Intentar compra de 15 unidades * 100.00 = 1500.00 (excede el presupuesto)
        PartidaEntity partida = partidaJpaRepository.findByPresupuestoId(presupuestoId.getValue())
                .stream()
                .findFirst()
                .orElseThrow();
        
        Compra compra = crearCompra(partida.getId(), new BigDecimal("15"), new BigDecimal("100.00"));
        Billetera billetera = crearBilleteraConSaldo(new BigDecimal("10000.00"));

        // When/Then: Debe fallar por saldo insuficiente (antes de validar integridad)
        TransactionTemplate tx = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        assertThrows(SaldoInsuficienteException.class, () -> {
            tx.executeWithoutResult(status -> procesarCompraService.procesar(compra, billetera));
        }, "Debe rechazar compra por saldo insuficiente en partida");

        assertFalse(compra.isAprobada(), "Compra no debe estar aprobada");
    }

    @Test
    void aprobarPresupuesto_conPartidasSinAPU_debeRechazar() {
        // Given: Presupuesto con partidas sin APU
        PresupuestoEntity presupuestoEntity = crearPresupuestoSinAPU();
        PresupuestoId presupuestoId = PresupuestoId.from(presupuestoEntity.getId());

        // When/Then: Aprobación debe fallar
        assertThrows(Exception.class, () -> {
            aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());
        }, "No se debe aprobar presupuesto con partidas sin APU");
    }

    // Helper methods

    private PresupuestoEntity crearPresupuestoConPartidas() {
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoId);
        proyecto.setNombre("Proyecto Test");
        proyecto.setUbicacion("Lima");
        proyecto.setEstado(com.budgetpro.domain.proyecto.model.EstadoProyecto.ACTIVO);
        proyecto.setMoneda("USD");
        proyecto.setPresupuestoTotal(new BigDecimal("100000.00"));
        proyectoJpaRepository.save(proyecto);

        PresupuestoEntity presupuesto = new PresupuestoEntity();
        presupuesto.setId(UUID.randomUUID());
        presupuesto.setProyectoId(proyectoId);
        presupuesto.setProyecto(proyecto);
        presupuesto.setNombre("Presupuesto Test");
        presupuesto.setEstado(EstadoPresupuesto.BORRADOR);
        presupuesto.setEsLineaBase(Boolean.TRUE);
        presupuesto.setEsContractual(Boolean.FALSE);
        presupuesto = presupuestoJpaRepository.save(presupuesto);

        // Crear partida (requerido para aprobación)
        PartidaEntity partida = new PartidaEntity();
        partida.setId(UUID.randomUUID());
        partida.setPresupuesto(presupuesto);
        partida.setPadre(null);
        partida.setCodigo("01.01");
        partida.setItem("01.01");
        partida.setDescripcion("Partida Test");
        partida.setUnidad("UND");
        partida.setMetradoOriginal(new BigDecimal("10.00"));
        partida.setMetradoVigente(new BigDecimal("10.00"));
        partida.setPrecioUnitario(new BigDecimal("100.00"));
        partida.setGastosReales(BigDecimal.ZERO);
        partida.setCompromisosPendientes(BigDecimal.ZERO);
        partida.setNivel(1);
        partida = partidaJpaRepository.save(partida);

        // Crear recurso para el APU
        RecursoEntity recurso = new RecursoEntity(
                UUID.randomUUID(),
                "CEMENTO PORTLAND",
                "CEMENTO PORTLAND",
                com.budgetpro.domain.shared.model.TipoRecurso.MATERIAL,
                "BOLSA",
                null,
                com.budgetpro.domain.recurso.model.EstadoRecurso.ACTIVO,
                UUID.randomUUID()
        );
        recurso = recursoJpaRepository.save(recurso);

        // Crear APU para la partida (requerido para aprobación)
        ApuEntity apu = new ApuEntity(
                UUID.randomUUID(),
                partida,
                null, // Sin rendimiento
                "UND",
                null // version = null para nueva entidad
        );
        apu = apuJpaRepository.save(apu);

        // Agregar insumo al APU
        BigDecimal cantidad = new BigDecimal("10.0");
        BigDecimal precioUnitario = new BigDecimal("1.0");
        BigDecimal subtotal = cantidad.multiply(precioUnitario);
        ApuInsumoEntity insumo = new ApuInsumoEntity(
                UUID.randomUUID(),
                apu,
                recurso,
                cantidad,
                precioUnitario,
                subtotal,
                null // version = null para nueva entidad
        );
        apuInsumoJpaRepository.save(insumo);

        return presupuesto;
    }

    private PresupuestoEntity crearPresupuestoSinAPU() {
        ProyectoEntity proyecto = new ProyectoEntity();
        proyecto.setId(proyectoId);
        proyecto.setNombre("Proyecto Test");
        proyecto.setUbicacion("Lima");
        proyecto.setEstado(com.budgetpro.domain.proyecto.model.EstadoProyecto.ACTIVO);
        proyecto.setMoneda("USD");
        proyecto.setPresupuestoTotal(new BigDecimal("100000.00"));
        proyectoJpaRepository.save(proyecto);

        PresupuestoEntity presupuesto = new PresupuestoEntity();
        presupuesto.setId(UUID.randomUUID());
        presupuesto.setProyectoId(proyectoId);
        presupuesto.setProyecto(proyecto);
        presupuesto.setNombre("Presupuesto Sin APU");
        presupuesto.setEstado(EstadoPresupuesto.BORRADOR);
        presupuesto.setEsLineaBase(Boolean.TRUE);
        presupuesto.setEsContractual(Boolean.FALSE);
        presupuesto = presupuestoJpaRepository.save(presupuesto);

        // Crear partida SIN APU (no puede aprobarse)
        PartidaEntity partida = new PartidaEntity();
        partida.setId(UUID.randomUUID());
        partida.setPresupuesto(presupuesto);
        partida.setPadre(null);
        partida.setCodigo("01.01");
        partida.setItem("01.01");
        partida.setDescripcion("Partida Sin APU");
        partida.setUnidad("UND");
        partida.setMetradoOriginal(new BigDecimal("10.00"));
        partida.setMetradoVigente(new BigDecimal("10.00"));
        partida.setPrecioUnitario(new BigDecimal("100.00"));
        partida.setGastosReales(BigDecimal.ZERO);
        partida.setCompromisosPendientes(BigDecimal.ZERO);
        partida.setNivel(1);
        // Sin APU - esto causará que la aprobación falle
        partidaJpaRepository.save(partida);

        return presupuesto;
    }

    private Compra crearCompra(UUID partidaId, BigDecimal cantidad, BigDecimal precioUnitario) {
        CompraDetalle detalle = CompraDetalle.crear(
                CompraDetalleId.nuevo(),
                "MAT-001",
                "Cemento Portland",
                "BOL", // unidad (Authority by PO)
                partidaId,
                NaturalezaGasto.DIRECTO_PARTIDA,
                RelacionContractual.CONTRACTUAL,
                RubroInsumo.MATERIAL_CONSTRUCCION,
                cantidad,
                precioUnitario
        );
        return Compra.crear(
                CompraId.nuevo(),
                proyectoId,
                LocalDate.now(),
                "Proveedor Test",
                List.of(detalle)
        );
    }

    private Billetera crearBilleteraConSaldo(BigDecimal saldo) {
        Billetera billetera = Billetera.crear(BilleteraId.generate(), proyectoId);
        billetera.ingresar(saldo, "Ingreso Test", "http://evidencia/ok");
        return billetera;
    }
}
