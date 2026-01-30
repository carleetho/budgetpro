package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ViolationReporterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private ViolationReporter reporter;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        reporter = new ViolationReporter();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testFormatErrorViolation() {
        TransitionViolation violation = new TransitionViolation(ViolationSeverity.CRITICAL,
                "src/main/java/com/budgetpro/domain/Presupuesto.java", 145, "CONGELADO", "BORRADOR", List.of(),
                TransitionViolation.ViolationType.INVALID_TRANSITION, "Transición inválida", "Presupuesto", "revertir");

        reporter.report(List.of(violation));

        String output = outContent.toString();

        assertTrue(output.contains("❌ ERROR: Invalid state transition in Presupuesto.java:145"));
        assertTrue(output.contains("Attempted: CONGELADO → BORRADOR"));
        assertTrue(output.contains("Valid transitions from CONGELADO: (none - final state)"));
    }

    @Test
    void testFormatWarningViolation() {
        TransitionViolation violation = new TransitionViolation(ViolationSeverity.WARNING,
                "src/main/java/com/budgetpro/domain/Presupuesto.java", 89, null, "CONGELADO", null,
                TransitionViolation.ViolationType.MISSING_VALIDATION, "Falta lógica de validación", "Presupuesto",
                "aprobar");

        reporter.report(List.of(violation));

        String output = outContent.toString();

        assertTrue(output.contains("⚠️ WARNING: State change without validation in Presupuesto.java:89"));
        assertTrue(output.contains("Method 'aprobar' changes state without checking current state"));
        assertTrue(output.contains("Suggestion: Add validation to ensure current state allows this transition"));
    }

    @Test
    void testGroupingByFile() {
        TransitionViolation v1 = new TransitionViolation(ViolationSeverity.CRITICAL, "FileA.java", 10, "A", "B",
                List.of(), TransitionViolation.ViolationType.INVALID_TRANSITION, "Error", "Class", "Method");
        TransitionViolation v2 = new TransitionViolation(ViolationSeverity.CRITICAL, "FileB.java", 20, "C", "D",
                List.of(), TransitionViolation.ViolationType.INVALID_TRANSITION, "Error", "Class", "Method");
        TransitionViolation v3 = new TransitionViolation(ViolationSeverity.WARNING, "FileA.java", 15, null, "E", null,
                TransitionViolation.ViolationType.MISSING_VALIDATION, "Warning", "Class", "Method");

        reporter.report(List.of(v1, v2, v3));

        String output = outContent.toString();

        // Verificar que hay encabezados de archivo y que están agrupados
        assertTrue(output.contains("File: FileA.java"));
        assertTrue(output.contains("File: FileB.java"));

        // Verificar resumen
        assertTrue(output.contains("Found 3 violations (2 errors, 1 warnings)"));
    }
}
