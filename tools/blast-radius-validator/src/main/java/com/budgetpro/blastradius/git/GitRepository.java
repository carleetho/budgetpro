package com.budgetpro.blastradius.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Integración con Git usando JGit para obtener archivos staged y mensaje de commit.
 */
public class GitRepository {
    
    private final Path repositoryPath;
    private final Path gitDir;
    
    /**
     * Construye un GitRepository desde un directorio.
     * Busca el directorio .git caminando hacia arriba desde el path dado.
     * 
     * @param startPath Directorio desde donde comenzar la búsqueda
     * @throws RepositoryNotFoundException si no se encuentra un repositorio Git
     */
    public GitRepository(Path startPath) throws RepositoryNotFoundException {
        Objects.requireNonNull(startPath, "startPath cannot be null");
        
        this.gitDir = findGitDirectory(startPath);
        if (gitDir == null) {
            throw new RepositoryNotFoundException(
                "Git repository not found. Searched from: " + startPath.toAbsolutePath());
        }
        
        // El repositoryPath es el directorio padre de .git
        this.repositoryPath = gitDir.getParent();
    }
    
    /**
     * Busca el directorio .git caminando hacia arriba desde el path dado.
     * 
     * @param startPath Directorio desde donde comenzar
     * @return Path al directorio .git, o null si no se encuentra
     */
    private Path findGitDirectory(Path startPath) {
        Path current = startPath.toAbsolutePath().normalize();
        
        while (current != null) {
            Path gitDir = current.resolve(".git");
            if (Files.exists(gitDir) && Files.isDirectory(gitDir)) {
                return gitDir;
            }
            
            // También verificar si el directorio actual es .git
            if (current.getFileName() != null && current.getFileName().toString().equals(".git")) {
                return current;
            }
            
            Path parent = current.getParent();
            if (parent == null || parent.equals(current)) {
                break;
            }
            current = parent;
        }
        
        return null;
    }
    
    /**
     * Obtiene todos los archivos actualmente staged en el índice de Git.
     * Equivalente a: git diff --cached --name-only
     * 
     * @return Lista de archivos staged con paths normalizados
     * @throws GitOperationException si hay error al acceder a Git
     */
    public List<StagedFile> getStagedFiles() throws GitOperationException {
        try (Repository repository = openRepository()) {
            List<StagedFile> stagedFiles = new ArrayList<>();
            
            // Obtener HEAD commit
            ObjectId headId = repository.resolve("HEAD");
            
            if (headId != null) {
                // Hay HEAD, comparar HEAD con índice
                try (RevWalk revWalk = new RevWalk(repository)) {
                    RevCommit headCommit = revWalk.parseCommit(headId);
                    CanonicalTreeParser headTreeParser = new CanonicalTreeParser();
                    try (org.eclipse.jgit.lib.ObjectReader reader = repository.newObjectReader()) {
                        headTreeParser.reset(reader, headCommit.getTree().getId());
                    }
                    
                    FileTreeIterator indexTree = new FileTreeIterator(repository);
                    
                    try (DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE)) {
                        diffFormatter.setRepository(repository);
                        diffFormatter.setDetectRenames(true);
                        
                        List<DiffEntry> diffEntries = diffFormatter.scan(headTreeParser, indexTree);
                        
                        for (DiffEntry entry : diffEntries) {
                            String path = entry.getNewPath();
                            if (path != null && !path.equals("/dev/null")) {
                                String normalizedPath = StagedFile.normalizePath(path);
                                stagedFiles.add(new StagedFile(normalizedPath));
                            }
                        }
                    }
                }
            } else {
                // Repositorio vacío, leer directamente del índice
                org.eclipse.jgit.dircache.DirCache index = repository.readDirCache();
                for (int i = 0; i < index.getEntryCount(); i++) {
                    org.eclipse.jgit.dircache.DirCacheEntry entry = index.getEntry(i);
                    String path = entry.getPathString();
                    if (path != null) {
                        String normalizedPath = StagedFile.normalizePath(path);
                        stagedFiles.add(new StagedFile(normalizedPath));
                    }
                }
            }
            
            return stagedFiles;
            
        } catch (IOException e) {
            throw new GitOperationException("Failed to get staged files: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene el mensaje de commit desde el archivo .git/COMMIT_EDITMSG.
     * Este archivo contiene el mensaje del commit en progreso (útil para pre-commit hooks).
     * 
     * @return Mensaje de commit
     * @throws GitOperationException si hay error al leer el archivo
     */
    public CommitMessage getCommitMessage() throws GitOperationException {
        Path commitEditMsg = gitDir.resolve("COMMIT_EDITMSG");
        
        if (!Files.exists(commitEditMsg)) {
            // Si no existe COMMIT_EDITMSG, retornar mensaje vacío
            return new CommitMessage("");
        }
        
        try {
            String message = Files.readString(commitEditMsg);
            // Remover comentarios de Git (líneas que empiezan con #)
            String cleanedMessage = removeGitComments(message);
            return new CommitMessage(cleanedMessage.trim());
        } catch (IOException e) {
            throw new GitOperationException(
                "Failed to read commit message from " + commitEditMsg + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene el mensaje de commit desde un string proporcionado.
     * Útil cuando el mensaje se pasa como parámetro en lugar de leerlo del archivo.
     * 
     * @param message Mensaje de commit
     * @return CommitMessage object
     */
    public CommitMessage getCommitMessage(String message) {
        if (message == null) {
            return new CommitMessage("");
        }
        return new CommitMessage(message.trim());
    }
    
    /**
     * Remueve comentarios de Git del mensaje de commit.
     * Las líneas que empiezan con # son comentarios de Git.
     * 
     * @param message Mensaje original
     * @return Mensaje sin comentarios
     */
    private String removeGitComments(String message) {
        if (message == null) {
            return "";
        }
        
        StringBuilder cleaned = new StringBuilder();
        String[] lines = message.split("\n");
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("#") && !trimmed.isEmpty()) {
                if (cleaned.length() > 0) {
                    cleaned.append("\n");
                }
                cleaned.append(line);
            }
        }
        
        return cleaned.toString();
    }
    
    /**
     * Abre el repositorio JGit.
     * 
     * @return Repository object
     * @throws IOException si hay error al abrir el repositorio
     */
    private Repository openRepository() throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setGitDir(gitDir.toFile());
        builder.setMustExist(true);
        return builder.build();
    }
    
    /**
     * Obtiene el path del repositorio (directorio raíz del proyecto Git).
     * 
     * @return Path del repositorio
     */
    public Path getRepositoryPath() {
        return repositoryPath;
    }
    
    /**
     * Obtiene el path del directorio .git.
     * 
     * @return Path del directorio .git
     */
    public Path getGitDir() {
        return gitDir;
    }
    
    /**
     * Excepción lanzada cuando no se encuentra un repositorio Git.
     */
    public static class RepositoryNotFoundException extends Exception {
        public RepositoryNotFoundException(String message) {
            super(message);
        }
    }
    
    /**
     * Excepción lanzada cuando hay error en operaciones Git.
     */
    public static class GitOperationException extends Exception {
        public GitOperationException(String message) {
            super(message);
        }
        
        public GitOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
