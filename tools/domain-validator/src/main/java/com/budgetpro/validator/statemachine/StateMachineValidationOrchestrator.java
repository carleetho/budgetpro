package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.analyzer.StateAssignmentDetector;
import com.budgetpro.validator.config.StateMachineConfig;
import com.budgetpro.validator.config.StateMachineConfigLoader;
import com.budgetpro.validator.git.GitDiffParser;
import com.budgetpro.validator.model.ChangedFile;
import com.budgetpro.validator.model.StateAssignment;
import com.budgetpro.validator.model.TransitionViolation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Orquestador para la validación de máquinas de estado.
 * 
 * Coordina la carga de configuración, detección de cambios en Git, detección de
 * asignaciones de estado y validación de transiciones.
 */
public class StateMachineValidationOrchestrator {

    private final StateMachineConfigLoader configLoader;
    private final GitDiffParser gitDiffParser;
    private final StateAssignmentDetector assignmentDetector;
    private final TransitionValidator transitionValidator;
    private final ViolationReporter reporter;

    public StateMachineValidationOrchestrator() {
        this.configLoader = new StateMachineConfigLoader();
        this.gitDiffParser = new GitDiffParser();
        this.assignmentDetector = new StateAssignmentDetector();
        this.transitionValidator = new TransitionValidator();
        this.reporter = new ViolationReporter();
    }

    /**
     * Ejecuta el flujo completo de validación.
     */
    public List<TransitionViolation> orchestrate(Path repoPath, Path domainPath, String gitDiffCommand)
            throws Exception {
        // 1. Cargar configuración unificada
        StateMachineConfig config = configLoader.loadDefault();

        // 2. Parsear archivos cambiados en Git
        List<ChangedFile> changedFiles = gitDiffParser.parseChangedFiles(gitDiffCommand, config);

        List<TransitionViolation> allViolations = new ArrayList<>();

        // 3. Para cada archivo, detectar asignaciones y validar
        for (ChangedFile changedFile : changedFiles) {
            Path filePath = repoPath.resolve(changedFile.getFilePath());

            // Detectar asignaciones en el archivo
            List<StateAssignment> assignments = assignmentDetector.detectAssignments(filePath, config);

            // Filtrar asignaciones dentro de los rangos de líneas cambiados
            List<StateAssignment> changedAssignments = assignments.stream()
                    .filter(a -> isAssignmentInChangedRanges(a, changedFile.getChangedLineRanges()))
                    .collect(Collectors.toList());

            // Validar transiciones
            List<TransitionViolation> violations = transitionValidator.validate(changedAssignments, config);
            allViolations.addAll(violations);
        }

        // 4. Reportar violaciones
        reporter.report(allViolations);

        return allViolations;
    }

    private boolean isAssignmentInChangedRanges(StateAssignment assignment,
            List<com.budgetpro.validator.model.LineRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return true;
        }

        for (com.budgetpro.validator.model.LineRange range : ranges) {
            if (assignment.getLineNumber() >= range.startLine() && assignment.getLineNumber() <= range.endLine()) {
                return true;
            }
        }
        return false;
    }
}
