package com.budgetpro.validator.boundary.report;

import com.budgetpro.validator.boundary.config.BoundaryConfig;
import com.budgetpro.validator.boundary.scanner.DomainScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViolationReporterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFindViolationsInDomain() throws IOException {
        Path domainDir = tempDir.resolve("domain");
        Files.createDirectories(domainDir);

        Path modelFile = domainDir.resolve("Model.java");
        Files.write(modelFile, List.of("package com.budgetpro.domain.model;",
                "import org.springframework.stereotype.Component;", "import java.util.List;", "public class Model {}"));

        BoundaryConfig config = new BoundaryConfig(List.of("org.springframework.*"), List.of("java.*"));

        ViolationReporter reporter = new ViolationReporter(new DomainScanner(), config);
        List<BoundaryViolation> violations = reporter.validateDomain(domainDir);

        assertEquals(1, violations.size());
        assertEquals(modelFile, violations.get(0).file());
        assertEquals("org.springframework.stereotype.Component", violations.get(0).forbiddenImport());
    }

    @Test
    void shouldReturnEmptyListWhenNoViolations() throws IOException {
        Path domainDir = tempDir.resolve("domain");
        Files.createDirectories(domainDir);

        Path modelFile = domainDir.resolve("Model.java");
        Files.write(modelFile,
                List.of("package com.budgetpro.domain.model;", "import java.util.List;", "public class Model {}"));

        BoundaryConfig config = new BoundaryConfig(List.of("org.springframework.*"), List.of("java.*"));

        ViolationReporter reporter = new ViolationReporter(new DomainScanner(), config);
        List<BoundaryViolation> violations = reporter.validateDomain(domainDir);

        assertTrue(violations.isEmpty());
    }
}
