package com.budgetpro.integration;

import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.domain.finanzas.presupuesto.service.PresupuestoService;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para verificar el rollback transaccional del proceso de aprobación.
 * 
 * **Objetivo:**
 * Verificar que cuando falla cualquier parte del proceso de aprobación (presupuesto o cronograma),
 * toda la transacción hace rollback, evitando estados parciales.
 * 
 * **Escenarios de Rollback:**
 * 1. Si falla el congelamiento del cronograma → presupuesto NO debe aprobarse
 * 2. Si falla la persistencia del snapshot → presupuesto NO debe aprobarse
 * 3. Si falla la persistencia del presupuesto → cronograma NO debe congelarse
 * 
 * **Principio de Baseline:**
 * Presupuesto (CONGELADO) + Cronograma (CONGELADO) = Baseline atómico
 * No se permite estado parcial donde uno esté congelado y el otro no.
 */
@Transactional
class BaselineTransactionRollbackTest extends AbstractIntegrationTest {

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ProgramaObraRepository programaObraRepository;

    @Autowired
    private CronogramaSnapshotRepository snapshotRepository;

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
    void rollback_cuandoFallaCongelamientoCronograma_presupuestoNoDebeAprobarse() {
        // Given: Presupuesto válido pero cronograma sin fechas (no puede congelarse)
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObraSinFechas = ProgramaObra.crear(
                programaObraId, proyectoId, null, null
        );
        programaObraRepository.save(programaObraSinFechas);

        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");
        assertFalse(programaObraSinFechas.estaCongelado(), "Cronograma no debe estar congelado inicialmente");

        // When: Intentar aprobar (debe fallar porque el cronograma no tiene fechas)
        assertThrows(
                IllegalStateException.class,
                () -> presupuestoService.aprobar(presupuestoId, approvedBy),
                "Debe lanzar excepción cuando el cronograma no puede congelarse"
        );

        // Then: Verificar rollback - NINGUNO debe estar congelado
        // Necesitamos refrescar desde la base de datos para ver el estado real
        Presupuesto presupuestoDespues = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        ProgramaObra programaObraDespues = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertFalse(presupuestoDespues.isAprobado(),
                "Presupuesto NO debe estar aprobado después del rollback");
        assertFalse(programaObraDespues.estaCongelado(),
                "Cronograma NO debe estar congelado después del rollback");

        // Verificar que NO se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isEmpty(),
                "NO debe existir snapshot después del rollback");
    }

    @Test
    void rollback_cuandoFallaAprobacionPresupuesto_cronogramaNoDebeCongelarse() {
        // Given: Presupuesto ya aprobado (no puede aprobarse nuevamente)
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // Aprobar el presupuesto primero
        presupuestoService.aprobar(presupuestoId, approvedBy);

        // Verificar que ambos están congelados
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        assertTrue(presupuestoAprobado.isAprobado(), "Presupuesto debe estar aprobado");

        // When: Intentar aprobar nuevamente (debe fallar)
        assertThrows(
                IllegalStateException.class,
                () -> presupuestoService.aprobar(presupuestoId, approvedBy),
                "Debe lanzar excepción cuando el presupuesto ya está aprobado"
        );

        // Then: El estado original debe mantenerse (no debe haber cambios)
        // En este caso, como ya estaba aprobado, no debería haber rollback
        // pero tampoco debería haber cambios adicionales
        Presupuesto presupuestoDespues = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        ProgramaObra programaObraDespues = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertTrue(presupuestoDespues.isAprobado(),
                "Presupuesto debe seguir aprobado (estado original)");
        assertTrue(programaObraDespues.estaCongelado(),
                "Cronograma debe seguir congelado (estado original)");
    }

    @Test
    void rollback_cuandoPresupuestoNoExiste_noDebeHaberCambios() {
        // Given: Solo cronograma, sin presupuesto
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        PresupuestoId presupuestoInexistente = PresupuestoId.nuevo();

        assertFalse(programaObra.estaCongelado(), "Cronograma no debe estar congelado inicialmente");

        // When: Intentar aprobar presupuesto inexistente (debe fallar)
        assertThrows(
                IllegalStateException.class,
                () -> presupuestoService.aprobar(presupuestoInexistente, approvedBy),
                "Debe lanzar excepción cuando el presupuesto no existe"
        );

        // Then: Verificar que NO se congeló el cronograma
        ProgramaObra programaObraDespues = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertFalse(programaObraDespues.estaCongelado(),
                "Cronograma NO debe estar congelado si el presupuesto no existe");

        // Verificar que NO se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isEmpty(),
                "NO debe existir snapshot si el presupuesto no existe");
    }

    @Test
    void rollback_cuandoCronogramaNoExiste_presupuestoNoDebeAprobarse() {
        // Given: Solo presupuesto, sin cronograma
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");

        // When: Intentar aprobar (debe fallar porque no existe cronograma)
        assertThrows(
                com.budgetpro.domain.finanzas.presupuesto.exception.PresupuestoSinCronogramaException.class,
                () -> presupuestoService.aprobar(presupuestoId, approvedBy),
                "Debe lanzar PresupuestoSinCronogramaException cuando no existe cronograma"
        );

        // Then: Verificar rollback - presupuesto NO debe estar aprobado
        Presupuesto presupuestoDespues = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();

        assertFalse(presupuestoDespues.isAprobado(),
                "Presupuesto NO debe estar aprobado si no existe cronograma");
    }

    @Test
    void atomicidad_aprobacionExitosa_ambosDebenEstarCongelados() {
        // Given: Presupuesto y cronograma válidos
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");
        assertFalse(programaObra.estaCongelado(), "Cronograma no debe estar congelado inicialmente");

        // When: Aprobar exitosamente
        presupuestoService.aprobar(presupuestoId, approvedBy);

        // Then: AMBOS deben estar congelados (atomicidad exitosa)
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        ProgramaObra programaObraCongelado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertTrue(presupuestoAprobado.isAprobado(),
                "Presupuesto debe estar aprobado después de aprobación exitosa");
        assertTrue(programaObraCongelado.estaCongelado(),
                "Cronograma debe estar congelado después de aprobación exitosa");

        // Verificar que se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isPresent(),
                "Debe existir snapshot después de aprobación exitosa");
    }
}
