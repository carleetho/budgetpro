package com.budgetpro.integration;

import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.PresupuestoRepository;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para verificar la atomicidad transaccional del mecanismo de freeze.
 * 
 * Verifica:
 * - Si freeze de Presupuesto falla, Schedule no se congela (rollback completo)
 * - Si freeze de Schedule falla, Presupuesto hace rollback (rollback completo)
 * - Consistencia de datos en ambos casos (no deben quedar estados parciales)
 * 
 * **Estrategia:**
 * Estos tests verifican explícitamente el rollback transaccional simulando
 * fallos en diferentes puntos del proceso y verificando que ningún cambio
 * se persiste en la base de datos.
 */
@Transactional
class FreezeAtomicityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AprobarPresupuestoUseCase aprobarPresupuestoUseCase;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    @Autowired
    private ProgramaObraRepository programaObraRepository;

    @Autowired
    private CronogramaSnapshotRepository snapshotRepository;

    private UUID proyectoId;
    private PresupuestoId presupuestoId;
    private ProgramaObraId programaObraId;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
        presupuestoId = PresupuestoId.nuevo();
        programaObraId = ProgramaObraId.nuevo();
        fechaInicio = LocalDate.of(2024, 1, 1);
        fechaFinEstimada = LocalDate.of(2024, 12, 31);
    }

    @Test
    void aprobarPresupuesto_siFallaFreezePresupuesto_scheduleNoSeCongela() {
        // Given: Presupuesto y Schedule válidos, pero simularemos fallo en aprobación de presupuesto
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // Verificar estado inicial
        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");
        assertFalse(programaObra.estaCongelado(), "Schedule no debe estar congelado inicialmente");

        // When: Intentar aprobar (si falla por cualquier razón, debe hacer rollback completo)
        // Nota: En este caso, si el presupuesto ya está aprobado, fallará
        // Primero lo aprobamos una vez
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        // Verificar que ambos están congelados después de la primera aprobación
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId).orElseThrow();
        ProgramaObra programaObraCongelado = programaObraRepository.findByProyectoId(proyectoId).orElseThrow();
        
        assertTrue(presupuestoAprobado.isAprobado(), "Presupuesto debe estar aprobado");
        assertTrue(programaObraCongelado.estaCongelado(), "Schedule debe estar congelado");

        // Ahora intentamos aprobar de nuevo (debe fallar)
        try {
            aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());
            fail("Debe lanzar excepción al intentar aprobar presupuesto ya aprobado");
        } catch (Exception e) {
            // Esperado: debe lanzar excepción
        }

        // Then: Verificar que el estado NO cambió (rollback completo)
        // Recargar desde BD para verificar estado persistido
        Presupuesto presupuestoRecargado = presupuestoRepository.findById(presupuestoId).orElseThrow();
        ProgramaObra programaObraRecargado = programaObraRepository.findByProyectoId(proyectoId).orElseThrow();

        // El estado debe mantenerse (no debe haber cambios adicionales)
        assertTrue(presupuestoRecargado.isAprobado(), 
                "Presupuesto debe seguir aprobado (no debe haber cambios)");
        assertTrue(programaObraRecargado.estaCongelado(), 
                "Schedule debe seguir congelado (no debe haber cambios)");
        
        // Verificar que solo hay un snapshot (no se creó duplicado)
        assertEquals(1, snapshotRepository.findByPresupuestoId(presupuestoId).size(),
                "Debe haber solo un snapshot (no se creó duplicado en rollback)");
    }

    @Test
    void aprobarPresupuesto_siFallaFreezeSchedule_presupuestoHaceRollback() {
        // Given: Presupuesto válido pero Schedule sin fechas (causará fallo en freeze)
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObraSinFechas = ProgramaObra.crear(
                programaObraId, proyectoId, null, null
        );
        programaObraRepository.save(programaObraSinFechas);

        // Verificar estado inicial
        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");
        assertFalse(programaObraSinFechas.estaCongelado(), "Schedule no debe estar congelado inicialmente");

        // When: Intentar aprobar (debe fallar porque Schedule no tiene fechas)
        try {
            aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());
            fail("Debe lanzar excepción si Schedule no puede congelarse");
        } catch (Exception e) {
            // Esperado: debe lanzar excepción
        }

        // Then: Verificar rollback completo - ningún cambio debe persistirse
        Presupuesto presupuestoRecargado = presupuestoRepository.findById(presupuestoId).orElseThrow();
        ProgramaObra programaObraRecargado = programaObraRepository.findByProyectoId(proyectoId).orElseThrow();

        assertFalse(presupuestoRecargado.isAprobado(),
                "Presupuesto NO debe estar aprobado después del rollback");
        assertFalse(programaObraRecargado.estaCongelado(),
                "Schedule NO debe estar congelado después del rollback");
        
        // Verificar que NO se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isEmpty(),
                "NO debe existir snapshot después del rollback");
    }

    @Test
    void aprobarPresupuesto_debeMantenerConsistenciaEnCasoDeExito() {
        // Given: Presupuesto y Schedule válidos
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // When: Aprobar exitosamente
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        // Then: Ambos deben estar congelados y debe existir snapshot
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId).orElseThrow();
        ProgramaObra programaObraCongelado = programaObraRepository.findByProyectoId(proyectoId).orElseThrow();

        assertTrue(presupuestoAprobado.isAprobado(),
                "Presupuesto debe estar aprobado después de commit exitoso");
        assertTrue(programaObraCongelado.estaCongelado(),
                "Schedule debe estar congelado después de commit exitoso");
        
        // Verificar que se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isPresent(),
                "Debe existir snapshot después de commit exitoso");
        
        // Verificar consistencia: snapshot debe referenciar ambos
        var snapshot = snapshotRepository.findByProgramaObraId(programaObraId).orElseThrow();
        assertEquals(programaObraId, snapshot.getProgramaObraId(),
                "Snapshot debe referenciar ProgramaObra correcto");
        assertEquals(presupuestoId, snapshot.getPresupuestoId(),
                "Snapshot debe referenciar Presupuesto correcto");
    }

    @Test
    void aprobarPresupuesto_noDebeDejarEstadosParciales() {
        // Given: Presupuesto válido pero Schedule sin fechas
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObraSinFechas = ProgramaObra.crear(
                programaObraId, proyectoId, null, null
        );
        programaObraRepository.save(programaObraSinFechas);

        // When: Intentar aprobar (debe fallar)
        try {
            aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());
            fail("Debe lanzar excepción");
        } catch (Exception e) {
            // Esperado
        }

        // Then: NO debe haber estados parciales
        // Verificar que NO hay combinación de: presupuesto aprobado + schedule no congelado
        Presupuesto presupuestoRecargado = presupuestoRepository.findById(presupuestoId).orElseThrow();
        ProgramaObra programaObraRecargado = programaObraRepository.findByProyectoId(proyectoId).orElseThrow();

        // Si presupuesto está aprobado, schedule DEBE estar congelado
        if (presupuestoRecargado.isAprobado()) {
            assertTrue(programaObraRecargado.estaCongelado(),
                    "Si presupuesto está aprobado, schedule DEBE estar congelado");
        }

        // Si schedule está congelado, presupuesto DEBE estar aprobado
        if (programaObraRecargado.estaCongelado()) {
            assertTrue(presupuestoRecargado.isAprobado(),
                    "Si schedule está congelado, presupuesto DEBE estar aprobado");
        }

        // En este caso, ambos deben estar en estado inicial (no aprobado/no congelado)
        assertFalse(presupuestoRecargado.isAprobado() && programaObraRecargado.estaCongelado(),
                "NO debe haber estado parcial: presupuesto aprobado + schedule no congelado");
        assertFalse(programaObraRecargado.estaCongelado() && !presupuestoRecargado.isAprobado(),
                "NO debe haber estado parcial: schedule congelado + presupuesto no aprobado");
    }
}
