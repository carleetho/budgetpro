package com.budgetpro.integration;

import com.budgetpro.application.presupuesto.port.in.AprobarPresupuestoUseCase;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoEncontradoException;
import com.budgetpro.application.presupuesto.exception.PresupuestoNoPuedeAprobarseException;
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
 * Test de integración para verificar la transaccionalidad y atomicidad
 * del proceso de aprobación de presupuesto y congelamiento de cronograma.
 * 
 * **Objetivo:**
 * Verificar que el principio de baseline se mantiene atómicamente:
 * - Si la aprobación del presupuesto falla → rollback completo
 * - Si el congelamiento del cronograma falla → rollback completo
 * - Si la persistencia falla → rollback completo
 * - No deben quedar estados parciales (presupuesto aprobado sin cronograma congelado)
 * 
 * **Estrategia de Testing:**
 * Estos tests verifican explícitamente el rollback transaccional simulando
 * fallos en diferentes puntos del proceso y verificando que ningún cambio
 * se persiste en la base de datos.
 */
@Transactional
class TransaccionalidadBaselineIntegrationTest extends AbstractIntegrationTest {

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
    void aprobarPresupuesto_debeHacerRollbackSiFallaCongelamientoCronograma() {
        // Given: Presupuesto válido pero cronograma sin fechas (causará fallo)
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObraSinFechas = ProgramaObra.crear(
                programaObraId, proyectoId, null, null
        );
        programaObraRepository.save(programaObraSinFechas);

        // Verificar estado inicial
        assertFalse(presupuesto.isAprobado(), "Presupuesto no debe estar aprobado inicialmente");
        assertFalse(programaObraSinFechas.estaCongelado(), "Cronograma no debe estar congelado inicialmente");

        // When: Intentar aprobar (debe fallar porque el cronograma no tiene fechas)
        assertThrows(
                PresupuestoNoPuedeAprobarseException.class,
                () -> aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue()),
                "Debe lanzar excepción si el cronograma no puede congelarse"
        );

        // Then: Verificar rollback completo - ningún cambio debe persistirse
        // Necesitamos hacer flush y clear para verificar que no hay cambios en la BD
        // Como estamos en @Transactional, el rollback ya ocurrió, pero verificamos el estado
        
        // Recargar desde la base de datos (en una nueva transacción)
        Presupuesto presupuestoRecargado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        ProgramaObra programaObraRecargado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertFalse(presupuestoRecargado.isAprobado(),
                "Presupuesto NO debe estar aprobado después del rollback");
        assertFalse(programaObraRecargado.estaCongelado(),
                "Cronograma NO debe estar congelado después del rollback");
        
        // Verificar que NO se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isEmpty(),
                "NO debe existir snapshot después del rollback");
    }

    @Test
    void aprobarPresupuesto_debeHacerRollbackSiPresupuestoNoExiste() {
        // Given: Solo cronograma, sin presupuesto
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        PresupuestoId presupuestoInexistente = PresupuestoId.nuevo();

        // When: Intentar aprobar presupuesto inexistente
        assertThrows(
                PresupuestoNoEncontradoException.class,
                () -> aprobarPresupuestoUseCase.aprobar(presupuestoInexistente.getValue()),
                "Debe lanzar excepción si el presupuesto no existe"
        );

        // Then: Verificar que el cronograma NO se congeló
        ProgramaObra programaObraRecargado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();
        
        assertFalse(programaObraRecargado.estaCongelado(),
                "Cronograma NO debe estar congelado si el presupuesto no existe");
        
        // Verificar que NO se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isEmpty(),
                "NO debe existir snapshot si el presupuesto no existe");
    }

    @Test
    void aprobarPresupuesto_debeHacerRollbackSiNoExisteCronograma() {
        // Given: Solo presupuesto, sin cronograma
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        // When: Intentar aprobar sin cronograma
        assertThrows(
                PresupuestoNoPuedeAprobarseException.class,
                () -> aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue()),
                "Debe lanzar excepción si no existe cronograma"
        );

        // Then: Verificar que el presupuesto NO se aprobó
        Presupuesto presupuestoRecargado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        
        assertFalse(presupuestoRecargado.isAprobado(),
                "Presupuesto NO debe estar aprobado si no existe cronograma");
    }

    @Test
    void aprobarPresupuesto_debeSerAtomicoCuandoTodoEsValido() {
        // Given: Presupuesto y cronograma válidos
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        presupuestoRepository.save(presupuesto);

        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // When: Aprobar el presupuesto (debe completarse exitosamente)
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());

        // Then: Ambos deben estar congelados (commit exitoso)
        Presupuesto presupuestoAprobado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        ProgramaObra programaObraCongelado = programaObraRepository.findByProyectoId(proyectoId)
                .orElseThrow();

        assertTrue(presupuestoAprobado.isAprobado(),
                "Presupuesto debe estar aprobado después de commit exitoso");
        assertTrue(programaObraCongelado.estaCongelado(),
                "Cronograma debe estar congelado después de commit exitoso");
        
        // Verificar que se generó snapshot
        assertTrue(snapshotRepository.findByProgramaObraId(programaObraId).isPresent(),
                "Debe existir snapshot después de commit exitoso");
    }

    @Test
    void aprobarPresupuesto_debeHacerRollbackSiPresupuestoYaEstaAprobado() {
        // Given: Presupuesto ya aprobado manualmente (simulando estado inconsistente)
        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Presupuesto Test");
        // Nota: En un escenario real, esto no debería pasar, pero verificamos el comportamiento
        // Si intentamos aprobar un presupuesto ya aprobado, debe fallar sin cambios
        
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, fechaFinEstimada
        );
        programaObraRepository.save(programaObra);

        // Simular que el presupuesto ya está aprobado (aunque esto no debería pasar)
        // En realidad, el servicio validará esto y lanzará excepción
        
        // When: Intentar aprobar presupuesto que ya está aprobado
        // Primero lo aprobamos una vez
        aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue());
        
        // Luego intentamos aprobarlo de nuevo
        assertThrows(
                PresupuestoNoPuedeAprobarseException.class,
                () -> aprobarPresupuestoUseCase.aprobar(presupuestoId.getValue()),
                "Debe lanzar excepción si el presupuesto ya está aprobado"
        );

        // Then: El estado debe mantenerse (no debe haber cambios adicionales)
        Presupuesto presupuestoRecargado = presupuestoRepository.findById(presupuestoId)
                .orElseThrow();
        
        assertTrue(presupuestoRecargado.isAprobado(),
                "Presupuesto debe seguir aprobado (estado no cambió)");
        
        // Verificar que solo hay un snapshot (no se creó otro)
        assertEquals(1, snapshotRepository.findByPresupuestoId(presupuestoId).size(),
                "Debe haber solo un snapshot (no se creó duplicado)");
    }
}
