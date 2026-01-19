package com.budgetpro.domain.finanzas.presupuesto.exception;

import com.budgetpro.domain.finanzas.presupuesto.model.PresupuestoId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BudgetIntegrityViolationException.
 * 
 * Verifica que la excepción se crea correctamente con todos los campos
 * y que el mensaje se formatea apropiadamente con hashes truncados.
 */
class BudgetIntegrityViolationExceptionTest {

    @Test
    void constructor_debeCrearExcepcionConTodosLosCampos() {
        // Given
        PresupuestoId id = PresupuestoId.from(UUID.randomUUID());
        String expectedHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String actualHash = "x9y8z7w6v5u4321098765432109876543210987654321098765432109876543210987";
        String violationType = "Tampering detected";

        // When
        BudgetIntegrityViolationException ex = new BudgetIntegrityViolationException(
                id, expectedHash, actualHash, violationType
        );

        // Then
        assertNotNull(ex);
        assertEquals(id, ex.getPresupuestoId());
        assertEquals(expectedHash, ex.getExpectedHash());
        assertEquals(actualHash, ex.getActualHash());
        assertEquals(violationType, ex.getViolationType());
    }

    @Test
    void constructor_debeFormatearMensajeConHashesTruncados() {
        // Given
        PresupuestoId id = PresupuestoId.from(UUID.randomUUID());
        String expectedHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String actualHash = "x9y8z7w6v5u4321098765432109876543210987654321098765432109876543210987";
        String violationType = "Tampering detected";

        // When
        BudgetIntegrityViolationException ex = new BudgetIntegrityViolationException(
                id, expectedHash, actualHash, violationType
        );

        // Then
        String message = ex.getMessage();
        assertNotNull(message);
        assertTrue(message.contains("integrity violation"), "Mensaje debe contener 'integrity violation'");
        assertTrue(message.contains("a1b2c3d4e5f67890..."), "Mensaje debe contener hash esperado truncado");
        assertTrue(message.contains("x9y8z7w6v5u43210..."), "Mensaje debe contener hash actual truncado");
        assertTrue(message.contains(violationType), "Mensaje debe contener el tipo de violación");
        assertTrue(message.contains(id.getValue().toString()), "Mensaje debe contener el ID del presupuesto");
    }

    @Test
    void constructor_debeManejarHashesNull() {
        // Given
        PresupuestoId id = PresupuestoId.from(UUID.randomUUID());
        String expectedHash = null;
        String actualHash = null;
        String violationType = "Structure modification attempted";

        // When
        BudgetIntegrityViolationException ex = new BudgetIntegrityViolationException(
                id, expectedHash, actualHash, violationType
        );

        // Then
        assertNotNull(ex);
        assertNull(ex.getExpectedHash());
        assertNull(ex.getActualHash());
        assertTrue(ex.getMessage().contains("null"), "Mensaje debe manejar hashes null");
    }

    @Test
    void constructor_debeManejarHashesCortos() {
        // Given
        PresupuestoId id = PresupuestoId.from(UUID.randomUUID());
        String expectedHash = "short";
        String actualHash = "hash";
        String violationType = "Hash mismatch";

        // When
        BudgetIntegrityViolationException ex = new BudgetIntegrityViolationException(
                id, expectedHash, actualHash, violationType
        );

        // Then
        assertNotNull(ex);
        assertEquals(expectedHash, ex.getExpectedHash());
        assertEquals(actualHash, ex.getActualHash());
        // Hashes cortos no deben truncarse
        assertTrue(ex.getMessage().contains(expectedHash), "Mensaje debe contener hash corto completo");
        assertTrue(ex.getMessage().contains(actualHash), "Mensaje debe contener hash corto completo");
    }

    @Test
    void constructor_debeLanzarNullPointerExceptionSiPresupuestoIdEsNull() {
        // Given
        PresupuestoId id = null;
        String expectedHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String actualHash = "x9y8z7w6v5u4321098765432109876543210987654321098765432109876543210987";
        String violationType = "Tampering detected";

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            new BudgetIntegrityViolationException(id, expectedHash, actualHash, violationType);
        }, "Debe lanzar NullPointerException si presupuestoId es null");
    }

    @Test
    void getters_debenRetornarValoresCorrectos() {
        // Given
        PresupuestoId id = PresupuestoId.from(UUID.randomUUID());
        String expectedHash = "expected123456789012345678901234567890123456789012345678901234567890";
        String actualHash = "actual12345678901234567890123456789012345678901234567890123456789012";
        String violationType = "Structure modification attempted";

        // When
        BudgetIntegrityViolationException ex = new BudgetIntegrityViolationException(
                id, expectedHash, actualHash, violationType
        );

        // Then
        assertEquals(id, ex.getPresupuestoId());
        assertEquals(expectedHash, ex.getExpectedHash());
        assertEquals(actualHash, ex.getActualHash());
        assertEquals(violationType, ex.getViolationType());
    }

    @Test
    void mensaje_debeIncluirTodosLosComponentes() {
        // Given
        UUID uuid = UUID.randomUUID();
        PresupuestoId id = PresupuestoId.from(uuid);
        String expectedHash = "a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890ab";
        String actualHash = "x9y8z7w6v5u4321098765432109876543210987654321098765432109876543210987";
        String violationType = "Hash mismatch";

        // When
        BudgetIntegrityViolationException ex = new BudgetIntegrityViolationException(
                id, expectedHash, actualHash, violationType
        );

        // Then
        String message = ex.getMessage();
        assertTrue(message.contains(uuid.toString()), "Mensaje debe contener UUID del presupuesto");
        assertTrue(message.contains("Expected hash"), "Mensaje debe mencionar hash esperado");
        assertTrue(message.contains("Actual hash"), "Mensaje debe mencionar hash actual");
        assertTrue(message.contains(violationType), "Mensaje debe contener tipo de violación");
    }
}
