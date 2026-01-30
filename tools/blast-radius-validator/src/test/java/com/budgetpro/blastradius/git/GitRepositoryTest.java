package com.budgetpro.blastradius.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para GitRepository.
 */
class GitRepositoryTest {
    
    @Test
    void testRetrieveStagedFilesFromGitRepository(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio Git temporal y stagear 3 archivos
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            // Crear y stagear archivos
            Path file1 = repoPath.resolve("file1.java");
            Files.createDirectories(repoPath.resolve("src/main"));
            Files.createDirectories(repoPath.resolve("domain/test"));
            Path file2 = repoPath.resolve("src/main/File2.java");
            Path file3 = repoPath.resolve("domain/test/File3.java");
            
            Files.writeString(file1, "public class File1 {}");
            Files.writeString(file2, "public class File2 {}");
            Files.writeString(file3, "public class File3 {}");
            
            git.add().addFilepattern("file1.java").call();
            git.add().addFilepattern("src/main/File2.java").call();
            git.add().addFilepattern("domain/test/File3.java").call();
            
            // Action: Obtener archivos staged
            GitRepository gitRepo = new GitRepository(repoPath);
            List<StagedFile> stagedFiles = gitRepo.getStagedFiles();
            
            // Expect: Lista contiene 3 StagedFile objects con paths correctos
            assertEquals(3, stagedFiles.size());
            
            Set<String> paths = stagedFiles.stream()
                .map(StagedFile::path)
                .collect(Collectors.toSet());
            
            assertTrue(paths.contains("file1.java"));
            assertTrue(paths.contains("src/main/File2.java"));
            assertTrue(paths.contains("domain/test/File3.java"));
        }
    }
    
    @Test
    void testPathNormalizationOnWindowsStylePaths(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio y archivo con path que podría tener backslashes
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            // Crear archivo en subdirectorio
            Path subDir = repoPath.resolve("src").resolve("main");
            Files.createDirectories(subDir);
            Path file = subDir.resolve("Test.java");
            Files.writeString(file, "public class Test {}");
            
            git.add().addFilepattern("src/main/Test.java").call();
            
            // Action: Obtener archivos staged
            GitRepository gitRepo = new GitRepository(repoPath);
            List<StagedFile> stagedFiles = gitRepo.getStagedFiles();
            
            // Expect: Todos los paths usan forward slashes
            assertEquals(1, stagedFiles.size());
            String path = stagedFiles.get(0).path();
            assertFalse(path.contains("\\"), "Path should not contain backslashes: " + path);
            assertTrue(path.contains("/"), "Path should contain forward slashes: " + path);
            assertEquals("src/main/Test.java", path);
        }
    }
    
    @Test
    void testDetectOverrideKeywordInCommitMessage() {
        // Setup: Mensaje de commit conteniendo "BIGBANG_APPROVED"
        String message = "feat: Add new feature\n\nBIGBANG_APPROVED\n\nThis change is approved.";
        CommitMessage commitMessage = new CommitMessage(message);
        
        // Action: Verificar si contiene override keyword
        boolean hasOverride = commitMessage.hasOverrideKeyword("BIGBANG_APPROVED");
        
        // Expect: Retorna true
        assertTrue(hasOverride);
    }
    
    @Test
    void testOverrideKeywordDetectionIsCaseSensitive() {
        // Setup: Mensaje con keyword en minúsculas
        String message = "feat: Add feature bigbang_approved";
        CommitMessage commitMessage = new CommitMessage(message);
        
        // Action: Buscar keyword en mayúsculas
        boolean hasOverride = commitMessage.hasOverrideKeyword("BIGBANG_APPROVED");
        
        // Expect: Retorna false (case-sensitive)
        assertFalse(hasOverride);
    }
    
    @Test
    void testOverrideKeywordNotFound() {
        // Setup: Mensaje sin keyword
        String message = "feat: Add new feature";
        CommitMessage commitMessage = new CommitMessage(message);
        
        // Action: Buscar keyword
        boolean hasOverride = commitMessage.hasOverrideKeyword("BIGBANG_APPROVED");
        
        // Expect: Retorna false
        assertFalse(hasOverride);
    }
    
    @Test
    void testHandleRepositoryNotFound(@TempDir Path tempDir) throws IOException {
        // Setup: Directorio que no es un repositorio Git
        Path nonGitDir = tempDir.resolve("not-a-repo");
        Files.createDirectories(nonGitDir);
        
        // Action: Intentar crear GitRepository desde directorio no-Git
        GitRepository.RepositoryNotFoundException exception = assertThrows(
            GitRepository.RepositoryNotFoundException.class,
            () -> new GitRepository(nonGitDir)
        );
        
        // Expect: Excepción indicando que el repositorio no se encontró
        assertTrue(exception.getMessage().contains("not found"));
    }
    
    @Test
    void testGetCommitMessageFromFile(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio y archivo COMMIT_EDITMSG
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            // Crear archivo COMMIT_EDITMSG
            Path gitDir = repoPath.resolve(".git");
            Path commitEditMsg = gitDir.resolve("COMMIT_EDITMSG");
            Files.writeString(commitEditMsg, "feat: Add new feature\n\nBIGBANG_APPROVED");
            
            // Action: Leer mensaje de commit
            GitRepository gitRepo = new GitRepository(repoPath);
            CommitMessage commitMessage = gitRepo.getCommitMessage();
            
            // Expect: Mensaje leído correctamente
            assertFalse(commitMessage.isEmpty());
            assertTrue(commitMessage.getMessage().contains("feat: Add new feature"));
            assertTrue(commitMessage.hasOverrideKeyword("BIGBANG_APPROVED"));
        }
    }
    
    @Test
    void testGetCommitMessageFromString() {
        // Setup: Mensaje proporcionado como string
        String message = "feat: Add feature\n\nBIGBANG_APPROVED";
        
        // Action: Crear CommitMessage desde string
        GitRepository gitRepo;
        try {
            // Necesitamos un repositorio válido, pero no usaremos el archivo
            Path repoPath = Path.of("."); // Usar directorio actual (asumiendo que estamos en un repo)
            gitRepo = new GitRepository(repoPath);
        } catch (GitRepository.RepositoryNotFoundException e) {
            // Si no estamos en un repo, crear uno temporal
            // Para este test, solo probamos el método que acepta string
            CommitMessage commitMessage = new CommitMessage(message);
            assertTrue(commitMessage.hasOverrideKeyword("BIGBANG_APPROVED"));
            return;
        }
        
        CommitMessage commitMessage = gitRepo.getCommitMessage(message);
        
        // Expect: Mensaje correcto
        assertEquals(message.trim(), commitMessage.getMessage());
        assertTrue(commitMessage.hasOverrideKeyword("BIGBANG_APPROVED"));
    }
    
    @Test
    void testGetCommitMessageRemovesGitComments(@TempDir Path tempDir) throws Exception {
        // Setup: Crear COMMIT_EDITMSG con comentarios de Git
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            Path gitDir = repoPath.resolve(".git");
            String messageWithComments = """
                feat: Add feature
                
                # Please enter the commit message for your changes.
                # Lines starting with '#' will be ignored.
                # This is a comment
                BIGBANG_APPROVED
                """;
            Files.writeString(gitDir.resolve("COMMIT_EDITMSG"), messageWithComments);
            
            // Action: Leer mensaje
            GitRepository gitRepo = new GitRepository(repoPath);
            CommitMessage commitMessage = gitRepo.getCommitMessage();
            
            // Expect: Comentarios removidos
            String message = commitMessage.getMessage();
            assertFalse(message.contains("#"));
            assertTrue(message.contains("feat: Add feature"));
            assertTrue(commitMessage.hasOverrideKeyword("BIGBANG_APPROVED"));
        }
    }
    
    @Test
    void testGetCommitMessageWhenFileDoesNotExist(@TempDir Path tempDir) throws Exception {
        // Setup: Repositorio sin COMMIT_EDITMSG
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            // Action: Leer mensaje cuando no existe el archivo
            GitRepository gitRepo = new GitRepository(repoPath);
            CommitMessage commitMessage = gitRepo.getCommitMessage();
            
            // Expect: Mensaje vacío
            assertTrue(commitMessage.isEmpty());
        }
    }
    
    @Test
    void testRepositoryDiscoveryFromSubdirectory(@TempDir Path tempDir) throws Exception {
        // Setup: Crear repositorio y subdirectorio
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            Path subDir = repoPath.resolve("src").resolve("main").resolve("deep");
            Files.createDirectories(subDir);
            
            // Crear y stagear archivo
            Path file = subDir.resolve("Test.java");
            Files.writeString(file, "public class Test {}");
            git.add().addFilepattern("src/main/deep/Test.java").call();
            
            // Action: Crear GitRepository desde subdirectorio
            GitRepository gitRepo = new GitRepository(subDir);
            
            // Expect: Repositorio encontrado correctamente
            assertEquals(repoPath, gitRepo.getRepositoryPath());
            List<StagedFile> stagedFiles = gitRepo.getStagedFiles();
            assertEquals(1, stagedFiles.size());
            assertEquals("src/main/deep/Test.java", stagedFiles.get(0).path());
        }
    }
    
    @Test
    void testStagedFileNormalization() {
        // Test del método estático de normalización
        String windowsPath = "src\\main\\Test.java";
        String normalized = StagedFile.normalizePath(windowsPath);
        
        assertEquals("src/main/Test.java", normalized);
        assertFalse(normalized.contains("\\"));
    }
    
    @Test
    void testStagedFileWithNullPath() {
        // Verificar que StagedFile rechaza null
        assertThrows(NullPointerException.class, () -> new StagedFile(null));
    }
    
    @Test
    void testStagedFileWithEmptyPath() {
        // Verificar que StagedFile rechaza path vacío
        assertThrows(IllegalArgumentException.class, () -> new StagedFile(""));
        assertThrows(IllegalArgumentException.class, () -> new StagedFile("   "));
    }
    
    @Test
    void testEmptyRepositoryStagedFiles(@TempDir Path tempDir) throws Exception {
        // Setup: Repositorio vacío (sin commits)
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            // Crear y stagear archivo en repositorio vacío
            Path file = repoPath.resolve("file.java");
            Files.writeString(file, "public class File {}");
            git.add().addFilepattern("file.java").call();
            
            // Action: Obtener archivos staged
            GitRepository gitRepo = new GitRepository(repoPath);
            List<StagedFile> stagedFiles = gitRepo.getStagedFiles();
            
            // Expect: Archivo staged encontrado
            assertEquals(1, stagedFiles.size());
            assertEquals("file.java", stagedFiles.get(0).path());
        }
    }
    
    @Test
    void testMultipleStagedFilesInDifferentDirectories(@TempDir Path tempDir) throws Exception {
        // Setup: Stagear múltiples archivos en diferentes directorios
        Path repoPath = tempDir.resolve("test-repo");
        Files.createDirectories(repoPath);
        
        try (Git git = Git.init().setDirectory(repoPath.toFile()).call()) {
            // Crear estructura de directorios y archivos
            Files.createDirectories(repoPath.resolve("domain/presupuesto"));
            Files.createDirectories(repoPath.resolve("infrastructure/persistence"));
            
            Files.writeString(repoPath.resolve("domain/presupuesto/File1.java"), "class File1 {}");
            Files.writeString(repoPath.resolve("infrastructure/persistence/File2.java"), "class File2 {}");
            Files.writeString(repoPath.resolve("File3.java"), "class File3 {}");
            
            git.add().addFilepattern(".").call();
            
            // Action: Obtener archivos staged
            GitRepository gitRepo = new GitRepository(repoPath);
            List<StagedFile> stagedFiles = gitRepo.getStagedFiles();
            
            // Expect: Todos los archivos encontrados
            assertTrue(stagedFiles.size() >= 3);
            Set<String> paths = stagedFiles.stream()
                .map(StagedFile::path)
                .collect(Collectors.toSet());
            
            assertTrue(paths.contains("domain/presupuesto/File1.java"));
            assertTrue(paths.contains("infrastructure/persistence/File2.java"));
            assertTrue(paths.contains("File3.java"));
        }
    }
}
