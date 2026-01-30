package com.budgetpro.tools.naming.engine;

import com.budgetpro.tools.naming.layer.ArchitecturalLayer;
import com.budgetpro.tools.naming.layer.LayerDetector;
import com.budgetpro.tools.naming.model.NamingViolation;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.model.ViolationSeverity;
import com.budgetpro.tools.naming.rules.DomainEntityRule;
import com.budgetpro.tools.naming.rules.JpaEntityRule;
import com.budgetpro.tools.naming.scanner.ClassDeclarationExtractor;
import com.budgetpro.tools.naming.scanner.JavaFileScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValidationEngineTest {

    @TempDir
    Path tempDir;

    private ValidationEngine engine;

    @BeforeEach
    void setUp() {
        JavaFileScanner scanner = new JavaFileScanner();
        ClassDeclarationExtractor extractor = new ClassDeclarationExtractor();
        LayerDetector detector = new LayerDetector();

        Map<ArchitecturalLayer, ValidationRule> rules = new HashMap<>();
        rules.put(ArchitecturalLayer.DOMAIN_ENTITY, new DomainEntityRule());
        rules.put(ArchitecturalLayer.JPA_ENTITY, new JpaEntityRule());
        // Se pueden añadir más reglas según sea necesario para las pruebas

        engine = new ValidationEngine(scanner, extractor, detector, rules);
    }

    @Test
    void testValidateNoViolations() throws IOException {
        Path domainDir = Files.createDirectories(tempDir.resolve("src/main/java/com/budgetpro/domain/model"));
        Files.writeString(domainDir.resolve("Presupuesto.java"), "public class Presupuesto {}");

        ValidationResult result = engine.validate(tempDir);

        assertEquals(0, result.getTotalViolationCount());
        assertFalse(result.hasBlockingViolations());
    }

    @Test
    void testValidateWithBlockingViolations() throws IOException {
        Path domainDir = Files.createDirectories(tempDir.resolve("src/main/java/com/budgetpro/domain/model"));
        Files.writeString(domainDir.resolve("PresupuestoEntity.java"), "public class PresupuestoEntity {}");

        ValidationResult result = engine.validate(tempDir);

        assertEquals(1, result.getTotalViolationCount());
        assertTrue(result.hasBlockingViolations());
    }

    @Test
    void testValidateWithWarningViolations() throws IOException {
        Path infraDir = Files
                .createDirectories(tempDir.resolve("src/main/java/com/budgetpro/infrastructure/persistence/entity"));
        Files.writeString(infraDir.resolve("Partida.java"), "public class Partida {}");

        ValidationResult result = engine.validate(tempDir);

        assertEquals(1, result.getTotalViolationCount());
        assertFalse(result.hasBlockingViolations());
        assertEquals(1, result.getWarningViolations().size());
    }

    @Test
    void testMixedViolations() throws IOException {
        Path domainDir = Files.createDirectories(tempDir.resolve("src/main/java/com/budgetpro/domain/model"));
        Files.writeString(domainDir.resolve("PresupuestoEntity.java"), "public class PresupuestoEntity {}");

        Path infraDir = Files
                .createDirectories(tempDir.resolve("src/main/java/com/budgetpro/infrastructure/persistence/entity"));
        Files.writeString(infraDir.resolve("Partida.java"), "public class Partida {}");

        ValidationResult result = engine.validate(tempDir);

        assertEquals(2, result.getTotalViolationCount());
        assertEquals(1, result.getBlockingViolations().size());
        assertEquals(1, result.getWarningViolations().size());
    }

    @Test
    void testSkipUnknownLayer() throws IOException {
        Path utilDir = Files.createDirectories(tempDir.resolve("src/main/java/com/budgetpro/util"));
        Files.writeString(utilDir.resolve("StringUtils.java"), "public class StringUtils {}");

        ValidationResult result = engine.validate(tempDir);

        assertEquals(0, result.getTotalViolationCount());
    }

    @Test
    void testSkipMalformedFile() throws IOException {
        Path domainDir = Files.createDirectories(tempDir.resolve("src/main/java/com/budgetpro/domain/model"));
        Files.writeString(domainDir.resolve("Invalid.java"), "not a java class content");

        ValidationResult result = engine.validate(tempDir);

        assertEquals(0, result.getTotalViolationCount());
    }
}
