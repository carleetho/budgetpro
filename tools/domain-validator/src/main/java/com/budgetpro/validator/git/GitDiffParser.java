package com.budgetpro.validator.git;

import com.budgetpro.validator.analyzer.StateMachineConfig;
import com.budgetpro.validator.model.ChangedFile;
import com.budgetpro.validator.model.ChangedFile.ChangeType;
import com.budgetpro.validator.model.LineRange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsea la salida de git diff para detectar archivos Java modificados y sus
 * rangos de líneas.
 */
public class GitDiffParser {

    private static final Pattern DIFF_GIT_PATTERN = Pattern.compile("^diff --git a/(.+) b/(.+)$");
    private static final Pattern HUNK_HEADER_PATTERN = Pattern.compile("^@@ -(\\d+),?(\\d*) \\+(\\d+),?(\\d*) @@.*$");
    private static final Pattern NEW_FILE_PATTERN = Pattern.compile("^new file mode \\d+$");
    private static final Pattern DELETED_FILE_PATTERN = Pattern.compile("^deleted file mode \\d+$");
    private static final Pattern RENAME_FROM_PATTERN = Pattern.compile("^rename from (.+)$");
    private static final Pattern RENAME_TO_PATTERN = Pattern.compile("^rename to (.+)$");

    /**
     * Parsea los archivos cambiados ejecutando un comando de git.
     *
     * @param gitDiffCommand El comando de git a ejecutar (ej: "git diff --cached")
     * @param config         Configuración de máquinas de estado para filtrar
     *                       archivos
     * @return Lista de archivos cambiados que coinciden con la configuración
     */
    public List<ChangedFile> parseChangedFiles(String gitDiffCommand, StateMachineConfig config)
            throws IOException, InterruptedException {
        String[] command = gitDiffCommand.split("\\s+");
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        process.waitFor();
        return parseDiffOutput(lines, config);
    }

    /**
     * Parsea la salida de texto de git diff.
     */
    public List<ChangedFile> parseDiffOutput(List<String> diffLines, StateMachineConfig config) {
        List<ChangedFile> changedFiles = new ArrayList<>();
        String currentFile = null;
        ChangeType currentType = ChangeType.MODIFIED;
        List<LineRange> currentLineRanges = new ArrayList<>();

        for (int i = 0; i < diffLines.size(); i++) {
            String line = diffLines.get(i);

            Matcher diffMatcher = DIFF_GIT_PATTERN.matcher(line);
            if (diffMatcher.matches()) {
                // Guardar el archivo anterior si es válido
                if (currentFile != null && isStateMachineFile(currentFile, config)) {
                    changedFiles.add(new ChangedFile(currentFile, currentType, currentLineRanges));
                }

                // Resetear para el nuevo archivo
                currentFile = diffMatcher.group(2); // Usar la ruta 'b/' (nueva)
                currentType = ChangeType.MODIFIED;
                currentLineRanges = new ArrayList<>();
                continue;
            }

            if (currentFile == null)
                continue;

            if (NEW_FILE_PATTERN.matcher(line).matches()) {
                currentType = ChangeType.ADDED;
            } else if (DELETED_FILE_PATTERN.matcher(line).matches()) {
                currentType = ChangeType.DELETED;
            } else if (RENAME_FROM_PATTERN.matcher(line).matches()) {
                // Opcional: manejar renames si es necesario, por ahora MODIFIED es suficiente
                // para el target
            } else if (RENAME_TO_PATTERN.matcher(line).matches()) {
                currentType = ChangeType.MODIFIED;
            } else {
                Matcher hunkMatcher = HUNK_HEADER_PATTERN.matcher(line);
                if (hunkMatcher.matches()) {
                    int startLine = Integer.parseInt(hunkMatcher.group(3));
                    int count = hunkMatcher.group(4).isEmpty() ? 1 : Integer.parseInt(hunkMatcher.group(4));
                    // El rango en el archivo nuevo es de startLine a startLine + count - 1
                    currentLineRanges.add(new LineRange(startLine, startLine + Math.max(0, count - 1)));
                }
            }
        }

        // Agregar el último archivo
        if (currentFile != null && isStateMachineFile(currentFile, config)) {
            changedFiles.add(new ChangedFile(currentFile, currentType, currentLineRanges));
        }

        return changedFiles.stream().filter(cf -> cf.getChangeType() != ChangeType.DELETED).toList();
    }

    private boolean isStateMachineFile(String filePath, StateMachineConfig config) {
        if (!filePath.endsWith(".java")) {
            return false;
        }

        // Extraer el nombre de la clase (basename sin .java)
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        String className = fileName.substring(0, fileName.lastIndexOf("."));

        // Verificar si la clase está en la configuración
        // El config puede tener FQN o nombres simples.
        for (String configuredClass : config.getStateMachines().keySet()) {
            if (configuredClass.equals(className) || configuredClass.endsWith("." + className)) {
                return true;
            }
        }
        return false;
    }
}
