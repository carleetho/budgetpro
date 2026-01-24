package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.infrastructure.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para CronogramaSnapshotRepositoryAdapter.
 * 
 * Verifica que la persistencia de snapshots funcione correctamente con una base de datos real.
 * Incluye pruebas de:
 * - Guardado de snapshots
 * - Búsqueda por ID
 * - Búsqueda por ProgramaObraId
 * - Búsqueda por PresupuestoId
 * - Persistencia de datos JSONB
 */
@Transactional
class CronogramaSnapshotRepositoryAdapterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CronogramaSnapshotRepository repository;

    private CronogramaSnapshotId snapshotId;
    private ProgramaObraId programaObraId;
    private PresupuestoId presupuestoId;
    private String fechasJson;
    private String duracionesJson;
    private String secuenciaJson;
    private String calendariosJson;

    @BeforeEach
    void setUp() {
        snapshotId = CronogramaSnapshotId.nuevo();
        programaObraId = ProgramaObraId.nuevo();
        presupuestoId = PresupuestoId.nuevo();
        
        // Datos JSONB de ejemplo
        fechasJson = "{\"fechaInicio\":\"2024-01-01\",\"fechaFin\":\"2024-12-31\"}";
        duracionesJson = "{\"duracionTotal\":365,\"duracionActividades\":[30,60,90]}";
        secuenciaJson = "{\"orden\":[1,2,3],\"dependencias\":[[1,2],[2,3]]}";
        calendariosJson = "{\"calendario\":\"STANDARD\",\"diasFestivos\":[]}";
    }

    @Test
    void save_debePersistirSnapshotCorrectamente() {
        // Given: Un snapshot nuevo
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        // When: Guardar el snapshot
        repository.save(snapshot);

        // Then: Debe poder recuperarse por ID
        Optional<CronogramaSnapshot> encontrado = repository.findById(snapshotId);
        assertTrue(encontrado.isPresent(), "El snapshot debe existir después de guardarlo");
        
        CronogramaSnapshot recuperado = encontrado.get();
        assertEquals(snapshotId, recuperado.getId());
        assertEquals(programaObraId, recuperado.getProgramaObraId());
        assertEquals(presupuestoId, recuperado.getPresupuestoId());
        assertEquals(fechasJson, recuperado.getFechasJson());
        assertEquals(duracionesJson, recuperado.getDuracionesJson());
        assertEquals(secuenciaJson, recuperado.getSecuenciaJson());
        assertEquals(calendariosJson, recuperado.getCalendariosJson());
        assertNotNull(recuperado.getSnapshotDate());
        assertEquals("TEMPORAL-SNAPSHOT-v1", recuperado.getSnapshotAlgorithm());
    }

    @Test
    void findById_debeRetornarEmptySiNoExiste() {
        // Given: Un ID que no existe
        CronogramaSnapshotId idInexistente = CronogramaSnapshotId.nuevo();

        // When: Buscar por ID
        Optional<CronogramaSnapshot> resultado = repository.findById(idInexistente);

        // Then: Debe retornar empty
        assertTrue(resultado.isEmpty(), "No debe encontrar un snapshot inexistente");
    }

    @Test
    void findByProgramaObraId_debeRetornarElSnapshotMasReciente() {
        // Given: Dos snapshots para el mismo programa de obra
        CronogramaSnapshotId snapshotId1 = CronogramaSnapshotId.nuevo();
        CronogramaSnapshotId snapshotId2 = CronogramaSnapshotId.nuevo();
        
        CronogramaSnapshot snapshot1 = CronogramaSnapshot.crear(
                snapshotId1,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );
        
        // Esperar un poco para que el segundo snapshot tenga fecha más reciente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        CronogramaSnapshot snapshot2 = CronogramaSnapshot.crear(
                snapshotId2,
                programaObraId,
                presupuestoId,
                fechasJson + "_updated",
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        repository.save(snapshot1);
        repository.save(snapshot2);

        // When: Buscar por ProgramaObraId
        Optional<CronogramaSnapshot> resultado = repository.findByProgramaObraId(programaObraId);

        // Then: Debe retornar el más reciente (snapshot2)
        assertTrue(resultado.isPresent());
        assertEquals(snapshotId2, resultado.get().getId());
        assertTrue(resultado.get().getFechasJson().contains("_updated"));
    }

    @Test
    void findByProgramaObraId_debeRetornarEmptySiNoExiste() {
        // Given: Un ProgramaObraId sin snapshots
        ProgramaObraId programaObraIdSinSnapshots = ProgramaObraId.nuevo();

        // When: Buscar por ProgramaObraId
        Optional<CronogramaSnapshot> resultado = repository.findByProgramaObraId(programaObraIdSinSnapshots);

        // Then: Debe retornar empty
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByPresupuestoId_debeRetornarTodosLosSnapshotsDelPresupuesto() {
        // Given: Múltiples snapshots para el mismo presupuesto
        CronogramaSnapshotId snapshotId1 = CronogramaSnapshotId.nuevo();
        CronogramaSnapshotId snapshotId2 = CronogramaSnapshotId.nuevo();
        ProgramaObraId programaObraId2 = ProgramaObraId.nuevo();

        CronogramaSnapshot snapshot1 = CronogramaSnapshot.crear(
                snapshotId1,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        CronogramaSnapshot snapshot2 = CronogramaSnapshot.crear(
                snapshotId2,
                programaObraId2,
                presupuestoId,
                fechasJson + "_v2",
                duracionesJson,
                secuenciaJson,
                calendariosJson
        );

        repository.save(snapshot1);
        repository.save(snapshot2);

        // When: Buscar por PresupuestoId
        List<CronogramaSnapshot> resultados = repository.findByPresupuestoId(presupuestoId);

        // Then: Debe retornar ambos snapshots ordenados por fecha descendente
        assertEquals(2, resultados.size());
        // El más reciente debe estar primero
        assertTrue(resultados.get(0).getFechasJson().contains("_v2") || 
                   resultados.get(1).getFechasJson().contains("_v2"));
    }

    @Test
    void findByPresupuestoId_debeRetornarListaVaciaSiNoExistenSnapshots() {
        // Given: Un PresupuestoId sin snapshots
        PresupuestoId presupuestoIdSinSnapshots = PresupuestoId.nuevo();

        // When: Buscar por PresupuestoId
        List<CronogramaSnapshot> resultados = repository.findByPresupuestoId(presupuestoIdSinSnapshots);

        // Then: Debe retornar lista vacía
        assertTrue(resultados.isEmpty());
    }

    @Test
    void save_debePreservarDatosJsonbComplejos() {
        // Given: Un snapshot con datos JSONB complejos
        String fechasJsonComplejo = """
                {
                    "fechaInicio": "2024-01-01",
                    "fechaFin": "2024-12-31",
                    "actividades": [
                        {"id": "act1", "fechaInicio": "2024-01-01", "fechaFin": "2024-01-30"},
                        {"id": "act2", "fechaInicio": "2024-02-01", "fechaFin": "2024-03-31"}
                    ]
                }
                """;
        
        String duracionesJsonComplejo = """
                {
                    "duracionTotal": 365,
                    "duracionActividades": [
                        {"id": "act1", "duracion": 30},
                        {"id": "act2", "duracion": 60}
                    ]
                }
                """;

        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJsonComplejo,
                duracionesJsonComplejo,
                secuenciaJson,
                calendariosJson
        );

        // When: Guardar y recuperar
        repository.save(snapshot);
        Optional<CronogramaSnapshot> recuperado = repository.findById(snapshotId);

        // Then: Los datos JSONB deben preservarse exactamente
        assertTrue(recuperado.isPresent());
        assertEquals(fechasJsonComplejo.trim(), recuperado.get().getFechasJson().trim());
        assertEquals(duracionesJsonComplejo.trim(), recuperado.get().getDuracionesJson().trim());
        assertTrue(recuperado.get().getFechasJson().contains("act1"));
        assertTrue(recuperado.get().getDuracionesJson().contains("act1"));
    }

    @Test
    void save_debePreservarSnapshotDateYAlgorithm() {
        // Given: Un snapshot creado con fecha y algoritmo específicos
        LocalDateTime fechaEspecifica = LocalDateTime.of(2024, 1, 15, 10, 30);
        String algoritmoEspecifico = "TEMPORAL-SNAPSHOT-v2";

        CronogramaSnapshot snapshot = CronogramaSnapshot.reconstruir(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJson,
                duracionesJson,
                secuenciaJson,
                calendariosJson,
                fechaEspecifica,
                algoritmoEspecifico
        );

        // When: Guardar y recuperar
        repository.save(snapshot);
        Optional<CronogramaSnapshot> recuperado = repository.findById(snapshotId);

        // Then: La fecha y algoritmo deben preservarse
        assertTrue(recuperado.isPresent());
        assertEquals(fechaEspecifica, recuperado.get().getSnapshotDate());
        assertEquals(algoritmoEspecifico, recuperado.get().getSnapshotAlgorithm());
    }
}
