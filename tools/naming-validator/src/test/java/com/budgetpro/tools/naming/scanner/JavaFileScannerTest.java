package com.budgetpro.tools.naming.scanner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JavaFileScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void testScanJavaFiles() throws IOException {
        // Setup
        Files.createFile(tempDir.resolve("Test1.java"));
        Files.createFile(tempDir.resolve("Test2.java"));
        Files.createFile(tempDir.resolve("readme.txt"));

        Path subDir = Files.createDirectory(tempDir.resolve("subdir"));
        Files.createFile(subDir.resolve("Test3.java"));

        JavaFileScanner scanner = new JavaFileScanner();

        // Action
        List<Path> results = scanner.scanJavaFiles(tempDir);

        // Expect
        assertEquals(3, results.size());
        assertTrue(results.stream().anyMatch(p -> p.getFileName().toString().equals("Test1.java")));
        assertTrue(results.stream().anyMatch(p -> p.getFileName().toString().equals("Test2.java")));
        assertTrue(results.stream().anyMatch(p -> p.getFileName().toString().equals("Test3.java")));
        assertFalse(results.stream().anyMatch(p -> p.getFileName().toString().equals("readme.txt")));
    }

    @Test
    void testScanInvalidPath() {
        JavaFileScanner scanner = new JavaFileScanner();
        assertThrows(IllegalArgumentException.class, () -> scanner.scanJavaFiles(Path.of("/non/existent/path")));
    }
}
