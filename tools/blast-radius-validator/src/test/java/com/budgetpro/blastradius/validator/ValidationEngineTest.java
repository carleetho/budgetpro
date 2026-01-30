package com.budgetpro.blastradius.validator;

import com.budgetpro.blastradius.classifier.ClassifiedFiles;
import com.budgetpro.blastradius.classifier.Zone;
import com.budgetpro.blastradius.config.BlastRadiusConfig;
import com.budgetpro.blastradius.git.StagedFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para ValidationEngine.
 */
class ValidationEngineTest {
    
    @Test
    void testValidationSuccess(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio Git con archivos staged dentro de límites
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.init()
            .setDirectory(repoPath.toFile())
            .call();
        
        // Crear archivos en green zone (dentro de límites)
        Files.createDirectories(repoPath.resolve("utils"));
        Files.writeString(repoPath.resolve("utils/File1.java"), "class File1 {}");
        Files.writeString(repoPath.resolve("utils/File2.java"), "class File2 {}");
        
        git.add().addFilepattern("utils/").call();
        
        // Crear configuración
        Path configFile = tempDir.resolve("config.json");
        String configJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": ["domain/presupuesto/"],
              "yellow_zone_paths": ["infrastructure/persistence/"]
            }
            """;
        Files.writeString(configFile, configJson);
        
        // Action: Validar
        ValidationEngine engine = new ValidationEngine();
        ValidationResult result = engine.validate(repoPath, configFile);
        
        // Expect: Validación exitosa
        assertTrue(result.isSuccess());
        assertEquals(0, result.getExitCode());
        assertFalse(result.isOverrideDetected());
        assertNotNull(result.getClassifiedFiles());
        assertEquals(2, result.getClassifiedFiles().getTotalCount());
    }
    
    @Test
    void testValidationFailureTotalFilesExceeded(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio con más archivos que el límite
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.init()
            .setDirectory(repoPath.toFile())
            .call();
        
        // Crear 15 archivos (límite es 10)
        for (int i = 1; i <= 15; i++) {
            Files.writeString(repoPath.resolve("file" + i + ".java"), "class File" + i + " {}");
            git.add().addFilepattern("file" + i + ".java").call();
        }
        
        Path configFile = tempDir.resolve("config.json");
        String configJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": ["domain/"],
              "yellow_zone_paths": ["infrastructure/"]
            }
            """;
        Files.writeString(configFile, configJson);
        
        // Action: Validar
        ValidationEngine engine = new ValidationEngine();
        ValidationResult result = engine.validate(repoPath, configFile);
        
        // Expect: Validación fallida
        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
        assertTrue(result.hasViolations());
        assertEquals(1, result.getViolations().size());
        assertEquals(Violation.ViolationType.TOTAL_FILES_EXCEEDED, 
                     result.getViolations().get(0).getType());
    }
    
    @Test
    void testValidationFailureRedZoneExceeded(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio con 2 archivos en red zone (límite es 1)
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.init()
            .setDirectory(repoPath.toFile())
            .call();
        
        Files.createDirectories(repoPath.resolve("domain/presupuesto"));
        Files.writeString(repoPath.resolve("domain/presupuesto/File1.java"), "class File1 {}");
        Files.writeString(repoPath.resolve("domain/presupuesto/File2.java"), "class File2 {}");
        
        git.add().addFilepattern("domain/").call();
        
        Path configFile = tempDir.resolve("config.json");
        String configJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": ["domain/presupuesto/"],
              "yellow_zone_paths": ["infrastructure/"]
            }
            """;
        Files.writeString(configFile, configJson);
        
        // Action: Validar
        ValidationEngine engine = new ValidationEngine();
        ValidationResult result = engine.validate(repoPath, configFile);
        
        // Expect: Validación fallida por red zone
        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
        assertTrue(result.hasViolations());
        assertEquals(1, result.getViolations().size());
        assertEquals(Violation.ViolationType.RED_ZONE_EXCEEDED, 
                     result.getViolations().get(0).getType());
        assertEquals(Zone.RED, result.getViolations().get(0).getZone());
    }
    
    @Test
    void testOverrideKeywordBypassesValidation(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio con override keyword en commit message
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.init()
            .setDirectory(repoPath.toFile())
            .call();
        
        // Crear muchos archivos que excederían límites
        for (int i = 1; i <= 20; i++) {
            Files.writeString(repoPath.resolve("file" + i + ".java"), "class File" + i + " {}");
            git.add().addFilepattern("file" + i + ".java").call();
        }
        
        // Crear COMMIT_EDITMSG con override keyword
        Path gitDir = repoPath.resolve(".git");
        Files.writeString(gitDir.resolve("COMMIT_EDITMSG"), 
            "feat: Add feature\n\nBIGBANG_APPROVED\n\nThis is approved");
        
        Path configFile = tempDir.resolve("config.json");
        String configJson = """
            {
              "max_files_without_approval": 10,
              "max_files_red_zone": 1,
              "max_files_yellow_zone": 3,
              "red_zone_paths": ["domain/"],
              "yellow_zone_paths": ["infrastructure/"],
              "override_keyword": "BIGBANG_APPROVED"
            }
            """;
        Files.writeString(configFile, configJson);
        
        // Action: Validar
        ValidationEngine engine = new ValidationEngine();
        ValidationResult result = engine.validate(repoPath, configFile);
        
        // Expect: Validación exitosa debido a override
        assertTrue(result.isSuccess());
        assertTrue(result.isOverrideDetected());
        assertEquals(0, result.getExitCode());
        assertFalse(result.hasViolations());
    }
    
    @Test
    void testValidationUsesDefaultConfigWhenNoConfigFile(@TempDir Path tempDir) throws Exception {
        // Setup: Repositorio sin archivo de configuración
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        org.eclipse.jgit.api.Git git = org.eclipse.jgit.api.Git.init()
            .setDirectory(repoPath.toFile())
            .call();
        
        Files.createDirectories(repoPath.resolve("utils"));
        Files.writeString(repoPath.resolve("utils/File.java"), "class File {}");
        git.add().addFilepattern("utils/").call();
        
        // Action: Validar sin config file
        ValidationEngine engine = new ValidationEngine();
        ValidationResult result = engine.validate(repoPath, null);
        
        // Expect: Usa configuración por defecto y valida exitosamente
        assertTrue(result.isSuccess());
        assertNotNull(result.getClassifiedFiles());
    }
    
    @Test
    void testValidationErrorOnNonExistentRepository(@TempDir Path tempDir) {
        // Setup: Path que no existe
        Path nonExistentPath = tempDir.resolve("non-existent");
        
        // Action: Validar
        ValidationEngine engine = new ValidationEngine();
        ValidationResult result = engine.validate(nonExistentPath, null);
        
        // Expect: Error
        assertTrue(result.isError());
        assertEquals(2, result.getExitCode());
        assertNotNull(result.getErrorMessage());
    }
}
