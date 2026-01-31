package com.budgetpro.tools.naming.engine;

import com.budgetpro.tools.naming.layer.ArchitecturalLayer;
import com.budgetpro.tools.naming.layer.LayerDetector;
import com.budgetpro.tools.naming.model.ValidationRule;
import com.budgetpro.tools.naming.rules.DomainEntityRule;
import com.budgetpro.tools.naming.rules.JpaEntityRule;
import com.budgetpro.tools.naming.scanner.ClassDeclarationExtractor;
import com.budgetpro.tools.naming.scanner.JavaFileScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ValidationEngineTest {

    private JavaFileScanner scanner;
    private ClassDeclarationExtractor extractor;
    private LayerDetector detector;
    private ValidationEngine engine;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = Mockito.mock(JavaFileScanner.class);
        extractor = Mockito.mock(ClassDeclarationExtractor.class);
        detector = Mockito.mock(LayerDetector.class);

        Map<ArchitecturalLayer, ValidationRule> rules = new HashMap<>();
        rules.put(ArchitecturalLayer.DOMAIN_ENTITY, new DomainEntityRule(null));
        rules.put(ArchitecturalLayer.JPA_ENTITY, new JpaEntityRule(null));

        engine = new ValidationEngine(scanner, extractor, detector, rules);
    }

    @Test
    void validate_WithViolations_ReturnsViolations() throws IOException {
        Path filePath = tempDir.resolve("TestEntity.java");
        Files.writeString(filePath, "public class TestEntity {}");

        when(scanner.scanJavaFiles(any())).thenReturn(List.of(filePath));
        when(extractor.extractClassName(any())).thenReturn(Optional.of("TestEntity"));
        when(detector.detectLayer(any(), any())).thenReturn(ArchitecturalLayer.DOMAIN_ENTITY);

        ValidationResult result = engine.validate(tempDir);

        assertFalse(result.getAllViolations().isEmpty());
        // El default de DomainEntity forbidden suffixes es Entity, JpaEntity.
        // TestEntity termina en Entity -> 1 violación.
        assertEquals(1, result.getTotalViolationCount());
    }
}
