package com.budgetpro.domain.finanzas.presupuesto.service;

import com.budgetpro.domain.finanzas.presupuesto.exception.BudgetIntegrityViolationException;
import com.budgetpro.domain.finanzas.presupuesto.model.EstadoPresupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.IntegrityAuditEntry;
import com.budgetpro.domain.finanzas.presupuesto.model.Presupuesto;
import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import com.budgetpro.domain.finanzas.presupuesto.port.out.IntegrityAuditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para IntegrityAuditLog.
 * 
 * Verifica:
 * - Registro de generación de hashes
 * - Registro de validación de hashes (éxito y fallo)
 * - Registro de violaciones de integridad
 * - Manejo de casos edge (null, valores faltantes)
 */
@ExtendWith(MockitoExtension.class)
class IntegrityAuditLogTest {

    @Mock
    private IntegrityAuditRepository auditRepository;

    private IntegrityAuditLog auditLog;

    private Presupuesto presupuestoAprobado;
    private UUID proyectoId;
    private UUID approvedBy;
    private PresupuestoId presupuestoId;

    @BeforeEach
    void setUp() {
        auditLog = new IntegrityAuditLog(auditRepository);
        
        proyectoId = UUID.randomUUID();
        presupuestoId = PresupuestoId.from(UUID.randomUUID());
        approvedBy = UUID.randomUUID();
        
        String approvalHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String executionHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
        LocalDateTime generatedAt = LocalDateTime.now();

        presupuestoAprobado = Presupuesto.reconstruir(
                presupuestoId,
                proyectoId,
                "Presupuesto Aprobado",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                approvalHash,
                executionHash,
                generatedAt,
                approvedBy,
                "SHA-256-v1"
        );
    }

    @Test
    void logHashGeneration_conPresupuestoAprobado_debeCrearEntradaCorrecta() {
        // When
        auditLog.logHashGeneration(presupuestoAprobado);

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertNotNull(entry);
        assertEquals(presupuestoId.getValue(), entry.getPresupuestoId());
        assertEquals("HASH_GENERATED", entry.getEventType());
        assertEquals(presupuestoAprobado.getIntegrityHashApproval(), entry.getHashApproval());
        assertEquals(presupuestoAprobado.getIntegrityHashExecution(), entry.getHashExecution());
        assertEquals(approvedBy, entry.getValidatedBy());
        assertEquals("SUCCESS", entry.getValidationResult());
        assertEquals("SHA-256-v1", entry.getAlgorithmVersion());
        assertNull(entry.getViolationDetails());
    }

    @Test
    void logHashGeneration_conPresupuestoNull_debeLanzarExcepcion() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            auditLog.logHashGeneration(null);
        });

        verify(auditRepository, never()).save(any());
    }

    @Test
    void logHashGeneration_conPresupuestoSinHash_debeLanzarExcepcion() {
        // Given: Presupuesto sin hash (no aprobado)
        Presupuesto presupuestoSinHash = Presupuesto.crear(
                PresupuestoId.from(UUID.randomUUID()),
                proyectoId,
                "Presupuesto Borrador"
        );

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            auditLog.logHashGeneration(presupuestoSinHash);
        });

        verify(auditRepository, never()).save(any());
    }

    @Test
    void logHashValidation_conValidacionExitosa_debeCrearEntradaSuccess() {
        // Given
        UUID validatedBy = UUID.randomUUID();
        String details = "Validación antes de aprobar compra";

        // When
        auditLog.logHashValidation(presupuestoAprobado, validatedBy, true, details);

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertEquals("HASH_VALIDATED", entry.getEventType());
        assertEquals("SUCCESS", entry.getValidationResult());
        assertEquals(validatedBy, entry.getValidatedBy());
        assertEquals(details, entry.getViolationDetails());
        assertTrue(entry.isSuccess());
        assertFalse(entry.isViolation());
    }

    @Test
    void logHashValidation_conValidacionFallida_debeCrearEntradaFailure() {
        // Given
        UUID validatedBy = UUID.randomUUID();
        String details = "Hash de aprobación no coincide";

        // When
        auditLog.logHashValidation(presupuestoAprobado, validatedBy, false, details);

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertEquals("HASH_VALIDATED", entry.getEventType());
        assertEquals("FAILURE", entry.getValidationResult());
        assertEquals(validatedBy, entry.getValidatedBy());
        assertEquals(details, entry.getViolationDetails());
        assertFalse(entry.isSuccess());
    }

    @Test
    void logHashValidation_conDetailsNull_debeCrearEntradaCorrecta() {
        // Given
        UUID validatedBy = UUID.randomUUID();

        // When
        auditLog.logHashValidation(presupuestoAprobado, validatedBy, true, null);

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertNull(entry.getViolationDetails());
        assertEquals("SUCCESS", entry.getValidationResult());
    }

    @Test
    void logHashValidation_conPresupuestoNull_debeLanzarExcepcion() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            auditLog.logHashValidation(null, UUID.randomUUID(), true, null);
        });

        verify(auditRepository, never()).save(any());
    }

    @Test
    void logIntegrityViolation_debeCrearEntradaConViolationType() {
        // Given
        UUID detectedBy = UUID.randomUUID();
        String expectedHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String actualHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
        String violationType = "Tampering detected";

        BudgetIntegrityViolationException exception = new BudgetIntegrityViolationException(
                presupuestoId,
                expectedHash,
                actualHash,
                violationType
        );

        // When
        auditLog.logIntegrityViolation(exception, detectedBy);

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertEquals("HASH_VIOLATION", entry.getEventType());
        assertEquals("FAILURE", entry.getValidationResult());
        assertEquals(expectedHash, entry.getHashApproval());
        assertEquals(actualHash, entry.getHashExecution());
        assertEquals(detectedBy, entry.getValidatedBy());
        assertEquals(violationType, entry.getViolationDetails());
        assertTrue(entry.isViolation());
        assertFalse(entry.isSuccess());
    }

    @Test
    void logIntegrityViolation_conExceptionNull_debeLanzarExcepcion() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            auditLog.logIntegrityViolation(null, UUID.randomUUID());
        });

        verify(auditRepository, never()).save(any());
    }

    @Test
    void logIntegrityViolation_debeUsarAlgoritmoActual() {
        // Given
        String expectedHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        String actualHash = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210";
        BudgetIntegrityViolationException exception = new BudgetIntegrityViolationException(
                presupuestoId,
                expectedHash,
                actualHash,
                "Test violation"
        );

        // When
        auditLog.logIntegrityViolation(exception, UUID.randomUUID());

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertEquals("SHA-256-v1", entry.getAlgorithmVersion());
    }

    @Test
    void logHashGeneration_debeUsarTimestampDelPresupuesto() {
        // Given: Presupuesto con timestamp específico
        LocalDateTime specificTime = LocalDateTime.of(2026, 1, 19, 12, 0, 0);
        Presupuesto presupuesto = Presupuesto.reconstruir(
                presupuestoId,
                proyectoId,
                "Test",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210",
                specificTime,
                approvedBy,
                "SHA-256-v1"
        );

        // When
        auditLog.logHashGeneration(presupuesto);

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertEquals(specificTime, entry.getValidatedAt());
    }

    @Test
    void logHashGeneration_conTimestampNull_debeUsarTimestampActual() {
        // Given: Presupuesto con timestamp null (edge case)
        Presupuesto presupuesto = Presupuesto.reconstruir(
                presupuestoId,
                proyectoId,
                "Test",
                EstadoPresupuesto.CONGELADO,
                true,
                1L,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210",
                null, // Timestamp null
                approvedBy,
                "SHA-256-v1"
        );

        // When
        LocalDateTime before = LocalDateTime.now();
        auditLog.logHashGeneration(presupuesto);
        LocalDateTime after = LocalDateTime.now();

        // Then
        ArgumentCaptor<IntegrityAuditEntry> entryCaptor = ArgumentCaptor.forClass(IntegrityAuditEntry.class);
        verify(auditRepository, times(1)).save(entryCaptor.capture());

        IntegrityAuditEntry entry = entryCaptor.getValue();
        assertNotNull(entry.getValidatedAt());
        assertTrue(entry.getValidatedAt().isAfter(before.minusSeconds(1)) || entry.getValidatedAt().isEqual(before));
        assertTrue(entry.getValidatedAt().isBefore(after.plusSeconds(1)) || entry.getValidatedAt().isEqual(after));
    }
}
