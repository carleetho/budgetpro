package com.budgetpro.validator;

import com.budgetpro.validator.boundary.config.BoundaryConfig;
import com.budgetpro.validator.boundary.config.BoundaryConfigLoader;
import com.budgetpro.validator.boundary.report.BoundaryViolation;
import com.budgetpro.validator.boundary.report.ViolationReporter;
import com.budgetpro.validator.boundary.scanner.DomainScanner;
import com.budgetpro.validator.engine.ValidationEngine;
import com.budgetpro.validator.model.ValidationResult;
import com.budgetpro.validator.output.JsonReportGenerator;
import com.budgetpro.validator.output.MarkdownGenerator;
import com.budgetpro.validator.output.MermaidGenerator;
import com.budgetpro.validator.roadmap.CanonicalRoadmap;
import com.budgetpro.validator.roadmap.RoadmapLoader;
import com.budgetpro.validator.statemachine.StateMachineValidationOrchestrator;
import com.budgetpro.validator.model.TransitionViolation;
import com.budgetpro.validator.model.ViolationSeverity;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * CLI tool para validar el orden de desarrollo de módulos BudgetPro contra el
 * roadmap canónico del dominio.
 * 
 * Exit Codes: - 0: Validación pasada sin violaciones - 1: Violaciones críticas
 * detectadas (bloquea CI/CD) - 2: Advertencias detectadas (requiere revisión) -
 * 3: Error durante el análisis (estructura inválida)
 */
@Command(name = "domain-validator", mixinStandardHelpOptions = true, version = "Domain Validator 1.0.0", description = "Valida el orden de desarrollo y las fronteras arquitectónicas de BudgetPro", subcommands = {
        ValidateCommand.class, GenerateRoadmapCommand.class, CheckModuleCommand.class, ValidateBoundaryCommand.class,
        ValidateStateMachineCommand.class })
public class DomainValidator implements Callable<Integer> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(DomainValidator.class.getName());

    public static void main(String[] args) {
        int exitCode = new CommandLine(new DomainValidator()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // Si no se especifica un subcomando, mostrar ayuda
        CommandLine.usage(this, System.out);
        return 0;
    }
}

/**
 * Comando para validar el código actual contra el roadmap canónico.
 */
@Command(name = "validate", description = "Valida la estructura del código contra el roadmap canónico")
class ValidateCommand implements Callable<Integer> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ValidateCommand.class.getName());

    @Option(names = { "--repo-path",
            "-p" }, description = "Ruta al directorio del repositorio (default: ./backend)", defaultValue = "./backend")
    private String repoPath;

    @Option(names = { "--strict",
            "-s" }, description = "Modo estricto: bloquea en advertencias además de violaciones críticas", defaultValue = "false")
    private boolean strict;

    @Option(names = { "--output-format",
            "-f" }, description = "Formato de salida: json, markdown, mermaid (default: json)", defaultValue = "json")
    private String outputFormat;

    @Option(names = { "--output-file", "-o" }, description = "Archivo de salida (default: stdout)")
    private String outputFile;

    @Override
    public Integer call() {
        try {
            Path repositoryPath = Paths.get(repoPath).toAbsolutePath().normalize();

            if (!repositoryPath.toFile().exists()) {
                LOGGER.severe("Error: Repository path does not exist: " + repositoryPath);
                return 3; // ERROR
            }

            LOGGER.info("Validating repository: " + repositoryPath);
            LOGGER.info("Strict mode: " + strict);

            // Cargar roadmap canónico
            RoadmapLoader roadmapLoader = new RoadmapLoader();
            CanonicalRoadmap roadmap = roadmapLoader.load();

            // Ejecutar validación completa
            ValidationEngine engine = new ValidationEngine();
            ValidationResult result = engine.validate(repositoryPath);

            // Generar salida según formato solicitado
            if ("json".equalsIgnoreCase(outputFormat)) {
                generateJsonOutput(result, roadmap);
            } else {
                // Formato texto (default)
                generateTextOutput(result);
            }

            // Retornar exit code considerando modo estricto
            return result.getExitCode(strict);

        } catch (Throwable e) {
            LOGGER.severe("Critical error during validation: " + e.getMessage());

            // Ensure JSON report is written even on crash if requested
            if ("json".equalsIgnoreCase(outputFormat) && outputFile != null) {
                try {
                    ValidationResult errorResult = ValidationResult.error("Critical internal error: " + e.getMessage());
                    JsonReportGenerator generator = new JsonReportGenerator();
                    generator.generateToFile(errorResult, null, Paths.get(outputFile));
                    LOGGER.severe("Emergency JSON report written to: " + outputFile);
                } catch (Exception writeEx) {
                    LOGGER.severe("Failed to write emergency JSON report: " + writeEx.getMessage());
                }
            }

            return 3; // ERROR
        }
    }

    /**
     * Genera salida en formato JSON.
     */
    private void generateJsonOutput(ValidationResult result, CanonicalRoadmap roadmap) {
        try {
            JsonReportGenerator generator = new JsonReportGenerator();

            if (outputFile != null) {
                // Escribir a archivo
                Path outputPath = Paths.get(outputFile);
                generator.generateToFile(result, roadmap, outputPath);
                LOGGER.info("JSON report written to: " + outputPath);
            } else {
                // Escribir a stdout (sin mensajes adicionales para facilitar parsing)
                // Note: Changed to LOGGER for AXIOM compliance
                String json = generator.generate(result, roadmap);
                LOGGER.info(json);
            }
        } catch (Exception e) {
            LOGGER.severe("Error generating JSON report: " + e.getMessage());
            // Fallback a formato texto
            generateTextOutput(result);
        }
    }

    /**
     * Genera salida en formato texto (legible por humanos).
     */
    private void generateTextOutput(ValidationResult result) {
        // Mostrar resumen
        LOGGER.info("\nValidation completed: " + result.getStatus());
        LOGGER.info("Violations: " + result.getViolations().size());
        LOGGER.info("Modules analyzed: " + result.getModuleStatuses().size());

        // Mostrar violaciones críticas
        long criticalCount = result.getViolations().stream()
                .filter(v -> v.getSeverity() == com.budgetpro.validator.model.ViolationSeverity.CRITICAL).count();
        long warningCount = result.getViolations().stream()
                .filter(v -> v.getSeverity() == com.budgetpro.validator.model.ViolationSeverity.WARNING).count();

        if (criticalCount > 0) {
            LOGGER.severe("\n⚠️  Critical Violations (" + criticalCount + "):");
            result.getViolations().stream()
                    .filter(v -> v.getSeverity() == com.budgetpro.validator.model.ViolationSeverity.CRITICAL).limit(5)
                    .forEach(v -> {
                        LOGGER.severe("  • [" + v.getModuleId() + "] " + v.getMessage());
                        if (v.getSuggestion() != null) {
                            LOGGER.severe("    → " + v.getSuggestion());
                        }
                    });
            if (criticalCount > 5) {
                LOGGER.severe("  ... and " + (criticalCount - 5) + " more");
            }
        }

        if (warningCount > 0) {
            LOGGER.warning("\n⚠️  Warnings (" + warningCount + "):");
            result.getViolations().stream()
                    .filter(v -> v.getSeverity() == com.budgetpro.validator.model.ViolationSeverity.WARNING).limit(3)
                    .forEach(v -> {
                        LOGGER.warning("  • [" + v.getModuleId() + "] " + v.getMessage());
                    });
            if (warningCount > 3) {
                LOGGER.warning("  ... and " + (warningCount - 3) + " more");
            }
        }

        // Mostrar resumen por módulo
        LOGGER.info("\nModule Status Summary:");
        for (var status : result.getModuleStatuses()) {
            LOGGER.info(String.format("  %s: %s (%d entities, %d services, %d endpoints)%n", status.getModuleId(),
                    status.getImplementationStatus(), status.getDetectedEntities().size(),
                    status.getDetectedServices().size(), status.getDetectedEndpoints().size()));
        }
    }
}

/**
 * Comando para generar el roadmap canónico en múltiples formatos.
 */
@Command(name = "generate-roadmap", description = "Genera el roadmap canónico en múltiples formatos")
class GenerateRoadmapCommand implements Callable<Integer> {

    @Option(names = { "--output-dir",
            "-o" }, description = "Directorio de salida (default: ./docs/roadmap)", defaultValue = "./docs/roadmap")
    private String outputDir;

    @Option(names = { "--format",
            "-f" }, description = "Formatos a generar: json, markdown, mermaid (default: mermaid)", defaultValue = "mermaid")
    private String format;

    @Option(names = {
            "--output-file" }, description = "Archivo de salida específico (sobrescribe output-dir para ese formato)")
    private String outputFile;

    @Option(names = { "--simplified",
            "-s" }, description = "Generar diagrama simplificado sin subgrafos", defaultValue = "false")
    private boolean simplified;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(GenerateRoadmapCommand.class.getName());

    @Override
    public Integer call() {
        try {
            // Cargar roadmap canónico
            RoadmapLoader roadmapLoader = new RoadmapLoader();
            var roadmap = roadmapLoader.load();

            LOGGER.info("Loaded canonical roadmap version: " + roadmap.getVersion());
            LOGGER.info("Generating formats: " + format);

            Path outputPath = Paths.get(outputDir).toAbsolutePath().normalize();

            // Crear directorio si no existe
            if (!outputPath.toFile().exists()) {
                Files.createDirectories(outputPath);
                LOGGER.info("Created output directory: " + outputPath);
            }

            // Generar según formato solicitado
            if ("all".equals(format) || "mermaid".equals(format)) {
                generateMermaid(roadmap, outputPath);
            }

            if ("all".equals(format) || "json".equals(format)) {
                generateJson(roadmap, outputPath);
            }

            if ("all".equals(format) || "markdown".equals(format)) {
                generateMarkdown(roadmap, outputPath);
            }

            LOGGER.info("Roadmap generation completed successfully");
            return 0;

        } catch (RoadmapLoader.RoadmapLoadException e) {
            LOGGER.severe("Error loading roadmap: " + e.getMessage());
            return 3; // ERROR
        } catch (Exception e) {
            LOGGER.severe("Error generating roadmap: " + e.getMessage());
            return 3; // ERROR
        }
    }

    /**
     * Genera diagrama Mermaid.
     */
    private void generateMermaid(com.budgetpro.validator.roadmap.CanonicalRoadmap roadmap, Path outputDir)
            throws IOException {
        MermaidGenerator generator = new MermaidGenerator();
        String mermaidContent;

        if (simplified) {
            mermaidContent = generator.generateSimplified(roadmap);
        } else {
            mermaidContent = generator.generate(roadmap);
        }

        Path mermaidOutputFile;
        if (outputFile != null) {
            mermaidOutputFile = Paths.get(outputFile);
            // Si es relativo, crear en outputDir
            if (!mermaidOutputFile.isAbsolute()) {
                mermaidOutputFile = outputDir.resolve(mermaidOutputFile);
            }
        } else {
            mermaidOutputFile = outputDir.resolve("roadmap.mmd");
        }

        Files.writeString(mermaidOutputFile, mermaidContent, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.info("Generated Mermaid diagram: " + mermaidOutputFile);

        // También mostrar en stdout si no se especificó archivo
        if (outputFile == null) {
            LOGGER.info("\n--- Mermaid Diagram ---");
            LOGGER.info(mermaidContent);
        }
    }

    /**
     * Genera JSON del roadmap.
     */
    private void generateJson(com.budgetpro.validator.roadmap.CanonicalRoadmap roadmap, Path outputDir)
            throws IOException {
        // TODO: Implementar generación JSON
        LOGGER.info("JSON generation not yet implemented");
    }

    /**
     * Genera Markdown del roadmap.
     */
    private void generateMarkdown(com.budgetpro.validator.roadmap.CanonicalRoadmap roadmap, Path outputDir)
            throws IOException {
        MarkdownGenerator generator = new MarkdownGenerator();
        String markdownContent = generator.generate(roadmap);

        Path markdownOutputFile;
        // Si se especificó outputFile y el formato es markdown o all, usarlo
        if (outputFile != null && ("markdown".equals(format) || "all".equals(format))) {
            markdownOutputFile = Paths.get(outputFile);
            // Si es relativo, crear en outputDir
            if (!markdownOutputFile.isAbsolute()) {
                markdownOutputFile = outputDir.resolve(markdownOutputFile);
            }
        } else {
            markdownOutputFile = outputDir.resolve("ROADMAP_CANONICO.md");
        }

        Files.writeString(markdownOutputFile, markdownContent, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.info("Generated Markdown document: " + markdownOutputFile);

        // También mostrar preview en stdout si no se especificó archivo y es el único
        // formato
        if (outputFile == null && "markdown".equals(format)) {
            LOGGER.info("\n--- Markdown Document Preview (first 50 lines) ---");
            String[] lines = markdownContent.split("\n");
            int linesToShow = Math.min(50, lines.length);
            for (int i = 0; i < linesToShow; i++) {
                LOGGER.info(lines[i]);
            }
            if (lines.length > 50) {
                LOGGER.info("... (" + (lines.length - 50) + " more lines)");
            }
        }
    }
}

/**
 * Comando para verificar el estado de un módulo específico.
 */
@Command(name = "check-module", description = "Verifica el estado de implementación de un módulo específico")
class CheckModuleCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "ID del módulo a verificar (ej: presupuesto, compras, tiempo)")
    private String moduleName;

    @Option(names = { "--repo-path",
            "-p" }, description = "Ruta al directorio del repositorio (default: ./backend)", defaultValue = "./backend")
    private String repoPath;

    @Option(names = { "--show-dependencies",
            "-d" }, description = "Mostrar dependencias del módulo", defaultValue = "false")
    private boolean showDependencies;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(CheckModuleCommand.class.getName());

    @Override
    public Integer call() {
        try {
            Path repositoryPath = Paths.get(repoPath).toAbsolutePath().normalize();

            LOGGER.info("Checking module: " + moduleName);
            LOGGER.info("Repository: " + repositoryPath);
            LOGGER.info("Show dependencies: " + showDependencies);

            // TODO: Implementar verificación real del módulo
            // Por ahora, solo confirmar que el comando funciona
            LOGGER.info("Module check completed (not yet implemented)");
            LOGGER.info("Module: " + moduleName);

            return 0;

        } catch (Exception e) {
            LOGGER.severe("Error checking module: " + e.getMessage());
            return 3; // ERROR
        }
    }
}

@Command(name = "validate-boundary", description = "Valida que la capa de dominio sea independiente de frameworks e infraestructura")
class ValidateBoundaryCommand implements Callable<Integer> {

    @Option(names = { "--domain-root",
            "-r" }, description = "Ruta a la capa de dominio (default: ./backend/src/main/java/com/budgetpro/domain)", defaultValue = "./backend/src/main/java/com/budgetpro/domain")
    private String domainRoot;

    @Option(names = { "--config", "-c" }, description = "Ruta al archivo de configuración (YAML/JSON)")
    private String configPath;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ValidateBoundaryCommand.class.getName());

    @Override
    public Integer call() {
        try {
            Path domainPath = Paths.get(domainRoot).toAbsolutePath().normalize();

            if (!domainPath.toFile().exists()) {
                LOGGER.severe("Error: Domain root path does not exist: " + domainPath);
                return 3; // ERROR
            }

            // Cargar configuración de fronteras
            BoundaryConfigLoader loader = new BoundaryConfigLoader();
            BoundaryConfig config;

            if (configPath != null) {
                LOGGER.info("Loading custom configuration from: " + configPath);
                config = loader.loadFromFile(Paths.get(configPath));
            } else {
                LOGGER.info("Loading default boundary rules...");
                config = loader.loadDefault();
            }

            LOGGER.info("Validating hexagonal boundaries in: " + domainPath);
            LOGGER.info("Structural Analysis: " + config.structuralAnalysis());
            LOGGER.info("Enforced Severity: " + config.severity());

            // Ejecutar validación
            ViolationReporter reporter = new ViolationReporter(new DomainScanner(), config);
            List<BoundaryViolation> violations = reporter.validateDomain(domainPath);

            // Reportar resultados
            reporter.reportViolations(violations);

            // Retornar exit code 1 si hay violaciones críticas (bloquea CI)
            if (violations.isEmpty()) {
                return 0;
            }

            // Si la severidad es CRITICAL o ERROR, devolvemos 1 (bloqueante)
            if ("CRITICAL".equalsIgnoreCase(config.severity()) || "ERROR".equalsIgnoreCase(config.severity())) {
                return 1;
            }

            // Si es WARNING, devolvemos 2 (no bloqueante pero indicativo)
            return 2;

        } catch (Exception e) {
            LOGGER.severe("Error validating boundaries: " + e.getMessage());
            return 3; // ERROR
        }
    }
}

/**
 * Comando para validar transiciones de máquinas de estado.
 */
@Command(name = "validate-state-machine", description = "Valida las transiciones de máquinas de estado basadas en los cambios detectados por Git")
class ValidateStateMachineCommand implements Callable<Integer> {

    @Option(names = { "--repo-path",
            "-p" }, description = "Ruta al directorio del repositorio (default: .)", defaultValue = ".")
    private String repoPath;

    @Option(names = { "--domain-path",
            "-d" }, description = "Ruta a la capa de dominio (default: src/main/java/com/budgetpro/domain)", defaultValue = "src/main/java/com/budgetpro/domain")
    private String domainPath;

    @Option(names = { "--git-diff-command",
            "-g" }, description = "Comando de git para obtener cambios (default: git diff --cached)", defaultValue = "git diff --cached")
    private String gitDiffCommand;

    @Option(names = { "--strict",
            "-s" }, description = "Modo estricto: trata advertencias como errores", defaultValue = "false")
    private boolean strict;

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ValidateStateMachineCommand.class.getName());

    @Override
    public Integer call() {
        try {
            Path repositoryPath = Paths.get(repoPath).toAbsolutePath().normalize();
            Path domainFullPath = repositoryPath.resolve(domainPath).toAbsolutePath().normalize();

            if (!repositoryPath.toFile().exists()) {
                LOGGER.severe("Error: Repository path does not exist: " + repositoryPath);
                return 3;
            }

            LOGGER.info("Validating state machine transitions...");
            LOGGER.info("Repository: " + repositoryPath);
            LOGGER.info("Git command: " + gitDiffCommand);
            LOGGER.info("Strict mode: " + strict);

            StateMachineValidationOrchestrator orchestrator = new StateMachineValidationOrchestrator();
            List<TransitionViolation> violations = orchestrator.orchestrate(repositoryPath, domainFullPath,
                    gitDiffCommand);

            if (violations.isEmpty()) {
                return 0;
            }

            // Determinar exit code
            boolean hasCritical = violations.stream().anyMatch(v -> v.getSeverity() == ViolationSeverity.CRITICAL);
            boolean hasWarning = violations.stream().anyMatch(v -> v.getSeverity() == ViolationSeverity.WARNING);

            if (hasCritical) {
                return 1; // Error crítico
            }
            if (strict && hasWarning) {
                return 1; // Advertencia en modo estricto
            }

            return 0; // Solo advertencias en modo no estricto

        } catch (Exception e) {
            LOGGER.severe("Critical error during state machine validation: " + e.getMessage());
            return 3;
        }
    }
}
