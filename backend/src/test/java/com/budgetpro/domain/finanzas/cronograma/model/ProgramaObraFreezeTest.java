package com.budgetpro.domain.finanzas.cronograma.model;

import com.budgetpro.domain.finanzas.cronograma.exception.CronogramaCongeladoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para el mecanismo de congelamiento (freeze) de ProgramaObra.
 * 
 * Verifica:
 * - Congelamiento exitoso con fechas válidas
 * - Validación de prerrequisitos (fechas requeridas)
 * - Guards de freeze en métodos de mutación
 * - Metadata de congelamiento capturada correctamente
 * - Inmutabilidad después del congelamiento
 */
class ProgramaObraFreezeTest {

    private ProgramaObraId programaObraId;
    private UUID proyectoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFinEstimada;
    private UUID approvedBy;

    @BeforeEach
    void setUp() {
        programaObraId = ProgramaObraId.nuevo();
        proyectoId = UUID.randomUUID();
        fechaInicio = LocalDate.of(2024, 1, 1);
        fechaFinEstimada = LocalDate.of(2024, 12, 31);
        approvedBy = UUID.randomUUID();
    }

    @Test
    void congelar_debeCongelarProgramaObraConFechasValidas() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        assertFalse(programaObra.estaCongelado(), "ProgramaObra no debe estar congelado inicialmente");

        // When
        programaObra.congelar(approvedBy);

        // Then
        assertTrue(programaObra.estaCongelado(), "ProgramaObra debe estar congelado después de congelar()");
        assertNotNull(programaObra.getCongeladoAt(), "congeladoAt no debe ser null");
        assertEquals(approvedBy, programaObra.getCongeladoBy(), "congeladoBy debe ser el usuario aprobador");
        assertEquals("v1", programaObra.getSnapshotAlgorithm(), "snapshotAlgorithm debe ser 'v1'");
    }

    @Test
    void congelar_debeLanzarIllegalStateExceptionSiFaltaFechaInicio() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, null, fechaFinEstimada);

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            programaObra.congelar(approvedBy);
        }, "Debe lanzar IllegalStateException si falta fechaInicio");

        assertTrue(exception.getMessage().contains("fecha de inicio"), 
                "Mensaje debe mencionar fecha de inicio");
        assertFalse(programaObra.estaCongelado(), "ProgramaObra no debe estar congelado");
    }

    @Test
    void congelar_debeLanzarIllegalStateExceptionSiFaltaFechaFinEstimada() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, null);

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            programaObra.congelar(approvedBy);
        }, "Debe lanzar IllegalStateException si falta fechaFinEstimada");

        assertTrue(exception.getMessage().contains("fecha de fin estimada"), 
                "Mensaje debe mencionar fecha de fin estimada");
        assertFalse(programaObra.estaCongelado(), "ProgramaObra no debe estar congelado");
    }

    @Test
    void congelar_debeLanzarIllegalArgumentExceptionSiApprovedByEsNull() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            programaObra.congelar(null);
        }, "Debe lanzar IllegalArgumentException si approvedBy es null");
    }

    @Test
    void actualizarFechas_debeLanzarCronogramaCongeladoExceptionSiEstaCongelado() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        programaObra.congelar(approvedBy);
        
        LocalDate nuevaFechaInicio = LocalDate.of(2024, 2, 1);
        LocalDate nuevaFechaFin = LocalDate.of(2025, 1, 31);

        // When/Then
        CronogramaCongeladoException exception = assertThrows(CronogramaCongeladoException.class, () -> {
            programaObra.actualizarFechas(nuevaFechaInicio, nuevaFechaFin);
        }, "Debe lanzar CronogramaCongeladoException si está congelado");

        assertEquals(programaObraId, exception.getProgramaObraId(), 
                "Excepción debe incluir el ID del programa de obra");
        assertTrue(exception.getMessage().contains("congelado"), 
                "Mensaje debe mencionar que está congelado");
        assertTrue(exception.getMessage().contains("actualizarFechas"), 
                "Mensaje debe mencionar la operación intentada");
    }

    @Test
    void actualizarFechas_debePermitirModificacionSiNoEstaCongelado() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        LocalDate nuevaFechaInicio = LocalDate.of(2024, 2, 1);
        LocalDate nuevaFechaFin = LocalDate.of(2025, 1, 31);

        // When
        programaObra.actualizarFechas(nuevaFechaInicio, nuevaFechaFin);

        // Then
        assertEquals(nuevaFechaInicio, programaObra.getFechaInicio(), 
                "Fecha de inicio debe actualizarse");
        assertEquals(nuevaFechaFin, programaObra.getFechaFinEstimada(), 
                "Fecha de fin estimada debe actualizarse");
    }

    @Test
    void actualizarFechaFinDesdeActividades_debeLanzarCronogramaCongeladoExceptionSiEstaCongelado() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        programaObra.congelar(approvedBy);
        
        LocalDate nuevaFechaFin = LocalDate.of(2025, 1, 31);

        // When/Then
        CronogramaCongeladoException exception = assertThrows(CronogramaCongeladoException.class, () -> {
            programaObra.actualizarFechaFinDesdeActividades(nuevaFechaFin);
        }, "Debe lanzar CronogramaCongeladoException si está congelado");

        assertEquals(programaObraId, exception.getProgramaObraId(), 
                "Excepción debe incluir el ID del programa de obra");
        assertTrue(exception.getMessage().contains("actualizarFechaFinDesdeActividades"), 
                "Mensaje debe mencionar la operación intentada");
    }

    @Test
    void actualizarFechaFinDesdeActividades_debePermitirModificacionSiNoEstaCongelado() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        LocalDate nuevaFechaFin = LocalDate.of(2025, 1, 31);

        // When
        programaObra.actualizarFechaFinDesdeActividades(nuevaFechaFin);

        // Then
        assertEquals(nuevaFechaFin, programaObra.getFechaFinEstimada(), 
                "Fecha de fin estimada debe actualizarse");
    }

    @Test
    void estaCongelado_debeRetornarFalseInicialmente() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);

        // When/Then
        assertFalse(programaObra.estaCongelado(), "ProgramaObra no debe estar congelado inicialmente");
    }

    @Test
    void estaCongelado_debeRetornarTrueDespuesDeCongelar() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);

        // When
        programaObra.congelar(approvedBy);

        // Then
        assertTrue(programaObra.estaCongelado(), "ProgramaObra debe estar congelado después de congelar()");
    }

    @Test
    void congelar_debeCapturarMetadataCorrectamente() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        UUID otroUsuario = UUID.randomUUID();

        // When
        programaObra.congelar(otroUsuario);

        // Then
        assertEquals(otroUsuario, programaObra.getCongeladoBy(), 
                "congeladoBy debe ser el usuario que aprobó");
        assertNotNull(programaObra.getCongeladoAt(), 
                "congeladoAt no debe ser null");
        assertTrue(programaObra.getCongeladoAt().isBefore(java.time.LocalDateTime.now().plusSeconds(1)), 
                "congeladoAt debe ser un timestamp reciente");
        assertEquals("v1", programaObra.getSnapshotAlgorithm(), 
                "snapshotAlgorithm debe ser 'v1'");
    }

    @Test
    void freeze_debeSerIrreversible() {
        // Given
        ProgramaObra programaObra = ProgramaObra.crear(programaObraId, proyectoId, fechaInicio, fechaFinEstimada);
        programaObra.congelar(approvedBy);
        
        // When/Then - Intentar congelar nuevamente no debe cambiar el estado
        // (aunque no hay método para intentar congelar nuevamente, verificamos que el estado persiste)
        assertTrue(programaObra.estaCongelado(), 
                "ProgramaObra debe permanecer congelado");
        
        // Verificar que no se puede modificar
        assertThrows(CronogramaCongeladoException.class, () -> {
            programaObra.actualizarFechas(fechaInicio.plusDays(1), fechaFinEstimada.plusDays(1));
        }, "No se debe poder modificar fechas después de congelar");
    }
}
