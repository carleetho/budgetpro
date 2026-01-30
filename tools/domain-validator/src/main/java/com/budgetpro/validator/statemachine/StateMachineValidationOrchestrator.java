package com.budgetpro.validator.statemachine;

import com.budgetpro.validator.analyzer.StateAssignmentDetector;
import com.budgetpro.validator.analyzer.StateMachineConfig;
import com.budgetpro.validator.analyzer.StateMachineDetector;
import com.budgetpro.validator.config.StateMachineConfigLoader;
import com.budgetpro.validator.git.GitDiffParser;
import com.budgetpro.validator.model.ChangedFile;
import com.budgetpro.validator.model.StateAssignment;
import com.budgetpro.validator.model.TransitionViolation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final StateMachineDetector stateMachineDetector;
    private final TransitionValidator transitionValidator;
    private final ViolationReporter reporter;

    public StateMachineValidationOrchestrator() {
        this.configLoader = new StateMachineConfigLoader();
        this.gitDiffParser = new GitDiffParser();
        this.assignmentDetector = new StateAssignmentDetector();
        this.stateMachineDetector = new StateMachineDetector();
        this.transitionValidator = new TransitionValidator();
        this.reporter = new ViolationReporter();
    }

    /**
     * Ejecuta el flujo completo de validación.
     * 
     * @param repoPath       Ruta base del repositorio
     * @param domainPath     Ruta a la capa de dominio
     * @param gitDiffCommand Comando de git para obtener cambios
     * @return Lista de violaciones encontradas
     */
    public List<TransitionViolation> orchestrate(Path repoPath, Path domainPath, String gitDiffCommand)
            throws Exception {
        // 1. Cargar configuración YAML
        com.budgetpro.validator.config.StateMachineConfig yamlConfig = configLoader.loadConfig();

        // 2. Detectar enums de estado en el código fuente
        Map<String, List<String>> detectedEnums = stateMachineDetector.detectStateMachines(domainPath);

        // 3. Unificar en la configuración del analizador
        StateMachineConfig analyzerConfig = buildAnalyzerConfig(yamlConfig, detectedEnums);

        // 4. Parsear archivos cambiados en Git
        List<ChangedFile> changedFiles = gitDiffParser.parseChangedFiles(gitDiffCommand, analyzerConfig);

        List<TransitionViolation> allViolations = new ArrayList<>();

        // 5. Para cada archivo, detectar asignaciones y validar
        for (ChangedFile changedFile : changedFiles) {
            Path filePath = repoPath.resolve(changedFile.getFilePath());

            // Detectar asignaciones en el archivo
            List<StateAssignment> assignments = assignmentDetector.detectAssignments(filePath, analyzerConfig);

            // Filtrar asignaciones dentro de los rangos de líneas cambiados
            List<StateAssignment> changedAssignments = assignments.stream()
                    .filter(a -> isAssignmentInChangedRanges(a, changedFile.getChangedLineRanges()))
                    .collect(Collectors.toList());

            // Validar transiciones
            List<TransitionViolation> violations = transitionValidator.validate(changedAssignments, analyzerConfig);
            allViolations.addAll(violations);
        }

        // 6. Reportar violaciones
        reporter.report(allViolations);

        return allViolations;
    }

    private StateMachineConfig buildAnalyzerConfig(com.budgetpro.validator.config.StateMachineConfig yamlConfig,
            Map<String, List<String>> detectedEnums) {
        Map<String, Map<String, List<String>>> transitions = new HashMap<>();
        Map<String, Set<String>> finalStates = new HashMap<>();

        for (com.budgetpro.validator.config.StateMachineConfig.StateMachineDefinition def : yamlConfig
                .getStateMachines()) {
            String className = def.getClassFqn();
            if (className.contains(".")) {
                className = className.substring(className.lastIndexOf(".") + 1);
            }

            transitions.put(className, def.getTransitions());

            // Identificar estados finales (aquellos sin transiciones salientes en el config
            // o explícitos si hubiera campo)
            Set<String> finals = def.getTransitions().entrySet().stream().filter(e -> e.getValue().isEmpty())
                    .map(Map.Entry::getKey).collect(Collectors.toSet());
            finalStates.put(className, finals);
        }

        return new StateMachineConfig(detectedEnums, transitions, finalStates);
    }

    private boolean isAssignmentInChangedRanges(StateAssignment assignment,
            List<com.budgetpro.validator.model.LineRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return true; // Si no hay rangos (ej: archivo nuevo completo), validar todo
        }

        for (com.budgetpro.validator.model.LineRange range : ranges) {
            if (assignment.getLineNumber() >= range.startLine() && assignment.getLineNumber() <= range.endLine()) {
                return true;
            }
        }
        return false;
    }
}
