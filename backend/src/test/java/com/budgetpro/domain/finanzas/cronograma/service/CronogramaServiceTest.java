package com.budgetpro.domain.finanzas.cronograma.service;

import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramada;
import com.budgetpro.domain.finanzas.cronograma.model.ActividadProgramadaId;
import com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshot;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObra;
import com.budgetpro.domain.finanzas.cronograma.model.ProgramaObraId;
import com.budgetpro.domain.finanzas.cronograma.port.out.ActividadProgramadaRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.CronogramaSnapshotRepository;
import com.budgetpro.domain.finanzas.cronograma.port.out.ProgramaObraRepository;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.shared.validation.JsonSchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para CronogramaService.
 * 
 * Verifica:
 * - Orquestación del congelamiento del cronograma
 * - Generación de snapshots con datos temporales
 * - Validaciones de prerrequisitos
 * - Persistencia atómica de freeze + snapshot
 * - Métodos de consulta
 */
@ExtendWith(MockitoExtension.class)
class CronogramaServiceTest {

    @Mock
    private ProgramaObraRepository programaObraRepository;

    @Mock
    private ActividadProgramadaRepository actividadProgramadaRepository;

    @Mock
    private CronogramaSnapshotRepository snapshotRepository;

    @Mock
    private JsonSchemaValidator jsonSchemaValidator;

    private SnapshotGeneratorService snapshotGeneratorService;
    private CronogramaService cronogramaService;

    private UUID proyectoId;
    private PresupuestoId presupuestoId;
    private ProgramaObraId programaObraId;
    private ProgramaObra programaObra;
    private UUID approvedBy;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;

    @BeforeEach
    void setUp() {
        snapshotGeneratorService = new SnapshotGeneratorService();
        cronogramaService = new CronogramaService(
                programaObraRepository,
                actividadProgramadaRepository,
                snapshotRepository,
                snapshotGeneratorService,
                jsonSchemaValidator
        );

        proyectoId = UUID.randomUUID();
        presupuestoId = PresupuestoId.from(UUID.randomUUID());
        programaObraId = ProgramaObraId.nuevo();
        approvedBy = UUID.randomUUID();
        fechaInicio = LocalDate.of(2024, 1, 1);
        fechaFinEstimada = LocalDate.of(2024, 12, 31);

        programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
    }

    @Test
    void congelarPorPresupuesto_debeCongelarProgramaObraYGenerarSnapshot() {
        // Given
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));
        when(actividadProgramadaRepository.findByProgramaObraId(programaObraId.getValue()))
                .thenReturn(new ArrayList<>());

        // When
        CronogramaSnapshot snapshot = cronogramaService.congelarPorPresupuesto(
                proyectoId, presupuestoId, approvedBy
        );

        // Then
        assertNotNull(snapshot, "El snapshot debe generarse");
        assertTrue(programaObra.estaCongelado(), "El ProgramaObra debe estar congelado");
        assertEquals(presupuestoId, snapshot.getPresupuestoId(), "El snapshot debe estar asociado al presupuesto");
        assertEquals(programaObraId, snapshot.getProgramaObraId(), "El snapshot debe estar asociado al programa de obra");

        // Verificar que se guardó el ProgramaObra congelado
        verify(programaObraRepository, times(1)).save(programaObra);
        
        // Verificar que se guardó el snapshot
        ArgumentCaptor<CronogramaSnapshot> snapshotCaptor = ArgumentCaptor.forClass(CronogramaSnapshot.class);
        verify(snapshotRepository, times(1)).save(snapshotCaptor.capture());
        
        CronogramaSnapshot savedSnapshot = snapshotCaptor.getValue();
        assertNotNull(savedSnapshot.getFechasJson(), "El JSON de fechas no debe ser null");
        assertNotNull(savedSnapshot.getDuracionesJson(), "El JSON de duraciones no debe ser null");
        assertNotNull(savedSnapshot.getSecuenciaJson(), "El JSON de secuencia no debe ser null");
        assertNotNull(savedSnapshot.getCalendariosJson(), "El JSON de calendarios no debe ser null");
    }

    @Test
    void congelarPorPresupuesto_debeIncluirActividadesEnSnapshot() {
        // Given
        List<ActividadProgramada> actividades = crearActividades(3);
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));
        when(actividadProgramadaRepository.findByProgramaObraId(programaObraId.getValue()))
                .thenReturn(actividades);

        // When
        CronogramaSnapshot snapshot = cronogramaService.congelarPorPresupuesto(
                proyectoId, presupuestoId, approvedBy
        );

        // Then
        assertNotNull(snapshot);
        
        // Verificar que el JSON de fechas incluye las actividades
        assertTrue(snapshot.getFechasJson().contains("actividades"), 
                "El JSON de fechas debe incluir actividades");
        
        // Verificar que el JSON de duraciones incluye las actividades
        assertTrue(snapshot.getDuracionesJson().contains("actividades"), 
                "El JSON de duraciones debe incluir actividades");
        
        // Verificar que el JSON de secuencia incluye las actividades
        assertTrue(snapshot.getSecuenciaJson().contains("actividades"), 
                "El JSON de secuencia debe incluir actividades");
    }

    @Test
    void congelarPorPresupuesto_debeLanzarExcepcionSiNoExisteProgramaObra() {
        // Given
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.empty());

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            cronogramaService.congelarPorPresupuesto(proyectoId, presupuestoId, approvedBy);
        }, "Debe lanzar IllegalStateException si no existe ProgramaObra");

        assertTrue(exception.getMessage().contains("No existe un programa de obra"), 
                "Mensaje debe indicar que no existe ProgramaObra");
        
        verify(programaObraRepository, never()).save(any());
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void congelarPorPresupuesto_debeLanzarExcepcionSiFaltaFechaInicio() {
        // Given
        ProgramaObra programaSinFechaInicio = ProgramaObra.crear(
                programaObraId, proyectoId, null, fechaFinEstimada
        );
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaSinFechaInicio));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            cronogramaService.congelarPorPresupuesto(proyectoId, presupuestoId, approvedBy);
        }, "Debe lanzar IllegalStateException si falta fechaInicio");

        assertTrue(exception.getMessage().contains("fecha de inicio"), 
                "Mensaje debe mencionar fecha de inicio");
        
        verify(programaObraRepository, never()).save(any());
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void congelarPorPresupuesto_debeLanzarExcepcionSiFaltaFechaFinEstimada() {
        // Given
        ProgramaObra programaSinFechaFin = ProgramaObra.crear(
                programaObraId, proyectoId, fechaInicio, null
        );
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaSinFechaFin));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            cronogramaService.congelarPorPresupuesto(proyectoId, presupuestoId, approvedBy);
        }, "Debe lanzar IllegalStateException si falta fechaFinEstimada");

        assertTrue(exception.getMessage().contains("fecha de fin estimada"), 
                "Mensaje debe mencionar fecha de fin estimada");
        
        verify(programaObraRepository, never()).save(any());
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void congelarPorPresupuesto_debeLanzarExcepcionSiYaEstaCongelado() {
        // Given
        programaObra.congelar(approvedBy);
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            cronogramaService.congelarPorPresupuesto(proyectoId, presupuestoId, approvedBy);
        }, "Debe lanzar IllegalStateException si ya está congelado");

        assertTrue(exception.getMessage().contains("ya está congelado"), 
                "Mensaje debe indicar que ya está congelado");
    }

    @Test
    void findByProyectoId_debeRetornarProgramaObraSiExiste() {
        // Given
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));

        // When
        Optional<ProgramaObra> resultado = cronogramaService.findByProyectoId(proyectoId);

        // Then
        assertTrue(resultado.isPresent(), "Debe retornar el ProgramaObra");
        assertEquals(programaObraId, resultado.get().getId(), "Debe ser el ProgramaObra correcto");
    }

    @Test
    void findByProyectoId_debeRetornarEmptySiNoExiste() {
        // Given
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.empty());

        // When
        Optional<ProgramaObra> resultado = cronogramaService.findByProyectoId(proyectoId);

        // Then
        assertTrue(resultado.isEmpty(), "Debe retornar empty si no existe");
    }

    @Test
    void estaCongelado_debeRetornarTrueSiEstaCongelado() {
        // Given
        programaObra.congelar(approvedBy);
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));

        // When
        boolean resultado = cronogramaService.estaCongelado(proyectoId);

        // Then
        assertTrue(resultado, "Debe retornar true si está congelado");
    }

    @Test
    void estaCongelado_debeRetornarFalseSiNoEstaCongelado() {
        // Given
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));

        // When
        boolean resultado = cronogramaService.estaCongelado(proyectoId);

        // Then
        assertFalse(resultado, "Debe retornar false si no está congelado");
    }

    @Test
    void estaCongelado_debeRetornarFalseSiNoExiste() {
        // Given
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.empty());

        // When
        boolean resultado = cronogramaService.estaCongelado(proyectoId);

        // Then
        assertFalse(resultado, "Debe retornar false si no existe");
    }

    @Test
    void findSnapshotByProyectoId_debeRetornarSnapshotSiExiste() {
        // Given
        CronogramaSnapshot snapshot = crearSnapshot();
        when(programaObraRepository.findByProyectoId(proyectoId))
                .thenReturn(Optional.of(programaObra));
        when(snapshotRepository.findByProgramaObraId(programaObraId))
                .thenReturn(Optional.of(snapshot));

        // When
        Optional<CronogramaSnapshot> resultado = cronogramaService.findSnapshotByProyectoId(proyectoId);

        // Then
        assertTrue(resultado.isPresent(), "Debe retornar el snapshot");
        assertEquals(snapshot.getId(), resultado.get().getId(), "Debe ser el snapshot correcto");
    }

    // Helper methods

    private List<ActividadProgramada> crearActividades(int cantidad) {
        List<ActividadProgramada> actividades = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            ActividadProgramada actividad = ActividadProgramada.crear(
                    ActividadProgramadaId.nuevo(),
                    UUID.randomUUID(),
                    programaObraId.getValue(),
                    fechaInicio.plusDays(i * 10),
                    fechaInicio.plusDays((i + 1) * 10)
            );
            actividades.add(actividad);
        }
        return actividades;
    }

    private CronogramaSnapshot crearSnapshot() {
        return CronogramaSnapshot.crear(
                com.budgetpro.domain.finanzas.cronograma.model.CronogramaSnapshotId.nuevo(),
                programaObraId,
                presupuestoId,
                "{\"programa\":{},\"actividades\":[]}",
                "{\"duracionTotalDias\":365,\"actividades\":[]}",
                "{\"actividades\":[]}",
                "{\"calendarios\":[],\"diasFestivos\":[],\"restricciones\":[]}"
        );
    }
}
