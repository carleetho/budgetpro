package com.budgetpro.infrastructure.persistence.adapter.cronograma;

import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test de integración para verificar la persistencia de CronogramaSnapshot con JSONB.
 * 
 * Verifica:
 * - Serialización correcta de JSONB en PostgreSQL
 * - Lectura correcta de JSONB después de persistir
 * - Relaciones con ProgramaObra y Presupuesto
 * - Integridad de datos JSONB complejos
 */
@Transactional
class CronogramaSnapshotJsonbPersistenceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CronogramaSnapshotRepository snapshotRepository;

    @Autowired
    private ProgramaObraRepository programaObraRepository;

    @Autowired
    private PresupuestoRepository presupuestoRepository;

    private UUID proyectoId;
    private ProgramaObraId programaObraId;
    private PresupuestoId presupuestoId;
    private String fechasJsonValido;
    private String duracionesJsonValido;
    private String secuenciaJsonValido;
    private String calendariosJsonValido;

    @BeforeEach
    void setUp() {
        proyectoId = UUID.randomUUID();
        programaObraId = ProgramaObraId.nuevo();
        presupuestoId = PresupuestoId.nuevo();
        
        // JSON válidos según los esquemas definidos
        fechasJsonValido = """
            {
                "programa": {
                    "fechaInicio": "2024-01-01",
                    "fechaFinEstimada": "2024-12-31"
                },
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "fechaInicio": "2024-01-15",
                        "fechaFin": "2024-02-15"
                    }
                ]
            }
            """;
        
        duracionesJsonValido = """
            {
                "duracionTotalDias": 365,
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "duracionDias": 30
                    }
                ]
            }
            """;
        
        secuenciaJsonValido = """
            {
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "predecesoras": []
                    }
                ]
            }
            """;
        
        calendariosJsonValido = """
            {
                "calendarios": [],
                "diasFestivos": [],
                "restricciones": []
            }
            """;
    }

    @Test
    void save_debeSerializarJsonbCorrectamente() {
        // Given: Un snapshot con JSON válidos
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                CronogramaSnapshotId.nuevo(),
                programaObraId,
                presupuestoId,
                fechasJsonValido,
                duracionesJsonValido,
                secuenciaJsonValido,
                calendariosJsonValido
        );

        // When: Persistir
        snapshotRepository.save(snapshot);

        // Then: Debe poder recuperarse con JSONB intactos
        Optional<CronogramaSnapshot> recuperado = snapshotRepository.findById(snapshot.getId());
        
        assertTrue(recuperado.isPresent(), "Snapshot debe existir después de guardarlo");
        CronogramaSnapshot snapshotRecuperado = recuperado.get();
        
        // Verificar que los JSONB se preservaron correctamente
        assertNotNull(snapshotRecuperado.getFechasJson(), "fechasJson no debe ser null");
        assertNotNull(snapshotRecuperado.getDuracionesJson(), "duracionesJson no debe ser null");
        assertNotNull(snapshotRecuperado.getSecuenciaJson(), "secuenciaJson no debe ser null");
        assertNotNull(snapshotRecuperado.getCalendariosJson(), "calendariosJson no debe ser null");
        
        // Verificar contenido específico
        assertTrue(snapshotRecuperado.getFechasJson().contains("2024-01-01"), 
                "fechasJson debe contener fecha de inicio");
        assertTrue(snapshotRecuperado.getDuracionesJson().contains("365"), 
                "duracionesJson debe contener duración total");
    }

    @Test
    void save_debeLeerJsonbDespuesDePersistir() {
        // Given: Un snapshot persistido
        CronogramaSnapshotId snapshotId = CronogramaSnapshotId.nuevo();
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                snapshotId,
                programaObraId,
                presupuestoId,
                fechasJsonValido,
                duracionesJsonValido,
                secuenciaJsonValido,
                calendariosJsonValido
        );
        snapshotRepository.save(snapshot);

        // When: Leer desde la base de datos
        Optional<CronogramaSnapshot> recuperado = snapshotRepository.findById(snapshotId);

        // Then: Los JSONB deben ser parseables y completos
        assertTrue(recuperado.isPresent());
        CronogramaSnapshot snapshotRecuperado = recuperado.get();
        
        // Verificar que los JSON son válidos (parseables)
        assertDoesNotThrow(() -> {
            new org.json.JSONObject(snapshotRecuperado.getFechasJson());
            new org.json.JSONObject(snapshotRecuperado.getDuracionesJson());
            new org.json.JSONObject(snapshotRecuperado.getSecuenciaJson());
            new org.json.JSONObject(snapshotRecuperado.getCalendariosJson());
        }, "Todos los JSONB deben ser parseables");
    }

    @Test
    void save_debeMantenerRelacionConProgramaObra() {
        // Given: ProgramaObra y Presupuesto existentes
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        programaObraRepository.save(programaObra);

        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Test");
        presupuestoRepository.save(presupuesto);

        // When: Crear y persistir snapshot
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                CronogramaSnapshotId.nuevo(),
                programaObraId,
                presupuestoId,
                fechasJsonValido,
                duracionesJsonValido,
                secuenciaJsonValido,
                calendariosJsonValido
        );
        snapshotRepository.save(snapshot);

        // Then: Debe poder recuperarse por ProgramaObraId
        Optional<CronogramaSnapshot> encontrado = snapshotRepository.findByProgramaObraId(programaObraId);
        
        assertTrue(encontrado.isPresent(), "Debe encontrar snapshot por ProgramaObraId");
        assertEquals(programaObraId, encontrado.get().getProgramaObraId(), 
                "ProgramaObraId debe coincidir");
    }

    @Test
    void save_debeMantenerRelacionConPresupuesto() {
        // Given: ProgramaObra y Presupuesto existentes
        ProgramaObra programaObra = ProgramaObra.crear(
                programaObraId, proyectoId, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        programaObraRepository.save(programaObra);

        Presupuesto presupuesto = Presupuesto.crear(presupuestoId, proyectoId, "Test");
        presupuestoRepository.save(presupuesto);

        // When: Crear y persistir snapshot
        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                CronogramaSnapshotId.nuevo(),
                programaObraId,
                presupuestoId,
                fechasJsonValido,
                duracionesJsonValido,
                secuenciaJsonValido,
                calendariosJsonValido
        );
        snapshotRepository.save(snapshot);

        // Then: Debe poder recuperarse por PresupuestoId
        var snapshots = snapshotRepository.findByPresupuestoId(presupuestoId);
        
        assertFalse(snapshots.isEmpty(), "Debe encontrar snapshots por PresupuestoId");
        assertTrue(snapshots.stream().anyMatch(s -> s.getId().equals(snapshot.getId())), 
                "Debe encontrar el snapshot creado");
        assertEquals(presupuestoId, snapshots.get(0).getPresupuestoId(), 
                "PresupuestoId debe coincidir");
    }

    @Test
    void save_debePreservarJsonbComplejos() {
        // Given: JSONB con estructuras complejas
        String fechasJsonComplejo = """
            {
                "programa": {
                    "fechaInicio": "2024-01-01",
                    "fechaFinEstimada": "2024-12-31"
                },
                "actividades": [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440001",
                        "fechaInicio": "2024-01-15",
                        "fechaFin": "2024-02-15"
                    },
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440002",
                        "partidaId": "550e8400-e29b-41d4-a716-446655440003",
                        "fechaInicio": "2024-02-16",
                        "fechaFin": "2024-03-16"
                    }
                ]
            }
            """;

        CronogramaSnapshot snapshot = CronogramaSnapshot.crear(
                CronogramaSnapshotId.nuevo(),
                programaObraId,
                presupuestoId,
                fechasJsonComplejo,
                duracionesJsonValido,
                secuenciaJsonValido,
                calendariosJsonValido
        );

        // When: Persistir y recuperar
        snapshotRepository.save(snapshot);
        Optional<CronogramaSnapshot> recuperado = snapshotRepository.findById(snapshot.getId());

        // Then: JSONB complejo debe preservarse exactamente
        assertTrue(recuperado.isPresent());
        String fechasRecuperado = recuperado.get().getFechasJson();
        
        // Verificar que contiene todas las actividades
        assertTrue(fechasRecuperado.contains("550e8400-e29b-41d4-a716-446655440000"), 
                "Debe contener primera actividad");
        assertTrue(fechasRecuperado.contains("550e8400-e29b-41d4-a716-446655440002"), 
                "Debe contener segunda actividad");
        assertTrue(fechasRecuperado.contains("2024-01-15"), 
                "Debe contener fechas de actividades");
    }

    @Test
    void save_debePreservarSnapshotDateYAlgorithm() {
        // Given: Un snapshot con fecha y algoritmo específicos
        LocalDate fechaEspecifica = java.time.LocalDateTime.of(2024, 6, 15, 14, 30).toLocalDate();
        String algoritmoEspecifico = "TEMPORAL-SNAPSHOT-v2";

        CronogramaSnapshot snapshot = CronogramaSnapshot.reconstruir(
                CronogramaSnapshotId.nuevo(),
                programaObraId,
                presupuestoId,
                fechasJsonValido,
                duracionesJsonValido,
                secuenciaJsonValido,
                calendariosJsonValido,
                java.time.LocalDateTime.of(2024, 6, 15, 14, 30),
                algoritmoEspecifico
        );

        // When: Persistir y recuperar
        snapshotRepository.save(snapshot);
        Optional<CronogramaSnapshot> recuperado = snapshotRepository.findById(snapshot.getId());

        // Then: Fecha y algoritmo deben preservarse
        assertTrue(recuperado.isPresent());
        assertEquals(algoritmoEspecifico, recuperado.get().getSnapshotAlgorithm(), 
                "snapshot_algorithm debe preservarse");
        assertNotNull(recuperado.get().getSnapshotDate(), 
                "snapshot_date no debe ser null");
    }
}
