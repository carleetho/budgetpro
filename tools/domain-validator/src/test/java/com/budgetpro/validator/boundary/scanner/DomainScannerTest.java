package com.budgetpro.validator.boundary.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldScanJavaFilesRecursively() throws IOException {
        Path domainDir = tempDir.resolve("domain");
        Files.createDirectories(domainDir);
        Files.createFile(domainDir.resolve("User.java"));

        Path modelDir = domainDir.resolve("model");
        Files.createDirectories(modelDir);
        Files.createFile(modelDir.resolve("Account.java"));
        Files.createFile(modelDir.resolve("README.md")); // Should be ignored

        DomainScanner scanner = new DomainScanner();
        List<Path> javaFiles = scanner.scanJavaFiles(domainDir);

        assertEquals(2, javaFiles.size());
        assertTrue(javaFiles.stream().anyMatch(p -> p.getFileName().toString().equals("User.java")));
        assertTrue(javaFiles.stream().anyMatch(p -> p.getFileName().toString().equals("Account.java")));
        assertFalse(javaFiles.stream().anyMatch(p -> p.getFileName().toString().equals("README.md")));
    }

    @Test
    void shouldExtractImportsFromFile() throws IOException {
        Path javaFile = tempDir.resolve("Test.java");
        Files.write(javaFile,
                List.of("package com.example;", "", "import java.util.List;", "import java.util.ArrayList;",
                        "import org.springframework.stereotype.Service; // Forbidden but should be extracted", "",
                        "public class Test {}"));

        DomainScanner scanner = new DomainScanner();
        List<String> imports = scanner.extractImports(javaFile);

        assertEquals(3, imports.size());
        assertTrue(imports.contains("java.util.List"));
        assertTrue(imports.contains("java.util.ArrayList"));
        assertTrue(imports.contains("org.springframework.stereotype.Service"));
    }

    @Test
    void shouldStopExtractingAfterClassDeclaration() throws IOException {
        Path javaFile = tempDir.resolve("TestStop.java");
        Files.write(javaFile, List.of("package com.example;", "import java.util.List;", "public class TestStop {",
                "    // String dummy = \"import ignored.package;\"", "    public void hello() {}", "}"));

        DomainScanner scanner = new DomainScanner();
        List<String> imports = scanner.extractImports(javaFile);

        assertEquals(1, imports.size());
        assertEquals("java.util.List", imports.get(0));
    }
}
