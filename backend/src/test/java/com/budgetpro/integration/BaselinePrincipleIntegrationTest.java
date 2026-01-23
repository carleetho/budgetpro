package com.budgetpro.integration;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.presupuesto.exception.PresupuestoSinCronogramaException;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.IntegrityHashService;
import com.budgetpro.domain.finanzas.presupuesto.service.PresupuestoService;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para el Principio de Baseline.
 * 
 * Verifica que el acoplamiento temporal entre Presupuesto y Cronograma
 * funcione correctamente:
 * 
 * - Cuando se aprueba un Presupuesto, el Cronograma también se congela
 * - Si no existe Cronograma, la aprobación del Presupuesto falla
 * - Si el Cronograma no puede congelarse, la aprobación del Presupuesto falla
 * - Ambas operaciones son atómicas (transaccionales)
 * 
 * **Principio de Baseline:**
 * Presupuesto (CONGELADO) + Cronograma (CONGELADO) = Baseline del Proyecto
 */
@Transactional
class BaselinePrincipleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ProgramaObraRepository programaObraRepository;

    @Autowired
    private CronogramaSnapshotRepository snapshotRepository;

    @Autowired
    private IntegrityHashService integrityHashService;

    private UUID proyectoId;
    private PresupuestoId presupuestoId;
    private ProgramaObraId programaObraId;
    private UUID approvedBy;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
        presupuestoId = PresupuestoId.nuevo();
        programaObraId = ProgramaObraId.nuevo();
        approvedBy = UUID.randomUUID();
        fechaInicio = LocalDate.of(2024, 1, 1);
        fechaFinEstimada = LocalDate.of(2024, 12, 31);
    }

    @Test
    void aprobarPresupuesto_debeCongelarCronogramaAutomaticamente() {
        // Given: Presupuesto y Cronograma creados
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");
        assertFalse(programaObra.estaCongelado(), "Cronograma no debe estar congelado inicialmente");

        // When: Aprobar el presupuesto
        CronogramaSnapshot snapshot = presupuestoService.aprobar(presupuestoId, approvedBy);

        // Then: Ambos deben estar congelados
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        assertTrue(presupuestoAprobado.isAprobado(), "Presupuesto debe estar aprobado");

        ProgramaObra programaObraCongelado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();
        assertTrue(programaObraCongelado.estaCongelado(), "Cronograma debe estar congelado");
        assertEquals(approvedBy, programaObraCongelado.getCongeladoBy(), 
                "Cronograma debe tener el mismo usuario aprobador");

        // Verificar que se generó el snapshot
        assertNotNull(snapshot, "Debe generarse un snapshot");
        assertEquals(presupuestoId, snapshot.getPresupuestoId(), 
                "Snapshot debe estar asociado al presupuesto");
        assertEquals(programaObraId, snapshot.getProgramaObraId(), 
                "Snapshot debe estar asociado al programa de obra");

        // Verificar que el snapshot está persistido
        Optional<CronogramaSnapshot> snapshotPersistido = snapshotRepository.findByProgramaObraId(programaObraId);
        assertTrue(snapshotPersistido.isPresent(), "Snapshot debe estar persistido");
    }

    @Test
    void aprobarPresupuesto_debeBloquearSiNoExisteCronograma() {
        // Given: Solo Presupuesto, sin Cronograma
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        // When/Then: Intentar aprobar debe fallar
        PresupuestoSinCronogramaException exception = assertThrows(
                PresupuestoSinCronogramaException.class,
                () -> presupuestoService.aprobar(presupuestoId, approvedBy),
                "Debe lanzar PresupuestoSinCronogramaException si no existe cronograma"
        );

        assertEquals(presupuestoId, exception.getPresupuestoId(), 
                "Excepción debe incluir el ID del presupuesto");
        assertEquals(proyectoId, exception.getProyectoId(), 
                "Excepción debe incluir el ID del proyecto");
        assertTrue(exception.getMessage().contains("cronograma"), 
                "Mensaje debe mencionar cronograma");

        // Verificar que el presupuesto NO se aprobó
        Presupuesto presupuestoNoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        assertFalse(presupuestoNoAprobado.isAprobado(), 
                "Presupuesto NO debe estar aprobado si no existe cronograma");
    }

    @Test
    void aprobarPresupuesto_debeFallarSiCronogramaNoTieneFechas() {
        // Given: Presupuesto y Cronograma sin fechas
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObraSinFechas = ProgramaObra.crear(
                programaObraId, proyectoId, null, null
        );
        programaObraRepository.save(programaObraSinFechas);

        // When/Then: Intentar aprobar debe fallar
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> presupuestoService.aprobar(presupuestoId, approvedBy),
                "Debe lanzar IllegalStateException si el cronograma no tiene fechas"
        );

        assertTrue(exception.getMessage().contains("fecha") || 
                   exception.getMessage().contains("congelar"), 
                "Mensaje debe mencionar fechas o congelamiento");

        // Verificar que el presupuesto NO se aprobó (rollback transaccional)
        Presupuesto presupuestoNoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        assertFalse(presupuestoNoAprobado.isAprobado(), 
                "Presupuesto NO debe estar aprobado si el cronograma no puede congelarse");
    }

    @Test
    void aprobarPresupuesto_debeFallarSiPresupuestoYaEstaAprobado() {
        // Given: Presupuesto ya aprobado
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuesto.aprobar(approvedBy, integrityHashService);
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // When/Then: Intentar aprobar nuevamente debe fallar
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> presupuestoService.aprobar(presupuestoId, approvedBy),
                "Debe lanzar IllegalStateException si el presupuesto ya está aprobado"
        );

        assertTrue(exception.getMessage().contains("aprobado") || 
                   exception.getMessage().contains("congelado"), 
                "Mensaje debe mencionar que ya está aprobado");
    }

    @Test
    void aprobarPresupuesto_debeSerAtomico() {
        // Given: Presupuesto y Cronograma válidos
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // When: Aprobar el presupuesto
        presupuestoService.aprobar(presupuestoId, approvedBy);

        // Then: Ambos deben estar en estado congelado
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        ProgramaObra programaObraCongelado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertTrue(presupuestoAprobado.isAprobado(), 
                "Presupuesto debe estar aprobado");
        assertTrue(programaObraCongelado.estaCongelado(), 
                "Cronograma debe estar congelado");

        // Verificar que ambos tienen el mismo usuario aprobador
        assertEquals(approvedBy, presupuestoAprobado.getIntegrityHashGeneratedBy(), 
                "Presupuesto debe tener el usuario aprobador correcto");
        assertEquals(approvedBy, programaObraCongelado.getCongeladoBy(), 
                "Cronograma debe tener el usuario aprobador correcto");
    }
}
